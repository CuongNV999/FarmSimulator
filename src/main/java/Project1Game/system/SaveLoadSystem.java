package Project1Game.system;

import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.SoilComponent;
import Project1Game.component.player.PlayerComponent; // Import PlayerComponent
import Project1Game.core.EntityType;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.model.SaveData;
import Project1Game.ui.view.hud.StatusBarsView;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import java.util.Arrays; // Import Arrays
import Project1Game.Main;

public class SaveLoadSystem {
    private final Inventory inventory;
    private final StatusBarsView statusBarsView;
    private final TimeSystem timeSystem;

    public SaveLoadSystem(Inventory inventory, StatusBarsView statusBarsView, TimeSystem timeSystem) {
        this.inventory = inventory;
        this.statusBarsView = statusBarsView;
        this.timeSystem = timeSystem;
    }

    public void save(SaveData data) {
        save(data, false);
    }

    public void save(SaveData data, boolean isMapTransition) {
        if (!isMapTransition) {
            data.gameTime = timeSystem.getGameTime(); // Lấy từ TimeSystem
            data.health = statusBarsView.getHealth();
            data.hunger = statusBarsView.getHunger();

            // Lấy PlayerComponent từ player entity
            Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
            PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
            data.playerMoney = playerComponent.getMoney(); // Lưu tiền của người chơi
            data.playerSkin = playerComponent.getCurrentSkin();

            // Lưu bản đồ và vị trí người chơi
            data.currentMap = ((Main) FXGL.getApp()).getCurrentMap();
            data.playerX = player.getX();
            data.playerY = player.getY();

            // Lưu thời tiết
            data.weather = WeatherSystem.getCurrentWeather().name();

            System.out.println(String.format("LƯU GAME: Bản đồ: %s, Vị trí: (%.1f, %.1f), Tiền: %d G, Thời gian: %.1f, Thời tiết: %s", 
                data.currentMap, data.playerX, data.playerY, data.playerMoney, data.gameTime, data.weather));

            // Lưu Inventory
            data.inventoryItems.clear(); // Xóa dữ liệu cũ trước khi lưu mới
            for (ItemType t : ItemType.values()) {
                if (inventory.getCount(t) > 0) {
                    data.inventoryItems.put(t.name(), inventory.getCount(t));
                }
            }

            // Save Quests
            data.npcQuests.clear();
            for (Project1Game.quest.QuestGiver npc : Project1Game.quest.QuestManager.getInstance().getAllNPCs()) {
                SaveData.NPCSave npcSave = new SaveData.NPCSave();
                npcSave.npcName = npc.getName();
                for (Project1Game.quest.Quest q : npc.getQuests()) {
                    SaveData.QuestSave questSave = new SaveData.QuestSave();
                    questSave.questId = q.getId();
                    questSave.status = q.getStatus().name();
                    for (Project1Game.quest.QuestObjective obj : q.getObjectives()) {
                        questSave.objectiveProgress.add(obj.getCurrent());
                    }
                    npcSave.quests.add(questSave);
                }
                data.npcQuests.add(npcSave);
            }
        }

        // Save Trader Relationships & Session Histories (Runs for both map transitions and global saves)
        if (data.traderRelationship == null) data.traderRelationship = new java.util.HashMap<>();
        if (data.traderNegotiationCount == null) data.traderNegotiationCount = new java.util.HashMap<>();
        if (data.traderNegotiatedThisSession == null) data.traderNegotiatedThisSession = new java.util.HashMap<>();
        if (data.traderNegotiationBonusPercent == null) data.traderNegotiationBonusPercent = new java.util.HashMap<>();

        data.traderRelationship.clear();
        data.traderNegotiationCount.clear();
        data.traderNegotiatedThisSession.clear();
        data.traderNegotiationBonusPercent.clear();

        FXGL.getGameWorld().getEntitiesByType(EntityType.TRADER).forEach(t -> {
            Project1Game.component.npc.TraderComponent tc = t.getComponentOptional(Project1Game.component.npc.TraderComponent.class).orElse(null);
            if (tc != null) {
                String name = t.getProperties().keys().contains("name") ? t.getString("name") : "Trader";
                data.traderRelationship.put(name, tc.getRelationship().name());
                data.traderNegotiationCount.put(name, tc.getNegotiationCount());
                data.traderNegotiatedThisSession.put(name, tc.hasNegotiatedThisSession());
                data.traderNegotiationBonusPercent.put(name, tc.getNegotiationBonusPercent());
            }
        });

        // Lưu Đất (Soil)
        data.soils.clear(); // Xóa dữ liệu cũ trước khi lưu mới
        java.util.Set<String> savedCoords = new java.util.HashSet<>();
        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(s -> {
            int gridX = (int) Math.round(s.getX() / 32.0);
            int gridY = (int) Math.round(s.getY() / 32.0);
            String coordKey = gridX + "," + gridY;
            if (!savedCoords.contains(coordKey)) {
                savedCoords.add(coordKey);
                SaveData.SoilData sd = new SaveData.SoilData();
                sd.x = gridX * 32.0;
                sd.y = gridY * 32.0;
                sd.isWet = s.getComponent(SoilComponent.class).isWet();
                sd.hasPlant = s.getComponent(SoilComponent.class).isHasPlant();
                data.soils.add(sd);
            }
        });

        // Lưu Cây trồng (Crops)
        data.crops.clear(); // Xóa dữ liệu cũ trước khi lưu mới
        // Lấy tất cả các loại cây trồng đã định nghĩa trong CropRegistry
        for (EntityType cropType : Project1Game.core.CropRegistry.getInstance().getSupportedCrops()) {
            FXGL.getGameWorld().getEntitiesByType(cropType).forEach(c -> {
                SaveData.CropDataSave cd = new SaveData.CropDataSave();
                cd.x = c.getX();
                cd.y = c.getY();
                cd.type = c.getType().toString(); // Lưu tên loại cây
                cd.stage = c.getComponent(CropComponent.class).getStage(); // Lưu giai đoạn phát triển
                data.crops.add(cd);
            });
        }

        // Lưu Động vật (Animals)
        data.animals.clear();
        FXGL.getGameWorld().getEntitiesByType(EntityType.ANIMAL).forEach(a -> {
            Project1Game.component.farming.animal.BaseAnimalComponent bac = a.getComponents().stream()
                    .filter(c -> c instanceof Project1Game.component.farming.animal.BaseAnimalComponent)
                    .map(c -> (Project1Game.component.farming.animal.BaseAnimalComponent) c)
                    .findFirst()
                    .orElse(null);
            if (bac != null) {
                SaveData.AnimalSaveData asd = new SaveData.AnimalSaveData();
                asd.x = a.getX();
                asd.y = a.getY();
                asd.type = bac.getType().name();
                asd.daysGrown = bac.getDaysGrown();
                data.animals.add(asd);
            }
        });

        // Lưu Quái vật (Monsters)
        data.monsters.clear();
        FXGL.getGameWorld().getEntitiesByType(EntityType.MONSTER).forEach(m -> {
            String spawnName = m.getString("monsterType");
            if (spawnName != null && !spawnName.isEmpty()) {
                SaveData.MonsterSaveData msd = new SaveData.MonsterSaveData();
                msd.x = m.getX();
                msd.y = m.getY();
                msd.type = spawnName;
                data.monsters.add(msd);
            }
        });
    }

    public void load(SaveData data) {
        load(data, false);
    }

    public void load(SaveData data, boolean isMapTransition) {
        if (!isMapTransition) {
            timeSystem.setGameTime(data.gameTime); // Gán lại cho TimeSystem
            statusBarsView.setHealth(data.health);
            statusBarsView.setHunger(data.hunger);

            // Lấy PlayerComponent từ player entity
            Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
            PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
            playerComponent.setMoney(data.playerMoney); // Tải tiền của người chơi
            if (data.playerSkin != null) {
                playerComponent.changeSkin(data.playerSkin);
            }

            // Tải Inventory (cần xóa inventory hiện tại và thêm lại)
            inventory.clear();
            for (java.util.Map.Entry<String, Integer> entry : data.inventoryItems.entrySet()) {
                inventory.addItem(ItemType.valueOf(entry.getKey()), entry.getValue());
            }

            // Tải thời tiết
            if (data.weather != null) {
                WeatherSystem.getInstance().changeWeather(WeatherSystem.Weather.valueOf(data.weather));
            }

            // Reset and initialize QuestManager before loading quests to avoid corruption
            Project1Game.quest.QuestManager.getInstance().reset();
            Project1Game.quest.QuestManager.getInstance().init();

            // Load Quests
            if (data.npcQuests != null) {
                for (SaveData.NPCSave npcSave : data.npcQuests) {
                    Project1Game.quest.QuestGiver npc = Project1Game.quest.QuestManager.getInstance().getNPC(npcSave.npcName);
                    if (npc != null) {
                        for (SaveData.QuestSave questSave : npcSave.quests) {
                            Project1Game.quest.Quest q = npc.getQuests().stream()
                                    .filter(quest -> quest.getId().equals(questSave.questId))
                                    .findFirst()
                                    .orElse(null);
                            if (q != null) {
                                q.setStatus(Project1Game.quest.QuestStatus.valueOf(questSave.status));
                                var objectives = q.getObjectives();
                                for (int i = 0; i < Math.min(objectives.size(), questSave.objectiveProgress.size()); i++) {
                                    objectives.get(i).setCurrent(questSave.objectiveProgress.get(i));
                                }
                            }
                        }
                    }
                }
            }

            System.out.println(String.format("NẠP GAME: Bản đồ: %s, Vị trí: (%.1f, %.1f), Tiền: %d G, Thời gian: %.1f, Thời tiết: %s", 
                data.currentMap, data.playerX, data.playerY, data.playerMoney, data.gameTime, data.weather));
        }

        // Load Trader Relationships & Histories (Runs for both map transitions and global saves)
        FXGL.getGameWorld().getEntitiesByType(EntityType.TRADER).forEach(t -> {
            Project1Game.component.npc.TraderComponent tc = t.getComponentOptional(Project1Game.component.npc.TraderComponent.class).orElse(null);
            if (tc != null) {
                String name = t.getProperties().keys().contains("name") ? t.getString("name") : "Trader";
                if (data.traderRelationship != null && data.traderRelationship.containsKey(name)) {
                    tc.setRelationship(Project1Game.component.npc.RelationshipLevel.valueOf(data.traderRelationship.get(name)));
                }
                if (data.traderNegotiationCount != null && data.traderNegotiationCount.containsKey(name)) {
                    tc.setNegotiationCount(data.traderNegotiationCount.get(name));
                }
                if (data.traderNegotiatedThisSession != null && data.traderNegotiatedThisSession.containsKey(name)) {
                    tc.setHasNegotiatedThisSession(data.traderNegotiatedThisSession.get(name));
                }
                if (data.traderNegotiationBonusPercent != null && data.traderNegotiationBonusPercent.containsKey(name)) {
                    tc.setNegotiationBonusPercent(data.traderNegotiationBonusPercent.get(name));
                }
            }
        });

        FXGL.getGameWorld().getEntitiesFiltered(e -> {
            Object type = e.getType();
            if (type == EntityType.SOIL || type == EntityType.ANIMAL || type == EntityType.MONSTER) {
                return true;
            }
            if (type instanceof EntityType && Project1Game.core.CropRegistry.getInstance().isCrop((EntityType) type)) {
                return true;
            }
            return false;
        }).forEach(Entity::removeFromWorld);

        // Tái tạo ô đất
        java.util.Set<String> loadedCoords = new java.util.HashSet<>();
        for (SaveData.SoilData sd : data.soils) {
            int gridX = (int) Math.round(sd.x / 32.0);
            int gridY = (int) Math.round(sd.y / 32.0);
            String coordKey = gridX + "," + gridY;
            if (!loadedCoords.contains(coordKey)) {
                loadedCoords.add(coordKey);
                Entity s = FXGL.getGameWorld().spawn("Soil", gridX * 32.0, gridY * 32.0);
                s.getComponent(SoilComponent.class).setWet(sd.isWet);
                s.getComponent(SoilComponent.class).setHasPlant(sd.hasPlant);
            }
        }

        // Tái tạo cây trồng
        for (SaveData.CropDataSave cd : data.crops) {
            // Chuyển đổi String type thành EntityType
            EntityType cropEntityType = EntityType.valueOf(cd.type);
            // Sửa đổi: Chuyển đổi tên enum thành dạng chữ cái đầu viết hoa, các chữ còn lại viết thường
            String spawnName = cropEntityType.name().substring(0, 1).toUpperCase() + cropEntityType.name().substring(1).toLowerCase();

            Entity c = FXGL.getGameWorld().spawn(spawnName, cd.x, cd.y);
            c.getComponent(CropComponent.class).setStage(cd.stage);
        }

        // Tái tạo động vật
        if (data.animals != null) {
            for (SaveData.AnimalSaveData asd : data.animals) {
                String typeName = asd.type;
                String spawnName = "";
                if (typeName.equals("CHICKEN")) spawnName = "Chick";
                else if (typeName.equals("COW")) spawnName = "Calf";
                else if (typeName.equals("SHEEP")) spawnName = "Lamb";
                else if (typeName.equals("PIG")) spawnName = "Piglet";
                else if (typeName.equals("TURKEY")) spawnName = "Turkey";

                if (!spawnName.isEmpty()) {
                    Entity a = FXGL.getGameWorld().spawn(spawnName, new com.almasb.fxgl.entity.SpawnData(asd.x, asd.y).put("fromSave", true));
                    Project1Game.component.farming.animal.BaseAnimalComponent bac = a.getComponents().stream()
                            .filter(c -> c instanceof Project1Game.component.farming.animal.BaseAnimalComponent)
                            .map(c -> (Project1Game.component.farming.animal.BaseAnimalComponent) c)
                            .findFirst()
                            .orElse(null);
                    if (bac != null) {
                        bac.setDaysGrown(asd.daysGrown);
                        bac.initAnimation();
                    }
                }
            }
        }

        // Tái tạo quái vật
        if (data.monsters != null) {
            for (SaveData.MonsterSaveData msd : data.monsters) {
                FXGL.getGameWorld().spawn(msd.type, msd.x, msd.y);
            }
        }
    }

    // Phương thức lưu/tải toàn bộ game (vào file)
    public void saveGameToFile() {
        SaveData currentSaveData = new SaveData();
        save(currentSaveData); // Lưu trạng thái hiện tại vào currentSaveData
        FXGL.getFileSystemService().writeDataTask(currentSaveData, "save_game.dat").run();
        System.out.println("Đã lưu game vào file thành công!");
    }

    public void loadGameFromFile() {
        var task = FXGL.getFileSystemService().<SaveData>readDataTask("save_game.dat")
                .onSuccess(data -> {
                    FXGL.getExecutor().startAsyncFX(() -> {
                        Main app = FXGL.getAppCast();
                        if (data.currentMap != null) {
                            app.updateLevelFromSave(data.currentMap, data.playerX, data.playerY);
                        }
                        load(data); // Tải dữ liệu từ file vào game
                        System.out.println("Đã tải game từ file thành công!");
                    });
                });
        FXGL.getExecutor().execute(() -> task.run());
    }
}
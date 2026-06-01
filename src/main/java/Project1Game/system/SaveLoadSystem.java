package Project1Game.system;

import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.SoilComponent;
import Project1Game.component.player.PlayerComponent; // Import PlayerComponent
import Project1Game.core.EntityType;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.model.SaveData;
import Project1Game.ui.StatusBarsView;
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
        }

        // Lưu Đất (Soil)
        data.soils.clear(); // Xóa dữ liệu cũ trước khi lưu mới
        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(s -> {
            SaveData.SoilData sd = new SaveData.SoilData();
            sd.x = s.getX();
            sd.y = s.getY();
            sd.isWet = s.getComponent(SoilComponent.class).isWet();
            sd.hasPlant = s.getComponent(SoilComponent.class).isHasPlant();
            data.soils.add(sd);
        });

        // Lưu Cây trồng (Crops)
        data.crops.clear(); // Xóa dữ liệu cũ trước khi lưu mới
        // Lấy tất cả các loại cây trồng đã định nghĩa trong EntityType
        EntityType[] cropTypes = {EntityType.WHEAT, EntityType.RADISH, EntityType.CABBAGE,
                EntityType.LETTUCE, EntityType.TOMATO, EntityType.CORN};

        for (EntityType cropType : cropTypes) {
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
            Project1Game.component.farming.BaseAnimalComponent bac = a.getComponents().stream()
                    .filter(c -> c instanceof Project1Game.component.farming.BaseAnimalComponent)
                    .map(c -> (Project1Game.component.farming.BaseAnimalComponent) c)
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

            System.out.println(String.format("NẠP GAME: Bản đồ: %s, Vị trí: (%.1f, %.1f), Tiền: %d G, Thời gian: %.1f, Thời tiết: %s", 
                data.currentMap, data.playerX, data.playerY, data.playerMoney, data.gameTime, data.weather));
        }

        // Xóa tất cả thực thể động cũ (đất, cây, động vật)
        EntityType[] allDynamicEntities = {EntityType.SOIL, EntityType.WHEAT, EntityType.CORN,
                EntityType.RADISH, EntityType.CABBAGE, EntityType.LETTUCE, EntityType.TOMATO, EntityType.ANIMAL};
        FXGL.getGameWorld().getEntitiesFiltered(e -> Arrays.asList(allDynamicEntities).contains(e.getType()))
                .forEach(Entity::removeFromWorld);


        // Tái tạo ô đất
        for (SaveData.SoilData sd : data.soils) {
            Entity s = FXGL.getGameWorld().spawn("Soil", sd.x, sd.y);
            s.getComponent(SoilComponent.class).setWet(sd.isWet);
            s.getComponent(SoilComponent.class).setHasPlant(sd.hasPlant);
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
                    Entity a = FXGL.getGameWorld().spawn(spawnName, asd.x, asd.y);
                    Project1Game.component.farming.BaseAnimalComponent bac = a.getComponents().stream()
                            .filter(c -> c instanceof Project1Game.component.farming.BaseAnimalComponent)
                            .map(c -> (Project1Game.component.farming.BaseAnimalComponent) c)
                            .findFirst()
                            .orElse(null);
                    if (bac != null) {
                        bac.setDaysGrown(asd.daysGrown);
                        bac.initAnimation();
                    }
                }
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
        FXGL.getFileSystemService().<SaveData>readDataTask("save_game.dat").onSuccess(data -> {
            Main app = FXGL.getAppCast();
            if (data.currentMap != null) {
                app.updateLevelFromSave(data.currentMap, data.playerX, data.playerY);
            }
            load(data); // Tải dữ liệu từ file vào game
            System.out.println("Đã tải game từ file thành công!");
        }).run();
    }
}
package Project1Game.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import Project1Game.Main;
import Project1Game.core.EntityType;
import Project1Game.component.player.PlayerComponent;
import Project1Game.component.npc.NPCBehaviorComponent;
import Project1Game.component.farming.monster.BaseMonsterComponent;
import Project1Game.component.farming.SoilComponent;
import Project1Game.model.SaveData;
import Project1Game.ui.view.shop.TradingView;
import Project1Game.ui.view.admin.AdminView;
import Project1Game.ui.view.overlay.NightLightingOverlay;
import Project1Game.ui.view.dialog.DialogView;
import javafx.geometry.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class LevelManager {

    public static class NPCSpawnConfig {
        public String type;
        public double x;
        public double y;
        public boolean isHidden;

        public NPCSpawnConfig(String type, double x, double y, boolean isHidden) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.isHidden = isHidden;
        }
    }

    private String currentMap = "Main_level.tmx";
    private final Map<String, SaveData> mapStates = new HashMap<>();
    private boolean isLevelTransitioning = false;
    private Point2D lastOutdoorPosition = null;

    private com.almasb.fxgl.entity.level.tiled.TiledMap currentTMXMap = null;
    private List<Long> overheadLayerData = null;
    private int currentTMXMapWidth = 0;
    private int currentTMXMapHeight = 0;
    private Entity overheadLayerEntity = null;

    private double currentMapWidth = 3520;
    private double currentMapHeight = 2048;
    private double bushMonsterSpawnTimer = 0.0;
    private final List<NPCSpawnConfig> pendingNPCSpawns = new ArrayList<>();

    public String getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(String map) {
        this.currentMap = map;
    }

    public Map<String, SaveData> getMapStates() {
        return mapStates;
    }

    public boolean isLevelTransitioning() {
        return isLevelTransitioning;
    }

    public Point2D getLastOutdoorPosition() {
        return lastOutdoorPosition;
    }

    public void setLastOutdoorPosition(Point2D pos) {
        this.lastOutdoorPosition = pos;
    }

    public double getCurrentMapWidth() {
        return currentMapWidth;
    }

    public double getCurrentMapHeight() {
        return currentMapHeight;
    }

    public void clearState() {
        currentMap = "Main_level.tmx";
        mapStates.clear();
        isLevelTransitioning = false;
        lastOutdoorPosition = null;
        currentTMXMap = null;
        overheadLayerData = null;
        overheadLayerEntity = null;
        pendingNPCSpawns.clear();
        bushMonsterSpawnTimer = 0.0;
    }

    public void handleDoorInteraction(Entity targetDoor, DialogView dialogView, CollisionManager collisionManager, SaveLoadSystem saveLoadSystem) {
        String mapFile = targetDoor.getString("targetMap");
        double tx = targetDoor.getDouble("teleportX");
        double ty = targetDoor.getDouble("teleportY");

        if (currentMap.equals("Main_level.tmx") && mapFile.equals("Main_house.tmx")) {
            lastOutdoorPosition = Main.getInstance().getPlayer().getPosition();
            System.out.println("Cached player outdoor position: " + lastOutdoorPosition);
        }

        if (currentMap.equals("Main_house.tmx") && mapFile.equals("Main_level.tmx") && lastOutdoorPosition != null) {
            tx = lastOutdoorPosition.getX();
            ty = lastOutdoorPosition.getY() + 32.0;
            System.out.println("Using cached outdoor position: " + tx + ", " + ty);
        }

        if (dialogView != null && dialogView.isOpen()) {
            dialogView.hide();
        }

        collisionManager.setNearbyDoor(null);
        updateLevel(mapFile, tx, ty, saveLoadSystem);
        System.out.println("Dịch chuyển đến: " + mapFile + " tại " + tx + ", " + ty);
    }

    public void updateLevel(String newMapName, double x, double y, SaveLoadSystem saveLoadSystem) {
        isLevelTransitioning = true;
        try {
            Main app = Main.getInstance();
            Entity player = app.getPlayer();
            int tempMoney = 1000;
            String tempSkin = PlayerComponent.SELECTED_SKIN;
            if (player != null && player.isActive() && player.hasComponent(PlayerComponent.class)) {
                PlayerComponent pc = player.getComponent(PlayerComponent.class);
                tempMoney = pc.getMoney();
                tempSkin = pc.getCurrentSkin();
            }

            // 1. LƯU TRẠNG THÁI BẢN ĐỒ HIỆN TẠI (nếu có)
            if (player != null && currentMap != null) {
                SaveData currentMapState = new SaveData();
                saveLoadSystem.save(currentMapState, true);
                mapStates.put(currentMap, currentMapState);
                System.out.println("Đã lưu trạng thái bản đồ: " + currentMap);
            }

            // 2. TẢI BẢN ĐỒ MỚI
            currentMap = newMapName;
            FXGL.setLevelFromMap(newMapName);

            // Load the TMX map details to query overhead tiles
            try (java.io.InputStream is = LevelManager.class.getResourceAsStream("/assets/levels/" + newMapName)) {
                if (is == null) {
                    throw new java.io.FileNotFoundException("Resource not found on classpath: /assets/levels/" + newMapName);
                }
                com.almasb.fxgl.entity.level.tiled.TMXLevelLoader loader = new com.almasb.fxgl.entity.level.tiled.TMXLevelLoader();
                currentTMXMap = loader.parse(is);
                overheadLayerData = null;
                if (currentTMXMap != null) {
                    currentTMXMapWidth = currentTMXMap.getWidth();
                    currentTMXMapHeight = currentTMXMap.getHeight();
                    for (com.almasb.fxgl.entity.level.tiled.Layer layer : currentTMXMap.getLayers()) {
                        if (layer.getName().equalsIgnoreCase("OverheadLayer")) {
                            overheadLayerData = layer.getData();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading TMX map details: " + e.getMessage());
                currentTMXMap = null;
                overheadLayerData = null;
            }
            overheadLayerEntity = null;

            // Clear hidden NPCs from the previous level
            NPCBehaviorComponent.clearHiddenNPCs();

            // Configure weather visuals and night lighting based on the environment
            NightLightingOverlay nightOverlay = app.getNightLightingOverlay();
            if (newMapName.equals("Main_house.tmx")) {
                WeatherSystem.getInstance().setVisualsEnabled(false);
                if (nightOverlay != null) {
                    nightOverlay.setEnabled(false);
                }
            } else {
                WeatherSystem.getInstance().setVisualsEnabled(true);
                if (nightOverlay != null) {
                    nightOverlay.setEnabled(true);
                }
            }

            // 3. TÁI TẠO PLAYER VÀ SELECTOR
            player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
            if (player.hasComponent(com.almasb.fxgl.physics.PhysicsComponent.class)) {
                player.getComponent(com.almasb.fxgl.physics.PhysicsComponent.class).overwritePosition(new Point2D(x, y));
            } else {
                player.setPosition(new Point2D(x, y));
            }
            app.setPlayer(player);

            PlayerComponent newPc = player.getComponent(PlayerComponent.class);
            if (newPc != null) {
                newPc.setMoney(tempMoney);
                newPc.changeSkin(tempSkin);
            }

            if (app.getTradingView() != null) {
                FXGL.getGameScene().removeUINode(app.getTradingView());
            }
            if (app.getAdminView() != null) {
                FXGL.getGameScene().removeUINode(app.getAdminView());
            }
            TradingView tradingView = new TradingView(app.getInventory(), newPc);
            app.setTradingView(tradingView);
            FXGL.getGameScene().addUINode(tradingView);

            AdminView adminView = new AdminView(app.getInventory(), newPc);
            app.setAdminView(adminView);
            FXGL.getGameScene().addUINode(adminView);

            app.bindPlayerUI();
            if (app.getSelector() == null) {
                app.setSelector(FXGL.spawn("Selector"));
            }

            // 4. CẤU HÌNH CAMERA & KÍCH THƯỚC BẢN ĐỒ
            double mapW = 3520;
            double mapH = 2048;
            if (newMapName.equals("Main_house.tmx")) {
                mapW = 1024;
                mapH = 1024;
            } else {
                double maxW = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                        .stream()
                        .mapToDouble(e -> e.getRightX()).max().orElse(3520);
                double maxH = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                        .stream()
                        .mapToDouble(e -> e.getBottomY()).max().orElse(2048);
                mapW = Math.max(mapW, maxW);
                mapH = Math.max(mapH, maxH);
            }

            currentMapWidth = mapW;
            currentMapHeight = mapH;

            FXGL.getGameScene().getViewport().setBounds(0, 0, (int) mapW, (int) mapH);
            FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
            FXGL.getGameScene().getViewport().setLazy(true);

            // Tạo biên bản đồ (Wall) bao quanh map để chặn người chơi đi ra ngoài
            FXGL.getGameWorld().getEntitiesByType(EntityType.WALL).forEach(Entity::removeFromWorld);
            spawnBoundaries((int) mapW, (int) mapH);

            // 5. TẢI TRẠNG THÁI BẢN ĐỒ MỚI (nếu có)
            if (mapStates.containsKey(newMapName)) {
                SaveData newMapState = mapStates.get(newMapName);
                saveLoadSystem.load(newMapState, true);
                System.out.println("Đã tải trạng thái bản đồ: " + newMapName);
            } else {
                FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL)
                        .forEach(s -> s.getComponent(SoilComponent.class).updateTexture());
                System.out.println("Tải bản đồ mới lần đầu: " + newMapName);
                if (newMapName.equals("Main_level.tmx")) {
                    spawnInitialMonsters();
                }
            }

            // Apply pending NPC spawns if returning to main level
            if (newMapName.equals("Main_level.tmx") && !pendingNPCSpawns.isEmpty()) {
                System.out.println("[NPC Cache] Applying cached spawn configs...");
                for (NPCSpawnConfig config : pendingNPCSpawns) {
                    EntityType type = config.type.equalsIgnoreCase("Guider") ? EntityType.GUIDER : EntityType.TRADER;
                    FXGL.getGameWorld().getEntitiesByType(type).forEach(npc -> {
                        NPCBehaviorComponent ai = npc.getComponentOptional(NPCBehaviorComponent.class).orElse(null);
                        if (ai != null) {
                            if (config.isHidden) {
                                ai.disappear();
                            } else {
                                ai.reappear();
                            }
                        }
                    });
                }
                pendingNPCSpawns.clear();
            }
        } finally {
            isLevelTransitioning = false;
        }
    }

    public void updateLevelFromSave(String newMapName, double x, double y) {
        isLevelTransitioning = true;
        try {
            Main app = Main.getInstance();
            currentMap = newMapName;
            FXGL.setLevelFromMap(newMapName);
            Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
            if (player.hasComponent(com.almasb.fxgl.physics.PhysicsComponent.class)) {
                player.getComponent(com.almasb.fxgl.physics.PhysicsComponent.class).overwritePosition(new Point2D(x, y));
            } else {
                player.setPosition(new Point2D(x, y));
            }
            app.setPlayer(player);
            PlayerComponent newPc = player.getComponent(PlayerComponent.class);

            if (app.getTradingView() != null) {
                FXGL.getGameScene().removeUINode(app.getTradingView());
            }
            if (app.getAdminView() != null) {
                FXGL.getGameScene().removeUINode(app.getAdminView());
            }
            TradingView tradingView = new TradingView(app.getInventory(), newPc);
            app.setTradingView(tradingView);
            FXGL.getGameScene().addUINode(tradingView);

            AdminView adminView = new AdminView(app.getInventory(), newPc);
            app.setAdminView(adminView);
            FXGL.getGameScene().addUINode(adminView);

            app.bindPlayerUI();
            if (app.getSelector() == null) {
                app.setSelector(FXGL.spawn("Selector"));
            }

            double mapW = 3520;
            double mapH = 2048;
            if (newMapName.equals("Main_house.tmx")) {
                mapW = 1024;
                mapH = 1024;
            } else {
                double maxW = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                        .stream()
                        .mapToDouble(e -> e.getRightX()).max().orElse(3520);
                double maxH = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                        .stream()
                        .mapToDouble(e -> e.getBottomY()).max().orElse(2048);
                mapW = Math.max(mapW, maxW);
                mapH = Math.max(mapH, maxH);
            }

            currentMapWidth = mapW;
            currentMapHeight = mapH;

            FXGL.getGameScene().getViewport().setBounds(0, 0, (int) mapW, (int) mapH);
            FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
            FXGL.getGameScene().getViewport().setLazy(true);

            FXGL.getGameWorld().getEntitiesByType(EntityType.WALL).forEach(Entity::removeFromWorld);
            spawnBoundaries((int) mapW, (int) mapH);
        } finally {
            isLevelTransitioning = false;
        }
    }

    public void spawnBoundaries(int w, int h) {
        int t = 64;
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(0, -t).put("width", w).put("height", t));
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(0, h).put("width", w).put("height", t));
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(-t, 0).put("width", t).put("height", h));
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(w, 0).put("width", t).put("height", h));
    }

    public void spawnInitialMonsters() {
        System.out.println("--- Spawning Initial Monsters ---");
        String[] types = { "Boar", "Fox", "Deer", "Hare" };
        int[] counts = { 2, 2, 3, 3 };

        java.util.Random rand = new java.util.Random();
        Point2D[] corners = {
                new Point2D(100, 100),
                new Point2D(2900, 100),
                new Point2D(100, 1900),
                new Point2D(2900, 1900)
        };

        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            int count = counts[i];
            for (int j = 0; j < count; j++) {
                Point2D corner = corners[rand.nextInt(corners.length)];
                double offsetX = -30.0 + rand.nextDouble() * 60.0;
                double offsetY = -30.0 + rand.nextDouble() * 60.0;
                double rx = corner.getX() + offsetX;
                double ry = corner.getY() + offsetY;
                FXGL.spawn(type, rx, ry);
            }
        }
    }

    public void spawnBushMonsterAdmin() {
        if (!currentMap.equals("Main_level.tmx")) {
            NotificationManager.pushNotification("Chỉ có thể spawn quái vật ở bản đồ ngoài trời!");
            return;
        }

        java.util.Random rng = new java.util.Random();
        double spawnX = 64;
        double spawnY = 64;
        int corner = rng.nextInt(4);
        if (corner == 0) {
            spawnX = 64;
            spawnY = 64;
        } else if (corner == 1) {
            spawnX = currentMapWidth - 96;
            spawnY = 64;
        } else if (corner == 2) {
            spawnX = 64;
            spawnY = currentMapHeight - 96;
        } else {
            spawnX = currentMapWidth - 96;
            spawnY = currentMapHeight - 96;
        }

        Entity monster = FXGL.spawn("BushMonster", spawnX, spawnY);
        BaseMonsterComponent bmc = monster.getComponentOptional(BaseMonsterComponent.class).orElse(null);
        if (bmc != null) {
            bmc.setTemporary(10.0);
            System.out.println("[Main] Configured spawned Admin BushMonster as temporary with 10.0s lifeTimer.");
        }

        NotificationManager.pushNotification("Admin: Đã spawn một quái vật tại góc bản đồ!");
        System.out.println("Admin spawned BushMonster at corner (" + spawnX + ", " + spawnY + ")");
    }

    public void handleBushMonsterSpawning(double tpf) {
        if (currentMap.equals("Main_level.tmx")) {
            bushMonsterSpawnTimer += tpf;
            if (bushMonsterSpawnTimer >= 15.0) {
                bushMonsterSpawnTimer = 0.0;
                List<Entity> bushes = FXGL.getGameWorld().getEntitiesByType(EntityType.BUSH);
                if (!bushes.isEmpty()) {
                    java.util.Random rng = new java.util.Random();
                    if (rng.nextDouble() < 0.15) {
                        Entity targetBush = bushes.get(rng.nextInt(bushes.size()));
                        FXGL.spawn("BushMonster", targetBush.getX() + targetBush.getWidth() / 2 - 16,
                                targetBush.getY() + targetBush.getHeight() / 2 - 16);
                        NotificationManager.pushNotification("Cảnh báo: Có quái vật xuất hiện từ bụi cây!");
                        System.out.println("Spawned BushMonster at " + targetBush.getPosition());
                    }
                }
            }
        }
    }

    public void handleNPCTransitions(double tpf, TimeSystem timeSystem, TradingView tradingView, CollisionManager collisionManager) {
        if (timeSystem != null) {
            if (currentMap.equals("Main_level.tmx")) {
                // 8:00 PM: đi vào nhà
                if (timeSystem.getHour() >= 20 || timeSystem.getHour() < 6) {
                    FXGL.getGameWorld().getEntitiesByType(EntityType.GUIDER).forEach(npc -> {
                        NPCBehaviorComponent ai = npc.getComponentOptional(NPCBehaviorComponent.class).orElse(null);
                        if (ai != null && !ai.isGoingHome() && !ai.isHidden()) {
                            FXGL.getGameWorld().getEntitiesByType(EntityType.GUIDER_IN).stream().findFirst()
                                    .ifPresent(target -> {
                                         collisionManager.setNearbyNPC(null);
                                         ai.goHome(target);
                                    });
                        }
                    });

                    FXGL.getGameWorld().getEntitiesByType(EntityType.TRADER).forEach(npc -> {
                        NPCBehaviorComponent ai = npc.getComponentOptional(NPCBehaviorComponent.class).orElse(null);
                        if (ai != null && !ai.isGoingHome() && !ai.isHidden()) {
                            FXGL.getGameWorld().getEntitiesByType(EntityType.TRADER_IN).stream().findFirst()
                                    .ifPresent(target -> {
                                         if (tradingView != null && tradingView.isOpen()) {
                                             tradingView.toggle();
                                         }
                                         collisionManager.setNearbyNPC(null);
                                         ai.goHome(target);
                                    });
                        }
                    });
                }
                // 6:00 AM: xuất hiện trở lại
                if (timeSystem.getHour() >= 6 && timeSystem.getHour() < 20) {
                    List<Entity> toReappear = new ArrayList<>(NPCBehaviorComponent.getHiddenNPCs());
                    if (!toReappear.isEmpty()) {
                        for (Entity npc : toReappear) {
                            FXGL.getGameWorld().addEntity(npc);
                            NPCBehaviorComponent ai = npc.getComponentOptional(NPCBehaviorComponent.class).orElse(null);
                            if (ai != null) {
                                ai.reappear();
                            }
                        }
                        NPCBehaviorComponent.clearHiddenNPCs();
                    }
                }
            } else {
                // 8:00 PM: cache that they should be hidden (go home)
                if (timeSystem.getHour() >= 20 || timeSystem.getHour() < 6) {
                    boolean alreadyCachedHidden = pendingNPCSpawns.stream().anyMatch(c -> c.isHidden);
                    if (!alreadyCachedHidden) {
                        pendingNPCSpawns.clear();
                        pendingNPCSpawns.add(new NPCSpawnConfig("Guider", 1792, 1024, true));
                        pendingNPCSpawns.add(new NPCSpawnConfig("Trader", 1600, 1024, true));
                        System.out.println("[NPC Cache] Cached 8:00 PM transitions: NPCs are hidden");
                    }
                }
                // 6:00 AM: cache that they should spawn/reappear
                if (timeSystem.getHour() >= 6 && timeSystem.getHour() < 20) {
                    boolean alreadyCachedVisible = pendingNPCSpawns.stream().anyMatch(c -> !c.isHidden);
                    if (!alreadyCachedVisible) {
                        pendingNPCSpawns.clear();
                        pendingNPCSpawns.add(new NPCSpawnConfig("Guider", 1792, 1024, false));
                        pendingNPCSpawns.add(new NPCSpawnConfig("Trader", 1600, 1024, false));
                        System.out.println("[NPC Cache] Cached 6:00 AM transitions: NPCs are visible");
                    }
                }
            }
        }
    }

    public void handleOverheadTransparency(Entity player) {
        if (overheadLayerData != null && player != null && player.isActive()) {
            if (overheadLayerEntity == null || !overheadLayerEntity.isActive()) {
                overheadLayerEntity = FXGL.getGameWorld().getEntities().stream()
                        .filter(e -> {
                            if (e.getProperties().keys().contains("layer")) {
                                Object layerObj = e.getProperties().getValue("layer");
                                if (layerObj instanceof com.almasb.fxgl.entity.level.tiled.Layer) {
                                    com.almasb.fxgl.entity.level.tiled.Layer l = (com.almasb.fxgl.entity.level.tiled.Layer) layerObj;
                                    return l.getName().equalsIgnoreCase("OverheadLayer");
                                }
                            }
                            return false;
                        })
                        .findFirst()
                        .orElse(null);

                if (overheadLayerEntity != null && overheadLayerEntity.getViewComponent() != null) {
                    overheadLayerEntity.getViewComponent().setZIndex(15);
                    System.out.println("Set OverheadLayer Z-index to 15!");
                }
            }
            if (overheadLayerEntity != null && overheadLayerEntity.getViewComponent() != null) {
                boolean underOverhead = isUnderOverhead(player.getCenter().getX(), player.getY() + 16.0);
                double currentOpacity = overheadLayerEntity.getViewComponent().getOpacity();
                double targetOpacity = underOverhead ? 0.45 : 1.0;
                if (Math.abs(currentOpacity - targetOpacity) > 0.01) {
                    overheadLayerEntity.getViewComponent().setOpacity(targetOpacity);
                }
            }
        }
    }

    public boolean isUnderOverhead(double x, double y) {
        if (overheadLayerData == null || currentTMXMapWidth <= 0) {
            return false;
        }
        int col = (int) (x / 32.0);
        int row = (int) (y / 32.0);
        if (col < 0 || col >= currentTMXMapWidth || row < 0 || row >= currentTMXMapHeight) {
            return false;
        }
        int index = row * currentTMXMapWidth + col;
        if (index >= 0 && index < overheadLayerData.size()) {
            Long gid = overheadLayerData.get(index);
            return gid != null && gid > 0;
        }
        return false;
    }

    public List<NPCSpawnConfig> getPendingNPCSpawns() {
        return pendingNPCSpawns;
    }
}

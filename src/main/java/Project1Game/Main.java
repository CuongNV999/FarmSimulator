package Project1Game;

// --- IMPORT CÁC THÀNH PHẦN NỘI BỘ DỰ ÁN ---
import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.SoilComponent;
import Project1Game.component.farming.animal.BaseAnimalComponent;
import Project1Game.component.farming.monster.BaseMonsterComponent;
import Project1Game.component.player.PlayerComponent;
import Project1Game.core.EntityType;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.model.InventorySlot; // Import InventorySlot
import Project1Game.model.SaveData; // Import SaveData
import Project1Game.system.*;
import Project1Game.ui.*;
import Project1Game.quest.*;
import Project1Game.component.npc.NPCBehaviorComponent;
import Project1Game.component.npc.TraderComponent;
import Project1Game.factory.GameEntityFactory;

// --- IMPORT THƯ VIỆN FXGL ---
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler; // Import CollisionHandler
import com.almasb.fxgl.physics.PhysicsComponent;

// --- IMPORT JAVA FX ---
import javafx.geometry.Point2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class Main extends GameApplication {
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public static boolean isAllowedNotification(String message) {
        if (message == null) {
            return false;
        }
        String msg = message.toLowerCase();

        // Ăn (Eat)
        if (msg.contains("ăn") || msg.contains("đầy bụng") || msg.contains("đói") || msg.contains("thức ăn")
                || msg.contains("eat")) {
            return true;
        }

        // Mua / Bán / Giao dịch (Buy / Sell / Trade / Negotiate)
        if (msg.contains("giao dịch") || msg.contains("tiền") || msg.contains("mua") ||
                msg.contains("bán") || msg.contains("kho đồ") || msg.contains("giỏ hàng") ||
                msg.contains("trader") || msg.contains("thương lượng") || msg.contains("giá") || msg.contains("shop")) {
            return true;
        }

        // Ngủ (Sleep)
        if (msg.contains("ngủ") || msg.contains("sleep") || msg.contains("hồi phục") || msg.contains("ngon")) {
            return true;
        }

        return false;
    }

    public static void pushNotification(String message) {
        if (isAllowedNotification(message)) {
            com.almasb.fxgl.dsl.FXGL.getNotificationService().pushNotification(message);
        }
    }

    public TimeSystem getTimeSystem() {
        return timeSystem;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ToolbarView getToolbarView() {
        return toolbarView;
    }

    public DialogView getDialogView() {
        return dialogView;
    }

    public TradingView getTradingView() {
        return tradingView;
    }

    public StatusBarsView getStatusBarsView() {
        return statusBarsView;
    }

    public void registerWeatherText(Text weatherText) {
        if (hudContainer != null && weatherText != null) {
            if (!hudContainer.getChildren().contains(weatherText)) {
                int moneyIndex = hudContainer.getChildren().indexOf(moneyText);
                if (moneyIndex >= 0) {
                    hudContainer.getChildren().add(moneyIndex, weatherText);
                } else {
                    hudContainer.getChildren().add(weatherText);
                }
            }
        }
    }

    public void drainHungerForWork(double amount) {
        if (statusBarsView != null) {
            statusBarsView.setHunger(Math.max(0, statusBarsView.getHunger() - amount));
        }
    }

    public void handlePlayerFaint() {
        System.out.println("--- Player Fainted! ---");
        if (timeSystem != null) {
            timeSystem.advanceToNextDay();
            // Cache the guider and trader reappearance at 6:00 AM morning
            pendingNPCSpawns.clear();
            pendingNPCSpawns.add(new NPCSpawnConfig("Guider", 1792, 1024, false));
            pendingNPCSpawns.add(new NPCSpawnConfig("Trader", 1600, 1024, false));
            System.out.println("[NPC Cache] Cached morning transitions via faint: NPCs are visible");
        }

        if (player != null) {
            PlayerComponent pc = player.getComponent(PlayerComponent.class);
            if (pc != null) {
                int newMoney = Math.max(0, pc.getMoney() - 100);
                pc.setMoney(newMoney);
            }
        }

        if (statusBarsView != null) {
            statusBarsView.setHealth(statusBarsView.getMaxHealth() * 0.5);
            statusBarsView.setHunger(statusBarsView.getMaxHunger() * 0.5);
        }

        lastHungerDrainTime = -1;
        lastStarveHPTime = -1;

        if (dialogView.isOpen())
            dialogView.hide();
        updateLevel("Main_house.tmx", 550, 350);

        dialogView.setDialog("Thông báo", "Bạn đã bị kiệt sức và ngất xỉu!", "Bác nông dân đã đưa bạn về nhà.",
                "Phạt viện phí: 100 G. Sức khỏe phục hồi 50%.");
        dialogView.show();
    }

    public void handleDoorInteraction(Entity targetDoor) {
        String mapFile = targetDoor.getString("targetMap");
        double tx = targetDoor.getDouble("teleportX");
        double ty = targetDoor.getDouble("teleportY");

        if (currentMap.equals("Main_level.tmx") && mapFile.equals("Main_house.tmx")) {
            lastOutdoorPosition = player.getPosition();
            System.out.println("Cached player outdoor position: " + lastOutdoorPosition);
        }

        if (currentMap.equals("Main_house.tmx") && mapFile.equals("Main_level.tmx") && lastOutdoorPosition != null) {
            tx = lastOutdoorPosition.getX();
            ty = lastOutdoorPosition.getY() + 32.0;
            System.out.println("Using cached outdoor position: " + tx + ", " + ty);
        }

        if (dialogView.isOpen())
            dialogView.hide();

        nearbyDoor = null;
        updateLevel(mapFile, tx, ty);
        System.out.println("Dịch chuyển đến: " + mapFile + " tại " + tx + ", " + ty);
    }

    public void handleSleepInteraction() {
        System.out.println("--- Main: Bắt đầu đi ngủ ---");
        if (timeSystem != null) {
            timeSystem.advanceToNextDay();
            statusBarsView.setHealth(statusBarsView.getMaxHealth());
            statusBarsView.setHunger(statusBarsView.getMaxHunger());

            // Cache the guider and trader reappearance at 6:00 AM morning
            if (currentMap.equals("Main_house.tmx")) {
                pendingNPCSpawns.clear();
                pendingNPCSpawns.add(new NPCSpawnConfig("Guider", 1792, 1024, false));
                pendingNPCSpawns.add(new NPCSpawnConfig("Trader", 1600, 1024, false));
                System.out.println("[NPC Cache] Cached morning transitions via sleep: NPCs are visible");
            }

            dialogView.setDialog("Thông báo", "Bạn đã ngủ một giấc thật ngon.", "Sức khỏe đã được hồi phục!");
            dialogView.show();
            System.out.println("Nhân vật đã đi ngủ.");
        }
        System.out.println("--- Main: Kết thúc đi ngủ ---");
    }

    // --- Các thực thể chính ---
    private Entity player;
    private Entity selector;
    private String lastFarmedCell = "";
    private Inventory inventory;
    private javafx.beans.property.IntegerProperty boundMoneyProperty = null;
    private javafx.beans.value.ChangeListener<Number> moneyListener = null;
    private final java.util.Random rng = new java.util.Random();
    private static boolean shiftHeld = false;
    private javafx.event.EventHandler<DayNightEvent> dayNightHandler = null;

    // --- Các lớp giao diện (UI) ---
    private ToolbarView toolbarView;
    private InventoryView inventoryView;
    private StatusBarsView statusBarsView;
    private DialogView dialogView;
    private MinimapView minimap;
    private Text moneyText; // Thêm Text để hiển thị tiền
    private TradingView tradingView; // Thêm TradingView
    private AdminView adminView; // Thêm AdminView

    // --- Các hệ thống logic (Systems) ---
    private TimeSystem timeSystem;
    private SaveLoadSystem saveLoadSystem;
    private FarmingSystem farmingSystem;

    // --- Node UI đặc thù phục vụ System ---
    private NightLightingOverlay nightOverlay;
    private Text clockText;

    // --- Trạng thái tương tác ---
    private Entity nearbyNPC = null; // Thay thế nearbyNPCName bằng Entity
    private Entity nearbyDoor = null; // Thêm biến để lưu trữ cửa gần đó
    private Entity nearbySleep = null; // Thêm biến để lưu giường gần đó
    private String currentMap = "Main_level.tmx"; // Bản đồ hiện tại
    private Map<String, SaveData> mapStates = new HashMap<>(); // Lưu trạng thái các bản đồ
    private boolean shouldLoadSaveOnStart = false; // Flag to load save game on startup
    private Point2D lastOutdoorPosition = null;

    // --- TMX Map for Overhead querying ---
    private com.almasb.fxgl.entity.level.tiled.TiledMap currentTMXMap = null;
    private java.util.List<Long> overheadLayerData = null;
    private int currentTMXMapWidth = 0;
    private int currentTMXMapHeight = 0;
    private Entity overheadLayerEntity = null;

    // --- Camera Roaming ---
    private double lastMouseX;
    private double lastMouseY;
    private boolean isDraggingCamera = false;
    private double currentMapWidth = 3520;
    private double currentMapHeight = 2048;

    // --- NPC Cache ---
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

    private final java.util.List<NPCSpawnConfig> pendingNPCSpawns = new java.util.ArrayList<>();
    private VBox hudContainer;
    private double lastHungerDrainTime = -1;
    private double lastStarveHPTime = -1;
    private boolean isHPDepletionEnabled = true;
    private double bushMonsterSpawnTimer = 0.0;

    public void toggleHPDepletion() {
        this.isHPDepletionEnabled = !this.isHPDepletionEnabled;
        pushNotification("HP Depletion: " + (isHPDepletionEnabled ? "ON" : "OFF"));
    }

    public boolean isHPDepletionEnabled() {
        return isHPDepletionEnabled;
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1280);
        gameSettings.setHeight(720);
        gameSettings.setTitle("Java Farming Professional");
        gameSettings.setVersion("2.5");
        gameSettings.setDeveloperMenuEnabled(true);
        gameSettings.setMainMenuEnabled(true);
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setSceneFactory(new FarmSceneFactory());
        gameSettings.setNotificationViewClass(CustomNotificationView.class);
    }

    @Override
    protected void onPreInit() {
        FXGL.loopBGM("background_music.mp3");
    }

    @Override
    protected void initPhysics() {
        // Khởi tạo hệ thống vật lý và lắng nghe NPC qua System
        // Sửa đổi: Truyền trực tiếp nearbyNPC và dialogView
        PhysicsSystem.init(new PhysicsSystem.NPCListener() {
            @Override
            public void onNPCNear(Entity npc) {
                nearbyNPC = npc;
            }

            @Override
            public void onNPCAway() {
                nearbyNPC = null;
            }
        }, dialogView);

        // Thêm CollisionHandler cho DOOR
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.DOOR) {
            @Override
            protected void onCollisionBegin(Entity player, Entity door) {
                nearbyDoor = door;
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity door) {
                nearbyDoor = null;
            }
        });

        // Handler cho việc đi ngủ
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.SLEEP) {
            @Override
            protected void onCollisionBegin(Entity player, Entity sleep) {
                nearbySleep = sleep;
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity sleep) {
                nearbySleep = null;
            }
        });

        // Dynamic collision avoidance (push prevention) for animals and monsters
        FXGL.getPhysicsWorld().addCollisionHandler(new CreatureAvoidanceHandler(EntityType.ANIMAL, EntityType.ANIMAL));
        FXGL.getPhysicsWorld().addCollisionHandler(new CreatureAvoidanceHandler(EntityType.ANIMAL, EntityType.MONSTER));
        FXGL.getPhysicsWorld()
                .addCollisionHandler(new CreatureAvoidanceHandler(EntityType.MONSTER, EntityType.MONSTER));

        // Tránh chướng ngại vật cho động vật khi va chạm với các vật cản khác
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.COLLISION));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.WALL));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.PLAYER));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.NPC));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.GUIDER));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.TRADER));
    }

    private static class CreatureAvoidanceHandler extends CollisionHandler {
        public CreatureAvoidanceHandler(EntityType a, EntityType b) {
            super(a, b);
        }

        @Override
        protected void onCollision(Entity entityA, Entity entityB) {
            boolean shouldForceA = false;
            boolean shouldForceB = false;

            BaseAnimalComponent animalA = null;
            BaseMonsterComponent monsterA = null;
            if (entityA.isType(EntityType.ANIMAL)) {
                animalA = entityA.getComponentOptional(BaseAnimalComponent.class).orElse(null);
                if (animalA != null && animalA.getCollisionCooldown() <= 0) {
                    shouldForceA = true;
                }
            } else if (entityA.isType(EntityType.MONSTER)) {
                monsterA = entityA.getComponentOptional(BaseMonsterComponent.class).orElse(null);
                if (monsterA != null && monsterA.getCollisionCooldown() <= 0) {
                    shouldForceA = true;
                }
            }

            BaseAnimalComponent animalB = null;
            BaseMonsterComponent monsterB = null;
            if (entityB.isType(EntityType.ANIMAL)) {
                animalB = entityB.getComponentOptional(BaseAnimalComponent.class).orElse(null);
                if (animalB != null && animalB.getCollisionCooldown() <= 0) {
                    shouldForceB = true;
                }
            } else if (entityB.isType(EntityType.MONSTER)) {
                monsterB = entityB.getComponentOptional(BaseMonsterComponent.class).orElse(null);
                if (monsterB != null && monsterB.getCollisionCooldown() <= 0) {
                    shouldForceB = true;
                }
            }

            if (shouldForceA) {
                if (animalA != null)
                    animalA.forceNewDirection();
                else if (monsterA != null)
                    monsterA.forceNewDirection();
            }

            if (shouldForceB) {
                if (animalB != null)
                    animalB.forceNewDirection();
                else if (monsterB != null)
                    monsterB.forceNewDirection();
            }
        }
    }

    private static class AnimalObstacleCollisionHandler extends CollisionHandler {
        public AnimalObstacleCollisionHandler(EntityType obstacleType) {
            super(EntityType.ANIMAL, obstacleType);
        }

        @Override
        protected void onCollision(Entity animal, Entity obstacle) {
            BaseAnimalComponent animalComp = animal.getComponentOptional(BaseAnimalComponent.class).orElse(null);
            if (animalComp != null && animalComp.getCollisionCooldown() <= 0) {
                animalComp.forceNewDirection();
            }
        }
    }

    @Override
    protected void initGame() {
        instance = this;

        // Reset state variables to prevent carry-over from previous sessions
        player = null;
        selector = null;
        nearbyNPC = null;
        nearbyDoor = null;
        nearbySleep = null;
        lastOutdoorPosition = null;
        mapStates.clear();
        pendingNPCSpawns.clear();
        Project1Game.component.npc.NPCBehaviorComponent.clearHiddenNPCs();

        // 1. Khởi tạo dữ liệu và System nông nghiệp
        inventory = new Inventory();
        farmingSystem = new FarmingSystem(inventory);

        // 2. Khởi tạo Quest
        QuestManager.getInstance().reset();
        QuestManager.getInstance().init();

        // Listen to DayNightEvent.SET_DAY to grow animals in inactive maps
        if (dayNightHandler != null) {
            FXGL.getEventBus().removeEventHandler(DayNightEvent.SET_DAY, dayNightHandler);
        }
        dayNightHandler = e -> {
            for (SaveData state : mapStates.values()) {
                if (state.animals != null) {
                    for (SaveData.AnimalSaveData asd : state.animals) {
                        int maxDays = 0;
                        if (asd.type != null) {
                            switch (asd.type.toUpperCase()) {
                                case "CHICKEN":
                                    maxDays = 4;
                                    break;
                                case "COW":
                                    maxDays = 7;
                                    break;
                                case "SHEEP":
                                    maxDays = 5;
                                    break;
                                case "PIG":
                                    maxDays = 6;
                                    break;
                                case "TURKEY":
                                    maxDays = 3;
                                    break;
                            }
                            if (asd.daysGrown < maxDays) {
                                asd.daysGrown++;
                                System.out.println("[Main] Inactive animal " + asd.type + " grew to " + asd.daysGrown
                                        + "/" + maxDays);
                            }
                        }
                    }
                }
            }
        };
        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_DAY, dayNightHandler);

        // 3. Nạp Map và Factory
        FXGL.getGameWorld().addEntityFactory(new GameEntityFactory());

        // Khởi tạo SaveLoadSystem trước khi gọi updateLevel lần đầu
        // (cần statusBarsView và timeSystem đã được khởi tạo trong initUI)
        // Tạm thời khởi tạo ở đây, sẽ chuyển sang initUI sau khi UI sẵn sàng
    }

    @Override
    protected void initUI() {
        FXGL.getGameScene().setBackgroundColor(Color.BLACK);
        // Khởi tạo các thành phần View
        toolbarView = new ToolbarView(inventory);
        toolbarView.setLayoutX((FXGL.getAppWidth() - 9 * 86) / 2.0);
        toolbarView.setLayoutY(FXGL.getAppHeight() - 100);

        inventoryView = new InventoryView(inventory);
        inventoryView.setLayoutX((FXGL.getAppWidth() - 620) / 2.0);
        inventoryView.setLayoutY((FXGL.getAppHeight() - 300) / 2.0);

        dialogView = new DialogView(FXGL.getAppWidth(), FXGL.getAppHeight());

        statusBarsView = new StatusBarsView();
        statusBarsView.setLayoutX(20);
        statusBarsView.setLayoutY(20);

        minimap = new MinimapView();
        double minimapHeight = 150;
        minimap.setLayoutX(10);
        minimap.setLayoutY(FXGL.getAppHeight() - minimapHeight - 10);

        // Khởi tạo UI đặc thù cho Thời gian
        nightOverlay = new NightLightingOverlay(FXGL.getAppWidth(), FXGL.getAppHeight());

        clockText = new Text();
        clockText.setFont(Project1Game.ui.GameFont.font(FontWeight.BOLD, 20));
        clockText.setStroke(Color.BLACK);
        clockText.setStrokeWidth(0.5);

        // Khởi tạo Text hiển thị tiền
        moneyText = new Text();
        moneyText.setFont(Project1Game.ui.GameFont.font(FontWeight.BOLD, 18));
        moneyText.setFill(Color.GOLD); // Màu chữ
        moneyText.setStroke(Color.BLACK);
        moneyText.setStrokeWidth(0.3);

        double hudContainerWidth = 220;
        hudContainer = new VBox(6);
        hudContainer.setPadding(new Insets(10, 15, 10, 15));
        hudContainer.setPrefWidth(hudContainerWidth);
        hudContainer.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.45); -fx-background-radius: 8; -fx-border-color: rgba(255, 255, 255, 0.15); -fx-border-width: 1; -fx-border-radius: 8;");
        hudContainer.setAlignment(Pos.TOP_RIGHT);
        hudContainer.setLayoutX(FXGL.getAppWidth() - hudContainerWidth - 15);
        hudContainer.setLayoutY(15);
        hudContainer.getChildren().addAll(clockText, moneyText);

        // Đưa tất cả UI vào màn hình
        FXGL.getGameScene().addUINodes(nightOverlay, toolbarView, inventoryView, dialogView, statusBarsView, minimap,
                hudContainer);

        // Khởi tạo các System phụ thuộc UI
        timeSystem = new TimeSystem(nightOverlay, clockText);
        saveLoadSystem = new SaveLoadSystem(inventory, statusBarsView, timeSystem);

        // Khởi tạo WeatherSystem
        WeatherSystem.getInstance().init();

        // Gọi updateLevel lần đầu sau khi tất cả UI và System đã sẵn sàng
        updateLevel("Main_level.tmx", 1792, 1024);
        currentMap = "Main_level.tmx"; // Đảm bảo currentMap được đặt đúng

        bindPlayerUI();

        // Khởi tạo TradingView
        tradingView = new TradingView(inventory, player.getComponent(PlayerComponent.class));
        FXGL.getGameScene().addUINode(tradingView);

        // Khởi tạo AdminView
        adminView = new AdminView(inventory, player.getComponent(PlayerComponent.class));
        FXGL.getGameScene().addUINode(adminView);

        if (shouldLoadSaveOnStart) {
            saveLoadSystem.loadGameFromFile();
            shouldLoadSaveOnStart = false;
        } else if (Project1Game.system.TutorialSystem.getInstance().isFirstTime()) {
            Project1Game.system.TutorialSystem.getInstance().startTutorial();
        }
    }

    /**
     * Phương thức hỗ trợ nạp map và cấu hình lại toàn bộ hệ thống (Player, Camera)
     */
    private void updateLevel(String newMapName, double x, double y) {
        int tempMoney = 1000;
        String tempSkin = PlayerComponent.SELECTED_SKIN;
        if (player != null && player.isActive() && player.hasComponent(PlayerComponent.class)) {
            PlayerComponent pc = player.getComponent(PlayerComponent.class);
            tempMoney = pc.getMoney();
            tempSkin = pc.getCurrentSkin();
        }

        // 1. LƯU TRẠNG THÁI BẢN ĐỒ HIỆN TẠI (nếu có)
        // CHỈ LƯU NẾU PLAYER ĐÃ TỒN TẠI (không phải lần tải map đầu tiên)
        if (player != null && currentMap != null) {
            SaveData currentMapState = new SaveData();
            saveLoadSystem.save(currentMapState, true); // Lưu trạng thái các thực thể động của bản đồ hiện tại (truyền
                                                        // true cho chuyển cảnh)
            mapStates.put(currentMap, currentMapState); // Lưu vào mapStates
            System.out.println("Đã lưu trạng thái bản đồ: " + currentMap);
        }

        // 2. TẢI BẢN ĐỒ MỚI
        currentMap = newMapName; // Cập nhật bản đồ hiện tại
        FXGL.setLevelFromMap(newMapName);

        // Load the TMX map details to query overhead tiles
        try (java.io.InputStream is = FXGL.getAssetLoader().getStream("levels/" + newMapName)) {
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
        Project1Game.component.npc.NPCBehaviorComponent.clearHiddenNPCs();

        // Configure weather visuals and night lighting based on the environment
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
        if (player.hasComponent(PhysicsComponent.class)) {
            player.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(x, y));
        } else {
            player.setPosition(new Point2D(x, y));
        }

        PlayerComponent newPc = player.getComponent(PlayerComponent.class);
        if (newPc != null) {
            newPc.setMoney(tempMoney);
            newPc.changeSkin(tempSkin);
        }

        if (tradingView != null) {
            FXGL.getGameScene().removeUINode(tradingView);
        }
        if (adminView != null) {
            FXGL.getGameScene().removeUINode(adminView);
        }
        tradingView = new TradingView(inventory, newPc);
        FXGL.getGameScene().addUINode(tradingView);

        adminView = new AdminView(inventory, newPc);
        FXGL.getGameScene().addUINode(adminView);

        bindPlayerUI();
        if (selector == null)
            selector = FXGL.spawn("Selector"); // Đảm bảo selector được spawn nếu chưa có

        // 4. CẤU HÌNH CAMERA & KÍCH THƯỚC BẢN ĐỒ
        double mapW = 3520;
        double mapH = 2048;
        if (newMapName.equals("Main_house.tmx")) {
            mapW = 1024;
            mapH = 1024;
        } else {
            // Tự động tính toán dựa trên các thực thể có sẵn
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
            saveLoadSystem.load(newMapState, true); // Tải trạng thái đã lưu cho bản đồ mới (truyền true cho chuyển
                                                    // cảnh)
            System.out.println("Đã tải trạng thái bản đồ: " + newMapName);
        } else {
            // Nếu chưa có trạng thái lưu, đây là lần đầu tiên tải bản đồ này
            // Đảm bảo các thực thể SOIL được cập nhật texture
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
    }

    private void spawnBoundaries(int w, int h) {
        int t = 64;
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(0, -t).put("width", w).put("height", t));
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(0, h).put("width", w).put("height", t));
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(-t, 0).put("width", t).put("height", h));
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(w, 0).put("width", t).put("height", h));
    }

    @Override
    protected void onUpdate(double tpf) {
        // 1. Cập nhật các hệ thống độc lập
        if (timeSystem != null)
            timeSystem.onUpdate(tpf);
        WeatherSystem.getInstance().onUpdate(tpf);
        if (minimap != null)
            minimap.update();

        if (nightOverlay != null && player != null && player.isActive() && player.hasComponent(PlayerComponent.class)) {
            PlayerComponent pc = player.getComponent(PlayerComponent.class);
            nightOverlay.update(player.getCenter(), pc.getDirection());
        }

        // --- Cập nhật chỉ số đói và máu của người chơi ---
        if (timeSystem != null && statusBarsView != null) {
            double currentMins = timeSystem.getGameTime();

            if (lastHungerDrainTime == -1) {
                lastHungerDrainTime = currentMins;
            }

            double hungerDiff = currentMins - lastHungerDrainTime;
            if (hungerDiff < 0)
                hungerDiff += 1440;
            if (hungerDiff >= 30) {
                int intervals = (int) (hungerDiff / 30);
                double newHunger = Math.max(0, statusBarsView.getHunger() - intervals);
                statusBarsView.setHunger(newHunger);
                lastHungerDrainTime = (lastHungerDrainTime + intervals * 30) % 1440;
            }

            if (statusBarsView.getHunger() <= 0) {
                if (isHPDepletionEnabled) {
                    if (lastStarveHPTime == -1) {
                        lastStarveHPTime = currentMins;
                    }
                    double starveDiff = currentMins - lastStarveHPTime;
                    if (starveDiff < 0)
                        starveDiff += 1440;
                    if (starveDiff >= 5) {
                        int intervals = (int) (starveDiff / 5);
                        double newHP = Math.max(0, statusBarsView.getHealth() - (intervals * 1.0));
                        statusBarsView.setHealth(newHP);
                        lastStarveHPTime = (lastStarveHPTime + intervals * 5) % 1440;
                    }
                } else {
                    lastStarveHPTime = -1;
                }
            } else {
                lastStarveHPTime = -1;
            }

            if (statusBarsView.getHealth() <= 0) {
                handlePlayerFaint();
            }
        }

        // AI: Đi vào nhà lúc 8:00 PM và xuất hiện lại lúc 6:00 AM
        if (timeSystem != null) {
            if (currentMap.equals("Main_level.tmx")) {
                // 8:00 PM: đi vào nhà
                if (timeSystem.getHour() >= 20 || timeSystem.getHour() < 6) {
                    FXGL.getGameWorld().getEntitiesByType(EntityType.GUIDER).forEach(npc -> {
                        NPCBehaviorComponent ai = npc.getComponentOptional(NPCBehaviorComponent.class).orElse(null);
                        if (ai != null && !ai.isGoingHome() && !ai.isHidden()) {
                            FXGL.getGameWorld().getEntitiesByType(EntityType.GUIDER_IN).stream().findFirst()
                                    .ifPresent(target -> {
                                        nearbyNPC = null;
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
                                        nearbyNPC = null;
                                        ai.goHome(target);
                                    });
                        }
                    });
                }
                // 6:00 AM: xuất hiện trở lại
                if (timeSystem.getHour() >= 6 && timeSystem.getHour() < 20) {
                    java.util.List<Entity> toReappear = new java.util.ArrayList<>(
                            Project1Game.component.npc.NPCBehaviorComponent.getHiddenNPCs());
                    if (!toReappear.isEmpty()) {
                        for (Entity npc : toReappear) {
                            FXGL.getGameWorld().addEntity(npc);
                            NPCBehaviorComponent ai = npc.getComponentOptional(NPCBehaviorComponent.class).orElse(null);
                            if (ai != null) {
                                ai.reappear();
                            }
                        }
                        Project1Game.component.npc.NPCBehaviorComponent.clearHiddenNPCs();
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

        if (currentMap.equals("Main_level.tmx")) {
            // Spawning Bush Monsters periodically
            bushMonsterSpawnTimer += tpf;
            if (bushMonsterSpawnTimer >= 15.0) {
                bushMonsterSpawnTimer = 0.0;
                java.util.List<Entity> bushes = FXGL.getGameWorld().getEntitiesByType(EntityType.BUSH);
                if (!bushes.isEmpty()) {
                    if (rng.nextDouble() < 0.15) {
                        Entity targetBush = bushes.get(rng.nextInt(bushes.size()));
                        FXGL.spawn("BushMonster", targetBush.getX() + targetBush.getWidth() / 2 - 16,
                                targetBush.getY() + targetBush.getHeight() / 2 - 16);
                        pushNotification("Cảnh báo: Có quái vật xuất hiện từ bụi cây!");
                        System.out.println("Spawned BushMonster at " + targetBush.getPosition());
                    }
                }
            }
        }

        // --- ĐOẠN SỬA MỚI: CHỐNG TRÔI BẰNG CÁCH KHỬ NHIỄU VẬT LÝ ---
        if (player != null) {
            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            if (physics != null) {
                // Nếu vận tốc cực nhỏ (dưới 15) -> Đây chắc chắn là nhiễu trôi vật lý, ép thẳng
                // về 0
                if (Math.abs(physics.getVelocityX()) > 0 && Math.abs(physics.getVelocityX()) < 15) {
                    physics.setVelocityX(0);
                }
                if (Math.abs(physics.getVelocityY()) > 0 && Math.abs(physics.getVelocityY()) < 15) {
                    physics.setVelocityY(0);
                }
            }
        }

        // 2. Cập nhật bộ chọn ô đất và đồng bộ chuyển động nhân vật
        if (farmingSystem != null)
            farmingSystem.updateSelector(selector, player);

        // --- Overhead Layer Transparency Logic ---

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
                // Check if player (head/chest) is under any overhead tile
                boolean underOverhead = isUnderOverhead(player.getCenter().getX(), player.getY() + 16.0);
                double currentOpacity = overheadLayerEntity.getViewComponent().getOpacity();
                double targetOpacity = underOverhead ? 0.45 : 1.0;
                if (Math.abs(currentOpacity - targetOpacity) > 0.01) {
                    overheadLayerEntity.getViewComponent().setOpacity(targetOpacity);
                }
            }
        }
    }

    private boolean isUnderOverhead(double x, double y) {
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

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        input.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                shiftHeld = true;
            }
        });
        input.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                shiftHeld = false;
            }
        });

        // 1. DI CHUYỂN
        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onActionBegin() {
                isDraggingCamera = false;
                FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
            }

            @Override
            protected void onAction() {
                double speed = isShiftHeld() ? 300 : 200;
                player.getComponent(PhysicsComponent.class).setVelocityX(speed);
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PhysicsComponent.class).setVelocityX(0);
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onActionBegin() {
                isDraggingCamera = false;
                FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
            }

            @Override
            protected void onAction() {
                double speed = isShiftHeld() ? 300 : 200;
                player.getComponent(PhysicsComponent.class).setVelocityX(-speed);
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PhysicsComponent.class).setVelocityX(0);
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onActionBegin() {
                isDraggingCamera = false;
                FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
            }

            @Override
            protected void onAction() {
                double speed = isShiftHeld() ? 300 : 200;
                player.getComponent(PhysicsComponent.class).setVelocityY(-speed);
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PhysicsComponent.class).setVelocityY(0);
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onActionBegin() {
                isDraggingCamera = false;
                FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
            }

            @Override
            protected void onAction() {
                double speed = isShiftHeld() ? 300 : 200;
                player.getComponent(PhysicsComponent.class).setVelocityY(speed);
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PhysicsComponent.class).setVelocityY(0);
            }
        }, KeyCode.S);

        // 2. TƯƠNG TÁC TỔNG HỢP (NPC / THU HOẠCH / CỬA)
        input.addAction(new UserAction("Interact Action Key") {
            @Override
            protected void onActionBegin() {
                Entity target = null;
                if (player != null) {
                    double radius = 150.0;
                    javafx.geometry.Point2D playerCenter = player.getCenter();
                    javafx.geometry.Rectangle2D range = new javafx.geometry.Rectangle2D(
                            playerCenter.getX() - radius, playerCenter.getY() - radius,
                            radius * 2, radius * 2);
                    target = FXGL.getGameWorld().getEntitiesInRange(range).stream()
                            .filter(e -> e.hasComponent(Project1Game.interaction.InteractableComponent.class))
                            .findFirst()
                            .orElse(null);
                }

                if (target != null) {
                    target.getComponent(Project1Game.interaction.InteractableComponent.class).interact(player);
                } else {
                    farmingSystem.handleHarvest(selector);
                }
            }
        }, KeyCode.E);

        // 3. SỬ DỤNG CÔNG CỤ (F & CLICK CHUỘT)
        input.addAction(new UserAction("Use Tool Keyboard") {
            @Override
            protected void onActionBegin() {
                handleUseItem();
            }
        }, KeyCode.F);

        input.addAction(new UserAction("Use Tool Mouse") {
            @Override
            protected void onActionBegin() {
                if (selector != null) {
                    int gridX = (int) Math.round(selector.getX() / 32.0);
                    int gridY = (int) Math.round(selector.getY() / 32.0);
                    lastFarmedCell = gridX + "," + gridY;
                }
                handleUseItem();
            }

            @Override
            protected void onAction() {
                ItemType selected = inventory.getSelectedItem();
                if (selected == ItemType.HOE || selected == ItemType.WATERING_CAN) {
                    if (selector != null && selector.getViewComponent().getOpacity() >= 1.0) {
                        int gridX = (int) Math.round(selector.getX() / 32.0);
                        int gridY = (int) Math.round(selector.getY() / 32.0);
                        String currentCell = gridX + "," + gridY;
                        if (!currentCell.equals(lastFarmedCell)) {
                            lastFarmedCell = currentCell;
                            handleUseItem();
                        }
                    }
                }
            }

            @Override
            protected void onActionEnd() {
                lastFarmedCell = "";
            }
        }, MouseButton.PRIMARY);

        input.addAction(new UserAction("Test Quick Water") {
            @Override
            protected void onActionBegin() { // Đã sửa lỗi "void void"
                System.out.println("Test: Ép tưới cây bằng phím Q!");
                farmingSystem.useWateringCan(selector);
            }
        }, KeyCode.Q);

        // 4. HỆ THỐNG GIAO DIỆN
        input.addAction(new UserAction("Close UI Window") {
            @Override
            protected void onActionBegin() {
                if (dialogView.isOpen())
                    dialogView.hide();
                if (tradingView.isOpen())
                    tradingView.toggle(); // Đóng TradingView khi nhấn R
                if (inventoryView.isOpen())
                    inventoryView.toggle(); // Đóng InventoryView khi nhấn R
                if (adminView.isOpen())
                    adminView.toggle(); // Đóng AdminView khi nhấn R
            }
        }, KeyCode.R); // Sử dụng R để đóng UI

        input.addAction(new UserAction("Toggle Inventory Window") {
            @Override
            protected void onActionBegin() {
                inventoryView.toggle();
            }
        }, KeyCode.I);

        input.addAction(new UserAction("Toggle Inventory Window TAB") {
            @Override
            protected void onActionBegin() {
                inventoryView.toggle();
            }
        }, KeyCode.TAB);

        input.addAction(new UserAction("Toggle Admin Panel") {
            @Override
            protected void onActionBegin() {
                adminView.toggle();
            }
        }, KeyCode.BACK_QUOTE);

        // 5. LƯU & TẢI (F5 / F9)
        input.addAction(new UserAction("Quick Save") {
            @Override
            protected void onActionBegin() {
                saveLoadSystem.saveGameToFile();
            } // Gọi phương thức lưu vào file
        }, KeyCode.F5);

        input.addAction(new UserAction("Quick Load") {
            @Override
            protected void onActionBegin() {
                saveLoadSystem.loadGameFromFile();
            } // Gọi phương thức tải từ file
        }, KeyCode.F9);

        input.addAction(new UserAction("Toggle HP Depletion") {
            @Override
            protected void onActionBegin() {
                toggleHPDepletion();
            }
        }, KeyCode.F6);

        input.addAction(new UserAction("Cheat Mature All") {
            @Override
            protected void onActionBegin() {
                matureAllCropsAndAnimals();
            }
        }, KeyCode.F7);

        // Admin Console Time Speed Controls
        input.addAction(new UserAction("Set Time Speed 1x") {
            @Override
            protected void onActionBegin() {
                if (timeSystem != null)
                    timeSystem.setTimeSpeedMultiplier(1.0);
            }
        }, KeyCode.NUMPAD7);

        input.addAction(new UserAction("Set Time Speed 50x") {
            @Override
            protected void onActionBegin() {
                if (timeSystem != null)
                    timeSystem.setTimeSpeedMultiplier(50.0);
            }
        }, KeyCode.NUMPAD8);

        // 6. CHỌN SLOT 1-9
        KeyCode[] digitCodes = { KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4,
                KeyCode.DIGIT5, KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9 };
        for (int i = 0; i < 9; i++) {
            final int slot = i;
            input.addAction(new UserAction("Select Inventory Slot " + (i + 1)) {
                @Override
                protected void onActionBegin() {
                    inventory.setSelectedSlot(slot);
                    toolbarView.updateSelection();
                }
            }, digitCodes[i]);
        }

        // 7. CUỘN CHUỘT ĐỔI VẬT PHẨM
        input.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() < 0)
                inventory.selectNext();
            else
                inventory.selectPrevious();
            toolbarView.updateSelection();
        });

        // 8. CAMERA ROAMING (Middle / Right Click Drag)
        input.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.MIDDLE) {
                FXGL.getGameScene().getViewport().unbind();
                lastMouseX = e.getScreenX();
                lastMouseY = e.getScreenY();
                isDraggingCamera = true;
            }
        });

        input.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (isDraggingCamera) {
                double dx = e.getScreenX() - lastMouseX;
                double dy = e.getScreenY() - lastMouseY;
                var viewport = FXGL.getGameScene().getViewport();
                double targetX = viewport.getX() - dx;
                double targetY = viewport.getY() - dy;
                double maxX = currentMapWidth - FXGL.getAppWidth();
                double maxY = currentMapHeight - FXGL.getAppHeight();
                if (targetX < 0)
                    targetX = 0;
                if (targetX > maxX)
                    targetX = maxX;
                if (targetY < 0)
                    targetY = 0;
                if (targetY > maxY)
                    targetY = maxY;
                viewport.setX(targetX);
                viewport.setY(targetY);
                lastMouseX = e.getScreenX();
                lastMouseY = e.getScreenY();
            }
        });

        input.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (isDraggingCamera && (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.MIDDLE)) {
                isDraggingCamera = false;
            }
        });
    }

    /** Điều phối sử dụng vật phẩm dựa trên loại đang chọn */
    private void handleUseItem() {
        ItemType selected = inventory.getSelectedItem();
        if (selected != null) {
            selected.use(player, selector);
        }
    }

    /** Kiểm tra trạng thái ban ngày (Bridge method) */
    public String getCurrentMap() {
        return currentMap;
    }

    public double getCurrentMapWidth() {
        return currentMapWidth;
    }

    public double getCurrentMapHeight() {
        return currentMapHeight;
    }

    public void setShouldLoadSaveOnStart(boolean value) {
        this.shouldLoadSaveOnStart = value;
    }

    public boolean getShouldLoadSaveOnStart() {
        return shouldLoadSaveOnStart;
    }

    public void saveGame() {
        if (saveLoadSystem != null) {
            saveLoadSystem.saveGameToFile();
        }
    }

    public void loadGame() {
        if (saveLoadSystem != null) {
            saveLoadSystem.loadGameFromFile();
        }
    }

    public boolean hasSaveGame() {
        return new java.io.File("save_game.dat").exists();
    }

    private void bindPlayerUI() {
        if (player == null || !player.isActive() || !player.hasComponent(PlayerComponent.class) || moneyText == null)
            return;
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        if (boundMoneyProperty != null && moneyListener != null) {
            boundMoneyProperty.removeListener(moneyListener);
        }
        boundMoneyProperty = playerComponent.moneyProperty();
        moneyListener = (obs, old, newV) -> {
            moneyText.setText("Tiền: " + newV + " G");
        };
        boundMoneyProperty.addListener(moneyListener);
        moneyText.setText("Tiền: " + playerComponent.getMoney() + " G");
    }

    public void updateLevelFromSave(String newMapName, double x, double y) {
        currentMap = newMapName;
        FXGL.setLevelFromMap(newMapName);
        player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player.hasComponent(PhysicsComponent.class)) {
            player.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(x, y));
        } else {
            player.setPosition(new Point2D(x, y));
        }
        PlayerComponent newPc = player.getComponent(PlayerComponent.class);
        if (tradingView != null) {
            FXGL.getGameScene().removeUINode(tradingView);
        }
        if (adminView != null) {
            FXGL.getGameScene().removeUINode(adminView);
        }
        tradingView = new TradingView(inventory, newPc);
        FXGL.getGameScene().addUINode(tradingView);

        adminView = new AdminView(inventory, newPc);
        FXGL.getGameScene().addUINode(adminView);

        bindPlayerUI();
        if (selector == null)
            selector = FXGL.spawn("Selector");

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
    }

    public void spawnInitialMonsters() {
        System.out.println("--- Spawning Initial Monsters ---");
        String[] types = { "Boar", "Fox", "Deer", "Hare" };
        int[] counts = { 2, 2, 3, 3 };

        java.util.Random rand = new java.util.Random();
        javafx.geometry.Point2D[] corners = {
                new javafx.geometry.Point2D(100, 100), // Top-Left Boundary Corner
                new javafx.geometry.Point2D(2900, 100), // Top-Right Boundary Corner
                new javafx.geometry.Point2D(100, 1900), // Bottom-Left Boundary Corner
                new javafx.geometry.Point2D(2900, 1900) // Bottom-Right Boundary Corner
        };

        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            int count = counts[i];
            for (int j = 0; j < count; j++) {
                javafx.geometry.Point2D corner = corners[rand.nextInt(corners.length)];
                double offsetX = -30.0 + rand.nextDouble() * 60.0;
                double offsetY = -30.0 + rand.nextDouble() * 60.0;
                double rx = corner.getX() + offsetX;
                double ry = corner.getY() + offsetY;
                FXGL.spawn(type, rx, ry);
            }
        }
    }

    public void matureAllCropsAndAnimals() {
        // Animals
        FXGL.getGameWorld().getEntitiesByType(EntityType.ANIMAL).forEach(e -> {
            BaseAnimalComponent bac = e.getComponentOptional(BaseAnimalComponent.class).orElse(null);
            if (bac != null) {
                bac.setDaysGrown(bac.getMaxGrowthDays());
                bac.initAnimation();
            }
        });

        // Crops
        FXGL.getGameWorld().getEntitiesByComponent(CropComponent.class).forEach(e -> {
            CropComponent cc = e.getComponentOptional(CropComponent.class).orElse(null);
            if (cc != null) {
                cc.setStage(Project1Game.config.CropData.STAGE_RIPE);
            }
        });

        pushNotification("Admin Cheat: All animals and crops are now fully mature!");
    }

    public void spawnBushMonsterAdmin() {
        if (!currentMap.equals("Main_level.tmx")) {
            pushNotification("Chỉ có thể spawn quái vật ở bản đồ ngoài trời!");
            return;
        }

        double spawnX = 64;
        double spawnY = 64;
        int corner = rng.nextInt(4);
        if (corner == 0) { // Top-Left
            spawnX = 64;
            spawnY = 64;
        } else if (corner == 1) { // Top-Right
            spawnX = currentMapWidth - 96;
            spawnY = 64;
        } else if (corner == 2) { // Bottom-Left
            spawnX = 64;
            spawnY = currentMapHeight - 96;
        } else { // Bottom-Right
            spawnX = currentMapWidth - 96;
            spawnY = currentMapHeight - 96;
        }

        Entity monster = FXGL.spawn("BushMonster", spawnX, spawnY);
        BaseMonsterComponent bmc = monster.getComponentOptional(BaseMonsterComponent.class).orElse(null);
        if (bmc != null) {
            bmc.setTemporary(10.0);
            System.out.println("[Main] Configured spawned Admin BushMonster as temporary with 10.0s lifeTimer.");
        } else {
            System.out.println("[Main] Warning: Spawned Admin BushMonster does not have BaseMonsterComponent!");
        }

        pushNotification("Admin: Đã spawn một quái vật tại góc bản đồ!");
        System.out.println("Admin spawned BushMonster at corner (" + spawnX + ", " + spawnY + ")");
    }

    public static boolean isShiftHeld() {
        return shiftHeld;
    }

    public static boolean isDayTime() {
        Main app = (Main) FXGL.getApp();
        return app.timeSystem != null && app.timeSystem.isDayTime();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
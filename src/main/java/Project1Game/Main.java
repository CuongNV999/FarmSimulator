package Project1Game;

import Project1Game.ui.view.hud.*;
import Project1Game.ui.view.menu.*;
import Project1Game.ui.view.dialog.*;
import Project1Game.ui.view.shop.*;
import Project1Game.ui.view.admin.*;
import Project1Game.ui.view.inventory.*;
import Project1Game.ui.view.overlay.*;
import Project1Game.ui.utility.GameFont;
import Project1Game.ui.presenter.*;

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

    // --- Decoupled System Managers ---
    private final CollisionManager collisionManager = new CollisionManager();
    private final LevelManager levelManager = new LevelManager();
    private final PlayerStateManager playerStateManager = new PlayerStateManager();
    private final InputController inputController = new InputController();

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public PlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    public InputController getInputController() {
        return inputController;
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

    public InventoryView getInventoryView() {
        return inventoryView;
    }

    public AdminView getAdminView() {
        return adminView;
    }

    public void setTradingView(TradingView tradingView) {
        this.tradingView = tradingView;
    }

    public void setAdminView(AdminView adminView) {
        this.adminView = adminView;
    }

    public FarmingSystem getFarmingSystem() {
        return farmingSystem;
    }

    public NightLightingOverlay getNightLightingOverlay() {
        return nightOverlay;
    }

    public Entity getPlayer() {
        return player;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    public Entity getSelector() {
        return selector;
    }

    public void setSelector(Entity selector) {
        this.selector = selector;
    }

    // --- Notification Rules and Filter logic ---
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
        playerStateManager.drainHungerForWork(amount, statusBarsView);
    }

    public void handlePlayerFaint() {
        playerStateManager.handlePlayerFaint(timeSystem, player, statusBarsView, dialogView, levelManager);
    }

    public void handleDoorInteraction(Entity targetDoor) {
        levelManager.handleDoorInteraction(targetDoor, dialogView, collisionManager, saveLoadSystem);
    }

    public void handleSleepInteraction() {
        playerStateManager.handleSleepInteraction(timeSystem, statusBarsView, dialogView, levelManager);
    }

    public void toggleHPDepletion() {
        playerStateManager.toggleHPDepletion();
    }

    public boolean isHPDepletionEnabled() {
        return playerStateManager.isHPDepletionEnabled();
    }

    // --- Các thực thể chính ---
    private Entity player;
    private Entity selector;
    private Inventory inventory;
    private javafx.beans.property.IntegerProperty boundMoneyProperty = null;
    private javafx.beans.value.ChangeListener<Number> moneyListener = null;
    private final java.util.Random rng = new java.util.Random();
    private javafx.event.EventHandler<DayNightEvent> dayNightHandler = null;

    // --- Các lớp giao diện (UI) ---
    private ToolbarView toolbarView;
    private InventoryView inventoryView;
    private StatusBarsView statusBarsView;
    private DialogView dialogView;
    private MinimapView minimap;
    private Text moneyText;
    private TradingView tradingView;
    private AdminView adminView;

    // --- Các hệ thống logic (Systems) ---
    private TimeSystem timeSystem;
    private SaveLoadSystem saveLoadSystem;
    private FarmingSystem farmingSystem;

    // --- Node UI đặc thù phục vụ System ---
    private NightLightingOverlay nightOverlay;
    private Text clockText;

    private boolean shouldLoadSaveOnStart = false;
    private VBox hudContainer;

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
        collisionManager.initCollisionHandlers(dialogView);
    }

    @Override
    protected void initGame() {
        instance = this;

        // Reset state variables to prevent carry-over from previous sessions
        player = null;
        selector = null;
        collisionManager.clearNearby();
        levelManager.clearState();
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
            for (SaveData state : levelManager.getMapStates().values()) {
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
        clockText.setFont(Project1Game.ui.utility.GameFont.font(FontWeight.BOLD, 20));
        clockText.setStroke(Color.BLACK);
        clockText.setStrokeWidth(0.5);

        // Khởi tạo Text hiển thị tiền
        moneyText = new Text();
        moneyText.setFont(Project1Game.ui.utility.GameFont.font(FontWeight.BOLD, 18));
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
        levelManager.setCurrentMap("Main_level.tmx"); // Đảm bảo currentMap được đặt đúng

        bindPlayerUI();

        // Khởi tạo TradingView
        tradingView = new TradingView(inventory, player.getComponent(PlayerComponent.class));
        new TradingPresenter(tradingView, inventory, player.getComponent(PlayerComponent.class));
        FXGL.getGameScene().addUINode(tradingView);

        // Khởi tạo AdminView
        adminView = new AdminView(inventory, player.getComponent(PlayerComponent.class));
        new AdminPresenter(inventory, player.getComponent(PlayerComponent.class));
        FXGL.getGameScene().addUINode(adminView);

        if (shouldLoadSaveOnStart) {
            saveLoadSystem.loadGameFromFile();
            shouldLoadSaveOnStart = false;
        } else if (Project1Game.system.TutorialSystem.getInstance().isFirstTime()) {
            Project1Game.system.TutorialSystem.getInstance().startTutorial();
        }
    }

    public void updateLevel(String newMapName, double x, double y) {
        levelManager.updateLevel(newMapName, x, y, saveLoadSystem);
    }

    @Override
    protected void onUpdate(double tpf) {
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
        playerStateManager.updatePlayerStats(tpf, timeSystem, statusBarsView, player, dialogView, levelManager);

        // AI: Đi vào nhà lúc 8:00 PM và xuất hiện lại lúc 6:00 AM
        levelManager.handleNPCTransitions(tpf, timeSystem, tradingView, collisionManager);

        // Spawning Bush Monsters periodically
        levelManager.handleBushMonsterSpawning(tpf);

        // --- ĐOẠN SỬA MỚI: CHỐNG TRÔI BẰNG CÁCH KHỬ NHIỄU VẬT LÝ ---
        if (player != null) {
            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            if (physics != null) {
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
        levelManager.handleOverheadTransparency(player);
    }

    @Override
    protected void initInput() {
        inputController.initInputBindings(this);
    }

    public String getCurrentMap() {
        return levelManager.getCurrentMap();
    }

    public double getCurrentMapWidth() {
        return levelManager.getCurrentMapWidth();
    }

    public double getCurrentMapHeight() {
        return levelManager.getCurrentMapHeight();
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

    public void bindPlayerUI() {
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
        levelManager.updateLevelFromSave(newMapName, x, y);
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

        NotificationManager.pushNotification("Admin Cheat: All animals and crops are now fully mature!");
    }

    public void spawnBushMonsterAdmin() {
        levelManager.spawnBushMonsterAdmin();
    }

    public static boolean isShiftHeld() {
        return InputController.isShiftHeld();
    }

    public static boolean isDayTime() {
        Main app = (Main) FXGL.getApp();
        return app.timeSystem != null && app.timeSystem.isDayTime();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

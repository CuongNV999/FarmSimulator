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

    public FarmingSystem getFarmingSystem() {
        return farmingSystem;
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

    public void registerWeatherText(Text weatherText) {
        if (hudManager != null) {
            hudManager.registerWeatherText(weatherText);
        }
    }

    public void drainHungerForWork(double amount) {
        if (hudManager != null && hudManager.getStatusBarsView() != null) {
            playerStateManager.drainHungerForWork(amount, hudManager.getStatusBarsView());
        }
    }

    public void handlePlayerFaint() {
        if (hudManager != null) {
            playerStateManager.handlePlayerFaint(timeSystem, player, hudManager.getStatusBarsView(), hudManager.getDialogView(), levelManager);
        }
    }

    public void handleDoorInteraction(Entity targetDoor) {
        levelManager.handleDoorInteraction(targetDoor, hudManager.getDialogView(), collisionManager, saveLoadSystem);
    }

    public void handleSleepInteraction() {
        if (hudManager != null) {
            playerStateManager.handleSleepInteraction(timeSystem, hudManager.getStatusBarsView(), hudManager.getDialogView(), levelManager);
        }
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
    private final java.util.Random rng = new java.util.Random();

    // --- HUD Manager ---
    private HUDManager hudManager;

    // --- Offline World Growth Simulator ---
    private OfflineWorldGrowthManager offlineWorldGrowthManager;

    // --- Các hệ thống logic (Systems) ---
    private TimeSystem timeSystem;
    private SaveLoadSystem saveLoadSystem;
    private FarmingSystem farmingSystem;

    private boolean shouldLoadSaveOnStart = false;

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
        collisionManager.initCollisionHandlers(hudManager != null ? hudManager.getDialogView() : null);
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

        // 3. Khởi tạo Offline Growth Simulator
        offlineWorldGrowthManager = new OfflineWorldGrowthManager();
        offlineWorldGrowthManager.init();

        // 4. Nạp Map và Factory
        FXGL.getGameWorld().addEntityFactory(new GameEntityFactory());
    }

    @Override
    protected void initUI() {
        FXGL.getGameScene().setBackgroundColor(Color.BLACK);

        // Khởi tạo HUD Manager
        hudManager = new HUDManager();
        hudManager.initializeHUD(this);

        // Khởi tạo các System phụ thuộc UI
        timeSystem = new TimeSystem(hudManager.getNightOverlay(), hudManager.getClockText());
        saveLoadSystem = new SaveLoadSystem(inventory, hudManager.getStatusBarsView(), timeSystem);

        // Khởi tạo WeatherSystem
        WeatherSystem.getInstance().init();

        // Gọi updateLevel lần đầu sau khi tất cả UI và System đã sẵn sàng
        updateLevel("Main_level.tmx", 1792, 1024);
        levelManager.setCurrentMap("Main_level.tmx"); // Đảm bảo currentMap được đặt đúng

        bindPlayerUI();

        // Khởi tạo TradingView
        TradingView tradingView = new TradingView(inventory, player.getComponent(PlayerComponent.class));
        hudManager.setTradingView(tradingView);
        new TradingPresenter(tradingView, inventory, player.getComponent(PlayerComponent.class));
        FXGL.getGameScene().addUINode(tradingView);

        // Khởi tạo AdminView
        AdminView adminView = new AdminView(inventory, player.getComponent(PlayerComponent.class));
        hudManager.setAdminView(adminView);
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

        if (hudManager != null) {
            hudManager.refresh();
        }

        if (hudManager != null && hudManager.getNightOverlay() != null && player != null && player.isActive() && player.hasComponent(PlayerComponent.class)) {
            PlayerComponent pc = player.getComponent(PlayerComponent.class);
            hudManager.getNightOverlay().update(player.getCenter(), pc.getDirection());
        }

        // --- Cập nhật chỉ số đói và máu của người chơi ---
        if (hudManager != null) {
            playerStateManager.updatePlayerStats(tpf, timeSystem, hudManager.getStatusBarsView(), player, hudManager.getDialogView(), levelManager);
        }

        // AI: Đi vào nhà lúc 8:00 PM và xuất hiện lại lúc 6:00 AM
        if (hudManager != null) {
            levelManager.handleNPCTransitions(tpf, timeSystem, hudManager.getTradingView(), collisionManager);
        }

        // Spawning Bush Monsters periodically
        levelManager.handleBushMonsterSpawning(tpf);

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
        if (hudManager != null) {
            hudManager.bindPlayerUI(player);
        }
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

    public static boolean isDayTime() {
        Main app = (Main) FXGL.getApp();
        return app.timeSystem != null && app.timeSystem.isDayTime();
    }

    // --- Delegation methods for backward compatibility ---

    public ToolbarView getToolbarView() {
        return hudManager != null ? hudManager.getToolbarView() : null;
    }

    public DialogView getDialogView() {
        return hudManager != null ? hudManager.getDialogView() : null;
    }

    public TradingView getTradingView() {
        return hudManager != null ? hudManager.getTradingView() : null;
    }

    public StatusBarsView getStatusBarsView() {
        return hudManager != null ? hudManager.getStatusBarsView() : null;
    }

    public InventoryView getInventoryView() {
        return hudManager != null ? hudManager.getInventoryView() : null;
    }

    public AdminView getAdminView() {
        return hudManager != null ? hudManager.getAdminView() : null;
    }

    public void setTradingView(TradingView tradingView) {
        if (hudManager != null) {
            hudManager.setTradingView(tradingView);
        }
    }

    public void setAdminView(AdminView adminView) {
        if (hudManager != null) {
            hudManager.setAdminView(adminView);
        }
    }

    public NightLightingOverlay getNightLightingOverlay() {
        return hudManager != null ? hudManager.getNightOverlay() : null;
    }

    public HUDManager getHudManager() {
        return hudManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

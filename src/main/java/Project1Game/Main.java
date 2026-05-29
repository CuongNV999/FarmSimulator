package Project1Game;

// --- IMPORT CÁC THÀNH PHẦN NỘI BỘ DỰ ÁN ---
import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.SoilComponent;
import Project1Game.component.player.PlayerComponent;
import Project1Game.core.EntityType;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.model.InventorySlot; // Import InventorySlot
import Project1Game.model.SaveData; // Import SaveData
import Project1Game.system.*;
import Project1Game.ui.*;
import Project1Game.quest.*;
import Project1Game.component.NPCBehaviorComponent;
import Project1Game.component.TraderComponent;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class Main extends GameApplication {
    // --- Các thực thể chính ---
    private Entity player;
    private Entity selector;
    private Inventory inventory;

    // --- Các lớp giao diện (UI) ---
    private ToolbarView toolbarView;
    private InventoryView inventoryView;
    private StatusBarsView statusBarsView;
    private DialogView dialogView;
    private MinimapView minimap;
    private Text moneyText; // Thêm Text để hiển thị tiền
    private TradingView tradingView; // Thêm TradingView

    // --- Các hệ thống logic (Systems) ---
    private TimeSystem timeSystem;
    private SaveLoadSystem saveLoadSystem;
    private FarmingSystem farmingSystem;

    // --- Node UI đặc thù phục vụ System ---
    private Rectangle nightOverlay;
    private Text clockText;

    // --- Trạng thái tương tác ---
    private Entity nearbyNPC = null; // Thay thế nearbyNPCName bằng Entity
    private Entity nearbyDoor = null; // Thêm biến để lưu trữ cửa gần đó
    private Entity nearbySleep = null; // Thêm biến để lưu giường gần đó
    private String currentMap = "Main_level.tmx"; // Bản đồ hiện tại
    private Map<String, SaveData> mapStates = new HashMap<>(); // Lưu trạng thái các bản đồ
    private boolean shouldLoadSaveOnStart = false; // Flag to load save game on startup

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1280);
        gameSettings.setHeight(720);
        gameSettings.setTitle("Java Farming Professional");
        gameSettings.setVersion("2.5");
        gameSettings.setDeveloperMenuEnabled(true);
        gameSettings.setMainMenuEnabled(true);
        gameSettings.setSceneFactory(new FarmSceneFactory());
    }

    @Override
    protected void initPhysics() {
        // Khởi tạo hệ thống vật lý và lắng nghe NPC qua System
        // Sửa đổi: Truyền trực tiếp nearbyNPC và dialogView
        PhysicsSystem.init(new PhysicsSystem.NPCListener() {
            @Override
            public void onNPCNear(Entity npc) { nearbyNPC = npc; }

            @Override
            public void onNPCAway() { nearbyNPC = null; }
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
    }

    @Override
    protected void initGame() {
        // 1. Khởi tạo dữ liệu và System nông nghiệp
        inventory = new Inventory();
        farmingSystem = new FarmingSystem(inventory);

        // 2. Khởi tạo Quest
        QuestManager.getInstance().init();

        // 3. Nạp Map và Factory
        FXGL.getGameWorld().addEntityFactory(new GameEntityFactory());

        // Khởi tạo SaveLoadSystem trước khi gọi updateLevel lần đầu
        // (cần statusBarsView và timeSystem đã được khởi tạo trong initUI)
        // Tạm thời khởi tạo ở đây, sẽ chuyển sang initUI sau khi UI sẵn sàng
    }

    @Override
    protected void initUI() {
        // Khởi tạo các thành phần View
        toolbarView = new ToolbarView(inventory);
        toolbarView.setLayoutX((FXGL.getAppWidth() - 9 * 86) / 2.0);
        toolbarView.setLayoutY(FXGL.getAppHeight() - 100);

        inventoryView = new InventoryView(inventory);
        inventoryView.setLayoutX((FXGL.getAppWidth() - 620) / 2.0);
        inventoryView.setLayoutY((FXGL.getAppHeight() - 300) / 2.0);

        dialogView = new DialogView(FXGL.getAppWidth(), FXGL.getAppHeight());

        statusBarsView = new StatusBarsView();
        statusBarsView.setLayoutX(20); statusBarsView.setLayoutY(20);

        minimap = new MinimapView();
        minimap.setLayoutX(FXGL.getAppWidth() - 170); minimap.setLayoutY(60);

        // Khởi tạo UI đặc thù cho Thời gian
        nightOverlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.BLACK);
        nightOverlay.setMouseTransparent(true);
        nightOverlay.setOpacity(0);

        clockText = new Text();
        clockText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        clockText.setTranslateX(FXGL.getAppWidth() - 150); clockText.setTranslateY(40);
        clockText.setStroke(Color.BLACK); clockText.setStrokeWidth(0.5);

        // Khởi tạo Text hiển thị tiền
        moneyText = new Text();
        moneyText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        moneyText.setTranslateX(20); // Vị trí X
        moneyText.setTranslateY(FXGL.getAppHeight() - 20); // Vị trí Y (góc dưới bên trái)
        moneyText.setFill(Color.GOLD); // Màu chữ
        moneyText.setStroke(Color.BLACK);
        moneyText.setStrokeWidth(0.5);

        // Đưa tất cả UI vào màn hình
        FXGL.getGameScene().addUINodes(toolbarView, inventoryView, dialogView, statusBarsView, minimap, nightOverlay, clockText, moneyText);

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

        if (shouldLoadSaveOnStart) {
            saveLoadSystem.loadGameFromFile();
            shouldLoadSaveOnStart = false;
        }
    }

    /**
     * Phương thức hỗ trợ nạp map và cấu hình lại toàn bộ hệ thống (Player, Camera)
     */
    private void updateLevel(String newMapName, double x, double y) {
        // 1. LƯU TRẠNG THÁI BẢN ĐỒ HIỆN TẠI (nếu có)
        // CHỈ LƯU NẾU PLAYER ĐÃ TỒN TẠI (không phải lần tải map đầu tiên)
        if (player != null && currentMap != null) {
            SaveData currentMapState = new SaveData();
            saveLoadSystem.save(currentMapState, true); // Lưu trạng thái các thực thể động của bản đồ hiện tại (truyền true cho chuyển cảnh)
            mapStates.put(currentMap, currentMapState); // Lưu vào mapStates
            System.out.println("Đã lưu trạng thái bản đồ: " + currentMap);
        }

        // 2. TẢI BẢN ĐỒ MỚI
        currentMap = newMapName; // Cập nhật bản đồ hiện tại
        FXGL.setLevelFromMap(newMapName);

        // 3. TÁI TẠO PLAYER VÀ SELECTOR
        player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        if (player.hasComponent(PhysicsComponent.class)) {
            player.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(x, y));
        } else {
            player.setPosition(new Point2D(x, y));
        }
        bindPlayerUI();
        if (selector == null) selector = FXGL.spawn("Selector"); // Đảm bảo selector được spawn nếu chưa có

        // 4. CẤU HÌNH CAMERA & KÍCH THƯỚC BẢN ĐỒ
        double mapW = 3520;
        double mapH = 2048;
        if (newMapName.equals("Main_house.tmx")) {
            mapW = 1024;
            mapH = 1024;
        } else {
            // Tự động tính toán dựa trên các thực thể có sẵn
            double maxW = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION).stream()
                    .mapToDouble(e -> e.getRightX()).max().orElse(3520);
            double maxH = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION).stream()
                    .mapToDouble(e -> e.getBottomY()).max().orElse(2048);
            mapW = Math.max(mapW, maxW);
            mapH = Math.max(mapH, maxH);
        }

        FXGL.getGameScene().getViewport().setBounds(0, 0, (int)mapW, (int)mapH);
        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
        FXGL.getGameScene().getViewport().setLazy(true);

        // Tạo biên bản đồ (Wall) bao quanh map để chặn người chơi đi ra ngoài
        spawnBoundaries((int)mapW, (int)mapH);

        // 5. TẢI TRẠNG THÁI BẢN ĐỒ MỚI (nếu có)
        if (mapStates.containsKey(newMapName)) {
            SaveData newMapState = mapStates.get(newMapName);
            saveLoadSystem.load(newMapState, true); // Tải trạng thái đã lưu cho bản đồ mới (truyền true cho chuyển cảnh)
            System.out.println("Đã tải trạng thái bản đồ: " + newMapName);
        } else {
            // Nếu chưa có trạng thái lưu, đây là lần đầu tiên tải bản đồ này
            // Đảm bảo các thực thể SOIL được cập nhật texture
            FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL)
                    .forEach(s -> s.getComponent(SoilComponent.class).updateTexture());
            System.out.println("Tải bản đồ mới lần đầu: " + newMapName);
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
        if (timeSystem != null) timeSystem.onUpdate(tpf);
        WeatherSystem.getInstance().onUpdate(tpf);
        if (minimap != null) minimap.update();

        // AI: Đi vào nhà lúc 8:00 PM và xuất hiện lại lúc 6:00 AM
        if (currentMap.equals("Main_level.tmx") && timeSystem != null) {
            // 8:00 PM: đi vào nhà
            if (timeSystem.getHour() == 20 && timeSystem.getMinute() == 0) {
                FXGL.getGameWorld().getEntitiesByType(EntityType.GUIDER).forEach(npc -> {
                    NPCBehaviorComponent ai = npc.getComponent(NPCBehaviorComponent.class);
                    if (!ai.isGoingHome() && !ai.isHidden()) {
                        FXGL.getGameWorld().getEntitiesByType(EntityType.GUIDER_IN).stream().findFirst().ifPresent(target -> {
                            ai.goHome(target.getPosition());
                        });
                    }
                });
                
                FXGL.getGameWorld().getEntitiesByType(EntityType.TRADER).forEach(npc -> {
                    NPCBehaviorComponent ai = npc.getComponent(NPCBehaviorComponent.class);
                    if (!ai.isGoingHome() && !ai.isHidden()) {
                        FXGL.getGameWorld().getEntitiesByType(EntityType.TRADER_IN).stream().findFirst().ifPresent(target -> {
                            ai.goHome(target.getPosition());
                        });
                    }
                });
            }
            // 6:00 AM: xuất hiện trở lại
            if (timeSystem.getHour() == 6 && timeSystem.getMinute() == 0) {
                FXGL.getGameWorld().getEntitiesByType(EntityType.GUIDER, EntityType.TRADER).forEach(npc -> {
                    NPCBehaviorComponent ai = npc.getComponent(NPCBehaviorComponent.class);
                    if (ai.isHidden()) {
                        ai.reappear();
                    }
                });
            }
        }

        // --- ĐOẠN SỬA MỚI: CHỐNG TRÔI BẰNG CÁCH KHỬ NHIỄU VẬT LÝ ---
        if (player != null) {
            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            if (physics != null) {
                // Nếu vận tốc cực nhỏ (dưới 15) -> Đây chắc chắn là nhiễu trôi vật lý, ép thẳng về 0
                if (Math.abs(physics.getVelocityX()) > 0 && Math.abs(physics.getVelocityX()) < 15) {
                    physics.setVelocityX(0);
                }
                if (Math.abs(physics.getVelocityY()) > 0 && Math.abs(physics.getVelocityY()) < 15) {
                    physics.setVelocityY(0);
                }
            }
        }

        // 2. Cập nhật bộ chọn ô đất và đồng bộ chuyển động nhân vật
        if (farmingSystem != null) farmingSystem.updateSelector(selector, player);
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        // 1. DI CHUYỂN
        input.addAction(new UserAction("Move Right") {
            @Override protected void onAction() { player.getComponent(PhysicsComponent.class).setVelocityX(200); }
            @Override protected void onActionEnd() { player.getComponent(PhysicsComponent.class).setVelocityX(0); }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override protected void onAction() { player.getComponent(PhysicsComponent.class).setVelocityX(-200); }
            @Override protected void onActionEnd() { player.getComponent(PhysicsComponent.class).setVelocityX(0); }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Up") {
            @Override protected void onAction() { player.getComponent(PhysicsComponent.class).setVelocityY(-200); }
            @Override protected void onActionEnd() { player.getComponent(PhysicsComponent.class).setVelocityY(0); }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override protected void onAction() { player.getComponent(PhysicsComponent.class).setVelocityY(200); }
            @Override protected void onActionEnd() { player.getComponent(PhysicsComponent.class).setVelocityY(0); }
        }, KeyCode.S);

        // 2. TƯƠNG TÁC TỔNG HỢP (NPC / THU HOẠCH / CỬA)
        input.addAction(new UserAction("Interact Action Key") {
            @Override
            protected void onActionBegin() {
                // Sử dụng khoảng cách làm fallback nếu cảm biến vật lý chưa kích hoạt (do va chạm vật lý đặc)
                Entity targetNPC = nearbyNPC;
                if (targetNPC != null && targetNPC.getComponent(NPCBehaviorComponent.class).isHidden()) {
                    targetNPC = null;
                }
                if (targetNPC == null && player != null) {
                    targetNPC = FXGL.getGameWorld().getEntitiesByType(EntityType.GUIDER, EntityType.TRADER).stream()
                            .filter(e -> e.distance(player) < 70 && !e.getComponent(NPCBehaviorComponent.class).isHidden())
                            .findFirst()
                            .orElse(null);
                }

                Entity targetDoor = nearbyDoor;
                if (targetDoor == null && player != null) {
                    targetDoor = FXGL.getGameWorld().getEntitiesByType(EntityType.DOOR).stream()
                            .filter(e -> e.distance(player) < 70)
                            .findFirst()
                            .orElse(null);
                }

                Entity targetSleep = nearbySleep;
                if (targetSleep == null && player != null) {
                    targetSleep = FXGL.getGameWorld().getEntitiesByType(EntityType.SLEEP).stream()
                            .filter(e -> e.distance(player) < 70)
                            .findFirst()
                            .orElse(null);
                }

                if (targetNPC != null) { // Kiểm tra nếu có NPC gần đó
                    if (targetNPC.getType() == EntityType.TRADER) { // Kiểm tra nếu là Trader
                        tradingView.open(targetNPC.getComponent(TraderComponent.class));
                    } else {
                        // Logic tương tác NPC thông thường (Guider, v.v.)
                        String npcName = targetNPC.getProperties().keys().contains("name") ? targetNPC.getString("name") : "";
                        NPC npc = QuestManager.getInstance().getNPC(npcName);
                        if (npc != null) {
                            System.out.println("Tương tác với NPC: " + npcName);
                            String text = npc.interact();
                            
                            // Chỉ nhận quest mới nếu không có quest nào đang thực hiện hoặc chờ nhận thưởng
                            boolean hasActiveOrCompleted = npc.getQuests().stream()
                                    .anyMatch(q -> q.getStatus() == QuestStatus.IN_PROGRESS || q.getStatus() == QuestStatus.COMPLETED);
                            
                            if (!hasActiveOrCompleted) {
                                npc.acceptNextAvailableQuest();
                            }
                            
                            npc.claimFirstCompleted(inventory);
                            
                            dialogView.setDialog(targetNPC.getString("name"), text.split("\n"));
                            dialogView.show();
                            toolbarView.updateSelection();
                        }
                    }
                } else if (targetDoor != null) { // Xử lý tương tác với cửa
                    // 1. Lưu dữ liệu từ cửa cũ trước khi chuyển map (vì map cũ sẽ bị xóa)
                    String mapFile = targetDoor.getString("targetMap");

                    // Đọc từ teleportX/teleportY thay vì targetX/targetY cũ
                    double tx = targetDoor.getDouble("teleportX");
                    double ty = targetDoor.getDouble("teleportY");

                    // 2. Ẩn dialog nếu đang mở để tránh UI bị lỗi khi đổi map
                    if (dialogView.isOpen()) dialogView.hide();

                    nearbyDoor = null;
                    updateLevel(mapFile, tx, ty);

                    System.out.println("Dịch chuyển đến: " + mapFile + " tại " + tx + ", " + ty);
                } else if (targetSleep != null) {
                    System.out.println("--- Main: Bắt đầu đi ngủ ---");

                    // Debug: In trạng thái kho đồ trước khi ngủ
                    System.out.println("Kho đồ trước khi ngủ:");
                    for (InventorySlot slot : inventory.getSlots()) {
                        if (!slot.isEmpty()) {
                            System.out.println("  - " + slot.getItemType().getDisplayName() + ": " + slot.getCount());
                        }
                    }
                    // Debug: In trạng thái một số cây trồng (ví dụ: lúa mì)
                    FXGL.getGameWorld().getEntitiesByType(EntityType.WHEAT).forEach(wheat -> {
                        CropComponent cc = wheat.getComponent(CropComponent.class);
                        System.out.println("  - Lúa mì tại (" + wheat.getX() + ", " + wheat.getY() + ") - Stage: " + cc.getStage());
                    });


                    // Logic đi ngủ
                    if (timeSystem != null) {
                        timeSystem.advanceToNextDay(); // Bỏ nhận xét dòng này
                        statusBarsView.setHealth(100); // Reset máu
                        statusBarsView.setHunger(100); // Reset đói
                        dialogView.setDialog("Thông báo", "Bạn đã ngủ một giấc thật ngon.", "Sức khỏe đã được hồi phục!");
                        dialogView.show();
                        System.out.println("Nhân vật đã đi ngủ.");
                    }

                    // Debug: In trạng thái kho đồ sau khi ngủ
                    System.out.println("Kho đồ sau khi ngủ:");
                    for (InventorySlot slot : inventory.getSlots()) {
                        if (!slot.isEmpty()) {
                            System.out.println("  - " + slot.getItemType().getDisplayName() + ": " + slot.getCount());
                        }
                    }
                    // Debug: In trạng thái một số cây trồng sau khi ngủ
                    FXGL.getGameWorld().getEntitiesByType(EntityType.WHEAT).forEach(wheat -> {
                        CropComponent cc = wheat.getComponent(CropComponent.class);
                        System.out.println("  - Lúa mì tại (" + wheat.getX() + ", " + wheat.getY() + ") - Stage: " + cc.getStage());
                    });
                    System.out.println("--- Main: Kết thúc đi ngủ ---");
                }
                else {
                    farmingSystem.handleHarvest(selector);
                }
            }
        }, KeyCode.E);

        // 3. SỬ DỤNG CÔNG CỤ (F & CLICK CHUỘT)
        input.addAction(new UserAction("Use Tool Keyboard") {
            @Override protected void onActionBegin() { handleUseItem(); }
        }, KeyCode.F);

        input.addAction(new UserAction("Use Tool Mouse") {
            @Override protected void onActionBegin() { handleUseItem(); }
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
            @Override protected void onActionBegin() {
                if (dialogView.isOpen()) dialogView.hide();
                if (tradingView.isOpen()) tradingView.toggle(); // Đóng TradingView khi nhấn R
                if (inventoryView.isOpen()) inventoryView.toggle(); // Đóng InventoryView khi nhấn R
            }
        }, KeyCode.R); // Sử dụng R để đóng UI

        input.addAction(new UserAction("Toggle Inventory Window") {
            @Override protected void onActionBegin() { inventoryView.toggle(); }
        }, KeyCode.TAB);

        // 5. LƯU & TẢI (F5 / F9)
        input.addAction(new UserAction("Quick Save") {
            @Override protected void onActionBegin() { saveLoadSystem.saveGameToFile(); } // Gọi phương thức lưu vào file
        }, KeyCode.F5);

        input.addAction(new UserAction("Quick Load") {
            @Override protected void onActionBegin() { saveLoadSystem.loadGameFromFile(); } // Gọi phương thức tải từ file
        }, KeyCode.F9);

        // 6. CHỌN SLOT 1-9
        KeyCode[] digitCodes = {KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4,
                KeyCode.DIGIT5, KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9};
        for (int i = 0; i < 9; i++) {
            final int slot = i;
            input.addAction(new UserAction("Select Inventory Slot " + (i + 1)) {
                @Override protected void onActionBegin() {
                    inventory.setSelectedSlot(slot);
                    toolbarView.updateSelection();
                }
            }, digitCodes[i]);
        }

        // 7. CUỘN CHUỘT ĐỔI VẬT PHẨM
        input.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() < 0) inventory.selectNext();
            else inventory.selectPrevious();
            toolbarView.updateSelection();
        });
    }

    /** Điều phối sử dụng vật phẩm dựa trên loại đang chọn */
    private void handleUseItem() {
        ItemType selected = inventory.getSelectedItem();
        if (selected == null) return;

        switch (selected) {
            case HOE: farmingSystem.useHoe(selector); break;
            case WATERING_CAN: System.out.println("Đang sử dụng bình tưới...");
                farmingSystem.useWateringCan(selector);
                break;
            default: if (selected.isSeed()) farmingSystem.plantCrop(selector, selected); break;
        }
    }

    /** Kiểm tra trạng thái ban ngày (Bridge method) */
    public String getCurrentMap() {
        return currentMap;
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
        if (player == null || moneyText == null) return;
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        playerComponent.moneyProperty().addListener((obs, old, newV) -> {
            moneyText.setText("Tiền: " + newV + " G");
        });
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
        bindPlayerUI();
        if (selector == null) selector = FXGL.spawn("Selector");

        double mapW = 3520;
        double mapH = 2048;
        if (newMapName.equals("Main_house.tmx")) {
            mapW = 1024;
            mapH = 1024;
        } else {
            double maxW = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION).stream()
                    .mapToDouble(e -> e.getRightX()).max().orElse(3520);
            double maxH = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION).stream()
                    .mapToDouble(e -> e.getBottomY()).max().orElse(2048);
            mapW = Math.max(mapW, maxW);
            mapH = Math.max(mapH, maxH);
        }

        FXGL.getGameScene().getViewport().setBounds(0, 0, (int)mapW, (int)mapH);
        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
        FXGL.getGameScene().getViewport().setLazy(true);

        spawnBoundaries((int)mapW, (int)mapH);
    }

    public static boolean isDayTime() {
        Main app = (Main) FXGL.getApp();
        return app.timeSystem != null && app.timeSystem.isDayTime();
    }

    public static void main(String[] args) { launch(args); }
}
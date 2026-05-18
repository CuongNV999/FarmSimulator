package Project1Game;

// --- IMPORT CÁC THƯ VIỆN CỐT LÕI CỦA FXGL ENGINE ---
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

// --- IMPORT THÀNH PHẦN THỰC THỂ VÀ HỆ THỐNG VẬT LÝ ---
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;

// --- IMPORT HỆ THỐNG GIAO DIỆN NHIỆM VỤ (QUEST SYSTEM) ---
import Project1Game.quest.*;

// --- IMPORT CÁC THƯ VIỆN ĐỒ HỌA JAVA FX ---
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Lớp điều khiển trung tâm của trò chơi (Game Loop, Input, UI, Physics).
 * Kế thừa GameApplication của FXGL Engine.
 */
public class Main extends GameApplication {
    // --- Thực thể quản lý trò chơi ---
    private Entity player;               // Thực thể người chơi chính
    private Entity selector;             // Ô vuông con trỏ chuột bám lưới hệ thống ô đất

    // --- Quản lý dữ liệu và các lớp hiển thị giao diện UI ---
    private Inventory inventory;         // Đối tượng quản lý dữ liệu kho lưu trữ túi đồ
    private ToolbarView toolbarView;     // Thanh công cụ nhanh hiển thị dưới đáy màn hình
    private InventoryView inventoryView; // Bảng chi tiết toàn bộ túi đồ của nhân vật
    private StatusBarsView statusBarsView; // Thanh hiển thị trạng thái sinh tồn (Máu, Thức ăn)
    private DialogView dialogView;       // Khung hộp thoại hiển thị nội dung đàm thoại NPC
    private MinimapView minimap;         // Bản đồ thu nhỏ định vị không gian

    // --- Cấu trúc dữ liệu và logic hệ thống Thời gian (Ngày/Đêm) ---
    private double gameTime = 360;       // Bộ đếm thời gian quy đổi ra phút (360 phút = 6h sáng)
    private static int hour = 6;         // Biến lưu Giờ hiện tại trong thế giới game
    private int minute = 0;              // Biến lưu Phút hiện tại trong thế giới game
    private Rectangle nightOverlay;      // Tấm kính đen bao phủ màn hình phẳng tạo hiệu ứng trời tối
    private Text clockText;              // Văn bản hiển thị chuỗi giờ giấc lên màn hình UI

    // --- Trạng thái tương tác thực thể và NPC ---
    private Entity nearbyInteraction = null; // Lưu trữ vùng trigger kích hoạt gần nhân vật nhất
    private String nearbyNPCName = null;     // Lưu tên định danh của NPC mà người chơi đang đứng gần

    /**
     * Khởi tạo và thiết lập các thông số phần cứng cơ bản của cửa sổ trò chơi.
     */
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1280);                    // Chiều ngang độ phân giải màn hình
        gameSettings.setHeight(720);                    // Chiều dọc độ phân giải màn hình
        gameSettings.setTitle("Java Farming Professional"); // Tiêu đề hiển thị thanh Windows
        gameSettings.setVersion("2.0");                 // Phiên bản cập nhật tích hợp Quest
        gameSettings.setDeveloperMenuEnabled(true);    // Vô hiệu hóa Dev Menu tránh chiếm dụng phím 1-9
    }

    /**
     * Thiết lập hệ thống vật lý và các bộ lắng nghe xử lý va chạm không gian (Collision Handlers).
     */
    @Override
    protected void initPhysics() {
        // Đặt trọng lực bằng 0 vì game thiết kế theo góc nhìn từ trên xuống (Top-Down 2D)
        FXGL.getPhysicsWorld().setGravity(0, 0);

        // Đăng ký va chạm: Người chơi chạm vào thực thể cản địa hình (COLLISION)
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.COLLISION) {
            @Override
            protected void onCollisionBegin(Entity p, Entity c) {
                System.out.println("Vướng vật cản cứng trên bản đồ!");
            }
        });

        // Đăng ký va chạm: Người chơi bước vào/thoát ra khỏi vùng kích hoạt đa năng (INTERACTION)
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.INTERACTION) {
            @Override protected void onCollisionBegin(Entity p, Entity i) { nearbyInteraction = i; }
            @Override protected void onCollisionEnd(Entity p, Entity i) { nearbyInteraction = null; }
        });

        // Đăng ký va chạm: Người chơi tiếp cận gần vùng không gian của NPC Guider (Bác Nông Dân)
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.GUIDER) {
            @Override
            protected void onCollisionBegin(Entity p, Entity g) {
                nearbyNPCName = "Bác Nông Dân"; // Ghi nhận tên NPC gần nhất để phục vụ hội thoại Quest
            }
            @Override
            protected void onCollisionEnd(Entity p, Entity g) {
                nearbyNPCName = null;
                if (dialogView != null) dialogView.hide(); // Người chơi bỏ đi xa tự động ẩn khung thoại
            }
        });
    }

    /**
     * Khởi tạo trạng thái ban đầu của màn chơi, nạp dữ liệu bản đồ và khởi động hệ thống con.
     */
    @Override
    protected void initGame() {
        inventory = new Inventory(); // Tạo mới thực thể quản lý giỏ đồ cá nhân

        // KHỞI TẠO HỆ THỐNG QUEST: Triển khai theo mô hình Singleton để quản lý tập trung nhiệm vụ
        QuestManager.getInstance().init();

        FXGL.getGameWorld().addEntityFactory(new Factory()); // Khai báo nhà máy chế tạo thực thể đồ họa
        FXGL.setLevelFromMap("Main_level.tmx");             // Khởi dựng bản đồ từ tệp tin cấu trúc Tiled TMX

        player = getGameWorld().getSingleton(EntityType.PLAYER); // Liên kết thực thể người chơi duy nhất
        selector = FXGL.spawn("Selector");                      // Khởi tạo con trỏ ô vuông hỗ trợ nhắm mục tiêu

        // Thiết lập Camera di chuyển trễ mượt mà (Lazy) bám đuổi theo tâm vị trí nhân vật chính
        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
        FXGL.getGameScene().getViewport().setBounds(0, 0, 3840, 2176); // Giới hạn biên an toàn của camera
        FXGL.getGameScene().getViewport().setLazy(true);

        // Duyệt nạp và làm mới đồ họa bề mặt kết cấu hình ảnh cho toàn bộ ô đất ruộng (SOIL) trên map
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(s -> s.getComponent(SoilComponent.class).updateTexture());
        // Dựng 4 bức tường tàng hình bao quanh mép bản đồ để chặn người chơi
        int mapWidth = 3840;  // Chiều rộng thực của map
        int mapHeight = 2176; // Chiều cao thực của map
        int thickness = 64;   // Độ dày của bức tường tàng hình (px)

        // Tường cản phía Trên (Top)
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(0, -thickness)
                .put("width", mapWidth).put("height", thickness));

        // Tường cản phía Dưới (Bottom)
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(0, mapHeight)
                .put("width", mapWidth).put("height", thickness));

        // Tường cản bên Trái (Left)
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(-thickness, 0)
                .put("width", thickness).put("height", mapHeight));

        // Tường cản bên Phải (Right)
        FXGL.spawn("Wall", new com.almasb.fxgl.entity.SpawnData(mapWidth, 0)
                .put("width", thickness).put("height", mapHeight));
    }

    /**
     * Vòng lặp cập nhật dữ liệu logic trò chơi liên tục theo thời gian thực (Game Loop Tick).
     * @param tpf Khoảng thời gian sai lệch giữa hai khung hình kế tiếp (Time Per Frame).
     */
    @Override
    protected void onUpdate(double tpf) {
        updateTime(tpf); // Luôn ưu tiên cập nhật bộ đếm thời gian môi trường thế giới
        if (minimap != null) minimap.update(); // Làm mới vị trí định vị con trỏ trên radar Minimap

        // Xử lý tính toán lưới tọa độ và giới hạn bán kính thao tác của chuột nhắm đất Selector
        if (selector != null && player != null) {
            double mouseX = FXGL.getInput().getMouseXWorld(); // Tọa độ X thực tế của chuột trong thế giới game
            double mouseY = FXGL.getInput().getMouseYWorld(); // Tọa độ Y thực tế của chuột trong thế giới game

            // Thuật toán ép tọa độ tự do về dạng lưới ô vuông kích thước tiêu chuẩn 32x32 pixel (Grid Snapping)
            double x = Math.floor(mouseX / 32) * 32;
            double y = Math.floor(mouseY / 32) * 32;
            selector.setPosition(x, y);

            // Cập nhật và đồng bộ hoạt ảnh (Animation) chuyển động nhân vật thông qua vận tốc hộp vật lý
            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            player.getComponent(PlayerComponent.class).move(new Point2D(physics.getVelocityX(), physics.getVelocityY()));

            // Tính khoảng cách hình học thực tế giữa tâm nhân vật và tâm của ô lưới đang được trỏ chuột tới
            double distance = player.getCenter().distance(x + 16, y + 16);

            // Thiết lập độ mờ: Đứng gần dưới 96px (3 ô lưới) hiện rõ (1.0), đứng xa hiện mờ cảnh báo (0.3)
            selector.getViewComponent().setOpacity(distance <= 96 ? 1.0 : 0.3);
        }
    }

    /**
     * Hệ thống con xử lý tính toán dòng chảy thời gian và độ mờ hiệu ứng chuyển giao Ngày/Đêm.
     */
    private void updateTime(double tpf) {
        gameTime += tpf * 10; // Cấu hình tốc độ: 1 giây ngoài đời thực tương đương 10 phút trôi qua trong game
        if (gameTime >= 1440) gameTime = 0; // Vượt quá 24h (1440 phút) tự động reset về 0 để chuyển sang ngày mới

        hour = (int) (gameTime / 60);   // Trích xuất số Giờ
        minute = (int) (gameTime % 60); // Trích xuất số Phút dư

        // Định dạng chuỗi văn bản hiển thị đồng hồ chuẩn hóa 12 giờ đi kèm ký tự AM/PM
        if (clockText != null) {
            clockText.setText(String.format("%02d:%02d %s", (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)), minute, (hour >= 12 ? "PM" : "AM")));
            // Đổi tông màu chữ đồng hồ: Ban ngày hiển thị chữ màu Gold nắng, ban đêm hiển thị chữ màu Cyan Neon sáng rõ
            clockText.setFill(hour >= 18 || hour < 6 ? Color.CYAN : Color.GOLD);
        }

        // Thuật toán nội suy tuyến tính tính toán sắc độ bóng tối (Opacity) cho lớp phủ che màn hình
        double opacity = 0;
        if (hour >= 18 && hour < 22) {
            // Hoàng hôn (18h - 22h): Trời tối dần đều theo thời gian, opacity tăng tịnh tiến từ 0.0 lên mức tối đa 0.7
            opacity = ((gameTime - 18 * 60) / (4 * 60)) * 0.7;
        } else if (hour >= 22 || hour < 5) {
            // Đêm khuya (22h đêm - 5h sáng): Giữ nguyên độ tối ở ngưỡng an toàn 0.7 để bảo toàn tầm nhìn chơi game
            opacity = 0.7;
        } else if (hour >= 5 && hour < 6) {
            // Bình minh (5h sáng - 6h sáng): Mặt trời lên, opacity giảm cực nhanh về trạng thái trong suốt hoàn toàn 0.0
            opacity = 0.7 * (1 - (gameTime - 5 * 60) / 60);
        } // Ban ngày (6h - 18h) mặc định giữ nguyên opacity = 0.0 (Sáng sủa hoàn toàn)

        if (nightOverlay != null) nightOverlay.setOpacity(opacity);
    }

    /**
     * Khởi tạo, cấu hình tạo lập vị trí tọa độ cấu trúc giao diện đồ họa UI đè lên khung màn hình game.
     */
    @Override
    protected void initUI() {
        // 1. Tạo lập thanh Toolbar công cụ nhanh, đặt vị trí chính giữa sát đáy màn hình phẳng
        toolbarView = new ToolbarView(inventory);
        toolbarView.setLayoutX((FXGL.getAppWidth() - 9 * 86) / 2.0);
        toolbarView.setLayoutY(FXGL.getAppHeight() - 100);
        FXGL.getGameScene().addUINode(toolbarView);

        // 2. Khởi tạo Bảng chứa toàn bộ kho đồ chính của người chơi (Mặc định ở trạng thái ẩn)
        inventoryView = new InventoryView(inventory);
        inventoryView.setLayoutX((FXGL.getAppWidth() - 620) / 2.0);
        inventoryView.setLayoutY((FXGL.getAppHeight() - 300) / 2.0);
        FXGL.getGameScene().addUINode(inventoryView);

        // 3. Khởi tạo Khung hội thoại hiển thị chữ đàm thoại hội thoại với các NPC trò chơi
        dialogView = new DialogView(FXGL.getAppWidth(), FXGL.getAppHeight());
        FXGL.getGameScene().addUINode(dialogView);

        // 4. Khởi tạo các thanh chỉ số sinh tồn (Thanh Máu, Thanh Thức ăn) đặt cố định ở góc trên bên trái
        statusBarsView = new StatusBarsView();
        statusBarsView.setLayoutX(20); statusBarsView.setLayoutY(20);
        FXGL.getGameScene().addUINode(statusBarsView);

        // 5. Khởi tạo tấm phủ hiệu ứng lọc ánh sáng bóng tối Ngày và Đêm toàn màn hình
        nightOverlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.BLACK);
        nightOverlay.setMouseTransparent(true); // Thiết lập quan trọng: Cho phép click nhấn chuột xuyên thấu qua tấm màn đen
        nightOverlay.setOpacity(0);
        FXGL.getGameScene().addUINode(nightOverlay);

        // 6. Nhãn chữ hiển thị cấu trúc Đồng hồ thời gian hệ thống
        clockText = new Text();
        clockText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        clockText.setTranslateX(FXGL.getAppWidth() - 150); clockText.setTranslateY(40);
        clockText.setStroke(Color.BLACK);
        clockText.setStrokeWidth(0.5);
        FXGL.getGameScene().addUINode(clockText);

        // 7. Radar Bản đồ thu nhỏ không gian đặt nằm ở tháp góc lề bên phải màn hình UI
        minimap = new MinimapView();
        minimap.setLayoutX(FXGL.getAppWidth() - 170); minimap.setLayoutY(60);
        FXGL.getGameScene().addUINode(minimap);
    }

    /**
     * Đăng ký quản lý hệ thống Input phím bấm hành động bằng API chuẩn Java UserAction.
     * ĐÃ SỬA: Thay thế hoàn toàn các hàm onKeyDown lồng Lambda gây lỗi biên dịch Kotlin hệ thống.
     */
    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        // --- Đăng ký chuỗi hành động di chuyển nhân vật WASD liên kết với vận tốc vật lý ---
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

        // --- Phím R: Gọi đàm thoại hội thoại, kích hoạt tương tác lời thoại tích hợp từ hệ thống Quest ---
        input.addAction(new UserAction("Interact NPC") {
            @Override
            protected void onActionBegin() {
                if (nearbyNPCName != null) {
                    NPC npc = QuestManager.getInstance().getNPC(nearbyNPCName);
                    if (npc != null) {
                        String text = npc.interact(); // Gọi hàm trả về câu thoại động từ logic trạng thái Quest hiện tại
                        dialogView.setDialog(nearbyNPCName, text.split("\n")); // Cắt chuỗi theo dòng đổ vào UI khung thoại
                        dialogView.toggle(); // Bật ẩn/hiện bảng hội thoại
                    }
                }
            }
        }, KeyCode.R);

        // --- Phím E: Nhận/Nộp Quest thông minh hoặc thực hiện lệnh thu hoạch nông sản ---
        input.addAction(new UserAction("Accept Or Claim Quest Or Harvest") {
            @Override
            protected void onActionBegin() {
                if (nearbyNPCName != null) {
                    NPC npc = QuestManager.getInstance().getNPC(nearbyNPCName);
                    if (npc != null) {
                        npc.acceptNextAvailableQuest(); // Thực thi nhận tầng nhiệm vụ kế tiếp nếu đủ điều kiện
                        npc.claimFirstCompleted(inventory); // Thực thi kiểm tra nộp nhiệm vụ hoàn thành nhận quà thưởng
                        toolbarView.updateSelection(); // Cập nhật lại số lượng hiển thị vật phẩm phần thưởng trên UI Toolbar
                    }
                } else {
                    handleHarvest(); // Nếu không đứng gần NPC nào, phím E mặc định hiểu là phím tắt lệnh Thu hoạch cây chín
                }
            }
        }, KeyCode.E);

        // --- Phím F & Click chuột trái: Sử dụng vật phẩm/Công cụ đang chọn cầm trên tay ---
        input.addAction(new UserAction("Use Tool F") { @Override protected void onActionBegin() { handleUseItem(); } }, KeyCode.F);
        input.addAction(new UserAction("ClickTool Mouse") { @Override protected void onActionBegin() { handleUseItem(); } }, MouseButton.PRIMARY);

        // --- Phím G: Phím tắt kích hoạt lệnh sử dụng nhanh bình tưới nước ruộng ---
        input.addAction(new UserAction("Quick Water Can") {
            @Override
            protected void onActionBegin() {
                if (inventory.getCount(ItemType.WATERING_CAN) > 0) useWateringCan();
            }
        }, KeyCode.G);

        // --- Hệ thống phím tắt chức năng hệ thống và giao diện UI nền tảng ---
        input.addAction(new UserAction("Toggle Inventory") { @Override protected void onActionBegin() { inventoryView.toggle(); } }, KeyCode.I);
        input.addAction(new UserAction("Save Game Act") { @Override protected void onActionBegin() { saveGame(); } }, KeyCode.O);
        input.addAction(new UserAction("Load Game Act") { @Override protected void onActionBegin() { loadGame(); } }, KeyCode.P);

        // --- Đăng ký phím số 1 đến 9 để đổi nhanh vị trí Slot ô vật phẩm cầm tay trên Toolbar ---
        KeyCode[] codes = {KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5, KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9};
        for (int i = 0; i < 9; i++) {
            final int slot = i;
            input.addAction(new UserAction("Select Slot " + (i + 1)) {
                @Override protected void onActionBegin() { selectSlot(slot); }
            }, codes[i]);
        }

        // --- Lắng nghe sự kiện con lăn chuột (Mouse Scroll) đổi nhanh ô vật phẩm Toolbar ---
        input.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() < 0) inventory.selectNext(); else inventory.selectPrevious();
            toolbarView.updateSelection();
        });
    }

    // --- KHỐI LOGIC THAO TÁC CÔNG CỤ CHI TIẾT (CORE GAMEPLAY) ---

    /**
     * Kiểm tra loại vật phẩm đang chọn trên tay để phân phối đúng hàm xử lý chức năng nông nghiệp.
     */
    private void handleUseItem() {
        ItemType selected = inventory.getSelectedItem();
        if (selected == null) return;
        switch (selected) {
            case HOE: useHoe(); break;
            case WATERING_CAN: useWateringCan(); break;
            default: if (selected.isSeed()) plantCrop(selected); break;
        }
    }

    /**
     * Logic sử dụng Cuốc (HOE): Chuyển kết cấu vùng cỏ hoang dã dã ngoại (FIELD) thành ô đất nông nghiệp trồng trọt (SOIL).
     * Giải pháp: Sử dụng phép kiểm tra tọa độ không gian nguyên thủy thay vì Component để tránh NullPointerException.
     */
    private void useHoe() {
        if (selector.getViewComponent().getOpacity() < 1.0) return; // Chặn đứng thao tác nếu trỏ chuột quá khoảng cách an toàn

        double selX = selector.getX();
        double selY = selector.getY();

        // Thuật toán kiểm tra ranh giới: So sánh tọa độ điểm của Selector với kích thước hình học của vùng FIELD
        boolean inField = getGameWorld().getEntitiesByType(EntityType.FIELD).stream()
                .anyMatch(f -> f.getX() <= selX && selX < f.getX() + f.getWidth()
                        && f.getY() <= selY && selY < f.getY() + f.getHeight());

        // Điều kiện: Phải nằm lọt lòng trong Field và tại điểm tọa độ lưới chỉ định chưa từng có ô đất Soil nào tồn tại từ trước
        if (inField) {
            boolean hasSoil = getGameWorld().getEntitiesAt(new Point2D(selX + 16, selY + 16)).stream()
                    .anyMatch(e -> e.isType(EntityType.SOIL));

            if (!hasSoil) {
                getGameWorld().spawn("Soil", selX, selY); // Kích hoạt sinh thành thực thể đất ruộng mới
                // Duyệt ép làm mới đồ họa nối liền vân kết cấu ảnh bề mặt của toàn bộ mạng lưới ô đất ruộng hiện tại
                getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(s -> s.getComponent(SoilComponent.class).updateTexture());
            }
        }
    }

    /**
     * Logic sử dụng Hạt giống (SEED): Gieo hạt giống cây non xuống nền đất ruộng cày trống và thông báo cho hệ thống Quest.
     */
    private void plantCrop(ItemType seed) {
        if (inventory.getCount(seed) <= 0 || selector.getViewComponent().getOpacity() < 1.0) return;

        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(selector.getPosition()) < 10 && s.getComponent(SoilComponent.class).canPlant())
                .findFirst().ifPresent(soil -> {
                    getGameWorld().spawn(seed.getSpawnName(), soil.getX(), soil.getY()); // Spawn hạt giống tương ứng tên cấu hình gieo
                    soil.getComponent(SoilComponent.class).setHasPlant(true);           // Đánh dấu ô đất đã bị chiếm dụng, cấm gieo đè
                    inventory.removeItem(seed, 1);                                      // Khấu trừ bớt 1 hạt giống trong kho người chơi

                    // BÁO CÁO TIẾN ĐỘ QUEST: Phát tín hiệu sự kiện gieo hạt hạt giống tương ứng ra toàn hệ thống Quest lắng nghe cập nhật mục tiêu
                    QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.PLANT, seed));
                });
    }

    /**
     * Logic sử dụng Bình tưới (WATERING_CAN): Kích hoạt độ ẩm cho ô đất mục tiêu và truyền sự kiện tới bộ đếm Quest.
     */
    private void useWateringCan() {
        if (selector.getViewComponent().getOpacity() < 1.0) return;
        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(selector.getPosition()) < 15)
                .findFirst().ifPresent(soil -> {
                    soil.getComponent(SoilComponent.class).setWet(true); // Kích hoạt cờ trạng thái ô đất bị ướt nước (isWet = true)

                    // BÁO CÁO TIẾN ĐỘ QUEST: Phát tín hiệu phát sóng vừa thực hiện hành động tưới nước ruộng
                    QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.WATER, null));
                });
    }

    /**
     * Logic phím Thu hoạch (HARVEST): Quét tìm cây trồng chín hoàn toàn quanh Selector, xóa thực thể và cộng sản phẩm nông sản bỏ túi kèm báo cáo Quest.
     */
    private void handleHarvest() {
        if (selector.getViewComponent().getOpacity() < 1.0) return;
        EntityType[] types = {EntityType.WHEAT, EntityType.RADISH, EntityType.CABBAGE, EntityType.LETTUCE, EntityType.TOMATO, EntityType.CORN};

        for (EntityType t : types) {
            getGameWorld().getEntitiesByType(t).stream()
                    .filter(c -> c.getPosition().distance(selector.getPosition()) < 10 && c.getComponent(CropComponent.class).isRipe())
                    .findFirst().ifPresent(c -> {
                        // Tìm ô đất nằm bên dưới gốc cây thu hoạch này để hoàn tác giải phóng cờ trạng thái đất trống trống trải
                        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                                .filter(s -> s.getPosition().distance(c.getPosition()) < 5)
                                .findFirst().ifPresent(s -> s.getComponent(SoilComponent.class).setHasPlant(false));

                        ItemType res = ItemType.valueOf(t.name()); // Quy đổi an toàn tên loại cấu trúc thực thể cây chín sang Enum vật phẩm thành phẩm rơi ra
                        c.removeFromWorld(); // Giải phóng xóa bỏ hoàn toàn thực thể cây chín khỏi thế giới game
                        inventory.addItem(res, 1); // Cộng nạp 1 lượng nông sản sạch vào giỏ lưu trữ túi người chơi

                        // BÁO CÁO TIẾN ĐỘ QUEST: Phát tín hiệu sự kiện vừa thu hoạch thành công nông sản mục tiêu phục vụ bộ đếm Quest thu thập
                        QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.HARVEST, res));
                    });
        }
    }

    // --- HỆ THỐNG PHỤ TRỢ (SYSTEM HELPER METHODS) ---

    /**
     * Hàm hỗ trợ kiểm tra trạng thái quang hợp ban ngày ban đêm giúp cây tăng trưởng lớn lên.
     * @return true nếu số giờ hiện tại nằm lọt lòng trong khoảng thời gian ban ngày cho phép lớn (5h sáng đến trước 22h đêm).
     */
    public static boolean isDayTime() { return hour >= 5 && hour < 22; }

    /**
     * Chuyển đổi chỉ mục slot ô công cụ cầm tay trên thanh UI Toolbar dưới đáy màn hình.
     */
    private void selectSlot(int slot) { inventory.setSelectedSlot(slot); toolbarView.updateSelection(); }

    /**
     * Ghi gói toàn bộ thông tin tiến trình thực tại màn chơi biên dịch nén thành tệp nhị phân hệ thống lưu trữ lâu dài.
     */
    private void saveGame() {
        SaveData data = new SaveData();
        data.gameTime = gameTime;
        data.health = statusBarsView.getHealth(); data.hunger = statusBarsView.getHunger();

        // Quét nạp lưu trữ số lượng toàn bộ vật phẩm có trong kho túi đồ cá nhân nhân vật chính
        for (ItemType t : ItemType.values()) if (inventory.getCount(t) > 0) data.inventoryItems.put(t.name(), inventory.getCount(t));

        // Duyệt tuần tự tuần tự đóng gói lưu trữ tọa độ X-Y kèm tình trạng ẩm ướt của toàn bộ thực thể ô đất nông nghiệp
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(s -> {
            SaveData.SoilData sd = new SaveData.SoilData();
            sd.x = s.getX(); sd.y = s.getY(); sd.isWet = s.getComponent(SoilComponent.class).isWet();
            sd.hasPlant = s.getComponent(SoilComponent.class).isHasPlant(); data.soils.add(sd);
        });

        // Thực thi xuất luồng ghi tệp dữ liệu thông qua hệ thống tệp tin lõi của FXGL File System Service
        FXGL.getFileSystemService().writeDataTask(data, "save_game.dat").run();
        System.out.println("Đã đồng bộ sao lưu file save_game.dat thành công!");
    }

    /**
     * Giải mã dữ liệu tệp lưu trữ nhị phân hệ thống, xóa sạch thế giới cũ và tái thiết lập dựng lại nông trại cũ.
     */
    private void loadGame() {
        FXGL.getFileSystemService().<SaveData>readDataTask("save_game.dat").onSuccess(data -> {
            gameTime = data.gameTime;
            statusBarsView.setHealth(data.health); statusBarsView.setHunger(data.hunger);

            // Xóa sạch sẽ các thực thể động cũ để chống hiện tượng lỗi bóng ma đè hình ảnh đồ họa nền
            getGameWorld().getEntitiesByType(EntityType.SOIL, EntityType.WHEAT, EntityType.CORN).forEach(Entity::removeFromWorld);

            // Duyệt nạp tái thiết lập hồi sinh gieo lại cấu trúc ô đất ruộng vườn nông trại
            for (SaveData.SoilData sd : data.soils) {
                Entity s = getGameWorld().spawn("Soil", sd.x, sd.y);
                s.getComponent(SoilComponent.class).setWet(sd.isWet);
                s.getComponent(SoilComponent.class).setHasPlant(sd.hasPlant);
            }
            toolbarView.updateSelection(); // Làm mới hình hiển thị viền sáng trên UI Toolbar dưới đáy màn hình phẳng
            System.out.println("Đã tải khôi phục màn chơi thành công!");
        }).run();
    }

    /**
     * Hàm chính main kích hoạt khởi động chạy cấu trúc đồ họa ứng dụng JavaFX / FXGL Engine.
     */
    public static void main(String[] args) { launch(args); }

    /**
     * Hàm xử lý chuỗi bổ trợ: Viết hoa ký tự đầu tiên văn bản (Vd: "cabbage" -> "Cabbage") giúp spawn đúng định dạng Factory.
     */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
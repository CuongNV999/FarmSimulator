package Project1Game;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import static com.almasb.fxgl.dsl.FXGLForKtKt.*;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Main extends GameApplication {
    private Entity player;
    private Entity selector;
    private Entity nearbyInteraction = null;
    private Inventory inventory;
    private ToolbarView toolbarView;
    private InventoryView inventoryView;
    private StatusBarsView statusBarsView;

    // Logic hệ thống thời gian
    private double gameTime = 360; // Bắt đầu tại 360 phút (tức là 6h sáng)
    private static int hour = 6;
    private int minute = 0;

    // Giao diện Ngày/Đêm và Bản đồ
    private Rectangle nightOverlay; // Lớp phủ bóng tối ban đêm
    private Text clockText;         // Chữ hiển thị đồng hồ
    private MinimapView minimap;    // Bản đồ thu nhỏ

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1280);
        gameSettings.setHeight(720);
        gameSettings.setTitle("Java Farming Simulator");
        gameSettings.setVersion("1.0");
        // Quan trọng: Tắt Developer Menu để phím số 1-9 không bị chiếm dụng
        gameSettings.setDeveloperMenuEnabled(false);
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().setGravity(0, 0);

        // Xử lý va chạm với vật cản
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.COLLISION) {
            @Override
            protected void onCollisionBegin(Entity player, Entity collision) {
                System.out.println("Vướng vật cản!");
            }
        });

        // Xử lý vùng tương tác (Cửa, NPC...)
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.INTERACTION) {
            @Override
            protected void onCollisionBegin(Entity player, Entity interaction) {
                nearbyInteraction = interaction;
            }
            @Override
            protected void onCollisionEnd(Entity player, Entity interaction) {
                nearbyInteraction = null;
            }
        });
    }

    @Override
    protected void initGame() {
        inventory = new Inventory();

        FXGL.getGameWorld().addEntityFactory(new Factory());
        FXGL.setLevelFromMap("Main_level.tmx");

        player = getGameWorld().getSingleton(EntityType.PLAYER);
        selector = FXGL.spawn("Selector");

        // Cấu hình Camera bám theo Player
        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
        FXGL.getGameScene().getViewport().setBounds(0, 0, 3840, 2176); // Kích thước map thực tế
        FXGL.getGameScene().getViewport().setLazy(true);

        // Khởi tạo trạng thái hình ảnh cho tất cả ô đất có sẵn trên bản đồ
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
            soil.getComponent(SoilComponent.class).updateTexture();
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        // 1. Luôn cập nhật thời gian hệ thống đầu tiên, không phụ thuộc vào trạng thái Player
        updateTime(tpf);

        // 2. Cập nhật hệ thống quét vị trí của Minimap độc lập
        if (minimap != null) {
            minimap.update();
        }

        // 3. Logic tương tác di chuyển liên quan trực tiếp đến Player và chuột Selector
        if (selector != null && player != null) {
            // Lấy vị trí chuột trong thế giới game
            double mouseX = FXGL.getInput().getMouseXWorld();
            double mouseY = FXGL.getInput().getMouseYWorld();

            // Làm tròn tọa độ chuột về lưới ô vuông kích thước 32x32
            double x = Math.floor(mouseX / 32) * 32;
            double y = Math.floor(mouseY / 32) * 32;
            selector.setPosition(x, y);

            // Cập nhật hoạt ảnh di chuyển Animation Player thông qua vận tốc vật lý
            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            player.getComponent(PlayerComponent.class).move(new Point2D(physics.getVelocityX(), physics.getVelocityY()));

            // Kiểm tra bán kính khoảng cách tương tác an toàn giữa Player và ô Selector
            double distance = player.getCenter().distance(x + 16, y + 16);
            if (distance <= 96) {
                selector.getViewComponent().setOpacity(1.0);
            } else {
                selector.getViewComponent().setOpacity(0.3); // Hiện mờ để người chơi biết ô chuột đang ở đâu
            }
        }
    }

    private void updateTime(double tpf) {
        // Tính toán chu kỳ thời gian nền tảng
        // Tốc độ: 1 giây đời thực = 100 phút trong game (Đang tăng tốc để test nhanh hệ thống ngày đêm)
        gameTime += tpf * 100;
        if (gameTime >= 1440) gameTime = 0; // Reset bộ đếm phút sau chu kỳ 24h (1440 phút)

        hour = (int) (gameTime / 60);
        minute = (int) (gameTime % 60);

        // ================= XỬ LÝ CHỮ ĐỒNG HỒ GIAO DIỆN =================
        String ampm = (hour >= 12) ? "PM" : "AM";
        int displayHour = (hour > 12) ? hour - 12 : (hour == 0 ? 12 : hour);

        if (clockText != null) {
            clockText.setText(String.format("%02d:%02d %s", displayHour, minute, ampm));

            // Đổi tông màu chữ để hiển thị tốt nhất trên nền game
            if (hour >= 18 || hour < 6) {
                clockText.setFill(Color.CYAN); // Màu xanh lơ Neon sáng rõ vào ban đêm
            } else {
                clockText.setFill(Color.GOLD);  // Màu vàng nắng rực rỡ vào ban ngày
            }
        }

        // ================= TÍNH TOÁN HIỆU ỨNG ÁNH SÁNG (OPACITY) =================
        double opacity = 0.0;

        if (hour >= 18 && hour < 22) {
            // Chiều tà (18h - 22h): Hoàng hôn đổ xuống, tối dần từ 0.0 đến 0.7
            double t = (gameTime - 18 * 60) / (4 * 60);
            opacity = t * 0.7;
        } else if (hour >= 22 || hour < 5) {
            // Đêm khuya (22h - 5h sáng): Đạt mức tối đa để giữ độ nhìn cho người chơi (0.7)
            opacity = 0.7;
        } else if (hour >= 5 && hour < 6) {
            // Bình minh (5h - 6h): Mặt trời lên, sáng dần từ 0.7 trở về 0.0
            double t = (gameTime - 5 * 60) / (1 * 60);
            opacity = 0.7 * (1 - t);
        } else {
            // Ban ngày (6h - 18h): Sáng sủa hoàn toàn
            opacity = 0.0;
        }

        if (nightOverlay != null) {
            nightOverlay.setOpacity(opacity);
        }
    }

    @Override
    protected void initUI() {
        // 1. Khởi tạo Toolbar nằm ở chính giữa phía dưới màn hình
        toolbarView = new ToolbarView(inventory);
        double slotSize = 80, slotGap = 6;
        double toolbarWidth = 9 * (slotSize + slotGap) - slotGap;
        toolbarView.setLayoutX((FXGL.getAppWidth() - toolbarWidth) / 2);
        toolbarView.setLayoutY(FXGL.getAppHeight() - slotSize - 20);
        FXGL.getGameScene().addUINode(toolbarView);

        // 2. Khởi tạo Inventory chính hiển thị ở giữa màn hình (ẩn mặc định)
        inventoryView = new InventoryView(inventory);
        inventoryView.setLayoutX((FXGL.getAppWidth() - Inventory.COLS * (64 + 4) - 20) / 2.0);
        inventoryView.setLayoutY((FXGL.getAppHeight() - Inventory.ROWS * (64 + 4) - 50) / 2.0);
        FXGL.getGameScene().addUINode(inventoryView);

        // 3. Thanh trạng thái Máu và Thức ăn góc trên bên trái
        statusBarsView = new StatusBarsView();
        statusBarsView.setLayoutX(16);
        statusBarsView.setLayoutY(16);
        FXGL.getGameScene().addUINode(statusBarsView);

        // 4. Tạo lớp phủ bóng tối Ngày/Đêm (Đặt trước Đồng hồ và Minimap)
        nightOverlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.BLACK);
        nightOverlay.setMouseTransparent(true); // Để không chặn tương tác nhấn chuột vào thế giới game
        nightOverlay.setOpacity(0.0);
        FXGL.getGameScene().addUINode(nightOverlay);

        // 5. Khởi tạo và thiết kế Đồng hồ chữ nổi (Đặt sau lớp phủ để luôn sáng rõ)
        clockText = new Text();
        clockText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        clockText.setFill(Color.GOLD);
        clockText.setTranslateX(FXGL.getAppWidth() - 160); // Vị trí góc trên bên phải
        clockText.setTranslateY(40);
        clockText.setStroke(Color.BLACK); // Đổ viền đen mỏng bao quanh chữ
        clockText.setStrokeWidth(0.5);
        FXGL.getGameScene().addUINode(clockText);

        // 6. Khởi tạo hệ thống Bản đồ thu nhỏ Minimap nằm trên lớp bóng tối
        minimap = new MinimapView();
        minimap.setLayoutX(FXGL.getAppWidth() - 150 - 20); // Căn lề phải
        minimap.setLayoutY(60); // Đặt phía dưới thanh hiển thị số đồng hồ một chút
        FXGL.getGameScene().addUINode(minimap);
    }

    // Hàm tĩnh bổ trợ giúp kiểm tra trạng thái ngày/đêm từ các class khác (như CropComponent)
    public static boolean isDayTime() {
        // Quy ước ban ngày cây trồng phát triển là từ 5h sáng đến trước 22h đêm
        return hour >= 5 && hour < 22;
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        // Hệ thống phím điều khiển di chuyển nhân vật WASD
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

        // Các phím tương tác chức năng công cụ làm nông
        input.addAction(new UserAction("Use Tool") {
            @Override protected void onActionBegin() { handleUseItem(); }
        }, MouseButton.PRIMARY);

        onKeyDown(KeyCode.F, () -> { handleUseItem(); return null; });
        onKeyDown(KeyCode.E, () -> { handleHarvest(); return null; });
        onKeyDown(KeyCode.G, () -> {
            if (inventory.getCount(ItemType.WATERING_CAN) > 0) {
                useWateringCan();
            } else {
                System.out.println("Bạn chưa có bình tưới trong túi đồ!");
            }
            return null;
        });

        // Đóng/Mở túi đồ dọn kho nhanh bằng phím I hoặc Tab
        onKeyDown(KeyCode.I, () -> { inventoryView.toggle(); return null; });
        onKeyDown(KeyCode.TAB, () -> { inventoryView.toggle(); return null; });

        // ================= ĐỔI PHÍM SHORTCUT SAVE VÀ LOAD TẠI ĐÂY =================
        onKeyDown(KeyCode.O, () -> {
            saveGame();
            return null;
        });

        onKeyDown(KeyCode.P, () -> {
            loadGame();
            return null;
        });
        // =========================================================================

        // Đăng ký chuỗi phím số từ 1 đến 9 để đổi nhanh vật phẩm trên ô Toolbar
        KeyCode[] digitKeys = {
                KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3,
                KeyCode.DIGIT4, KeyCode.DIGIT5, KeyCode.DIGIT6,
                KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9
        };
        for (int i = 0; i < digitKeys.length; i++) {
            final int slot = i;
            onKeyDown(digitKeys[i], () -> {
                selectSlot(slot);
                return null;
            });
        }

        // Tích hợp con lăn cuộn chuột để thay đổi nhanh ô Toolbar
        input.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() < 0) inventory.selectNext();
            else inventory.selectPrevious();
            if (toolbarView != null) toolbarView.updateSelection();
        });
    }

    private void selectSlot(int slot) {
        inventory.setSelectedSlot(slot);
        if (toolbarView != null) toolbarView.updateSelection();
    }

    private void handleUseItem() {
        ItemType selected = inventory.getSelectedItem();
        switch (selected) {
            case HOE: useHoe(); break;
            case WATERING_CAN: useWateringCan(); break;
            default: if (selected.isSeed()) plantCrop(selected); break;
        }
    }

    private void handleHarvest() {
        if (selector.getViewComponent().getOpacity() < 1.0) return;

        EntityType[] cropTypes = {EntityType.WHEAT, EntityType.RADISH, EntityType.CABBAGE,
                EntityType.LETTUCE, EntityType.TOMATO, EntityType.CORN};

        for (EntityType type : cropTypes) {
            getGameWorld().getEntitiesByType(type).stream()
                    .filter(c -> c.getPosition().distance(selector.getPosition()) < 5)
                    .filter(c -> c.getComponent(CropComponent.class).isRipe())
                    .findFirst()
                    .ifPresent(c -> {
                        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                                .filter(s -> s.getPosition().distance(c.getPosition()) < 5)
                                .findFirst().ifPresent(s -> s.getComponent(SoilComponent.class).setHasPlant(false));

                        ItemType harvest = getHarvestItem(type);
                        c.removeFromWorld();
                        if (harvest != null) inventory.addItem(harvest, 1);
                        System.out.println("Thu hoạch thành công loại cây: " + type);
                    });
        }
    }

    private ItemType getHarvestItem(EntityType cropType) {
        try {
            return ItemType.valueOf(cropType.name());
        } catch (Exception e) {
            return null;
        }
    }

    private void useHoe() {
        if (selector.getViewComponent().getOpacity() < 1.0) return;
        double x = selector.getX(), y = selector.getY();

        boolean inField = getGameWorld().getEntitiesByType(EntityType.FIELD).stream()
                .anyMatch(f -> f.getX() <= x && x < f.getX() + f.getWidth()
                        && f.getY() <= y && y < f.getY() + f.getHeight());

        if (inField) {
            boolean hasSoil = getGameWorld().getEntitiesAt(new Point2D(x + 16, y + 16))
                    .stream().anyMatch(e -> e.isType(EntityType.SOIL));
            if (!hasSoil) {
                getGameWorld().spawn("Soil", x, y);
                getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(s ->
                        s.getComponent(SoilComponent.class).updateTexture());
            }
        }
    }

    private void plantCrop(ItemType seed) {
        if (inventory.getCount(seed) <= 0 || selector.getViewComponent().getOpacity() < 1.0) return;

        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(selector.getPosition()) < 5)
                .filter(s -> s.getComponent(SoilComponent.class).canPlant())
                .findFirst()
                .ifPresent(soil -> {
                    getGameWorld().spawn(seed.getSpawnName(), soil.getX(), soil.getY());
                    soil.getComponent(SoilComponent.class).setHasPlant(true);
                    inventory.removeItem(seed, 1);
                });
    }

    private void useWateringCan() {
        if (selector.getViewComponent().getOpacity() < 1.0) return;

        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(selector.getPosition()) < 15)
                .findFirst()
                .ifPresentOrElse(soil -> {
                    soil.getComponent(SoilComponent.class).setWet(true);
                    System.out.println("Đã tưới nước bằng phím tắt G!");
                }, () -> {
                    System.out.println("Hãy trỏ Selector vào ô đất để tưới!");
                });
    }

    private void saveGame() {
        SaveData data = new SaveData();

        data.gameTime = this.gameTime; // LƯU DỮ LIỆU THỜI GIAN
        data.health = statusBarsView.getHealth();
        data.hunger = statusBarsView.getHunger();

        // 1. Lưu kho đồ cá nhân Inventory
        for (ItemType type : ItemType.values()) {
            int count = inventory.getCount(type);
            if (count > 0) {
                data.inventoryItems.put(type.name(), count);
            }
        }

        // 2. Lưu trạng thái tất cả ô đất nông trại Soil
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
            SoilComponent sc = soil.getComponent(SoilComponent.class);
            SaveData.SoilData sData = new SaveData.SoilData();
            sData.x = soil.getX();
            sData.y = soil.getY();
            sData.isWet = sc.isWet();
            sData.hasPlant = sc.isHasPlant();
            data.soils.add(sData);
        });

        // 3. Lưu toàn bộ các cây nông sản đang trồng Crops
        EntityType[] cropTypes = {EntityType.WHEAT, EntityType.RADISH, EntityType.CABBAGE,
                EntityType.LETTUCE, EntityType.TOMATO, EntityType.CORN};
        for (EntityType type : cropTypes) {
            getGameWorld().getEntitiesByType(type).forEach(crop -> {
                CropComponent cc = crop.getComponent(CropComponent.class);
                SaveData.CropDataSave cData = new SaveData.CropDataSave();
                cData.x = crop.getX();
                cData.y = crop.getY();
                cData.type = type.name();
                cData.stage = cc.getStage();
                data.crops.add(cData);
            });
        }

        // Thực thi ghi dữ liệu nén thành file nhị phân qua FXGL Service
        FXGL.getFileSystemService().writeDataTask(data, "save_game.dat").run();
        System.out.println("Đã lưu game tại thời điểm: " + hour + ":" + minute);
    }

    private void loadGame() {
        FXGL.getFileSystemService().<SaveData>readDataTask("save_game.dat")
                .onSuccess(data -> {
                    // 1. Xóa sạch dữ liệu thực thể cũ trên bản đồ để tránh hiện tượng bóng ma đè ảnh
                    getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(Entity::removeFromWorld);
                    getGameWorld().getEntitiesByType(EntityType.WHEAT, EntityType.CORN, EntityType.RADISH,
                                    EntityType.CABBAGE, EntityType.LETTUCE, EntityType.TOMATO)
                            .forEach(Entity::removeFromWorld);

                    this.gameTime = data.gameTime; // KHÔI PHỤC THỜI GIAN TRẬN ĐẤU
                    statusBarsView.setHealth(data.health);
                    statusBarsView.setHunger(data.hunger);
                    toolbarView.updateSelection();

                    // 2. Khôi phục kho đồ cá nhân
                    for (ItemType type : ItemType.values()) {
                        inventory.removeItem(type, inventory.getCount(type));
                        if (data.inventoryItems.containsKey(type.name())) {
                            inventory.addItem(type, data.inventoryItems.get(type.name()));
                        }
                    }

                    // 3. Tái thiết lập cấu trúc ô đất
                    for (SaveData.SoilData sData : data.soils) {
                        Entity soil = getGameWorld().spawn("Soil", sData.x, sData.y);
                        SoilComponent sc = soil.getComponent(SoilComponent.class);
                        sc.setWet(sData.isWet);
                        sc.setHasPlant(sData.hasPlant);
                    }

                    // 4. Đổ lại dữ liệu giai đoạn trưởng thành của nông sản
                    for (SaveData.CropDataSave cData : data.crops) {
                        Entity crop = getGameWorld().spawn(capitalize(cData.type.toLowerCase()), cData.x, cData.y);
                        crop.getComponent(CropComponent.class).setStage(cData.stage);
                    }

                    System.out.println("Đã tải game! Thời gian hiện tại: " + hour + ":" + minute);
                })
                .onFailure(e -> System.out.println("Không tìm thấy dữ liệu file save cũ!"))
                .run();
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
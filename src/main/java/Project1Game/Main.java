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

    // Logic thời gian
    private double gameTime = 360; // Bắt đầu tại 360 phút (tức là 6h sáng)
    private static int hour = 6;
    private int minute = 0;
    private Rectangle nightOverlay; // Lớp phủ bóng tối

    private Text clockText; // Hiển thị giờ

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

        // Cấu hình Camera
        FXGL.getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth() / 2.0, FXGL.getAppHeight() / 2.0);
        FXGL.getGameScene().getViewport().setBounds(0, 0, 3840, 2176); // Kích thước map thực tế
        FXGL.getGameScene().getViewport().setLazy(true);

        // Khởi tạo texture cho tất cả ô đất có sẵn trên bản đồ
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
            soil.getComponent(SoilComponent.class).updateTexture();
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        // 1. Luôn cập nhật thời gian hệ thống đầu tiên, không phụ thuộc vào Player
        updateTime(tpf);

        // 2. Các logic tương tác liên quan đến Player và Selector
        if (selector != null && player != null) {
            // Lấy vị trí chuột trong thế giới game
            double mouseX = FXGL.getInput().getMouseXWorld();
            double mouseY = FXGL.getInput().getMouseYWorld();

            // Làm tròn về lưới 32x32
            double x = Math.floor(mouseX / 32) * 32;
            double y = Math.floor(mouseY / 32) * 32;
            selector.setPosition(x, y);

            // Cập nhật Animation Player
            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            player.getComponent(PlayerComponent.class).move(new Point2D(physics.getVelocityX(), physics.getVelocityY()));

            // Kiểm tra bán kính tương tác (tâm player tới tâm selector)
            double distance = player.getCenter().distance(x + 16, y + 16);
            if (distance <= 96) {
                selector.getViewComponent().setOpacity(1.0);
            } else {
                selector.getViewComponent().setOpacity(0.3);
            }
        }
    }

    private void updateTime(double tpf) {
        // 1. Tính toán thời gian nền tảng
        // 1 giây đời thực = 10 phút game -> tpf * 10
        gameTime += tpf * 100;
        if (gameTime >= 1440) gameTime = 0; // Reset sau 24h (1440 phút)

        hour = (int) (gameTime / 60);
        minute = (int) (gameTime % 60);

        // ================= ĐOẠN CODE CẬP NHẬT ĐỒNG HỒ MỚI THÊM VÀO =================
        // Thêm nhãn AM/PM cho "sang chảnh"
        String ampm = (hour >= 12) ? "PM" : "AM";
        int displayHour = (hour > 12) ? hour - 12 : (hour == 0 ? 12 : hour);

        // Cập nhật text và màu sắc hiển thị lên màn hình
        if (clockText != null) {
            clockText.setText(String.format("%02d:%02d %s", displayHour, minute, ampm));

            // Đã thêm logic đổi màu chữ tại đây
            if (hour >= 18 || hour < 6) {
                clockText.setFill(Color.CYAN); // Màu xanh dịu ban đêm (từ 18h tối đến trước 6h sáng)
            } else {
                clockText.setFill(Color.GOLD);      // Màu vàng nắng ban ngày (từ 6h sáng đến trước 18h tối)
            }
        }
        // ===========================================================================

        // 2. Tính toán độ mờ (Opacity) của nightOverlay (Giữ nguyên logic cũ của bạn)
        double opacity = 0.0;

        if (hour >= 18 && hour < 22) {
            // Chiều tà (18h - 22h): Tối dần từ 0.0 -> 0.7
            double t = (gameTime - 18 * 60) / (4 * 60);
            opacity = t * 0.7;
        } else if (hour >= 22 || hour < 5) {
            // Đêm khuya (22h - 5h sáng): Tối nhất (0.7)
            opacity = 0.7;
        } else if (hour >= 5 && hour < 6) {
            // Bình minh (5h - 6h): Sáng dần từ 0.7 -> 0.0
            double t = (gameTime - 5 * 60) / (1 * 60);
            opacity = 0.7 * (1 - t);
        } else {
            // Ban ngày (6h - 18h): Sáng hoàn toàn
            opacity = 0.0;
        }

        // Đảm bảo nightOverlay đã được khởi tạo trong initUI() trước khi gọi để tránh NullPointerException
        if (nightOverlay != null) {
            nightOverlay.setOpacity(opacity);
        }
    }

    @Override
    protected void initUI() {
        // Toolbar ở dưới cùng
        toolbarView = new ToolbarView(inventory);
        double slotSize = 80, slotGap = 6;
        double toolbarWidth = 9 * (slotSize + slotGap) - slotGap;
        toolbarView.setLayoutX((FXGL.getAppWidth() - toolbarWidth) / 2);
        toolbarView.setLayoutY(FXGL.getAppHeight() - slotSize - 20);
        FXGL.getGameScene().addUINode(toolbarView);

        // Inventory chính (ẩn mặc định)
        inventoryView = new InventoryView(inventory);
        inventoryView.setLayoutX((FXGL.getAppWidth() - Inventory.COLS * (64 + 4) - 20) / 2.0);
        inventoryView.setLayoutY((FXGL.getAppHeight() - Inventory.ROWS * (64 + 4) - 50) / 2.0);
        FXGL.getGameScene().addUINode(inventoryView);

        // Thanh trạng thái
        statusBarsView = new StatusBarsView();
        statusBarsView.setLayoutX(16);
        statusBarsView.setLayoutY(16);
        FXGL.getGameScene().addUINode(statusBarsView);

        // Tạo lớp phủ Ngày/Đêm
        nightOverlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.BLACK);
        nightOverlay.setMouseTransparent(true); // Quan trọng: Để không cản trở click chuột
        nightOverlay.setOpacity(0.0); // Mặc định là sáng (0% đen)

        // Khởi tạo đồng hồ
        clockText = new Text();
        clockText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        clockText.setFill(Color.GOLD);

        // Đặt vị trí góc trên bên phải (cách lề 20px)
        clockText.setTranslateX(FXGL.getAppWidth() - 150);
        clockText.setTranslateY(40);

        // Thêm hiệu ứng bóng đổ cho chữ dễ nhìn hơn
        clockText.setStroke(Color.BLACK);
        clockText.setStrokeWidth(0.5);


        // Thêm vào scene sau các UI khác để phủ toàn bộ màn hình
        FXGL.getGameScene().addUINode(nightOverlay);
        FXGL.getGameScene().addUINode(clockText);
    }

    public static boolean isDayTime() {
        // Trả về true nếu trong khoảng 5h sáng đến 22h tối
        return hour >= 5 && hour < 22;
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        // Di chuyển WASD
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

        // Sử dụng công cụ (Chuột trái hoặc phím F)
        input.addAction(new UserAction("Use Tool") {
            @Override
            protected void onActionBegin() { handleUseItem(); }
        }, MouseButton.PRIMARY);

        onKeyDown(KeyCode.F, () -> { handleUseItem(); return null; });

        // Thu hoạch (Phím E)
        onKeyDown(KeyCode.E, () -> { handleHarvest(); return null; });
        onKeyDown(KeyCode.G, () -> {
            // Kiểm tra xem trong túi đồ có bình tưới không (số lượng > 0)
            if (inventory.getCount(ItemType.WATERING_CAN) > 0) {
                useWateringCan();
            } else {
                System.out.println("Bạn chưa có bình tưới trong túi đồ!");
            }
            return null;
        });
        // Mở kho đồ (Phím I hoặc Tab)
        onKeyDown(KeyCode.I, () -> { inventoryView.toggle(); return null; });
        onKeyDown(KeyCode.TAB, () -> { inventoryView.toggle(); return null; });

        onKeyDown(KeyCode.O, () -> {
            saveGame();
            return null;
        });

        onKeyDown(KeyCode.P, () -> {
            loadGame();
            return null;
        });
        // Đăng ký phím số 1-9 một cách an toàn (tránh NullPointerException)
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

        // Cuộn chuột để chuyển slot nhanh
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
                        // Giải phóng ô đất bên dưới
                        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                                .filter(s -> s.getPosition().distance(c.getPosition()) < 5)
                                .findFirst().ifPresent(s -> s.getComponent(SoilComponent.class).setHasPlant(false));

                        // Thêm vật phẩm vào kho
                        ItemType harvest = getHarvestItem(type);
                        c.removeFromWorld();
                        if (harvest != null) inventory.addItem(harvest, 1);
                        System.out.println("Thu hoạch: " + type);
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

        // Kiểm tra xem có trong vùng Field không
        boolean inField = getGameWorld().getEntitiesByType(EntityType.FIELD).stream()
                .anyMatch(f -> f.getX() <= x && x < f.getX() + f.getWidth()
                        && f.getY() <= y && y < f.getY() + f.getHeight());

        if (inField) {
            // Kiểm tra xem chỗ này có đất chưa
            boolean hasSoil = getGameWorld().getEntitiesAt(new Point2D(x + 16, y + 16))
                    .stream().anyMatch(e -> e.isType(EntityType.SOIL));
            if (!hasSoil) {
                getGameWorld().spawn("Soil", x, y);
                // Cập nhật lại texture để khớp kiểu bàn cờ
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
                .filter(s -> s.getPosition().distance(selector.getPosition()) < 15) // Tăng từ 5 lên 15
                .findFirst()
                .ifPresentOrElse(soil -> {
                    soil.getComponent(SoilComponent.class).setWet(true);
                    System.out.println("Đã tưới nước bằng phím tắt G!");
                }, () -> {
                    System.out.println("Hãy trỏ Selector vào ô đất để tưới!");
                });
    }
    // Trong Main.java

    private void saveGame() {
        SaveData data = new SaveData();

        data.gameTime = this.gameTime; // LƯU THỜI GIAN HIỆN TẠI
        data.health = statusBarsView.getHealth();
        data.hunger = statusBarsView.getHunger();
        // 1. Lưu Inventory
        for (ItemType type : ItemType.values()) {
            int count = inventory.getCount(type);
            if (count > 0) {
                data.inventoryItems.put(type.name(), count);
            }
        }

        // 2. Lưu Soil
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
            SoilComponent sc = soil.getComponent(SoilComponent.class);
            SaveData.SoilData sData = new SaveData.SoilData();
            sData.x = soil.getX();
            sData.y = soil.getY();
            sData.isWet = sc.isWet();
            sData.hasPlant = sc.isHasPlant();
            data.soils.add(sData);
        });

        // 3. Lưu Crops
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

        // Ghi file bằng FXGL Service
        FXGL.getFileSystemService().writeDataTask(data, "save_game.dat").run();
        System.out.println("Đã lưu game tại thời điểm: " + hour + ":" + minute);
    }

    private void loadGame() {
        FXGL.getFileSystemService().<SaveData>readDataTask("save_game.dat")
                .onSuccess(data -> {
                    // 1. Xóa toàn bộ Soil và Crop cũ trên Map (để không bị chồng đè)
                    getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(Entity::removeFromWorld);
                    getGameWorld().getEntitiesByType(EntityType.WHEAT, EntityType.CORN, EntityType.RADISH,
                                    EntityType.CABBAGE, EntityType.LETTUCE, EntityType.TOMATO)
                            .forEach(Entity::removeFromWorld);

                    this.gameTime = data.gameTime; // TẢI LẠI THỜI GIAN
                    // Cập nhật lại thanh máu và thức ăn lên UI
                    statusBarsView.setHealth(data.health);
                    statusBarsView.setHunger(data.hunger);
                    toolbarView.updateSelection();

                    // 2. Khôi phục Inventory
                    for (ItemType type : ItemType.values()) {
                        // Reset về 0 trước
                        inventory.removeItem(type, inventory.getCount(type));
                        // Cộng lại số lượng từ file save
                        if (data.inventoryItems.containsKey(type.name())) {
                            inventory.addItem(type, data.inventoryItems.get(type.name()));
                        }
                    }

                    // 3. Khôi phục Soil
                    for (SaveData.SoilData sData : data.soils) {
                        Entity soil = getGameWorld().spawn("Soil", sData.x, sData.y);
                        SoilComponent sc = soil.getComponent(SoilComponent.class);
                        sc.setWet(sData.isWet);
                        sc.setHasPlant(sData.hasPlant);
                    }

                    // 4. Khôi phục Crops
                    for (SaveData.CropDataSave cData : data.crops) {
                        // Spawn cây dựa trên tên loại cây (cData.type)
                        Entity crop = getGameWorld().spawn(capitalize(cData.type.toLowerCase()), cData.x, cData.y);
                        crop.getComponent(CropComponent.class).setStage(cData.stage);
                    }

                    System.out.println("Đã tải game! Thời gian hiện tại: " + hour + ":" + minute);
                })
                .onFailure(e -> System.out.println("Không tìm thấy file save!"))
                .run();
    }

    // Hàm hỗ trợ viết hoa chữ cái đầu (WHEAT -> Wheat)
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
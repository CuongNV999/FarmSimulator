package Project1Game;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGLForKtKt.onKeyDown;

public class Main extends GameApplication {
    private Entity player;
    private Entity selector;
    private Inventory inventory;
    private ToolbarView toolbarView;
    private StatusBarsView statusBarsView;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1920);
        gameSettings.setHeight(1080);
        gameSettings.setDeveloperMenuEnabled(false);
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setFullScreenFromStart(false);
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().setGravity(0, 0);
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.WALL) {
            @Override
            protected void onCollisionBegin(Entity Player, Entity Wall) {
                System.out.println("On Collision wall");
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.WATER) {
            @Override
            protected void onCollisionBegin(Entity Player, Entity WATER) {
                System.out.println("On Collision water");
            }
        });
    }

    @Override
    protected void initGame() {
        inventory = new Inventory();

        FXGL.getGameWorld().addEntityFactory(new Factory());
        FXGL.spawn("Background", new SpawnData(0, 0).put("width", 1920).put("height", 1080));
        FXGL.setLevelFromMap("level2.tmx");

        player = getGameWorld().getSingleton(EntityType.PLAYER);
        selector = FXGL.spawn("Selector");

        // Đảm bảo tất cả các thực thể SOIL hiện có được cập nhật texture
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
            soil.getComponent(SoilComponent.class).updateTexture();
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        if (selector != null && player != null) {
            double mouseX = FXGL.getInput().getMouseXWorld();
            double mouseY = FXGL.getInput().getMouseYWorld();

            // Làm tròn vị trí chuột về lưới 32x32
            double x = Math.floor(mouseX / 32) * 32;
            double y = Math.floor(mouseY / 32) * 32;

            selector.setPosition(x, y);

            // Kiểm tra bán kính tương tác là 2 ô (64 pixels tính từ tâm)
            // Lấy tâm của player và tâm của selector
            double playerCenterX = player.getCenter().getX();
            double playerCenterY = player.getCenter().getY();
            double selectorCenterX = x + 16;
            double selectorCenterY = y + 16;

            double distance = Math.sqrt(Math.pow(playerCenterX - selectorCenterX, 2) + Math.pow(playerCenterY - selectorCenterY, 2));

            // Bán kính tương tác là 2 ô (32*2 = 64 pixels tính từ tâm của ô Player).
            // Nếu Player ở giữa 1 ô (16,16), và tâm ô thứ 2 là (80,16), khoảng cách là 64.
            // Để cho phép chọn chéo, ta có thể dùng 96 làm ngưỡng an toàn (cho phép 2 ô ngang/dọc và chéo).
            if (distance <= 96) {
                selector.getViewComponent().setOpacity(1.0);
                selector.setVisible(true);
            } else {
                selector.getViewComponent().setOpacity(0.3);
                selector.setVisible(true); // Vẫn cho thấy selector nhưng mờ để người dùng biết mình đang trỏ đi đâu
            }
        }
    }

    @Override
    protected void initUI() {
        toolbarView = new ToolbarView(inventory);

        // Đặt thanh công cụ ở giữa phía dưới màn hình
        // Sử dụng các hằng số từ ToolbarView để tính toán kích thước thực tế
        // (SLOT_SIZE = 80, SLOT_GAP = 6)
        double slotSize = 80;
        double slotGap = 6;
        double toolbarWidth = inventory.getSlots().length * (slotSize + slotGap) - slotGap;
        
        toolbarView.setLayoutX((FXGL.getAppWidth() - toolbarWidth) / 2);
        toolbarView.setLayoutY(FXGL.getAppHeight() - slotSize - 20);

        FXGL.getGameScene().addUINode(toolbarView);

        // Thanh máu và thanh thức ăn ở góc trên bên trái
        statusBarsView = new StatusBarsView();
        statusBarsView.setLayoutX(16);
        statusBarsView.setLayoutY(16);
        FXGL.getGameScene().addUINode(statusBarsView);
    }

    @Override
    protected void initInput() {
        int v = 150; // Tốc độ di chuyển thực tế

        Input input = FXGL.getInput();

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityX(v);
            }
            @Override
            protected void onActionEnd() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityX(0);
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityX(-v);
            }
            @Override
            protected void onActionEnd() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityX(0);
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityY(-v);
            }
            @Override
            protected void onActionEnd() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityY(0);
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityY(v);
            }
            @Override
            protected void onActionEnd() {
                getPlayer().getComponent(PhysicsComponent.class).setVelocityY(0);
            }
        }, KeyCode.S);

        // Phím F - Sử dụng vật phẩm đang chọn
        onKeyDown(KeyCode.F, () -> {
            ItemType selected = inventory.getSelectedItem();

            switch (selected) {
                case HOE:
                    // Cuốc đất - tạo ô đất tại vị trí player
                    useHoe();
                    break;
                case RICE_SEED:
                    // Trồng lúa - cần có hạt giống
                    plantRice();
                    break;
                case WATERING_CAN:
                    // Tưới nước (placeholder)
                    System.out.println("Đang tưới nước...");
                    break;
                default:
                    break;
            }
            return null;
        });

        // Phím E - Thu hoạch lúa chín
        onKeyDown(KeyCode.E, () -> {
            if (selector.getViewComponent().getOpacity() < 1.0) return null;

            getGameWorld().getEntitiesByType(EntityType.RICE).stream()
                    .filter(rice -> Math.abs(rice.getX() - selector.getX()) < 5 && Math.abs(rice.getY() - selector.getY()) < 5)
                    .filter(rice -> rice.getComponent(RiceComponent.class).isRipe())
                    .findFirst()
                    .ifPresent(rice -> {
                        // Tìm ô đất tương ứng và đánh dấu chưa có cây
                        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                                .filter(soil -> Math.abs(soil.getX() - rice.getX()) < 5
                                        && Math.abs(soil.getY() - rice.getY()) < 5)
                                .findFirst()
                                .ifPresent(soil -> soil.getComponent(SoilComponent.class).setHasPlant(false));

                        rice.removeFromWorld();
                        inventory.addItem(ItemType.RICE, 1);
                        System.out.println("Thu hoạch lúa! Số lúa: " + inventory.getCount(ItemType.RICE));
                    });
            return null;
        });

        // Phím số 1-9 để chọn slot trên toolbar
        onKeyDown(KeyCode.DIGIT1, () -> { selectSlot(0); return null; });
        onKeyDown(KeyCode.DIGIT2, () -> { selectSlot(1); return null; });
        onKeyDown(KeyCode.DIGIT3, () -> { selectSlot(2); return null; });
        onKeyDown(KeyCode.DIGIT4, () -> { selectSlot(3); return null; });
        onKeyDown(KeyCode.DIGIT5, () -> { selectSlot(4); return null; });
        onKeyDown(KeyCode.DIGIT6, () -> { selectSlot(5); return null; });
        onKeyDown(KeyCode.DIGIT7, () -> { selectSlot(6); return null; });
        onKeyDown(KeyCode.DIGIT8, () -> { selectSlot(7); return null; });
        onKeyDown(KeyCode.DIGIT9, () -> { selectSlot(8); return null; });

        // Phím Q/Tab để chuyển slot
        onKeyDown(KeyCode.Q, () -> {
            inventory.selectPrevious();
            if (toolbarView != null) toolbarView.updateSelection();
            System.out.println("Đã chọn: " + inventory.getSelectedItem().getDisplayName());
            return null;
        });

        onKeyDown(KeyCode.TAB, () -> {
            inventory.selectNext();
            if (toolbarView != null) toolbarView.updateSelection();
            System.out.println("Đã chọn: " + inventory.getSelectedItem().getDisplayName());
            return null;
        });
    }

    private void selectSlot(int slot) {
        inventory.setSelectedSlot(slot);
        if (toolbarView != null) toolbarView.updateSelection();
        System.out.println("Đã chọn: " + inventory.getSelectedItem().getDisplayName());
    }

    private void useHoe() {
        if (selector.getViewComponent().getOpacity() < 1.0) {
            System.out.println("Ngoài phạm vi tương tác!");
            return;
        }

        double x = selector.getX();
        double y = selector.getY();

        // Kiểm tra xem tại vị trí này đã có Soil chưa
        boolean alreadyHasSoil = getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .anyMatch(soil -> Math.abs(soil.getX() - x) < 5 && Math.abs(soil.getY() - y) < 5);

        if (!alreadyHasSoil) {
            getGameWorld().spawn("Soil", x, y);
            System.out.println("Đã tạo ô đất tại: " + x + ", " + y);

            // Chạy cập nhật texture ở frame tiếp theo để đảm bảo thực thể mới đã được thêm vào world list
            FXGL.getExecutor().startAsyncFX(() -> {
                getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
                    soil.getComponent(SoilComponent.class).updateTexture();
                });
            });
        } else {
            System.out.println("Vị trí này đã có ô đất!");
        }
    }

    private void plantRice() {
        if (inventory.getCount(ItemType.RICE_SEED) <= 0) {
            System.out.println("Không đủ hạt giống!");
            return;
        }

        if (selector.getViewComponent().getOpacity() < 1.0) {
            System.out.println("Ngoài phạm vi tương tác!");
            return;
        }

        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(soil -> Math.abs(soil.getX() - selector.getX()) < 5 && Math.abs(soil.getY() - selector.getY()) < 5)
                .filter(soil -> soil.getComponent(SoilComponent.class).canPlant())
                .findFirst()
                .ifPresent(soil -> {
                    SoilComponent config = soil.getComponent(SoilComponent.class);
                    getGameWorld().spawn("Rice", soil.getX(), soil.getY());
                    config.setHasPlant(true);
                    inventory.removeItem(ItemType.RICE_SEED, 1);
                    System.out.println("Đã trồng lúa tại: " + soil.getX() + ", " + soil.getY()
                            + " | Hạt giống còn: " + inventory.getCount(ItemType.RICE_SEED));
                });
    }

    private static Entity getPlayer() {
        return FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

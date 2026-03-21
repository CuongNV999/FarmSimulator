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
    private Inventory inventory;
    private ToolbarView toolbarView;

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

        // Đảm bảo tất cả các thực thể SOIL hiện có được cập nhật texture
        getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(soil -> {
            soil.getComponent(SoilComponent.class).updateTexture();
        });
    }

    @Override
    protected void initUI() {
        toolbarView = new ToolbarView(inventory);

        // Đặt thanh công cụ ở giữa phía dưới màn hình
        double toolbarWidth = inventory.getSlots().length * (64 + 4) - 4;
        toolbarView.setLayoutX((1920 - toolbarWidth) / 2);
        toolbarView.setLayoutY(1080 - 64 - 16);

        FXGL.getGameScene().addUINode(toolbarView);
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
            getGameWorld().getEntitiesByType(EntityType.RICE).stream()
                    .filter(rice -> player.isColliding(rice))
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

        // Phím số 1-4 để chọn slot trên toolbar
        onKeyDown(KeyCode.DIGIT1, () -> { selectSlot(0); return null; });
        onKeyDown(KeyCode.DIGIT2, () -> { selectSlot(1); return null; });
        onKeyDown(KeyCode.DIGIT3, () -> { selectSlot(2); return null; });
        onKeyDown(KeyCode.DIGIT4, () -> { selectSlot(3); return null; });

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
        // Làm tròn vị trí player về lưới 32x32 để Soil khít nhau
        double x = Math.floor(player.getX() / 32) * 32;
        double y = Math.floor(player.getY() / 32) * 32;

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

        getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(soil -> player.isColliding(soil))
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

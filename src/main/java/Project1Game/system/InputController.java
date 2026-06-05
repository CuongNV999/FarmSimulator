package Project1Game.system;

import Project1Game.Main;
import Project1Game.core.EntityType;
import Project1Game.core.ItemType;
import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.animal.BaseAnimalComponent;
import Project1Game.component.player.PlayerComponent;
import Project1Game.ui.DialogView;
import Project1Game.ui.ToolbarView;
import Project1Game.ui.InventoryView;
import Project1Game.ui.TradingView;
import Project1Game.ui.AdminView;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class InputController {
    private static boolean shiftHeld = false;
    private double lastMouseX;
    private double lastMouseY;
    private boolean isDraggingCamera = false;
    private String lastFarmedCell = "";

    public static boolean isShiftHeld() {
        return shiftHeld;
    }

    public void initInputBindings(Main app) {
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
                FXGL.getGameScene().getViewport().bindToEntity(app.getPlayer(), FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
            }

            @Override
            protected void onAction() {
                double speed = isShiftHeld() ? 300 : 200;
                app.getPlayer().getComponent(PhysicsComponent.class).setVelocityX(speed);
            }

            @Override
            protected void onActionEnd() {
                app.getPlayer().getComponent(PhysicsComponent.class).setVelocityX(0);
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onActionBegin() {
                isDraggingCamera = false;
                FXGL.getGameScene().getViewport().bindToEntity(app.getPlayer(), FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
            }

            @Override
            protected void onAction() {
                double speed = isShiftHeld() ? 300 : 200;
                app.getPlayer().getComponent(PhysicsComponent.class).setVelocityX(-speed);
            }

            @Override
            protected void onActionEnd() {
                app.getPlayer().getComponent(PhysicsComponent.class).setVelocityX(0);
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onActionBegin() {
                isDraggingCamera = false;
                FXGL.getGameScene().getViewport().bindToEntity(app.getPlayer(), FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
            }

            @Override
            protected void onAction() {
                double speed = isShiftHeld() ? 300 : 200;
                app.getPlayer().getComponent(PhysicsComponent.class).setVelocityY(-speed);
            }

            @Override
            protected void onActionEnd() {
                app.getPlayer().getComponent(PhysicsComponent.class).setVelocityY(0);
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onActionBegin() {
                isDraggingCamera = false;
                FXGL.getGameScene().getViewport().bindToEntity(app.getPlayer(), FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
            }

            @Override
            protected void onAction() {
                double speed = isShiftHeld() ? 300 : 200;
                app.getPlayer().getComponent(PhysicsComponent.class).setVelocityY(speed);
            }

            @Override
            protected void onActionEnd() {
                app.getPlayer().getComponent(PhysicsComponent.class).setVelocityY(0);
            }
        }, KeyCode.S);

        // 2. PHÍM TƯƠNG TÁC ĐA NĂNG
        input.addAction(new UserAction("Interact Action Key") {
            @Override
            protected void onActionBegin() {
                LevelManager lm = app.getLevelManager();
                CollisionManager cm = app.getCollisionManager();
                if (lm.isLevelTransitioning()) return;

                // ƯU TIÊN 1: [Level Transition & Sleep]
                if (cm.getNearbyDoor() != null && cm.getNearbyDoor().isActive()) {
                    app.handleDoorInteraction(cm.getNearbyDoor());
                    return;
                }
                if (cm.getNearbySleep() != null && cm.getNearbySleep().isActive()) {
                    app.handleSleepInteraction();
                    return;
                }

                // ƯU TIÊN 2: [Animal Harvesting / Product Collection]
                Entity player = app.getPlayer();
                if (player != null) {
                    Entity closestAnimal = null;
                    double closestDist = Double.MAX_VALUE;
                    double interactionRange = 150.0;

                    java.util.List<Entity> animals = FXGL.getGameWorld().getEntitiesByType(EntityType.ANIMAL);
                    for (Entity animal : animals) {
                        double dist = player.getCenter().distance(animal.getCenter());
                        if (dist < closestDist) {
                            closestDist = dist;
                            closestAnimal = animal;
                        }
                    }

                    if (closestAnimal != null && closestDist <= interactionRange) {
                        BaseAnimalComponent bac = closestAnimal.getComponentOptional(BaseAnimalComponent.class).orElse(null);
                        if (bac != null && bac.isReadyToHarvest()) {
                            Project1Game.model.Inventory inv = app.getInventory();
                            if (inv != null) {
                                inv.addItem(bac.getAdultItem(), 1);
                                NotificationManager.pushNotification("Đã thu hoạch một " + bac.getAdultName() + "!");
                                closestAnimal.removeFromWorld();
                            }
                            return;
                        }
                    }
                }

                // ƯU TIÊN 3: [Crop Harvesting]
                Entity selector = app.getSelector();
                if (selector != null && selector.getViewComponent().getOpacity() >= 1.0) {
                    boolean ripeCropTargeted = false;
                    for (EntityType t : Project1Game.core.CropRegistry.getInstance().getSupportedCrops()) {
                        boolean hasRipeCrop = FXGL.getGameWorld().getEntitiesByType(t).stream()
                                .anyMatch(c -> c.getPosition().distance(selector.getPosition()) < 10
                                        && c.getComponent(CropComponent.class).isRipe());
                        if (hasRipeCrop) {
                            ripeCropTargeted = true;
                            break;
                        }
                    }
                    if (ripeCropTargeted) {
                        app.getFarmingSystem().handleHarvest(selector);
                        return;
                    }
                }

                // ƯU TIÊN 4: [NPC / General Proximity Interactions]
                if (player != null) {
                    double radius = 150.0;
                    Point2D playerCenter = player.getCenter();
                    javafx.geometry.Rectangle2D range = new javafx.geometry.Rectangle2D(
                            playerCenter.getX() - radius, playerCenter.getY() - radius,
                            radius * 2, radius * 2);
                    Entity generalTarget = FXGL.getGameWorld().getEntitiesInRange(range).stream()
                            .filter(e -> e.hasComponent(Project1Game.interaction.InteractableComponent.class)
                                      && !e.isType(EntityType.ANIMAL))
                            .findFirst()
                            .orElse(null);

                    if (generalTarget != null) {
                        generalTarget.getComponent(Project1Game.interaction.InteractableComponent.class).interact(player);
                    }
                }
            }
        }, KeyCode.E);

        // PHÍM CHO ĐỘNG VẬT ĐI THEO NGƯỜI CHƠI (KeyCode.G)
        input.addAction(new UserAction("Follow Animal") {
            @Override
            protected void onActionBegin() {
                LevelManager lm = app.getLevelManager();
                if (lm.isLevelTransitioning()) return;
                Entity player = app.getPlayer();
                if (player != null) {
                    double radius = 150.0;
                    Point2D playerCenter = player.getCenter();
                    Entity closestAnimal = null;
                    double closestDist = Double.MAX_VALUE;

                    java.util.List<Entity> animals = FXGL.getGameWorld().getEntitiesByType(EntityType.ANIMAL);
                    for (Entity animal : animals) {
                        double dist = playerCenter.distance(animal.getCenter());
                        if (dist < closestDist) {
                            closestDist = dist;
                            closestAnimal = animal;
                        }
                    }

                    if (closestAnimal != null && closestDist <= radius) {
                        BaseAnimalComponent bac = closestAnimal.getComponentOptional(BaseAnimalComponent.class).orElse(null);
                        if (bac != null) {
                            bac.setFollowing(!bac.isFollowing());

                            if (!bac.isFollowing()) {
                                if (bac.getEntity().hasComponent(PhysicsComponent.class)) {
                                    PhysicsComponent p = bac.getEntity().getComponent(PhysicsComponent.class);
                                    p.setVelocityX(0);
                                    p.setVelocityY(0);
                                }
                            }

                            String name = bac.isReadyToHarvest() ? bac.getAdultName() : bac.getBabyName();
                            if (bac.isFollowing()) {
                                NotificationManager.pushNotification(name + " đang đi theo bạn!");
                            } else {
                                NotificationManager.pushNotification(name + " đã dừng lại.");
                            }
                        }
                    }
                }
            }
        }, KeyCode.G);

        // 3. SỬ DỤNG CÔNG CỤ
        input.addAction(new UserAction("Use Tool Keyboard") {
            @Override
            protected void onActionBegin() {
                handleUseItem(app);
            }
        }, KeyCode.F);

        input.addAction(new UserAction("Use Tool Mouse") {
            @Override
            protected void onActionBegin() {
                Entity selector = app.getSelector();
                if (selector != null) {
                    int gridX = (int) Math.round(selector.getX() / 32.0);
                    int gridY = (int) Math.round(selector.getY() / 32.0);
                    lastFarmedCell = gridX + "," + gridY;
                }
                handleUseItem(app);
            }

            @Override
            protected void onAction() {
                ItemType selected = app.getInventory().getSelectedItem();
                if (selected == ItemType.HOE || selected == ItemType.WATERING_CAN) {
                    Entity selector = app.getSelector();
                    if (selector != null && selector.getViewComponent().getOpacity() >= 1.0) {
                        int gridX = (int) Math.round(selector.getX() / 32.0);
                        int gridY = (int) Math.round(selector.getY() / 32.0);
                        String currentCell = gridX + "," + gridY;
                        if (!currentCell.equals(lastFarmedCell)) {
                            lastFarmedCell = currentCell;
                            handleUseItem(app);
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
            protected void onActionBegin() {
                System.out.println("Test: Ép tưới cây bằng phím Q!");
                app.getFarmingSystem().useWateringCan(app.getSelector());
            }
        }, KeyCode.Q);

        // 4. HỆ THỐNG GIAO DIỆN
        input.addAction(new UserAction("Close UI Window") {
            @Override
            protected void onActionBegin() {
                if (app.getDialogView().isOpen())
                    app.getDialogView().hide();
                if (app.getTradingView().isOpen())
                    app.getTradingView().toggle();
                if (app.getInventoryView().isOpen())
                    app.getInventoryView().toggle();
                if (app.getAdminView().isOpen())
                    app.getAdminView().toggle();
            }
        }, KeyCode.R);

        input.addAction(new UserAction("Toggle Inventory Window") {
            @Override
            protected void onActionBegin() {
                app.getInventoryView().toggle();
            }
        }, KeyCode.I);

        input.addAction(new UserAction("Toggle Inventory Window TAB") {
            @Override
            protected void onActionBegin() {
                app.getInventoryView().toggle();
            }
        }, KeyCode.TAB);

        input.addAction(new UserAction("Toggle Admin Panel") {
            @Override
            protected void onActionBegin() {
                app.getAdminView().toggle();
            }
        }, KeyCode.BACK_QUOTE);

        // 5. LƯU & TẢI (F5 / F9)
        input.addAction(new UserAction("Quick Save") {
            @Override
            protected void onActionBegin() {
                app.saveGame();
            }
        }, KeyCode.F5);

        input.addAction(new UserAction("Quick Load") {
            @Override
            protected void onActionBegin() {
                app.loadGame();
            }
        }, KeyCode.F9);

        input.addAction(new UserAction("Toggle HP Depletion") {
            @Override
            protected void onActionBegin() {
                app.getPlayerStateManager().toggleHPDepletion();
            }
        }, KeyCode.F6);

        input.addAction(new UserAction("Cheat Mature All") {
            @Override
            protected void onActionBegin() {
                app.matureAllCropsAndAnimals();
            }
        }, KeyCode.F7);

        // Admin Console Time Speed Controls
        input.addAction(new UserAction("Set Time Speed 1x") {
            @Override
            protected void onActionBegin() {
                if (app.getTimeSystem() != null)
                    app.getTimeSystem().setTimeSpeedMultiplier(1.0);
            }
        }, KeyCode.NUMPAD7);

        input.addAction(new UserAction("Set Time Speed 50x") {
            @Override
            protected void onActionBegin() {
                if (app.getTimeSystem() != null)
                    app.getTimeSystem().setTimeSpeedMultiplier(50.0);
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
                    app.getInventory().setSelectedSlot(slot);
                    app.getToolbarView().updateSelection();
                }
            }, digitCodes[i]);
        }

        // 7. CUỘN CHUỘT ĐỔI VẬT PHẨM
        input.addEventHandler(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() < 0)
                app.getInventory().selectNext();
            else
                app.getInventory().selectPrevious();
            app.getToolbarView().updateSelection();
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
                double maxX = app.getLevelManager().getCurrentMapWidth() - FXGL.getAppWidth();
                double maxY = app.getLevelManager().getCurrentMapHeight() - FXGL.getAppHeight();
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

    private void handleUseItem(Main app) {
        ItemType selected = app.getInventory().getSelectedItem();
        if (selected != null) {
            selected.use(app.getPlayer(), app.getSelector());
        }
    }
}

package Project1Game.component.farming.animal;

import Project1Game.Main;
import Project1Game.core.ItemType;
import Project1Game.core.EntityType;
import Project1Game.interaction.Interactable;
import Project1Game.interaction.InteractableComponent;
import Project1Game.system.DayNightEvent;
import Project1Game.ui.DialogView;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;

import java.util.List;
import java.util.Random;

public class BaseAnimalComponent extends Component implements Interactable {

    public enum AnimalType {
        CHICKEN, COW, SHEEP, PIG, TURKEY
    }

    protected final AnimalType type;
    protected final String animalName;
    protected final String babyName;
    protected final String adultName;
    protected final int maxGrowthDays;
    protected final String babyTexture;
    protected final String adultTexture;
    protected final ItemType adultItem;
    protected final int babyWidth, babyHeight;
    protected final int adultWidth, adultHeight;

    protected int daysGrown = 0;
    
    // Animation elements
    protected AnimatedTexture texture;
    protected AnimationChannel animWalkDown, animWalkUp, animWalkLeft, animWalkRight;
    protected AnimationChannel animIdleDown, animIdleUp, animIdleLeft, animIdleRight;

    // AI Wandering variables
    private double wanderTimer = 0;
    private double wanderDuration = 0;
    private Point2D moveDir = Point2D.ZERO;
    private final Random random = new Random();

    private EventHandler<DayNightEvent> dayHandler;

    // Fleeing variables
    private boolean isFleeing = false;
    private Point2D fleeTarget = null;
    private List<Point2D> fleePathWaypoints = new java.util.ArrayList<>();
    private double fleeCheckTimer = 0.0;
    
    private Point2D homePosition = null;
    
    protected PhysicsComponent physics;

    public BaseAnimalComponent(AnimalType type, String animalName, String babyName, String adultName, int maxGrowthDays,
                               String babyTexture, String adultTexture, ItemType adultItem,
                               int babyWidth, int babyHeight, int adultWidth, int adultHeight) {
        this.type = type;
        this.animalName = animalName;
        this.babyName = babyName;
        this.adultName = adultName;
        this.maxGrowthDays = maxGrowthDays;
        this.babyTexture = babyTexture;
        this.adultTexture = adultTexture;
        this.adultItem = adultItem;
        this.babyWidth = babyWidth;
        this.babyHeight = babyHeight;
        this.adultWidth = adultWidth;
        this.adultHeight = adultHeight;
    }

    public AnimalType getType() {
        return type;
    }

    public int getDaysGrown() {
        return daysGrown;
    }

    public void setDaysGrown(int daysGrown) {
        this.daysGrown = daysGrown;
    }

    public boolean isReadyToHarvest() {
        return daysGrown >= maxGrowthDays;
    }

    public int getMaxGrowthDays() {
        return maxGrowthDays;
    }

    // Dynamic icon extraction utility method
    public static Image extractFaceDownIdleImage(String texturePath) {
        Image fullImage = FXGL.image(texturePath);
        double imgWidth = fullImage.getWidth();
        double imgHeight = fullImage.getHeight();
        double frameWidth = imgWidth / 6;
        double frameHeight = imgHeight / 8;
        
        int x = 0;
        int y = (int) (4 * frameHeight); // Row 5 is index 4
        
        return new WritableImage(fullImage.getPixelReader(), x, y, (int) frameWidth, (int) frameHeight);
    }

    public void initAnimation() {
        boolean isMature = isReadyToHarvest();
        String currentTexture = isMature ? adultTexture : babyTexture;
        int frameW = isMature ? adultWidth : babyWidth;
        int frameH = isMature ? adultHeight : babyHeight;

        animWalkDown  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), 0, 5);
        animWalkUp    = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), 6, 11);
        animWalkLeft  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), 12, 17);
        animWalkRight = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), 18, 23);
        
        animIdleDown  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 24, 27);
        animIdleUp    = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 30, 33);
        animIdleLeft  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 36, 39);
        animIdleRight = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 42, 45);

        if (texture == null) {
            texture = new AnimatedTexture(animIdleDown);
            entity.getViewComponent().addChild(texture);
            texture.loop();
        } else {
            texture.loopAnimationChannel(animIdleDown);
        }

        // Handle visual scaling to distinguish age
        updateScale(isMature);

        if (entity != null) {
            entity.getTransformComponent().setScaleOrigin(new Point2D(frameW / 2.0, frameH / 2.0));
            entity.getTransformComponent().setRotationOrigin(new Point2D(frameW / 2.0, frameH / 2.0));
        }

        // Configure solid bounding box dynamically
        if (entity != null && entity.getBoundingBoxComponent() != null) {
            entity.getBoundingBoxComponent().clearHitBoxes();
            HitBox hitbox;
            switch (type) {
                case COW:
                    if (isMature) {
                        // Bò trưởng thành (Bull) - 64x64 frame
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(16, 16), BoundingShape.box(32, 32));
                    } else {
                        // Con bê (Calf) - 64x64 frame
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(22, 22), BoundingShape.box(20, 20));
                    }
                    break;
                case CHICKEN:
                    if (isMature) {
                        // Gà trưởng thành (Rooster) - 32x32 frame
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(6, 6), BoundingShape.box(20, 20));
                    } else {
                        // Gà con (Chick) - 16x16 frame
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(2, 2), BoundingShape.box(12, 12));
                    }
                    break;
                case SHEEP:
                    if (isMature) {
                        // Cừu trưởng thành - 32x32 frame
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(4, 4), BoundingShape.box(24, 24));
                    } else {
                        // Cừu non - 32x32 frame
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(8, 8), BoundingShape.box(16, 16));
                    }
                    break;
                case PIG:
                    if (isMature) {
                        // Heo trưởng thành - 32x32 frame
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(4, 4), BoundingShape.box(24, 24));
                    } else {
                        // Heo con - 32x32 frame
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(8, 8), BoundingShape.box(16, 16));
                    }
                    break;
                case TURKEY:
                    // Gà tây - 32x32 frame
                    hitbox = new HitBox("ANIMAL_BODY", new Point2D(6, 6), BoundingShape.box(20, 20));
                    break;
                default:
                    hitbox = new HitBox("ANIMAL_BODY", BoundingShape.box(frameW, frameH));
                    break;
            }
            entity.getBoundingBoxComponent().addHitBox(hitbox);
        }
    }

    private void updateScale(boolean isMature) {
        if (type == AnimalType.TURKEY) {
            entity.setScaleX(2.0);
            entity.setScaleY(2.0);
        } else {
            double scale = isMature ? 2.2 : 1.5;
            entity.setScaleX(scale);
            entity.setScaleY(scale);
        }
    }

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        initAnimation();
        homePosition = entity.getPosition();

        // Listen for day passage
        dayHandler = e -> growOneDay();
        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_DAY, dayHandler);

        // Register interaction handler
        entity.addComponent(new InteractableComponent(this));
    }

    @Override
    public void onRemoved() {
        if (dayHandler != null) {
            FXGL.getEventBus().removeEventHandler(DayNightEvent.SET_DAY, dayHandler);
        }
    }

    private void growOneDay() {
        if (daysGrown < maxGrowthDays) {
            daysGrown++;
            System.out.println(animalName + " grew: " + daysGrown + "/" + maxGrowthDays);
            if (daysGrown == maxGrowthDays) {
                initAnimation();
                System.out.println(animalName + " has matured into adult " + adultName + "!");
            }
        }
    }

    private boolean isMovementBlocked(Point2D velocity) {
        if (velocity.getX() == 0 && velocity.getY() == 0) {
            return false;
        }

        double nextX = entity.getX() + velocity.getX() * FXGL.tpf();
        double nextY = entity.getY() + velocity.getY() * FXGL.tpf();

        double minX = 50;
        double maxX = 3470;
        double minY = 50;
        double maxY = 1998;

        if (nextX < minX || nextX > maxX || nextY < minY || nextY > maxY) {
            return true;
        }

        if (homePosition != null) {
            Point2D nextPos = new Point2D(nextX, nextY);
            if (nextPos.distance(homePosition) > 300.0) {
                return true;
            }
        }

        double w = entity.getWidth();
        double h = entity.getHeight();
        Point2D probePos = entity.getPosition().add(velocity.normalize().multiply(16.0));
        javafx.geometry.Rectangle2D nextBox = new javafx.geometry.Rectangle2D(probePos.getX(), probePos.getY(), w, h);

        List<Entity> obstacles = FXGL.getGameWorld().getEntitiesInRange(nextBox);
        for (Entity obs : obstacles) {
            if (obs.getType() == EntityType.WALL || obs.getType() == EntityType.COLLISION) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onUpdate(double tpf) {
        fleeCheckTimer += tpf;
        if (fleeCheckTimer >= 0.3) {
            fleeCheckTimer = 0.0;
            checkForMonsters();
        }

        if (isFleeing) {
            handleFleeing(tpf);
            return;
        }

        wanderTimer += tpf;
        if (wanderTimer >= wanderDuration) {
            wanderTimer = 0;
            wanderDuration = 2.0 + random.nextDouble() * 3.0; // 2 to 5 seconds cooldown

            if (random.nextDouble() < 0.4) {
                // Idle
                moveDir = Point2D.ZERO;
                int dir = random.nextInt(4);
                if (dir == 0) texture.loopAnimationChannel(animIdleDown);
                else if (dir == 1) texture.loopAnimationChannel(animIdleUp);
                else if (dir == 2) texture.loopAnimationChannel(animIdleLeft);
                else texture.loopAnimationChannel(animIdleRight);
            } else {
                // Walk
                int dir = random.nextInt(4);
                double speed = 25.0; // Moderate speed
                if (dir == 0) {
                    moveDir = new Point2D(0, speed);
                    texture.loopAnimationChannel(animWalkDown);
                } else if (dir == 1) {
                    moveDir = new Point2D(0, -speed);
                    texture.loopAnimationChannel(animWalkUp);
                } else if (dir == 2) {
                    moveDir = new Point2D(-speed, 0);
                    texture.loopAnimationChannel(animWalkLeft);
                } else {
                    moveDir = new Point2D(speed, 0);
                    texture.loopAnimationChannel(animWalkRight);
                }
            }
        }

        if (moveDir.getX() != 0 || moveDir.getY() != 0) {
            if (isMovementBlocked(moveDir)) {
                moveDir = Point2D.ZERO;
                if (physics != null && physics.getBody() != null) {
                    physics.setVelocityX(0);
                    physics.setVelocityY(0);
                }
                texture.loopAnimationChannel(animIdleDown);
                wanderTimer = wanderDuration; // Force choice of new random direction next frame
            } else {
                if (physics != null && physics.getBody() != null) {
                    physics.setVelocityX(moveDir.getX());
                    physics.setVelocityY(moveDir.getY());
                } else {
                    entity.translate(moveDir.multiply(FXGL.tpf()));
                }
            }
        } else {
            if (physics != null && physics.getBody() != null) {
                physics.setVelocityX(0);
                physics.setVelocityY(0);
            }
        }
    }

    @Override
    public void interact(Entity player, Entity target) {
        if (isReadyToHarvest()) {
            Project1Game.model.Inventory inventory = Main.getInstance().getInventory();
            if (inventory != null) {
                inventory.addItem(adultItem, 1);
                FXGL.getNotificationService().pushNotification("Đã thu hoạch một " + adultName + "!");
                entity.removeFromWorld();
            }
        } else {
            int daysRemaining = maxGrowthDays - daysGrown;
            String msg;
            if (type == AnimalType.TURKEY) {
                msg = "Gà tây cần thêm " + daysRemaining + " ngày để sẵn sàng thu hoạch.";
            } else {
                msg = babyName + " cần thêm " + daysRemaining + " ngày để lớn lên.";
            }
            DialogView dialogView = Main.getInstance().getDialogView();
            dialogView.setDialog(animalName, msg);
            dialogView.show();
        }
    }

    private void checkForMonsters() {
        boolean monsterNearby = false;
        Point2D monsterPos = null;

        List<Entity> monsters = FXGL.getGameWorld().getEntitiesByType(EntityType.MONSTER);
        for (Entity m : monsters) {
            Project1Game.component.farming.monster.BushMonsterComponent bmc = 
                    m.getComponentOptional(Project1Game.component.farming.monster.BushMonsterComponent.class).orElse(null);
            if (bmc != null && !bmc.isReturning()) {
                double dist = entity.distance(m);
                if (dist < 250.0) {
                    monsterNearby = true;
                    monsterPos = m.getPosition();
                    break;
                }
            }
        }

        if (monsterNearby) {
            if (!isFleeing) {
                isFleeing = true;
                chooseFleeTarget(monsterPos);
            }
        } else {
            if (isFleeing) {
                isFleeing = false;
                fleeTarget = null;
                fleePathWaypoints.clear();
                texture.loopAnimationChannel(animIdleDown);
                homePosition = entity.getPosition(); // Reset home position to current position after fleeing
            }
        }
    }

    private void chooseFleeTarget(Point2D monsterPos) {
        double mapW = 3520;
        double mapH = 2048;
        double maxW = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                .stream()
                .mapToDouble(e -> e.getRightX()).max().orElse(3520);
        double maxH = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                .stream()
                .mapToDouble(e -> e.getBottomY()).max().orElse(2048);
        mapW = Math.max(mapW, maxW);
        mapH = Math.max(mapH, maxH);

        Point2D candidate = null;
        for (int i = 0; i < 10; i++) {
            double rx = 100 + random.nextDouble() * (mapW - 200);
            double ry = 100 + random.nextDouble() * (mapH - 200);
            Point2D pt = new Point2D(rx, ry);
            if (monsterPos == null || pt.distance(monsterPos) > 400.0) {
                candidate = pt;
                break;
            }
        }
        if (candidate == null) {
            candidate = new Point2D(100 + random.nextDouble() * (mapW - 200), 100 + random.nextDouble() * (mapH - 200));
        }

        fleeTarget = candidate;
        fleePathWaypoints = Project1Game.system.AStarPathfinder.findPath(entity.getPosition(), fleeTarget, mapW, mapH);
        if (fleePathWaypoints.isEmpty()) {
            fleePathWaypoints = new java.util.ArrayList<>();
            fleePathWaypoints.add(fleeTarget);
        }
    }

    private void handleFleeing(double tpf) {
        if (fleeTarget == null || fleePathWaypoints == null || fleePathWaypoints.isEmpty()) {
            chooseFleeTarget(null);
            return;
        }

        Point2D currentWaypoint = fleePathWaypoints.get(0);
        double distToWaypoint = entity.getPosition().distance(currentWaypoint);

        if (distToWaypoint < 12.0) {
            fleePathWaypoints.remove(0);
            if (fleePathWaypoints.isEmpty()) {
                chooseFleeTarget(null);
                return;
            }
            currentWaypoint = fleePathWaypoints.get(0);
        }

        Point2D dir = currentWaypoint.subtract(entity.getPosition()).normalize();
        double fleeSpeed = 60.0;
        Point2D velocity = dir.multiply(fleeSpeed);

        updateFleeAnimation(dir);

        if (physics != null && physics.getBody() != null) {
            physics.setVelocityX(velocity.getX());
            physics.setVelocityY(velocity.getY());
        } else {
            entity.translate(velocity.multiply(tpf));
        }
    }

    private void updateFleeAnimation(Point2D dir) {
        if (Math.abs(dir.getX()) > Math.abs(dir.getY())) {
            if (dir.getX() > 0) {
                texture.loopAnimationChannel(animWalkRight);
            } else {
                texture.loopAnimationChannel(animWalkLeft);
            }
        } else {
            if (dir.getY() > 0) {
                texture.loopAnimationChannel(animWalkDown);
            } else {
                texture.loopAnimationChannel(animWalkUp);
            }
        }
    }

    // Factory method for creating components
    public static BaseAnimalComponent create(String typeName) {
        switch (typeName.toLowerCase()) {
            case "chick":
            case "chicken":
            case "rooster":
                return new BaseAnimalComponent(AnimalType.CHICKEN, "Gà", "Gà con", "Rooster", 4,
                      "Animal/Chick_animation_with_shadow.png", "Animal/Rooster_animation_with_shadow.png",
                      ItemType.ROOSTER, 16, 16, 32, 32);
            case "calf":
            case "cow":
            case "bull":
                return new BaseAnimalComponent(AnimalType.COW, "Bò", "Bê", "Bull", 7,
                      "Animal/Calf_animation_with_shadow.png", "Animal/Bull_animation_with_shadow.png",
                      ItemType.BULL, 64, 64, 64, 64);
            case "lamb":
            case "sheep":
                return new BaseAnimalComponent(AnimalType.SHEEP, "Cừu", "Cừu non", "Sheep", 5,
                      "Animal/Lamb_animation_with_shadow.png", "Animal/Sheep_animation_with_shadow.png",
                      ItemType.SHEEP, 32, 32, 32, 32);
            case "piglet":
            case "pig":
                return new BaseAnimalComponent(AnimalType.PIG, "Heo", "Heo con", "Pig", 6,
                      "Animal/Piglet_animation_with_shadow.png", "Animal/Piglet_animation_with_shadow.png",
                      ItemType.PIG, 32, 32, 32, 32);
            case "turkey":
                return new BaseAnimalComponent(AnimalType.TURKEY, "Gà tây", "Gà tây", "Turkey", 3,
                      "Animal/Turkey_animation_with_shadow.png", "Animal/Turkey_animation_with_shadow.png",
                      ItemType.TURKEY, 32, 32, 32, 32);
            default:
                throw new IllegalArgumentException("Unknown animal type: " + typeName);
        }
    }
}

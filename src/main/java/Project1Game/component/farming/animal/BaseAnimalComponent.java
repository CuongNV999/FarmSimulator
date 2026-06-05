package Project1Game.component.farming.animal;

import Project1Game.Main;
import Project1Game.core.ItemType;
import Project1Game.core.EntityType;
import Project1Game.interaction.Interactable;
import Project1Game.interaction.InteractableComponent;
import Project1Game.system.DayNightEvent;
import Project1Game.system.SteeringComponent;
import Project1Game.ui.DialogView;
import Project1Game.system.NotificationManager;
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

    private final Random random = new Random();
    
    private EventHandler<DayNightEvent> dayHandler;

    // Fleeing variables
    private boolean isFleeing = false;
    private Point2D fleeTarget = null;
    private List<Point2D> fleePathWaypoints = new java.util.ArrayList<>();
    private double fleeCheckTimer = 0.0;
    
    private Point2D homePosition = null;
    private Point2D initialSpawnPos = null;
    
    private int lastStartGridX = -1;
    private int lastEndGridX = -1;
    private int lastStartGridY = -1;
    private int lastEndGridY = -1;
    
    protected PhysicsComponent physics;
    protected SteeringComponent steering;
    protected GrowthComponent growth;

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
        return growth != null ? growth.getDaysGrown() : daysGrown;
    }

    public void setDaysGrown(int daysGrown) {
        this.daysGrown = daysGrown;
        if (growth != null) {
            growth.setDaysGrown(daysGrown);
        }
    }

    public boolean isReadyToHarvest() {
        return growth != null ? growth.isReadyToHarvest() : daysGrown >= maxGrowthDays;
    }

    public ItemType getAdultItem() {
        return adultItem;
    }

    public String getAdultName() {
        return adultName;
    }

    public String getBabyName() {
        return babyName;
    }

    public int getMaxGrowthDays() {
        return maxGrowthDays;
    }

    // Following variables
    private boolean isFollowing = false;

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        this.isFollowing = following;
    }

    /**
     * Force the animal to stop and choose a new random direction.
     * Called when colliding with another creature.
     */
    public void forceNewDirection() {
        if (isFollowing || isFleeing) {
            return; // Don't interrupt following or fleeing behavior
        }
        
        if (steering != null) {
            steering.forceNewDirection(0.5); // 0.5 second pause
        }
        
        // Set to idle animation (random direction)
        int dir = random.nextInt(4);
        if (dir == 0) texture.loopAnimationChannel(animIdleDown);
        else if (dir == 1) texture.loopAnimationChannel(animIdleUp);
        else if (dir == 2) texture.loopAnimationChannel(animIdleLeft);
        else texture.loopAnimationChannel(animIdleRight);
    }

    public double getCollisionCooldown() {
        return steering != null ? steering.getCollisionCooldown() : 0.0;
    }

    private void setAnimationChannel(AnimationChannel channel) {
        if (texture != null && texture.getAnimationChannel() != channel) {
            texture.loopAnimationChannel(channel);
        }
    }

    // Dynamic icon extraction utility method
    public static Image extractFaceDownIdleImage(String texturePath) {
        Image fullImage = FXGL.image(texturePath);
        double imgWidth = fullImage.getWidth();
        double imgHeight = fullImage.getHeight();
        double frameWidth = imgWidth / 6;
        double frameHeight = imgHeight / 8;
        
        boolean isBull = texturePath.contains("Bull");
        int x = 0;
        int y = isBull ? 0 : (int) (4 * frameHeight); // Row 1 (index 0) for Bull, Row 5 (index 4) for others
        
        return new WritableImage(fullImage.getPixelReader(), x, y, (int) frameWidth, (int) frameHeight);
    }

    public void initAnimation() {
        boolean isMature = isReadyToHarvest();
        String currentTexture = isMature ? adultTexture : babyTexture;
        int frameW = isMature ? adultWidth : babyWidth;
        int frameH = isMature ? adultHeight : babyHeight;

        boolean isBull = currentTexture.contains("Bull");

        animWalkDown  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), 0, 5);
        animWalkUp    = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), 6, 11);
        animWalkLeft  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), 12, 17);
        animWalkRight = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), 18, 23);
        
        if (isBull) {
            animIdleDown  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 0, 0);
            animIdleUp    = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 6, 6);
            animIdleLeft  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 12, 12);
            animIdleRight = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 18, 18);
        } else {
            animIdleDown  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 24, 27);
            animIdleUp    = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 30, 33);
            animIdleLeft  = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 36, 39);
            animIdleRight = new AnimationChannel(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(1.0), 42, 45);
        }

        if (texture == null) {
            texture = new AnimatedTexture(animIdleDown);
            entity.getViewComponent().addChild(texture);
            texture.loop();
        } else {
            texture.loopAnimationChannel(animIdleDown);
        }

        // Handle visual scaling and hitboxes via GrowthComponent
        if (growth != null) {
            growth.updateHitboxAndScale(entity, type, frameW, frameH);
        } else {
            // Fallback scaling to distinguish age before GrowthComponent is added
            if (type == AnimalType.TURKEY) {
                entity.setScaleX(2.4);
                entity.setScaleY(2.4);
            } else {
                double scale = isMature ? 2.6 : 1.8;
                entity.setScaleX(scale);
                entity.setScaleY(scale);
            }
            entity.getTransformComponent().setScaleOrigin(new Point2D(frameW / 2.0, frameH / 2.0));
            entity.getTransformComponent().setRotationOrigin(new Point2D(frameW / 2.0, frameH / 2.0));
        }
    }

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        
        // Setup composition components
        steering = entity.getComponentOptional(SteeringComponent.class).orElse(null);
        if (steering == null) {
            steering = new SteeringComponent();
            entity.addComponent(steering);
        }
        
        growth = entity.getComponentOptional(GrowthComponent.class).orElse(null);
        if (growth == null) {
            growth = new GrowthComponent(maxGrowthDays);
            growth.setDaysGrown(daysGrown);
            entity.addComponent(growth);
        }

        initAnimation();
        homePosition = entity.getPosition();
        initialSpawnPos = entity.getPosition();

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
        if (growth != null) {
            growth.growOneDay();
            daysGrown = growth.getDaysGrown();
            System.out.println(animalName + " grew: " + daysGrown + "/" + maxGrowthDays);
            if (growth.isReadyToHarvest()) {
                initAnimation();
                System.out.println(animalName + " has matured into adult " + adultName + "!");
            }
        }
    }

    @Override
    public void onUpdate(double tpf) {
        if (steering == null) return;

        // Handle collision cooldown via SteeringComponent
        steering.updateCooldown(tpf);
        if (steering.getCollisionCooldown() > 0) {
            return; // Skip all other updates during cooldown
        }
        
        if (isFollowing) {
            // Auto-tilling logic for mature Bull in Field zone
            if (adultItem == ItemType.BULL && isReadyToHarvest()) {
                double minX = entity.getX();
                double minY = entity.getY();
                double maxX = minX + entity.getWidth();
                double maxY = minY + entity.getHeight();

                int startGridX = (int) Math.floor(minX / 32) * 32;
                int endGridX = (int) Math.floor((maxX - 1) / 32) * 32;
                int startGridY = (int) Math.floor(minY / 32) * 32;
                int endGridY = (int) Math.floor((maxY - 1) / 32) * 32;

                if (startGridX != lastStartGridX || endGridX != lastEndGridX ||
                    startGridY != lastStartGridY || endGridY != lastEndGridY) {

                    lastStartGridX = startGridX;
                    lastEndGridX = endGridX;
                    lastStartGridY = startGridY;
                    lastEndGridY = endGridY;

                    List<Entity> fields = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD);
                    List<Entity> soils = FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL);

                    for (int cellX = startGridX; cellX <= endGridX; cellX += 32) {
                        for (int cellY = startGridY; cellY <= endGridY; cellY += 32) {
                            final int cx = cellX;
                            final int cy = cellY;

                            boolean cellInField = fields.stream()
                                    .anyMatch(f -> f.getX() <= cx && cx < f.getX() + f.getWidth()
                                            && f.getY() <= cy && cy < f.getY() + f.getHeight());

                            if (cellInField) {
                                boolean cellHasSoil = soils.stream()
                                        .anyMatch(s -> Math.round(s.getX() / 32.0) == Math.round(cx / 32.0)
                                                && Math.round(s.getY() / 32.0) == Math.round(cy / 32.0));

                                if (!cellHasSoil) {
                                    FXGL.spawn("Soil", cx, cy);
                                }
                            }
                        }
                    }
                }
            }

            // Find player entity
            Entity player = FXGL.getGameWorld().getSingleton(Project1Game.core.EntityType.PLAYER);
            if (player != null) {
                Point2D playerCenter = player.getCenter();
                Point2D myCenter = entity.getCenter();
                double distance = playerCenter.distance(myCenter);

                if (distance > 90.0) {
                    Point2D dir = playerCenter.subtract(myCenter).normalize();
                    double speed = 150.0; // Moderate speed to keep up with player
                    Point2D velocity = dir.multiply(speed);

                    steering.moveDirect(velocity, tpf);

                    // Update animations
                    if (Math.abs(dir.getX()) > Math.abs(dir.getY())) {
                        if (dir.getX() > 0) {
                            setAnimationChannel(animWalkRight);
                        } else {
                            setAnimationChannel(animWalkLeft);
                        }
                    } else {
                        if (dir.getY() > 0) {
                            setAnimationChannel(animWalkDown);
                        } else {
                            setAnimationChannel(animWalkUp);
                        }
                    }
                } else {
                    steering.stop();
                    
                    // Loop idle animation in the direction of the last active walk animation
                    if (texture.getAnimationChannel() == animWalkRight) {
                        setAnimationChannel(animIdleRight);
                    } else if (texture.getAnimationChannel() == animWalkLeft) {
                        setAnimationChannel(animIdleLeft);
                    } else if (texture.getAnimationChannel() == animWalkUp) {
                        setAnimationChannel(animIdleUp);
                    } else if (texture.getAnimationChannel() == animWalkDown) {
                        setAnimationChannel(animIdleDown);
                    }
                }
            }
            return;
        }

        fleeCheckTimer += tpf;
        if (fleeCheckTimer >= 0.3) {
            fleeCheckTimer = 0.0;
            checkForMonsters();
        }

        if (isFleeing) {
            handleFleeing(tpf);
            return;
        }

        // Wandering logic delegated to SteeringComponent
        double mapW = 3520;
        double mapH = 2048;
        if (Project1Game.Main.getInstance() != null) {
            mapW = Project1Game.Main.getInstance().getCurrentMapWidth();
            mapH = Project1Game.Main.getInstance().getCurrentMapHeight();
        }

        steering.wander(tpf, 25.0, initialSpawnPos, mapW, mapH);

        // Update animations based on Steering wander direction
        Point2D wanderDir = steering.getWanderDir();
        if (wanderDir.magnitude() > 0) {
            if (Math.abs(wanderDir.getX()) > Math.abs(wanderDir.getY())) {
                if (wanderDir.getX() > 0) setAnimationChannel(animWalkRight);
                else setAnimationChannel(animWalkLeft);
            } else {
                if (wanderDir.getY() > 0) setAnimationChannel(animWalkDown);
                else setAnimationChannel(animWalkUp);
            }
        } else {
            if (texture.getAnimationChannel() == animWalkRight) setAnimationChannel(animIdleRight);
            else if (texture.getAnimationChannel() == animWalkLeft) setAnimationChannel(animIdleLeft);
            else if (texture.getAnimationChannel() == animWalkUp) setAnimationChannel(animIdleUp);
            else if (texture.getAnimationChannel() == animWalkDown) setAnimationChannel(animIdleDown);
        }
    }

    @Override
    public void interact(Entity player, Entity target) {
        if (Project1Game.Main.isShiftHeld()) {
            if (isReadyToHarvest()) {
                Project1Game.model.Inventory inventory = Main.getInstance().getInventory();
                if (inventory != null) {
                    inventory.addItem(adultItem, 1);
                    NotificationManager.pushNotification("Đã thu hoạch một " + adultName + "!");
                    entity.removeFromWorld();
                }
            } else {
                int daysRemaining = maxGrowthDays - getDaysGrown();
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
        } else {
            isFollowing = !isFollowing;
            String name = isReadyToHarvest() ? adultName : babyName;
            if (isFollowing) {
                NotificationManager.pushNotification(name + " đang đi theo bạn!");
            } else {
                if (steering != null) {
                    steering.stop();
                }
                
                // Reset initial spawn position to current position so the animal can wander in this new area
                initialSpawnPos = entity.getPosition();
                if (texture != null && animIdleDown != null) {
                    texture.loopAnimationChannel(animIdleDown);
                }

                NotificationManager.pushNotification(name + " đã dừng lại.");
            }
        }
    }

    private void checkForMonsters() {
        boolean monsterNearby = false;
        Point2D monsterPos = null;

        List<Entity> monsters = FXGL.getGameWorld().getEntitiesByType(EntityType.MONSTER);
        for (Entity m : monsters) {
            Project1Game.component.farming.monster.BaseMonsterComponent bmc = 
                    m.getComponentOptional(Project1Game.component.farming.monster.BaseMonsterComponent.class).orElse(null);
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
                if (steering != null) {
                    steering.clearPath();
                }
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
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32.0;
        fleePathWaypoints = Project1Game.system.AStarPathfinder.findPath(entity.getPosition(), fleeTarget, mapW, mapH, h);
        if (fleePathWaypoints.isEmpty()) {
            fleePathWaypoints = new java.util.ArrayList<>();
            fleePathWaypoints.add(fleeTarget);
        }
        if (steering != null) {
            steering.setPathWaypoints(fleePathWaypoints);
        }
    }

    private void handleFleeing(double tpf) {
        if (steering == null) return;
        
        if (fleeTarget == null || fleePathWaypoints == null || fleePathWaypoints.isEmpty()) {
            chooseFleeTarget(null);
            return;
        }

        double fleeSpeed = 60.0;
        steering.followPath(tpf, fleeSpeed);
        fleePathWaypoints = steering.getPathWaypoints();

        if (fleePathWaypoints.isEmpty()) {
            chooseFleeTarget(null);
            return;
        }

        Point2D currentWaypoint = fleePathWaypoints.get(0);
        Point2D dir = currentWaypoint.subtract(entity.getPosition());
        if (dir.magnitude() > 0.01) {
            updateFleeAnimation(dir.normalize());
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

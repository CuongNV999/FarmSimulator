package Project1Game.component.farming;

import Project1Game.Main;
import Project1Game.core.ItemType;
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

import java.util.Random;

public abstract class BaseAnimalComponent extends Component implements Interactable {

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
            entity.getBoundingBoxComponent().addHitBox(new HitBox("ANIMAL_BODY", BoundingShape.box(frameW, frameH)));
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

    @Override
    public void onUpdate(double tpf) {
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
            double newX = entity.getX() + moveDir.getX() * tpf;
            double newY = entity.getY() + moveDir.getY() * tpf;

            // Constraint boundaries to keep animals inside the main play field
            double minX = 1200;
            double maxX = 2400;
            double minY = 800;
            double maxY = 1600;

            if (newX < minX || newX > maxX || newY < minY || newY > maxY) {
                moveDir = Point2D.ZERO;
                if (physics != null) {
                    physics.setVelocityX(0);
                    physics.setVelocityY(0);
                }
                texture.loopAnimationChannel(animIdleDown);
            } else {
                if (physics != null) {
                    physics.setVelocityX(moveDir.getX());
                    physics.setVelocityY(moveDir.getY());
                }
            }
        } else {
            if (physics != null) {
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

    // Factory method for creating components
    public static BaseAnimalComponent create(String typeName) {
        switch (typeName.toLowerCase()) {
            case "chick":
            case "chicken":
            case "rooster":
                return new ChickenComponent();
            case "calf":
            case "cow":
            case "bull":
                return new CowComponent();
            case "lamb":
            case "sheep":
                return new SheepComponent();
            case "piglet":
            case "pig":
                return new PigComponent();
            case "turkey":
                return new TurkeyComponent();
            default:
                throw new IllegalArgumentException("Unknown animal type: " + typeName);
        }
    }
}

// Subclasses defining unique configuration properties
class ChickenComponent extends BaseAnimalComponent {
    public ChickenComponent() {
        super(AnimalType.CHICKEN, "Gà", "Gà con", "Rooster", 4,
              "Animal/Chick_animation_with_shadow.png", "Animal/Rooster_animation_with_shadow.png",
              ItemType.ROOSTER, 16, 16, 32, 32);
    }
}

class CowComponent extends BaseAnimalComponent {
    public CowComponent() {
        super(AnimalType.COW, "Bò", "Bê", "Bull", 7,
              "Animal/Calf_animation_with_shadow.png", "Animal/Bull_animation_with_shadow.png",
              ItemType.BULL, 64, 64, 64, 64);
    }
}

class SheepComponent extends BaseAnimalComponent {
    public SheepComponent() {
        super(AnimalType.SHEEP, "Cừu", "Cừu non", "Sheep", 5,
              "Animal/Lamb_animation_with_shadow.png", "Animal/Sheep_animation_with_shadow.png",
              ItemType.SHEEP, 32, 32, 32, 32);
    }
}

class PigComponent extends BaseAnimalComponent {
    public PigComponent() {
        super(AnimalType.PIG, "Heo", "Heo con", "Pig", 6,
              "Animal/Piglet_animation_with_shadow.png", "Animal/Piglet_animation_with_shadow.png",
              ItemType.PIG, 32, 32, 32, 32);
    }
}

class TurkeyComponent extends BaseAnimalComponent {
    public TurkeyComponent() {
        super(AnimalType.TURKEY, "Gà tây", "Gà tây", "Turkey", 3,
              "Animal/Turkey_animation_with_shadow.png", "Animal/Turkey_animation_with_shadow.png",
              ItemType.TURKEY, 32, 32, 32, 32);
    }
}

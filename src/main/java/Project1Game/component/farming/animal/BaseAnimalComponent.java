package Project1Game.component.farming.animal;

import Project1Game.config.AnimalConfig;
import Project1Game.config.AnimalConfigRegistry;

import Project1Game.Main;
import Project1Game.core.ItemType;
import Project1Game.core.EntityType;
import Project1Game.interaction.Interactable;
import Project1Game.interaction.InteractableComponent;
import Project1Game.system.DayNightEvent;
import Project1Game.component.common.SteeringComponent;
import Project1Game.ui.view.dialog.DialogView;
import Project1Game.system.NotificationManager;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.util.List;

public class BaseAnimalComponent extends Component implements Interactable {

    public enum AnimalType {
        CHICKEN, COW, SHEEP, PIG, TURKEY
    }

    private final AnimalConfig config;
    private int daysGrown = 0;
    private Point2D initialSpawnPos = null;
    private boolean isFollowing = false;
    private String internalIdentifier;

    private SteeringComponent steering;
    private GrowthComponent growth;
    private AnimalAnimationComponent animation;
    private EventHandler<DayNightEvent> dayHandler;

    public BaseAnimalComponent(AnimalConfig config) {
        this.config = config;
        this.daysGrown = 0; // Default to baby/young (0 days grown)
    }

    public AnimalConfig getConfig() {
        return config;
    }

    public AnimalType getType() {
        return config.type();
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
        return growth != null ? growth.isReadyToHarvest() : daysGrown >= config.maxGrowthDays();
    }

    public ItemType getAdultItem() {
        return config.adultItem();
    }

    public String getAdultName() {
        return config.adultName();
    }

    public String getBabyName() {
        return config.babyName();
    }

    public int getMaxGrowthDays() {
        return config.maxGrowthDays();
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        this.isFollowing = following;
    }

    public void forceNewDirection() {
        if (isFollowing) {
            return;
        }
        FleeBehaviorComponent flee = entity.getComponentOptional(FleeBehaviorComponent.class).orElse(null);
        if (flee != null && flee.isFleeing()) {
            return;
        }
        
        if (steering != null) {
            steering.forceNewDirection(0.5);
        }
        if (animation != null) {
            animation.playIdleInRandomDirection();
        }
    }

    public double getCollisionCooldown() {
        return steering != null ? steering.getCollisionCooldown() : 0.0;
    }

    public static Image extractFaceDownIdleImage(String texturePath) {
        Image fullImage = FXGL.image(texturePath);
        double imgWidth = fullImage.getWidth();
        double imgHeight = fullImage.getHeight();
        double frameWidth = imgWidth / 6;
        double frameHeight = imgHeight / 8;
        
        boolean isBull = texturePath.contains("Bull");
        int x = 0;
        int y = isBull ? 0 : (int) (4 * frameHeight);
        
        return new WritableImage(fullImage.getPixelReader(), x, y, (int) frameWidth, (int) frameHeight);
    }

    public void initAnimation() {
        if (animation == null && entity != null) {
            animation = entity.getComponentOptional(AnimalAnimationComponent.class).orElse(null);
        }
        if (animation != null) {
            animation.initAnimation();
        }
    }

    @Override
    public void onAdded() {
        steering = entity.getComponentOptional(SteeringComponent.class).orElse(null);
        if (steering == null) {
            steering = new SteeringComponent();
            entity.addComponent(steering);
        }

        growth = entity.getComponentOptional(GrowthComponent.class).orElse(null);
        if (growth == null) {
            growth = new GrowthComponent(config.maxGrowthDays());
            growth.setDaysGrown(daysGrown);
            entity.addComponent(growth);
        }

        animation = entity.getComponentOptional(AnimalAnimationComponent.class).orElse(null);
        if (animation == null) {
            animation = new AnimalAnimationComponent(config);
            entity.addComponent(animation);
        }

        initialSpawnPos = entity.getPosition();

        this.internalIdentifier = config.type() == AnimalType.TURKEY ? (growth.isReadyToHarvest() ? "Turkey" : "TurkeyChick") : config.animalName();
        entity.setProperty("internalIdentifier", this.internalIdentifier);

        // Listen for day passage
        dayHandler = e -> {
            if (growth != null) {
                boolean wasMature = growth.isReadyToHarvest();
                growth.growOneDay();
                daysGrown = growth.getDaysGrown();
                System.out.println(config.animalName() + " grew: " + daysGrown + "/" + config.maxGrowthDays());
                if (growth.isReadyToHarvest() && !wasMature) {
                    // Just matured — reinitialize animation to switch to adult texture & scale
                    if (animation != null) {
                        animation.initAnimation();
                    }
                    System.out.println(config.animalName() + " has matured into adult " + config.adultName() + "!");
                    if (config.type() == AnimalType.TURKEY) {
                        this.internalIdentifier = "Turkey";
                        entity.setProperty("internalIdentifier", "Turkey");
                        NotificationManager.pushNotification("Gà tây của bạn đã trưởng thành! Nhấn E để thu hoạch.");
                    }
                }
            }
        };
        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_DAY, dayHandler);

        // Register interaction handler
        entity.addComponent(new InteractableComponent(this));

        // Add flee component
        entity.addComponent(new FleeBehaviorComponent());
    }

    @Override
    public void onRemoved() {
        if (dayHandler != null) {
            FXGL.getEventBus().removeEventHandler(DayNightEvent.SET_DAY, dayHandler);
        }
    }

    @Override
    public void onUpdate(double tpf) {
        if (steering == null) return;

        steering.updateCooldown(tpf);
        if (steering.getCollisionCooldown() > 0) {
            return;
        }

        // FleeBehaviorComponent overrides steering & walk animation when fleeing
        FleeBehaviorComponent flee = entity.getComponentOptional(FleeBehaviorComponent.class).orElse(null);
        if (flee != null && flee.isFleeing()) {
            return;
        }

        if (isFollowing) {
            Entity player = FXGL.getGameWorld().getSingleton(Project1Game.core.EntityType.PLAYER);
            if (player != null) {
                Point2D playerCenter = player.getCenter();
                Point2D myCenter = entity.getCenter();
                double distance = playerCenter.distance(myCenter);

                if (distance > 90.0) {
                    Point2D dir = playerCenter.subtract(myCenter).normalize();
                    double speed = 150.0;
                    steering.moveDirect(dir.multiply(speed), tpf);
                    if (animation != null) {
                        animation.updateWalkAnimation(dir);
                    }
                } else {
                    steering.stop();
                    if (animation != null) {
                        animation.updateIdleAnimation();
                    }
                }
            }
            return;
        }

        // Wandering logic — mature Turkey does NOT wander (stays put)
        if (config.type() == AnimalType.TURKEY && isReadyToHarvest()) {
            // Mature turkey stands still; just update idle animation
            steering.stop();
            if (animation != null) {
                animation.updateIdleAnimation();
            }
            return;
        }

        double mapW = 3520;
        double mapH = 2048;
        if (Project1Game.Main.getInstance() != null) {
            mapW = Project1Game.Main.getInstance().getCurrentMapWidth();
            mapH = Project1Game.Main.getInstance().getCurrentMapHeight();
        }

        steering.wander(tpf, 25.0, initialSpawnPos, mapW, mapH);

        if (animation != null) {
            animation.updateWalkAnimation(steering.getWanderDir());
        }
    }

    @Override
    public void interact(Entity player, Entity target) {
        if (isReadyToHarvest()) {
            Project1Game.model.Inventory inventory = Main.getInstance().getInventory();
            if (inventory != null) {
                inventory.addItem(config.adultItem(), 1);
                NotificationManager.pushNotification("Đã thu hoạch một " + config.adultName() + "!");
                // CRITICAL: Remove entity from world to prevent ghost-spawn / duplication
                entity.removeFromWorld();
                Entity selector = null; // Clear active selector reference
            }
        } else {
            // Animal is not mature yet — show info dialog
            int daysRemaining = config.maxGrowthDays() - getDaysGrown();
            String msg;
            if (config.type() == AnimalType.TURKEY) {
                msg = "Gà tây con cần thêm " + daysRemaining + " ngày để sẵn sàng thu hoạch.";
            } else {
                msg = config.babyName() + " cần thêm " + daysRemaining + " ngày để lớn lên.";
            }
            DialogView dialogView = Main.getInstance().getDialogView();
            dialogView.setDialog(config.animalName(), msg);
            dialogView.show();
        }
    }

    public static BaseAnimalComponent create(String typeName) {
        AnimalConfig config = AnimalConfigRegistry.getInstance().getConfig(typeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown animal type: " + typeName));
        return new BaseAnimalComponent(config);
    }
}

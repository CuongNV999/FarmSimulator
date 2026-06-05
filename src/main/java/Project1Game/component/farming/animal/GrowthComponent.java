package Project1Game.component.farming.animal;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;

public class GrowthComponent extends Component {
    private int daysGrown;
    private final int maxGrowthDays;

    public GrowthComponent(int maxGrowthDays) {
        this.maxGrowthDays = maxGrowthDays;
        this.daysGrown = 0;
    }

    public int getDaysGrown() {
        return daysGrown;
    }

    public void setDaysGrown(int daysGrown) {
        this.daysGrown = daysGrown;
    }

    public int getMaxGrowthDays() {
        return maxGrowthDays;
    }

    public void growOneDay() {
        if (daysGrown < maxGrowthDays) {
            daysGrown++;
        }
    }

    public boolean isReadyToHarvest() {
        return daysGrown >= maxGrowthDays;
    }

    public void updateHitboxAndScale(Entity entity, BaseAnimalComponent.AnimalType type, int frameW, int frameH) {
        if (entity == null) return;

        boolean isMature = isReadyToHarvest();

        // 1. Scale updates
        if (type == BaseAnimalComponent.AnimalType.TURKEY) {
            entity.setScaleX(2.4);
            entity.setScaleY(2.4);
        } else {
            double scale = isMature ? 2.6 : 1.8;
            entity.setScaleX(scale);
            entity.setScaleY(scale);
        }

        entity.getTransformComponent().setScaleOrigin(new Point2D(frameW / 2.0, frameH / 2.0));
        entity.getTransformComponent().setRotationOrigin(new Point2D(frameW / 2.0, frameH / 2.0));

        // 2. Hitbox updates
        if (entity.getBoundingBoxComponent() != null) {
            entity.getBoundingBoxComponent().clearHitBoxes();
            HitBox hitbox;
            switch (type) {
                case COW:
                    if (isMature) {
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(16, 16), BoundingShape.box(32, 32));
                    } else {
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(22, 22), BoundingShape.box(20, 20));
                    }
                    break;
                case CHICKEN:
                    if (isMature) {
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(6, 6), BoundingShape.box(20, 20));
                    } else {
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(2, 2), BoundingShape.box(12, 12));
                    }
                    break;
                case SHEEP:
                    if (isMature) {
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(4, 4), BoundingShape.box(24, 24));
                    } else {
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(8, 8), BoundingShape.box(16, 16));
                    }
                    break;
                case PIG:
                    if (isMature) {
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(4, 4), BoundingShape.box(24, 24));
                    } else {
                        hitbox = new HitBox("ANIMAL_BODY", new Point2D(8, 8), BoundingShape.box(16, 16));
                    }
                    break;
                case TURKEY:
                    hitbox = new HitBox("ANIMAL_BODY", new Point2D(6, 6), BoundingShape.box(20, 20));
                    break;
                default:
                    hitbox = new HitBox("ANIMAL_BODY", BoundingShape.box(frameW, frameH));
                    break;
            }
            entity.getBoundingBoxComponent().addHitBox(hitbox);
        }
    }
}

package Project1Game.component.farming.animal;

import Project1Game.config.AnimalConfig;

import Project1Game.component.common.DirectionalAnimationComponent;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class AnimalAnimationComponent extends DirectionalAnimationComponent {
    private final AnimalConfig config;
    private GrowthComponent growth;

    public AnimalAnimationComponent(AnimalConfig config) {
        this.config = config;
    }

    @Override
    public void onAdded() {
        growth = entity.getComponentOptional(GrowthComponent.class).orElse(null);
        initAnimation();
    }

    public void initAnimation() {
        boolean isMature = growth != null ? growth.isReadyToHarvest() : false;
        String currentTexture = isMature ? config.adultTexture() : config.babyTexture();
        int frameW = isMature ? config.adultWidth() : config.babyWidth();
        int frameH = isMature ? config.adultHeight() : config.babyHeight();

        boolean isBull = currentTexture.contains("Bull");

        int idleDownMin, idleDownMax;
        int idleUpMin, idleUpMax;
        int idleLeftMin, idleLeftMax;
        int idleRightMin, idleRightMax;

        if (isBull) {
            idleDownMin = 0; idleDownMax = 0;
            idleUpMin = 6; idleUpMax = 6;
            idleLeftMin = 12; idleLeftMax = 12;
            idleRightMin = 18; idleRightMax = 18;
        } else {
            idleDownMin = 24; idleDownMax = 27;
            idleUpMin = 30; idleUpMax = 33;
            idleLeftMin = 36; idleLeftMax = 39;
            idleRightMin = 42; idleRightMax = 45;
        }

        setup(FXGL.image(currentTexture), 6, frameW, frameH, Duration.seconds(0.8), Duration.seconds(1.0),
              0, 5,    // Walk Down
              6, 11,   // Walk Up
              12, 17,  // Walk Left
              18, 23,  // Walk Right
              idleDownMin, idleDownMax,
              idleUpMin, idleUpMax,
              idleLeftMin, idleLeftMax,
              idleRightMin, idleRightMax
        );

        if (growth != null) {
            growth.updateHitboxAndScale(entity, config.type(), frameW, frameH);
        } else {
            double scale;
            if (config.type() == BaseAnimalComponent.AnimalType.TURKEY) {
                // Turkey chick = small (0.5), mature turkey = normal (1.0)
                scale = isMature ? 1.0 : 0.5;
            } else {
                scale = isMature ? 2.6 : 1.8;
            }
            entity.setScaleX(scale);
            entity.setScaleY(scale);
            if (config.type() == BaseAnimalComponent.AnimalType.TURKEY) {
                entity.setScaleUniform(scale);
            }
            entity.getTransformComponent().setScaleOrigin(new Point2D(frameW / 2.0, frameH / 2.0));
            entity.getTransformComponent().setRotationOrigin(new Point2D(frameW / 2.0, frameH / 2.0));
        }
    }
}

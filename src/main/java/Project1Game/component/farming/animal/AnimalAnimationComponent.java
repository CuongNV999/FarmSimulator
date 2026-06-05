package Project1Game.component.farming.animal;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class AnimalAnimationComponent extends Component {
    private final AnimalConfig config;
    private AnimatedTexture texture;
    private AnimationChannel animWalkDown, animWalkUp, animWalkLeft, animWalkRight;
    private AnimationChannel animIdleDown, animIdleUp, animIdleLeft, animIdleRight;
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
            growth.updateHitboxAndScale(entity, config.type(), frameW, frameH);
        } else {
            // Fallback scaling to distinguish age before GrowthComponent is added
            if (config.type() == BaseAnimalComponent.AnimalType.TURKEY) {
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

    public void setAnimationChannel(AnimationChannel channel) {
        if (texture != null && texture.getAnimationChannel() != channel) {
            texture.loopAnimationChannel(channel);
        }
    }

    public void updateWalkAnimation(Point2D dir) {
        if (dir.magnitude() > 0.01) {
            if (Math.abs(dir.getX()) > Math.abs(dir.getY())) {
                if (dir.getX() > 0) setAnimationChannel(animWalkRight);
                else setAnimationChannel(animWalkLeft);
            } else {
                if (dir.getY() > 0) setAnimationChannel(animWalkDown);
                else setAnimationChannel(animWalkUp);
            }
        } else {
            updateIdleAnimation();
        }
    }

    public void updateIdleAnimation() {
        if (texture == null) return;
        AnimationChannel current = texture.getAnimationChannel();
        if (current == animWalkRight) setAnimationChannel(animIdleRight);
        else if (current == animWalkLeft) setAnimationChannel(animIdleLeft);
        else if (current == animWalkUp) setAnimationChannel(animIdleUp);
        else if (current == animWalkDown) setAnimationChannel(animIdleDown);
    }
    
    public void playIdleInRandomDirection() {
        int dir = new java.util.Random().nextInt(4);
        if (dir == 0) setAnimationChannel(animIdleDown);
        else if (dir == 1) setAnimationChannel(animIdleUp);
        else if (dir == 2) setAnimationChannel(animIdleLeft);
        else setAnimationChannel(animIdleRight);
    }

    public AnimatedTexture getTexture() {
        return texture;
    }
}

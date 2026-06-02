package Project1Game.component.farming.monster;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

/**
 * Handles running and idling animations for wild monsters.
 */
public class MonsterAnimationComponent extends Component {
    private AnimatedTexture texture;
    private AnimationChannel animWalkDown, animWalkUp, animWalkLeft, animWalkRight;
    private AnimationChannel animIdleDown, animIdleUp, animIdleLeft, animIdleRight;
    private PhysicsComponent physics;
    private AnimationChannel lastWalkAnim;

    public MonsterAnimationComponent(String runTexturePath, String idleTexturePath) {
        // Load run animations (4 rows, 5 frames of interest per row)
        Image runImg = FXGL.image(runTexturePath);
        int runFrameW = 32;
        int runFrameH = 32;

        animWalkDown  = new AnimationChannel(runImg, 5, runFrameW, runFrameH, Duration.seconds(0.8), 0, 4);
        animWalkUp    = new AnimationChannel(runImg, 5, runFrameW, runFrameH, Duration.seconds(0.8), 5, 9);
        animWalkLeft  = new AnimationChannel(runImg, 5, runFrameW, runFrameH, Duration.seconds(0.8), 10, 14);
        animWalkRight = new AnimationChannel(runImg, 5, runFrameW, runFrameH, Duration.seconds(0.8), 15, 19);

        // Load idle animations (4 rows, 4 frames of interest per row)
        Image idleImg = FXGL.image(idleTexturePath);
        int idleFrameW = 32;
        int idleFrameH = 32;

        animIdleDown  = new AnimationChannel(idleImg, 4, idleFrameW, idleFrameH, Duration.seconds(1.0), 0, 3);
        animIdleUp    = new AnimationChannel(idleImg, 4, idleFrameW, idleFrameH, Duration.seconds(1.0), 4, 7);
        animIdleLeft  = new AnimationChannel(idleImg, 4, idleFrameW, idleFrameH, Duration.seconds(1.0), 8, 11);
        animIdleRight = new AnimationChannel(idleImg, 4, idleFrameW, idleFrameH, Duration.seconds(1.0), 12, 15);

        texture = new AnimatedTexture(animIdleDown);
        lastWalkAnim = animWalkDown;
    }

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        entity.getViewComponent().addChild(texture);
        texture.loop();
    }

    @Override
    public void onUpdate(double tpf) {
        double vx = 0;
        double vy = 0;
        if (physics != null) {
            vx = physics.getVelocityX();
            vy = physics.getVelocityY();
        }

        AnimationChannel targetAnim;
        if (vx == 0 && vy == 0) {
            // Idle state matching direction
            if (lastWalkAnim == animWalkDown) targetAnim = animIdleDown;
            else if (lastWalkAnim == animWalkUp) targetAnim = animIdleUp;
            else if (lastWalkAnim == animWalkLeft) targetAnim = animIdleLeft;
            else targetAnim = animIdleRight;
        } else {
            // Run state
            if (Math.abs(vx) > Math.abs(vy)) {
                if (vx < 0) {
                    targetAnim = animWalkLeft;
                } else {
                    targetAnim = animWalkRight;
                }
            } else {
                if (vy > 0) {
                    targetAnim = animWalkDown;
                } else {
                    targetAnim = animWalkUp;
                }
            }
            lastWalkAnim = targetAnim;
        }

        if (texture.getAnimationChannel() != targetAnim) {
            texture.loopAnimationChannel(targetAnim);
        }
    }
}

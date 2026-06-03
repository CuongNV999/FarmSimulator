package Project1Game.component.farming.monster;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

    private String monsterType;
    private boolean useFallback = false;

    public MonsterAnimationComponent(String monsterType, String runTexturePath, String idleTexturePath) {
        this.monsterType = monsterType;
        Image runImg = null;
        Image idleImg = null;

        // 1. Try specific assets
        try {
            runImg = FXGL.image(runTexturePath);
        } catch (Exception e) {
            System.err.println("Failed to load run texture " + runTexturePath + ", trying fallback...");
        }
        try {
            idleImg = FXGL.image(idleTexturePath);
        } catch (Exception e) {
            System.err.println("Failed to load idle texture " + idleTexturePath + ", trying fallback...");
        }

        // 2. Try falling back to Fox sheets
        if (runImg == null) {
            try {
                runImg = FXGL.image("monster/Fox/Fox_Run_with_shadow.png");
            } catch (Exception e) {
                System.err.println("Failed to load Fox run fallback texture.");
            }
        }
        if (idleImg == null) {
            try {
                idleImg = FXGL.image("monster/Fox/Fox_Idle_with_shadow.png");
            } catch (Exception e) {
                System.err.println("Failed to load Fox idle fallback texture.");
            }
        }

        // 3. Try falling back to Boar sheets
        if (runImg == null) {
            try {
                runImg = FXGL.image("monster/Boar/Boar_Run_with_shadow.png");
            } catch (Exception e) {
                System.err.println("Failed to load Boar run fallback texture.");
            }
        }
        if (idleImg == null) {
            try {
                idleImg = FXGL.image("monster/Boar/Boar_Idle_with_shadow.png");
            } catch (Exception e) {
                System.err.println("Failed to load Boar idle fallback texture.");
            }
        }

        if (runImg != null && idleImg != null) {
            int runFrameW = 32;
            int runFrameH = 32;
            int idleFrameW = 32;
            int idleFrameH = 32;

            animWalkDown  = new AnimationChannel(runImg, 5, runFrameW, runFrameH, Duration.seconds(0.8), 0, 4);
            animWalkUp    = new AnimationChannel(runImg, 5, runFrameW, runFrameH, Duration.seconds(0.8), 5, 9);
            
            animIdleDown  = new AnimationChannel(idleImg, 4, idleFrameW, idleFrameH, Duration.seconds(1.0), 0, 3);
            animIdleUp    = new AnimationChannel(idleImg, 4, idleFrameW, idleFrameH, Duration.seconds(1.0), 4, 7);

            // Left triggers true Left channel (Row 3, frames 10-14 for Walk, 8-11 for Idle)
            // Right triggers true Right channel (Row 4, frames 15-19 for Walk, 12-15 for Idle)
            animWalkLeft  = new AnimationChannel(runImg, 5, runFrameW, runFrameH, Duration.seconds(0.8), 10, 14); // Row 3
            animWalkRight = new AnimationChannel(runImg, 5, runFrameW, runFrameH, Duration.seconds(0.8), 15, 19); // Row 4
            
            animIdleLeft  = new AnimationChannel(idleImg, 4, idleFrameW, idleFrameH, Duration.seconds(1.0), 8, 11); // Row 3
            animIdleRight = new AnimationChannel(idleImg, 4, idleFrameW, idleFrameH, Duration.seconds(1.0), 12, 15); // Row 4

            texture = new AnimatedTexture(animIdleDown);
            lastWalkAnim = animWalkDown;
        } else {
            useFallback = true;
        }
    }

    @Override
    public void onAdded() {
        physics = entity.getComponentOptional(PhysicsComponent.class).orElse(null);
        if (useFallback) {
            Rectangle box = new Rectangle(32, 32);
            Color color;
            if ("Fox".equalsIgnoreCase(monsterType)) {
                color = Color.ORANGE;
            } else if ("Deer".equalsIgnoreCase(monsterType)) {
                color = Color.BROWN;
            } else if ("Hare".equalsIgnoreCase(monsterType)) {
                color = Color.LIGHTGRAY;
            } else {
                color = Color.RED;
            }
            box.setFill(color);
            box.setStroke(Color.BLACK);
            box.setStrokeWidth(1);
            entity.getViewComponent().addChild(box);
        } else {
            entity.getViewComponent().addChild(texture);
            texture.loop();
        }
    }

    @Override
    public void onUpdate(double tpf) {
        if (useFallback) {
            return;
        }
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

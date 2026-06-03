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

            // Walk/Run sprite sheet: 6 frames per row, 4 rows
            // Row 0 (frames 0-5):  Walk Down
            // Row 1 (frames 6-11): Walk Up
            // Row 2 (frames 12-17): Walk Left
            // Row 3 (frames 18-23): Walk Right
            int runCols = (int)(runImg.getWidth() / runFrameW);
            animWalkDown  = new AnimationChannel(runImg, runCols, runFrameW, runFrameH, Duration.seconds(0.7), 0,              runCols - 1);
            animWalkUp    = new AnimationChannel(runImg, runCols, runFrameW, runFrameH, Duration.seconds(0.7), runCols,        runCols * 2 - 1);
            animWalkLeft  = new AnimationChannel(runImg, runCols, runFrameW, runFrameH, Duration.seconds(0.7), runCols * 2,    runCols * 3 - 1);
            animWalkRight = new AnimationChannel(runImg, runCols, runFrameW, runFrameH, Duration.seconds(0.7), runCols * 3,    runCols * 4 - 1);

            // Idle sprite sheet: 4 frames per row, 4 rows
            // Row 0 (frames 0-3):  Idle Down
            // Row 1 (frames 4-7):  Idle Up
            // Row 2 (frames 8-11): Idle Left
            // Row 3 (frames 12-15): Idle Right
            int idleCols = (int)(idleImg.getWidth() / idleFrameW);
            animIdleDown  = new AnimationChannel(idleImg, idleCols, idleFrameW, idleFrameH, Duration.seconds(1.0), 0,               idleCols - 1);
            animIdleUp    = new AnimationChannel(idleImg, idleCols, idleFrameW, idleFrameH, Duration.seconds(1.0), idleCols,         idleCols * 2 - 1);
            animIdleLeft  = new AnimationChannel(idleImg, idleCols, idleFrameW, idleFrameH, Duration.seconds(1.0), idleCols * 2,     idleCols * 3 - 1);
            animIdleRight = new AnimationChannel(idleImg, idleCols, idleFrameW, idleFrameH, Duration.seconds(1.0), idleCols * 3,     idleCols * 4 - 1);

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

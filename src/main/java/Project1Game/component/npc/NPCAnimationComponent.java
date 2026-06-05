package Project1Game.component.npc;

import Project1Game.component.common.DirectionalAnimationComponent;
import com.almasb.fxgl.dsl.FXGL;
import javafx.util.Duration;
import javafx.scene.image.Image;

public class NPCAnimationComponent extends DirectionalAnimationComponent {
    public enum Direction {
        DOWN, RIGHT, LEFT, UP
    }

    private Direction currentDir = Direction.DOWN;
    private boolean isMoving = false;

    public NPCAnimationComponent() {
        this("NPC/Guider/Guider.png");
    }

    public NPCAnimationComponent(String textureName) {
        int framesPerRow = 4;
        Image originalImage = FXGL.image(textureName);
        Image processedImage = makeBackgroundTransparent(originalImage);

        setup(processedImage, framesPerRow, 32, 64, Duration.seconds(0.8), Duration.seconds(1.0),
              0, 3,    // Walk Down
              12, 15,  // Walk Up
              8, 11,   // Walk Left
              4, 7,    // Walk Right
              0, 0,    // Idle Down
              12, 12,  // Idle Up
              8, 8,    // Idle Left
              4, 4     // Idle Right
        );
    }

    @Override
    public void onAdded() {
        super.onAdded();

        if (entity.isType(Project1Game.core.EntityType.TRADER)) {
            faceRight();
        } else {
            faceDown();
        }

        isMoving = false;
        updateAnim();
    }

    public void setMoving(boolean moving) {
        if (this.isMoving == moving)
            return;
        this.isMoving = moving;
        updateAnim();
    }

    private void updateAnim() {
        if (isMoving) {
            switch (currentDir) {
                case DOWN:
                    setAnimationChannel(getAnimWalkDown());
                    break;
                case RIGHT:
                    setAnimationChannel(getAnimWalkRight());
                    break;
                case LEFT:
                    setAnimationChannel(getAnimWalkLeft());
                    break;
                case UP:
                    setAnimationChannel(getAnimWalkUp());
                    break;
            }
        } else {
            switch (currentDir) {
                case DOWN:
                    setAnimationChannel(getAnimIdleDown());
                    break;
                case RIGHT:
                    setAnimationChannel(getAnimIdleRight());
                    break;
                case LEFT:
                    setAnimationChannel(getAnimIdleLeft());
                    break;
                case UP:
                    setAnimationChannel(getAnimIdleUp());
                    break;
            }
        }
    }

    public void faceDown() {
        currentDir = Direction.DOWN;
        updateAnim();
    }

    public void faceRight() {
        currentDir = Direction.RIGHT;
        updateAnim();
    }

    public void faceLeft() {
        currentDir = Direction.LEFT;
        updateAnim();
    }

    public void faceUp() {
        currentDir = Direction.UP;
        updateAnim();
    }
}

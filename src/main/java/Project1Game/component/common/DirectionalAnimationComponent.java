package Project1Game.component.common;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class DirectionalAnimationComponent extends Component {
    private AnimatedTexture texture;
    private AnimationChannel animWalkDown, animWalkUp, animWalkLeft, animWalkRight;
    private AnimationChannel animIdleDown, animIdleUp, animIdleLeft, animIdleRight;

    public DirectionalAnimationComponent() {}

    @Override
    public void onAdded() {
        if (texture != null && !entity.getViewComponent().getChildren().contains(texture)) {
            entity.getViewComponent().addChild(texture);
        }
    }

    public void setup(Image sheet, int cols, int frameW, int frameH, Duration walkDuration, Duration idleDuration,
                      int walkDownMin, int walkDownMax,
                      int walkUpMin, int walkUpMax,
                      int walkLeftMin, int walkLeftMax,
                      int walkRightMin, int walkRightMax,
                      int idleDownMin, int idleDownMax,
                      int idleUpMin, int idleUpMax,
                      int idleLeftMin, int idleLeftMax,
                      int idleRightMin, int idleRightMax) {

        animWalkDown  = new AnimationChannel(sheet, cols, frameW, frameH, walkDuration, walkDownMin, walkDownMax);
        animWalkUp    = new AnimationChannel(sheet, cols, frameW, frameH, walkDuration, walkUpMin, walkUpMax);
        animWalkLeft  = new AnimationChannel(sheet, cols, frameW, frameH, walkDuration, walkLeftMin, walkLeftMax);
        animWalkRight = new AnimationChannel(sheet, cols, frameW, frameH, walkDuration, walkRightMin, walkRightMax);

        animIdleDown  = new AnimationChannel(sheet, cols, frameW, frameH, idleDuration, idleDownMin, idleDownMax);
        animIdleUp    = new AnimationChannel(sheet, cols, frameW, frameH, idleDuration, idleUpMin, idleUpMax);
        animIdleLeft  = new AnimationChannel(sheet, cols, frameW, frameH, idleDuration, idleLeftMin, idleLeftMax);
        animIdleRight = new AnimationChannel(sheet, cols, frameW, frameH, idleDuration, idleRightMin, idleRightMax);

        if (texture == null) {
            texture = new AnimatedTexture(animIdleDown);
            texture.loop();
        } else {
            texture.loopAnimationChannel(animIdleDown);
        }

        if (entity != null && !entity.getViewComponent().getChildren().contains(texture)) {
            entity.getViewComponent().addChild(texture);
        }
    }

    public void setup(Image walkSheet, Image idleSheet, int cols, int frameW, int frameH, Duration walkDuration, Duration idleDuration,
                      int walkDownMin, int walkDownMax,
                      int walkUpMin, int walkUpMax,
                      int walkLeftMin, int walkLeftMax,
                      int walkRightMin, int walkRightMax,
                      int idleDownMin, int idleDownMax,
                      int idleUpMin, int idleUpMax,
                      int idleLeftMin, int idleLeftMax,
                      int idleRightMin, int idleRightMax) {

        animWalkDown  = new AnimationChannel(walkSheet, cols, frameW, frameH, walkDuration, walkDownMin, walkDownMax);
        animWalkUp    = new AnimationChannel(walkSheet, cols, frameW, frameH, walkDuration, walkUpMin, walkUpMax);
        animWalkLeft  = new AnimationChannel(walkSheet, cols, frameW, frameH, walkDuration, walkLeftMin, walkLeftMax);
        animWalkRight = new AnimationChannel(walkSheet, cols, frameW, frameH, walkDuration, walkRightMin, walkRightMax);

        animIdleDown  = new AnimationChannel(idleSheet, cols, frameW, frameH, idleDuration, idleDownMin, idleDownMax);
        animIdleUp    = new AnimationChannel(idleSheet, cols, frameW, frameH, idleDuration, idleUpMin, idleUpMax);
        animIdleLeft  = new AnimationChannel(idleSheet, cols, frameW, frameH, idleDuration, idleLeftMin, idleLeftMax);
        animIdleRight = new AnimationChannel(idleSheet, cols, frameW, frameH, idleDuration, idleRightMin, idleRightMax);

        if (texture == null) {
            texture = new AnimatedTexture(animIdleDown);
            texture.loop();
        } else {
            texture.loopAnimationChannel(animIdleDown);
        }

        if (entity != null && !entity.getViewComponent().getChildren().contains(texture)) {
            entity.getViewComponent().addChild(texture);
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

    public AnimationChannel getAnimWalkDown() { return animWalkDown; }
    public AnimationChannel getAnimWalkUp() { return animWalkUp; }
    public AnimationChannel getAnimWalkLeft() { return animWalkLeft; }
    public AnimationChannel getAnimWalkRight() { return animWalkRight; }
    public AnimationChannel getAnimIdleDown() { return animIdleDown; }
    public AnimationChannel getAnimIdleUp() { return animIdleUp; }
    public AnimationChannel getAnimIdleLeft() { return animIdleLeft; }
    public AnimationChannel getAnimIdleRight() { return animIdleRight; }

    public static Image makeBackgroundTransparent(Image inputImage) {
        try {
            int w = (int) inputImage.getWidth();
            int h = (int) inputImage.getHeight();
            WritableImage outputImage = new WritableImage(w, h);
            PixelReader reader = inputImage.getPixelReader();
            PixelWriter writer = outputImage.getPixelWriter();

            Color bgColor = reader.getColor(0, 0);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color color = reader.getColor(x, y);
                    if (isCloseColor(color, bgColor)) {
                        writer.setColor(x, y, Color.TRANSPARENT);
                    } else {
                        writer.setColor(x, y, color);
                    }
                }
            }
            return outputImage;
        } catch (Exception e) {
            System.err.println("Error removing background: " + e.getMessage());
            return inputImage;
        }
    }

    private static boolean isCloseColor(Color c1, Color c2) {
        double threshold = 0.05;
        return Math.abs(c1.getRed() - c2.getRed()) < threshold &&
               Math.abs(c1.getGreen() - c2.getGreen()) < threshold &&
               Math.abs(c1.getBlue() - c2.getBlue()) < threshold;
    }
}

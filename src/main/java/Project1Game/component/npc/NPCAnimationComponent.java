package Project1Game.component.npc;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class NPCAnimationComponent extends Component {
    private AnimatedTexture texture;
    private AnimationChannel animDown, animRight, animLeft, animUp;
    private AnimationChannel animIdleDown, animIdleRight, animIdleLeft, animIdleUp;

    public enum Direction {
        DOWN, RIGHT, LEFT, UP
    }

    private Direction currentDir = Direction.DOWN;
    private boolean isMoving = false;

    public NPCAnimationComponent() {
        this("NPC/Guider/Guider.png");
    }

    public NPCAnimationComponent(String textureName) {
        // Giả sử sprite sheet có 4 khung hình mỗi hàng.

        int framesPerRow = 4;

        Image originalImage = FXGL.image(textureName);
        Image processedImage = makeBackgroundTransparent(originalImage);

        animDown = new AnimationChannel(processedImage, framesPerRow, 32, 64, Duration.seconds(0.8), 0, 3);
        animRight = new AnimationChannel(processedImage, framesPerRow, 32, 64, Duration.seconds(0.8), 4, 7);
        animLeft = new AnimationChannel(processedImage, framesPerRow, 32, 64, Duration.seconds(0.8), 8, 11);
        animUp = new AnimationChannel(processedImage, framesPerRow, 32, 64, Duration.seconds(0.8), 12, 15);

        // Kênh hoạt ảnh đứng yên (idle) hiển thị đúng 1 khung hình tĩnh
        animIdleDown = new AnimationChannel(processedImage, framesPerRow, 32, 64, Duration.seconds(1.0), 0, 0);
        animIdleRight = new AnimationChannel(processedImage, framesPerRow, 32, 64, Duration.seconds(1.0), 4, 4);
        animIdleLeft = new AnimationChannel(processedImage, framesPerRow, 32, 64, Duration.seconds(1.0), 8, 8);
        animIdleUp = new AnimationChannel(processedImage, framesPerRow, 32, 64, Duration.seconds(1.0), 12, 12);

        texture = new AnimatedTexture(animIdleDown);
    }

    private static Image makeBackgroundTransparent(Image inputImage) {
        try {
            int w = (int) inputImage.getWidth();
            int h = (int) inputImage.getHeight();
            WritableImage outputImage = new WritableImage(w, h);
            PixelReader reader = inputImage.getPixelReader();
            PixelWriter writer = outputImage.getPixelWriter();

            // Lấy màu nền từ điểm ảnh trên cùng bên trái (0, 0)
            Color bgColor = reader.getColor(0, 0);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color color = reader.getColor(x, y);
                    // So khớp màu với dung sai nhỏ (để khử nhiễu nén ảnh)
                    if (isCloseColor(color, bgColor)) {
                        writer.setColor(x, y, Color.TRANSPARENT);
                    } else {
                        writer.setColor(x, y, color);
                    }
                }
            }
            return outputImage;
        } catch (Exception e) {
            System.err.println("Lỗi khử nền xám: " + e.getMessage());
            return inputImage;
        }
    }

    private static boolean isCloseColor(Color c1, Color c2) {
        double threshold = 0.05; // Dung sai
        return Math.abs(c1.getRed() - c2.getRed()) < threshold &&
                Math.abs(c1.getGreen() - c2.getGreen()) < threshold &&
                Math.abs(c1.getBlue() - c2.getBlue()) < threshold;
    }

    @Override
    public void onAdded() {
        // Thêm texture vào view của Entity
        entity.getViewComponent().addChild(texture);

        // Hướng nhìn ban đầu dựa trên loại Entity
        if (entity.isType(Project1Game.core.EntityType.TRADER)) {
            faceRight();
        } else {
            faceDown();
        }

        isMoving = false;
        updateAnim();
    }

    @Override
    public void onUpdate(double tpf) {

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
                    texture.loopAnimationChannel(animDown);
                    break;
                case RIGHT:
                    texture.loopAnimationChannel(animRight);
                    break;
                case LEFT:
                    texture.loopAnimationChannel(animLeft);
                    break;
                case UP:
                    texture.loopAnimationChannel(animUp);
                    break;
            }
        } else {
            switch (currentDir) {
                case DOWN:
                    texture.loopAnimationChannel(animIdleDown);
                    break;
                case RIGHT:
                    texture.loopAnimationChannel(animIdleRight);
                    break;
                case LEFT:
                    texture.loopAnimationChannel(animIdleLeft);
                    break;
                case UP:
                    texture.loopAnimationChannel(animIdleUp);
                    break;
            }
        }
    }

    // Các phương thức hỗ trợ đổi hướng nhìn cho NPC
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

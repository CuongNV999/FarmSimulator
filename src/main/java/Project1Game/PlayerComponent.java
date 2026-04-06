package Project1Game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;

import javafx.geometry.Point2D;
import javafx.util.Duration;

public class PlayerComponent extends Component {
    private AnimatedTexture texture;

    // Các trạng thái animation
    private AnimationChannel idleDown, idleUp, idleSide;
    private AnimationChannel walkDown, walkUp, walkSide;
    private AnimationChannel runDown, runUp, runSide;

    private boolean isMoving = false;
    private boolean isRunning = true;
    private Point2D direction = new Point2D(0, 1); // Mặc định nhìn xuống
    private double lastDirectionX = 1; // 1 for right, -1 for left

    public PlayerComponent() {
        // Khởi tạo các AnimationChannel
        // Sheet của bộ asset này thường có 8 frame
        // Chiều rộng sheet là 256px, chia cho 8 frame -> mỗi frame 32px
        // Chiều cao sheet là 64px -> mỗi frame 64px
        double frameDuration = 0.1;
        
        idleDown = new AnimationChannel(FXGL.image("Player/Carry_Idle/Carry_Idle_Down-Sheet.png"), 4, 64, 64, Duration.seconds(frameDuration * 4), 0, 3);
        idleUp = new AnimationChannel(FXGL.image("Player/Carry_Idle/Carry_Idle_Up-Sheet.png"), 4, 64, 64, Duration.seconds(frameDuration * 4), 0, 3);
        idleSide = new AnimationChannel(FXGL.image("Player/Carry_Idle/Carry_Idle_Side-Sheet.png"), 4, 64, 64, Duration.seconds(frameDuration * 4), 0, 3);

        walkDown = new AnimationChannel(FXGL.image("Player/Carry_Walk/Carry_Walk_Down-Sheet.png"), 6, 64, 64, Duration.seconds(frameDuration * 6), 0, 5);
        walkUp = new AnimationChannel(FXGL.image("Player/Carry_Walk/Carry_Walk_Up-Sheet.png"), 6, 64, 64, Duration.seconds(frameDuration * 6), 0, 5);
        walkSide = new AnimationChannel(FXGL.image("Player/Carry_Walk/Carry_Walk_Side-Sheet.png"), 6, 64, 64, Duration.seconds(frameDuration * 6), 0, 5);

        runDown = new AnimationChannel(FXGL.image("Player/Carry_Run/Carry_Run_Down-Sheet.png"), 6, 64, 64, Duration.seconds(frameDuration * 6), 0, 5);
        runUp = new AnimationChannel(FXGL.image("Player/Carry_Run/Carry_Run_Up-Sheet.png"), 6, 64, 64, Duration.seconds(frameDuration * 6), 0, 5);
        runSide = new AnimationChannel(FXGL.image("Player/Carry_Run/Carry_Run_Side-Sheet.png"), 6, 64, 64, Duration.seconds(frameDuration * 6), 0, 5);

        texture = new AnimatedTexture(idleDown);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        // Phóng to player một chút để dễ quan sát trên màn hình lớn
        entity.setScaleX(1.8f);
        entity.setScaleY(1.8f);
    }

    @Override
    public void onUpdate(double tpf) {
        AnimationChannel next;

        // Sử dụng ngưỡng threshold để tránh rung lắc do sai số vật lý
        if (isMoving && direction.magnitude() > 5) {
            if (isRunning) {
                if (Math.abs(direction.getY()) > Math.abs(direction.getX())) {
                    next = direction.getY() > 0 ? runDown : runUp;
                } else {
                    next = runSide;
                    lastDirectionX = direction.getX() < 0 ? -1 : 1;
                }
            } else {
                if (Math.abs(direction.getY()) > Math.abs(direction.getX())) {
                    next = direction.getY() > 0 ? walkDown : walkUp;
                } else {
                    next = walkSide;
                    lastDirectionX = direction.getX() < 0 ? -1 : 1;
                }
            }
        } else {
            // Trạng thái nghỉ (Idle) - dựa trên hướng cuối cùng
            if (Math.abs(direction.getY()) > Math.abs(direction.getX())) {
                next = direction.getY() > 0 ? idleDown : idleUp;
            } else {
                next = idleSide;
                // lastDirectionX đã được lưu từ khi di chuyển
            }
        }

        // Cập nhật lật hình ảnh dựa trên hướng ngang
        texture.setScaleX(lastDirectionX);

        if (texture.getAnimationChannel() != next) {
            texture.loopAnimationChannel(next);
        }
    }

    public void move(Point2D dir) {
        // Chỉ cập nhật isMoving nếu có vận tốc thực sự
        if (dir.magnitude() > 5) {
            isMoving = true;
            direction = dir;
        } else {
            isMoving = false;
        }
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }
}
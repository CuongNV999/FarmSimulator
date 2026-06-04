package Project1Game.component.player;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class PlayerComponent extends Component {
    public static String SELECTED_SKIN = "Player";
    private String currentSkin = "Player";
    private AnimatedTexture texture;

    // Các trạng thái animation
    private AnimationChannel idleDown, idleUp, idleSide;
    private AnimationChannel walkDown, walkUp, walkSide;
    private AnimationChannel runDown, runUp, runSide;

    private boolean isMoving = false;
    private boolean isRunning = false; // Mặc định là đi bộ, nếu nhấn phím chạy sẽ thành true
    private Point2D direction = new Point2D(0, 1); // Hướng nhìn cuối cùng
    private double lastDirectionX = 1; // 1: phải, -1: trái

    // Thuộc tính tiền tệ
    private final IntegerProperty money = new SimpleIntegerProperty(0);

    public PlayerComponent() {
        this(SELECTED_SKIN);
    }

    public PlayerComponent(String skinFolder) {
        this.currentSkin = skinFolder;
        double frameDuration = 0.1;

        loadSkin(skinFolder, frameDuration);

        texture = new AnimatedTexture(idleDown);

        // Khởi tạo tiền ban đầu (ví dụ: 1000)
        money.set(1000);
    }

    private void loadSkin(String skinFolder, double frameDuration) {
        boolean isDefaultOrHUST = skinFolder.equals("Player") || skinFolder.equals("Player_HUST");
        int walkRunFrames = isDefaultOrHUST ? 6 : 4;
        int walkRunEndFrame = isDefaultOrHUST ? 5 : 3;
        double walkRunSec = frameDuration * walkRunFrames;

        idleDown = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Idle/Carry_Idle_Down-Sheet.png"), 4, 64, 64, Duration.seconds(frameDuration * 4), 0, 3);
        idleUp   = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Idle/Carry_Idle_Up-Sheet.png"),   4, 64, 64, Duration.seconds(frameDuration * 4), 0, 3);
        idleSide = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Idle/Carry_Idle_Side-Sheet.png"), 4, 64, 64, Duration.seconds(frameDuration * 4), 0, 3);

        walkDown = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Walk/Carry_Walk_Down-Sheet.png"), walkRunFrames, 64, 64, Duration.seconds(walkRunSec), 0, walkRunEndFrame);
        walkUp   = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Walk/Carry_Walk_Up-Sheet.png"),   walkRunFrames, 64, 64, Duration.seconds(walkRunSec), 0, walkRunEndFrame);
        walkSide = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Walk/Carry_Walk_Side-Sheet.png"), walkRunFrames, 64, 64, Duration.seconds(walkRunSec), 0, walkRunEndFrame);

        runDown  = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Run/Carry_Run_Down-Sheet.png"),  walkRunFrames, 64, 64, Duration.seconds(walkRunSec), 0, walkRunEndFrame);
        runUp    = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Run/Carry_Run_Up-Sheet.png"),    walkRunFrames, 64, 64, Duration.seconds(walkRunSec), 0, walkRunEndFrame);
        runSide  = new AnimationChannel(FXGL.image(skinFolder + "/Carry_Run/Carry_Run_Side-Sheet.png"),  walkRunFrames, 64, 64, Duration.seconds(walkRunSec), 0, walkRunEndFrame);
    }

    public void changeSkin(String skinFolder) {
        this.currentSkin = skinFolder;
        double frameDuration = 0.1;
        loadSkin(skinFolder, frameDuration);
        
        // Cưỡng ép cập nhật hoạt ảnh hiện tại về Idle Down của skin mới ngay lập tức
        texture.loopAnimationChannel(idleDown);
    }

    public String getCurrentSkin() {
        return currentSkin;
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        entity.setScaleX(1.8);
        entity.setScaleY(1.8);
    }

    @Override
    public void onUpdate(double tpf) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

        // 1. TRIỆT TIÊU HOÀN TOÀN LỰC XOAY VẬT LÝ
        if (entity != null) {
            entity.setRotation(0); // Ép cứng góc đồ họa về 0

            // Lấy thành phần vật lý để triệt tiêu vận tốc xoay ẩn của Box2D
            if (physics != null && physics.getBody() != null) {
                physics.getBody().setAngularVelocity(0); // Khóa vận tốc xoay vật lý bằng 0
            }
        }

        // 1. TỰ ĐỘNG LẤY VẬN TỐC TỪ HỆ THỐNG VẬT LÝ
        Point2D velocity = physics != null ? new Point2D(physics.getVelocityX(), physics.getVelocityY()) : Point2D.ZERO;

        // BUG-001 Fix: Manual clamping when no input keys are pressed
        boolean keysPressed = Project1Game.Main.isKeyPressed(javafx.scene.input.KeyCode.W) ||
                              Project1Game.Main.isKeyPressed(javafx.scene.input.KeyCode.S) ||
                              Project1Game.Main.isKeyPressed(javafx.scene.input.KeyCode.A) ||
                              Project1Game.Main.isKeyPressed(javafx.scene.input.KeyCode.D);
        if (!keysPressed && physics != null) {
            if (velocity.magnitude() < 50) {
                physics.setVelocityX(0);
                physics.setVelocityY(0);
                velocity = Point2D.ZERO;
            }
        }

        // Kiểm tra xem nhân vật có đang thực sự di chuyển không (ngưỡng 5 để tránh nhiễu)
        if (velocity.magnitude() > 5) {
            isMoving = true;
            direction = velocity; // Cập nhật hướng theo vận tốc
 
            // Cập nhật lật hình ảnh trái/phải dựa trên vận tốc X
            if (Math.abs(velocity.getX()) > 2) {
                lastDirectionX = velocity.getX() < 0 ? -1 : 1;
            }
 
            // Tự động nhận diện đang chạy nếu vận tốc lớn (ví dụ > 210)
            isRunning = velocity.magnitude() > 210;
        } else {
            isMoving = false;
        }
 
        // 2. CHỌN ANIMATION PHÙ HỢP
        AnimationChannel next;
 
        if (isMoving) {
            if (isRunning) {
                // Ưu tiên hướng Y nếu di chuyển dọc mạnh hơn ngang
                if (Math.abs(direction.getY()) > Math.abs(direction.getX())) {
                    next = direction.getY() > 0 ? runDown : runUp;
                } else {
                    next = runSide;
                }
            } else {
                if (Math.abs(direction.getY()) > Math.abs(direction.getX())) {
                    next = direction.getY() > 0 ? walkDown : walkUp;
                } else {
                    next = walkSide;
                }
            }
        } else {
            // Trạng thái nghỉ (Idle) - Dựa vào hướng cuối cùng trước khi dừng
            if (Math.abs(direction.getY()) > Math.abs(direction.getX())) {
                next = direction.getY() > 0 ? idleDown : idleUp;
            } else {
                next = idleSide;
            }
        }
 
        // 3. CẬP NHẬT ĐỒ HỌA
        texture.setScaleX(lastDirectionX); // Lật ảnh
        if (texture.getAnimationChannel() != next) {
            texture.loopAnimationChannel(next);
        }
    }
 
    // Bạn có thể giữ lại hàm này nếu muốn cưỡng ép hướng từ bên ngoài,
    // nhưng onUpdate ở trên đã tự động hóa việc này.
    public void setRunning(boolean running) {
        this.isRunning = running;
    }
 
    // --- Các phương thức quản lý tiền tệ ---
    public int getMoney() {
        return money.get();
    }
 
    public Point2D getDirection() {
        return direction;
    }
 
    public IntegerProperty moneyProperty() {
        return money;
    }
 
    public void addMoney(int amount) {
        money.set(money.get() + amount);
        System.out.println("Tiền: " + money.get());
    }
 
    public boolean removeMoney(int amount) {
        if (money.get() >= amount) {
            money.set(money.get() - amount);
            System.out.println("Tiền: " + money.get());
            return true;
        }
        System.out.println("Không đủ tiền! Hiện có: " + money.get());
        return false;
    }
 
    public void setMoney(int money) {
        this.money.set(Math.max(0, money));
    }
}
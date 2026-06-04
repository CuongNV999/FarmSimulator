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
 * Fixed to support standard 6-column 4-row grid sprite sheets.
 */
public class MonsterAnimationComponent extends Component {
    private AnimatedTexture texture;
    private AnimationChannel animWalkDown, animWalkUp, animWalkLeft, animWalkRight;
    private AnimationChannel animIdleDown, animIdleUp, animIdleLeft, animIdleRight;
    private PhysicsComponent physics;
    private AnimationChannel lastWalkAnim;

    private String monsterType;
    private boolean useFallback = false;

    public MonsterAnimationComponent(String monsterType, String walkTexturePath, String idleTexturePath) {
        this.monsterType = monsterType;
        Image walkImg = null;
        Image idleImg = null;

        // 1. Try specific assets
        try {
            walkImg = FXGL.image(walkTexturePath);
        } catch (Exception e) {
            System.err.println("Failed to load walk texture " + walkTexturePath + ", trying fallback...");
        }
        try {
            idleImg = FXGL.image(idleTexturePath);
        } catch (Exception e) {
            System.err.println("Failed to load idle texture " + idleTexturePath + ", trying fallback...");
        }

        // 2. Try falling back to Fox sheets
        if (walkImg == null) {
            try {
                walkImg = FXGL.image("monster/Fox/Fox_walk_with_shadow.png");
            } catch (Exception e) {
                System.err.println("Failed to load Fox walk fallback texture.");
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
        if (walkImg == null) {
            try {
                walkImg = FXGL.image("monster/Boar/Boar_Walk_with_shadow.png");
            } catch (Exception e) {
                System.err.println("Failed to load Boar walk fallback texture.");
            }
        }
        if (idleImg == null) {
            try {
                idleImg = FXGL.image("monster/Boar/Boar_Idle_with_shadow.png");
            } catch (Exception e) {
                System.err.println("Failed to load Boar idle fallback texture.");
            }
        }

        if (walkImg != null && idleImg != null) {
            int frameW = 32;
            int frameH = 32;
            
            // SỐ CỘT THỰC TẾ TRÊN 1 HÀNG LÀ 6 (Đã sửa từ 5 và 4 thành 6)
            int totalColumns = 6; 

            // Cấu hình lại chuẩn chỉ số khung hình dựa trên ma trận ảnh Fox thực tế
            animWalkDown  = new AnimationChannel(walkImg, totalColumns, frameW, frameH, Duration.seconds(0.8), 0, 5);  // Hàng 1
            animWalkUp    = new AnimationChannel(walkImg, totalColumns, frameW, frameH, Duration.seconds(0.8), 6, 11); // Hàng 2
            animWalkLeft  = new AnimationChannel(walkImg, totalColumns, frameW, frameH, Duration.seconds(0.8), 12, 17);// Hàng 3
            animWalkRight = new AnimationChannel(walkImg, totalColumns, frameW, frameH, Duration.seconds(0.8), 18, 23);// Hàng 4

            // Hoạt ảnh Đứng im (Idle): Lấy luôn khung hình đầu tiên của mỗi hướng di chuyển để làm trạng thái đứng im mượt mà
            animIdleDown  = new AnimationChannel(idleImg, totalColumns, frameW, frameH, Duration.seconds(0.2), 0, 0);
            animIdleUp    = new AnimationChannel(idleImg, totalColumns, frameW, frameH, Duration.seconds(0.2), 6, 6);
            animIdleLeft  = new AnimationChannel(idleImg, totalColumns, frameW, frameH, Duration.seconds(0.2), 12, 12);
            animIdleRight = new AnimationChannel(idleImg, totalColumns, frameW, frameH, Duration.seconds(0.2), 18, 18);

            texture = new AnimatedTexture(animIdleDown);
            lastWalkAnim = animWalkDown;
        } else {
            useFallback = true;
        }
    }

    @Override
    public void onAdded() {
        physics = entity.getComponentOptional(PhysicsComponent.class).orElse(null);

        if (entity != null) {
            entity.getTransformComponent().setScaleOrigin(new javafx.geometry.Point2D(16.0, 16.0));
            entity.getTransformComponent().setRotationOrigin(new javafx.geometry.Point2D(16.0, 16.0));
            entity.setScaleX(1.8);
            entity.setScaleY(1.8);
        }

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
            if (lastWalkAnim == animWalkDown) targetAnim = animIdleDown;
            else if (lastWalkAnim == animWalkUp) targetAnim = animIdleUp;
            else if (lastWalkAnim == animWalkLeft) targetAnim = animIdleLeft;
            else targetAnim = animIdleRight;
        } else {
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
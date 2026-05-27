package Project1Game.component;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

public class NPCBehaviorComponent extends Component {
    private PhysicsComponent physics;
    private NPCAnimationComponent animation;
    
    private Point2D target = null;
    private boolean isMovingToHouse = false;
    private double speed = 100;

    @Override
    public void onUpdate(double tpf) {
        if (isMovingToHouse && target != null) {
            double distance = entity.getPosition().distance(target);
            
            if (distance > 5) {
                // Tính toán hướng di chuyển
                Point2D velocity = target.subtract(entity.getPosition()).normalize().multiply(speed);
                physics.setVelocityX(velocity.getX());
                physics.setVelocityY(velocity.getY());

                // Cập nhật hướng quay mặt của Animation
                updateAnimation(velocity);
            } else {
                // Đã đến cửa, cho NPC biến mất (đi vào nhà)
                stopMoving();
                entity.removeFromWorld();
                System.out.println("NPC Guider đã đi vào nhà.");
            }
        }
    }

    private void updateAnimation(Point2D v) {
        if (animation == null) return;
        if (Math.abs(v.getX()) > Math.abs(v.getY())) {
            if (v.getX() > 0) animation.faceRight(); else animation.faceLeft();
        } else {
            if (v.getY() > 0) animation.faceDown(); else animation.faceUp();
        }
    }

    public void goHome(Point2D doorPosition) {
        this.target = doorPosition;
        this.isMovingToHouse = true;
        // Đảm bảo BodyType là Kinematic hoặc Dynamic để di chuyển được bằng vận tốc
        physics.setLinearVelocity(Point2D.ZERO);
    }

    public void stopMoving() {
        isMovingToHouse = false;
        physics.setVelocityX(0);
        physics.setVelocityY(0);
    }
    
    public boolean isGoingHome() {
        return isMovingToHouse;
    }
}
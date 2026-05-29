package Project1Game.component.npc;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import com.almasb.fxgl.entity.components.CollidableComponent;
import java.util.Random;

public class NPCBehaviorComponent extends Component {
    private PhysicsComponent physics;
    private NPCAnimationComponent animation;
    
    private Point2D spawnPosition;
    private Point2D target = null;
    private boolean isMovingToHouse = false;
    private double speed = 80; // NPC moves slightly slower for natural look

    // Roaming variables
    private boolean isRoaming = false;
    private Point2D roamTarget = null;
    private double roamTimer = 0.0;
    private double roamCooldown = 0.0;
    private final Random random = new Random();

    // Visibility/Active state
    private boolean isHidden = false;

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        if (entity.hasComponent(NPCAnimationComponent.class)) {
            animation = entity.getComponent(NPCAnimationComponent.class);
        }
        spawnPosition = entity.getPosition();
        roamCooldown = 2.0 + random.nextDouble() * 3.0; // Initial delay before roaming
        
        // Ensure name properties are set correctly after map loader initialization
        if (entity.isType(Project1Game.core.EntityType.GUIDER)) {
            entity.setProperty("name", "Bác Nông Dân");
        } else if (entity.isType(Project1Game.core.EntityType.TRADER)) {
            entity.setProperty("name", "Trader");
        }
    }

    private boolean firstFrame = true;

    @Override
    public void onUpdate(double tpf) {
        if (firstFrame) {
            firstFrame = false;
            if (entity.isType(Project1Game.core.EntityType.GUIDER)) {
                entity.setProperty("name", "Bác Nông Dân");
            } else if (entity.isType(Project1Game.core.EntityType.TRADER)) {
                entity.setProperty("name", "Trader");
            }
        }

        if (isHidden) {
            return;
        }

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
                disappear();
            }
        } else {
            // Roaming during the day
            handleRoaming(tpf);
        }
    }

    private void handleRoaming(double tpf) {
        if (isRoaming && roamTarget != null) {
            double distance = entity.getPosition().distance(roamTarget);
            if (distance > 5) {
                Point2D velocity = roamTarget.subtract(entity.getPosition()).normalize().multiply(speed * 0.6); // Roam slower
                physics.setVelocityX(velocity.getX());
                physics.setVelocityY(velocity.getY());
                updateAnimation(velocity);
            } else {
                // Reached roam target, wait
                physics.setVelocityX(0);
                physics.setVelocityY(0);
                isRoaming = false;
                roamTarget = null;
                roamCooldown = 4.0 + random.nextDouble() * 6.0; // Wait 4 to 10 seconds
                roamTimer = 0;
            }
        } else {
            // Wait for cooldown
            roamTimer += tpf;
            if (roamTimer >= roamCooldown) {
                roamTimer = 0;
                // Choose a random target near spawn position (radius 80px)
                double angle = random.nextDouble() * 2.0 * Math.PI;
                double dist = 30.0 + random.nextDouble() * 65.0;
                double tx = spawnPosition.getX() + Math.cos(angle) * dist;
                double ty = spawnPosition.getY() + Math.sin(angle) * dist;
                
                // Constrain target position to keep them inside map areas (rough boundary check)
                roamTarget = new Point2D(tx, ty);
                isRoaming = true;
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
        this.isRoaming = false;
        this.roamTarget = null;
        physics.setLinearVelocity(Point2D.ZERO);
    }

    public void stopMoving() {
        isMovingToHouse = false;
        isRoaming = false;
        roamTarget = null;
        physics.setVelocityX(0);
        physics.setVelocityY(0);
    }
    
    public boolean isGoingHome() {
        return isMovingToHouse;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void disappear() {
        stopMoving();
        isHidden = true;
        entity.getViewComponent().setOpacity(0.0); // Make invisible
        entity.getComponent(CollidableComponent.class).setValue(false); // Disable collision
        System.out.println("NPC " + entity.getString("name") + " đã đi vào nhà và biến mất.");
    }

    public void reappear() {
        isHidden = false;
        entity.setPosition(spawnPosition);
        entity.getViewComponent().setOpacity(1.0); // Make visible
        entity.getComponent(CollidableComponent.class).setValue(true); // Re-enable collision
        
        // Reset movement states
        isMovingToHouse = false;
        isRoaming = false;
        roamTarget = null;
        roamTimer = 0;
        roamCooldown = 2.0 + random.nextDouble() * 3.0;
        
        System.out.println("NPC " + entity.getString("name") + " đã xuất hiện trở lại tại điểm xuất phát.");
    }
}

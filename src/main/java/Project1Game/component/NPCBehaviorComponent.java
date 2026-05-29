package Project1Game.component;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import com.almasb.fxgl.entity.components.CollidableComponent;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class NPCBehaviorComponent extends Component {
    private PhysicsComponent physics;
    private NPCAnimationComponent animation;

    private Point2D spawnPosition;
    private Point2D target = null;
    private boolean isMovingToHouse = false;
    private double speed = 15; // NPC moves slightly slower for natural look

    // Roaming variables
    private boolean isRoaming = false;
    private Point2D roamTarget = null;
    private double roamTimer = 0.0;
    private double roamCooldown = 0.0;
    private final Random random = new Random();

    // Pathfinding waypoints
    private List<Point2D> pathWaypoints = new ArrayList<>();

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

    private boolean movingX = true;
    private double stuckTimer = 0.0;
    private double escapeTimer = 0.0;
    private Point2D escapeVelocity = Point2D.ZERO;
    private double totalHomeTimer = 0.0;

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
            totalHomeTimer += tpf;
            // Nếu mất quá 25 giây mà chưa về được nhà (do kẹt quá sâu), tự động dịch chuyển
            // về nhà
            if (totalHomeTimer > 25.0) {
                disappear();
                return;
            }

            double distance = entity.getPosition().distance(target);

            // Khi gần đến nhà (khoảng cách < 60 pixel), tắt va chạm vật lý để tránh bị kẹt
            // tường/cửa
            if (distance < 60) {
                if (entity.hasComponent(CollidableComponent.class)) {
                    entity.getComponent(CollidableComponent.class).setValue(false);
                }
            }

            if (distance > 15) {
                if (pathWaypoints == null || pathWaypoints.isEmpty()) {
                    // Fallback to direct path movement
                    double dx = target.getX() - entity.getX();
                    double dy = target.getY() - entity.getY();
                    double moveSpeed = speed * 15;
                    Point2D velocity;
                    if (Math.abs(dx) > 5) {
                        physics.setVelocityX(Math.signum(dx) * moveSpeed);
                        physics.setVelocityY(0);
                        velocity = new Point2D(Math.signum(dx) * speed, 0);
                    } else {
                        physics.setVelocityX(0);
                        physics.setVelocityY(Math.signum(dy) * moveSpeed);
                        velocity = new Point2D(0, Math.signum(dy) * speed);
                    }
                    updateAnimation(velocity);
                    return;
                }

                Point2D currentWaypoint = pathWaypoints.get(0);
                double distToWaypoint = entity.getPosition().distance(currentWaypoint);

                // If we are close to the waypoint, remove it and target the next
                if (distToWaypoint < 12.0) {
                    pathWaypoints.remove(0);
                    if (pathWaypoints.isEmpty()) {
                        physics.setVelocityX(0);
                        physics.setVelocityY(0);
                        return;
                    }
                    currentWaypoint = pathWaypoints.get(0);
                }

                // Move towards currentWaypoint
                double dx = currentWaypoint.getX() - entity.getX();
                double dy = currentWaypoint.getY() - entity.getY();
                double moveSpeed = speed * 15;

                double targetVelX = 0;
                double targetVelY = 0;
                Point2D animVelocity = Point2D.ZERO;

                if (Math.abs(dx) > 5) {
                    targetVelX = Math.signum(dx) * moveSpeed;
                    animVelocity = new Point2D(Math.signum(dx) * speed, 0);
                } else if (Math.abs(dy) > 5) {
                    targetVelY = Math.signum(dy) * moveSpeed;
                    animVelocity = new Point2D(0, Math.signum(dy) * speed);
                } else {
                    // If we are close on both axes, skip this waypoint
                    if (!pathWaypoints.isEmpty()) {
                        pathWaypoints.remove(0);
                    }
                }

                physics.setVelocityX(targetVelX);
                physics.setVelocityY(targetVelY);
                updateAnimation(animVelocity);

                // Stuck detection & recalculation
                boolean isTryingToMove = (targetVelX != 0 || targetVelY != 0);
                if (isTryingToMove) {
                    boolean isStuck = (targetVelX != 0 && Math.abs(physics.getVelocityX()) < 5)
                            || (targetVelY != 0 && Math.abs(physics.getVelocityY()) < 5);
                    if (isStuck) {
                        stuckTimer += tpf;
                        if (stuckTimer > 1.0) {
                            recalculatePath();
                            stuckTimer = 0;
                        }
                    } else {
                        stuckTimer = 0;
                    }
                } else {
                    stuckTimer = 0;
                }
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
        // Vô hiệu hóa roaming: NPC sẽ chỉ đứng yên 1 chỗ (trừ khi đến giờ đi về nhà)
        physics.setVelocityX(0);
        physics.setVelocityY(0);
    }

    private void updateAnimation(Point2D v) {
        if (animation == null)
            return;
        if (Math.abs(v.getX()) > Math.abs(v.getY())) {
            if (v.getX() > 0)
                animation.faceRight();
            else
                animation.faceLeft();
        } else {
            if (v.getY() > 0)
                animation.faceDown();
            else
                animation.faceUp();
        }
    }

    public void goHome(Point2D doorPosition) {
        this.target = doorPosition;
        this.isMovingToHouse = true;
        this.isRoaming = false;
        this.roamTarget = null;
        this.totalHomeTimer = 0.0;
        this.stuckTimer = 0.0;
        recalculatePath();
        physics.setLinearVelocity(Point2D.ZERO);
    }

    private void recalculatePath() {
        if (target == null)
            return;
        double mapW = 3520;
        double mapH = 2048;
        double maxW = FXGL.getGameWorld()
                .getEntitiesByType(Project1Game.core.EntityType.FIELD, Project1Game.core.EntityType.WALL,
                        Project1Game.core.EntityType.COLLISION)
                .stream()
                .mapToDouble(e -> e.getRightX()).max().orElse(3520);
        double maxH = FXGL.getGameWorld()
                .getEntitiesByType(Project1Game.core.EntityType.FIELD, Project1Game.core.EntityType.WALL,
                        Project1Game.core.EntityType.COLLISION)
                .stream()
                .mapToDouble(e -> e.getBottomY()).max().orElse(2048);
        mapW = Math.max(mapW, maxW);
        mapH = Math.max(mapH, maxH);

        this.pathWaypoints = Project1Game.system.AStarPathfinder.findPath(entity.getPosition(), target, mapW, mapH);

        if (this.pathWaypoints.isEmpty()) {
            System.out.println("Warning: A* pathfinding failed for NPC " + entity.getString("name")
                    + ". Using direct path fallback.");
            this.pathWaypoints = new ArrayList<>();
            this.pathWaypoints.add(target);
        }
    }

    public void stopMoving() {
        isMovingToHouse = false;
        isRoaming = false;
        roamTarget = null;
        physics.setVelocityX(0);
        physics.setVelocityY(0);
        pathWaypoints.clear();
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
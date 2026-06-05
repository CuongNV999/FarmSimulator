package Project1Game.component.common;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import Project1Game.core.EntityType;
import Project1Game.Main;
import Project1Game.system.AStarPathfinder;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Reusable, unified steering and pathfinding component.
 * Integrates A* pathfinding, target tracking, wandering, obstacle checks,
 * and physics-driven velocity vector application.
 */
public class SteeringComponent extends Component {
    private PhysicsComponent physics;
    private Point2D targetPosition = null;
    private Entity targetEntity = null;
    private List<Point2D> pathWaypoints = new ArrayList<>();
    
    private double speed = 50.0;
    private double collisionCooldown = 0.0;
    private double pathRecalcTimer = 0.0;
    private static final double RECALC_INTERVAL = 1.0; // Recalculate path every 1 second
    
    // Wander state variables
    private double wanderTimer = 0.0;
    private double wanderDuration = 0.0;
    private Point2D wanderDir = Point2D.ZERO;
    private Point2D initialSpawnPos = null;
    private final Random random = new Random();

    // Map boundary properties
    private double cachedMapWidth = 3520.0;
    private double cachedMapHeight = 2048.0;
    private double safetyMargin = 32.0;

    public SteeringComponent() {}

    public SteeringComponent(double speed) {
        this.speed = speed;
    }

    @Override
    public void onAdded() {
        physics = entity.getComponentOptional(PhysicsComponent.class).orElse(null);
        initialSpawnPos = entity.getPosition();

        // Dynamically cache map dimensions from Main
        if (Main.getInstance() != null) {
            cachedMapWidth = Main.getInstance().getCurrentMapWidth();
            cachedMapHeight = Main.getInstance().getCurrentMapHeight();
        }
    }

    public void updateCooldown(double dt) {
        if (collisionCooldown > 0) {
            collisionCooldown = Math.max(0, collisionCooldown - dt);
            stop();
        }
    }

    @Override
    public void onUpdate(double tpf) {
        if (entity == null || !entity.isActive()) return;

        // 1. Handle collision cooldown/obstacles stoppage
        if (collisionCooldown > 0) {
            return;
        }

        // 2. Resolve target entity position if tracking an entity
        if (targetEntity != null) {
            if (targetEntity.isActive()) {
                targetPosition = targetEntity.getPosition();
            } else {
                clearTarget();
            }
        }

        // 3. Drive behavior based on target presence
        if (targetPosition != null) {
            // Periodic path recalculation
            pathRecalcTimer += tpf;
            if (pathRecalcTimer >= RECALC_INTERVAL || pathWaypoints.isEmpty()) {
                pathRecalcTimer = 0.0;
                recalculatePath();
            }

            followPath(tpf);
        } else {
            // Wander if no target is active
            wander(tpf);
        }

        // 4. Force containment inside bounds
        clampToBoundaries();
    }

    public void setTarget(Point2D position) {
        this.targetPosition = position;
        this.targetEntity = null;
        this.pathRecalcTimer = 0.0;
        recalculatePath();
    }

    public void setTarget(Entity entity) {
        this.targetEntity = entity;
        if (entity != null) {
            this.targetPosition = entity.getPosition();
        }
        this.pathRecalcTimer = 0.0;
        recalculatePath();
    }

    public void clearTarget() {
        this.targetPosition = null;
        this.targetEntity = null;
        this.pathWaypoints.clear();
        stop();
    }

    public Point2D getTargetPosition() {
        return targetPosition;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getCollisionCooldown() {
        return collisionCooldown;
    }

    public void setCollisionCooldown(double cooldown) {
        this.collisionCooldown = cooldown;
        if (cooldown > 0) {
            stop();
        }
    }

    public void stop() {
        if (physics != null) {
            physics.setVelocityX(0);
            physics.setVelocityY(0);
        }
    }

    public void recalculatePath() {
        if (targetPosition == null) {
            pathWaypoints.clear();
            return;
        }

        double height = entity.getHeight() > 0 ? entity.getHeight() : 32.0;
        this.pathWaypoints = AStarPathfinder.findPath(
            entity.getPosition(), 
            targetPosition, 
            cachedMapWidth, 
            cachedMapHeight, 
            height
        );

        if (this.pathWaypoints.isEmpty()) {
            // Pathfinding fallback: direct straight line drive
            this.pathWaypoints = new ArrayList<>();
            this.pathWaypoints.add(targetPosition);
        }
    }

    // Retained for compatibility with legacy callers
    public void followPath(double tpf, double customSpeed) {
        double originalSpeed = this.speed;
        this.speed = customSpeed;
        followPath(tpf);
        this.speed = originalSpeed;
    }

    private void followPath(double tpf) {
        if (pathWaypoints == null || pathWaypoints.isEmpty()) {
            stop();
            return;
        }

        Point2D currentWaypoint = pathWaypoints.get(0);
        double distToWaypoint = entity.getPosition().distance(currentWaypoint);

        // Switch to next waypoint if reached close proximity
        if (distToWaypoint < 12.0) {
            pathWaypoints.remove(0);
            if (pathWaypoints.isEmpty()) {
                stop();
                return;
            }
            currentWaypoint = pathWaypoints.get(0);
        }

        Point2D direction = currentWaypoint.subtract(entity.getPosition());
        if (direction.magnitude() > 0.01) {
            direction = direction.normalize();
        }

        moveDirect(direction.multiply(speed), tpf);
    }

    // Retained for compatibility with legacy callers
    public void wander(double tpf, double customSpeed, Point2D initialSpawnPos, double mapWidth, double mapHeight) {
        this.initialSpawnPos = initialSpawnPos;
        this.cachedMapWidth = mapWidth;
        this.cachedMapHeight = mapHeight;
        double originalSpeed = this.speed;
        this.speed = customSpeed;
        wander(tpf);
        this.speed = originalSpeed;
    }

    private void wander(double tpf) {
        wanderTimer += tpf;
        if (wanderTimer >= wanderDuration) {
            wanderTimer = 0.0;
            wanderDuration = 2.0 + random.nextDouble() * 3.0;

            if (random.nextDouble() < 0.3) {
                // Stand idle occasionally
                wanderDir = Point2D.ZERO;
            } else {
                double angle = random.nextDouble() * 2 * Math.PI;
                wanderDir = new Point2D(Math.cos(angle), Math.sin(angle)).normalize().multiply(speed * 0.6);
            }
        }

        if (wanderDir.magnitude() > 0.01) {
            double nextX = entity.getX() + wanderDir.getX() * tpf;
            double nextY = entity.getY() + wanderDir.getY() * tpf;
            boolean limitExceeded = false;

            if (initialSpawnPos != null) {
                // Keep wander within 300px of initial spawn anchor
                if (Math.abs(nextX - initialSpawnPos.getX()) > 300.0 || Math.abs(nextY - initialSpawnPos.getY()) > 300.0) {
                    limitExceeded = true;
                }
            }

            if (limitExceeded || isMovementBlocked(wanderDir)) {
                wanderDir = Point2D.ZERO;
                wanderTimer = wanderDuration; // Force recalculation next tick
                stop();
            } else {
                moveDirect(wanderDir, tpf);
            }
        } else {
            stop();
        }
    }

    public void moveDirect(Point2D velocity) {
        moveDirect(velocity, FXGL.tpf());
    }

    public void moveDirect(Point2D velocity, double tpf) {
        if (physics != null) {
            physics.setVelocityX(velocity.getX());
            physics.setVelocityY(velocity.getY());
        } else {
            entity.translate(velocity.multiply(tpf));
        }
    }

    public boolean isMovementBlocked(Point2D velocity) {
        if (velocity.getX() == 0 && velocity.getY() == 0) return false;

        double w = entity.getWidth() > 0 ? entity.getWidth() : 32.0;
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32.0;

        Point2D direction = velocity.normalize();
        double probeX = entity.getX() + direction.getX() * 16.0;
        double probeY = entity.getY() + direction.getY() * 16.0;

        Rectangle2D nextBox = new Rectangle2D(probeX, probeY, w, h);

        List<Entity> obstacles = FXGL.getGameWorld().getEntitiesInRange(nextBox);
        for (Entity obs : obstacles) {
            Object type = obs.getType();
            if (type == EntityType.WALL || type == EntityType.COLLISION) {
                return true;
            }
        }
        return false;
    }

    // Retained for compatibility with legacy callers
    public boolean isMovementBlocked(Point2D velocity, double mapWidth, double mapHeight, double safetyMargin) {
        this.cachedMapWidth = mapWidth;
        this.cachedMapHeight = mapHeight;
        this.safetyMargin = safetyMargin;
        return isMovementBlocked(velocity);
    }

    public void forceNewDirection(double cooldown) {
        this.collisionCooldown = cooldown;
        this.wanderDir = Point2D.ZERO;
        this.wanderTimer = wanderDuration;
        stop();
    }

    public void clampToBoundaries() {
        double w = entity.getWidth() > 0 ? entity.getWidth() : 32.0;
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32.0;

        double maxLegalX = cachedMapWidth - w - safetyMargin;
        double maxLegalY = cachedMapHeight - h - safetyMargin;

        if (entity.getX() < safetyMargin) {
            entity.setX(safetyMargin);
            if (physics != null) physics.setVelocityX(Math.abs(physics.getVelocityX()));
        }
        if (entity.getX() > maxLegalX) {
            entity.setX(maxLegalX);
            if (physics != null) physics.setVelocityX(-Math.abs(physics.getVelocityX()));
        }
        if (entity.getY() < safetyMargin) {
            entity.setY(safetyMargin);
            if (physics != null) physics.setVelocityY(Math.abs(physics.getVelocityY()));
        }
        if (entity.getY() > maxLegalY) {
            entity.setY(maxLegalY);
            if (physics != null) physics.setVelocityY(-Math.abs(physics.getVelocityY()));
        }
    }

    // Retained for compatibility with legacy callers
    public void clampToBoundaries(double mapWidth, double mapHeight, double safetyMargin) {
        this.cachedMapWidth = mapWidth;
        this.cachedMapHeight = mapHeight;
        this.safetyMargin = safetyMargin;
        clampToBoundaries();
    }

    public void clearPath() {
        pathWaypoints.clear();
    }

    public List<Point2D> getPathWaypoints() {
        return pathWaypoints;
    }

    public void setPathWaypoints(List<Point2D> waypoints) {
        this.pathWaypoints = waypoints != null ? waypoints : new ArrayList<>();
    }

    public Point2D getWanderDir() {
        return wanderDir;
    }
}

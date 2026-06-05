package Project1Game.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import Project1Game.core.EntityType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SteeringComponent extends Component {
    private PhysicsComponent physics;
    private List<Point2D> pathWaypoints = new ArrayList<>();
    private double collisionCooldown = 0.0;
    private final Random random = new Random();

    // Wander state
    private double wanderTimer = 0.0;
    private double wanderDuration = 0.0;
    private Point2D wanderDir = Point2D.ZERO;

    @Override
    public void onAdded() {
        physics = entity.getComponentOptional(PhysicsComponent.class).orElse(null);
    }

    public void updateCooldown(double dt) {
        if (collisionCooldown > 0) {
            collisionCooldown -= dt;
            if (physics != null && physics.getBody() != null) {
                physics.setVelocityX(0);
                physics.setVelocityY(0);
            }
        }
    }

    public double getCollisionCooldown() {
        return collisionCooldown;
    }

    public void setCollisionCooldown(double cooldown) {
        this.collisionCooldown = cooldown;
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

    public void stop() {
        if (physics != null && physics.getBody() != null) {
            physics.setVelocityX(0);
            physics.setVelocityY(0);
        }
        pathWaypoints.clear();
    }

    public void moveDirect(Point2D velocity) {
        moveDirect(velocity, FXGL.tpf());
    }

    public void moveDirect(Point2D velocity, double dt) {
        if (physics != null && physics.getBody() != null) {
            physics.setVelocityX(velocity.getX());
            physics.setVelocityY(velocity.getY());
        } else {
            entity.translate(velocity.multiply(dt));
        }
    }

    public boolean isMovementBlocked(Point2D velocity, double mapWidth, double mapHeight, double safetyMargin) {
        if (velocity.getX() == 0 && velocity.getY() == 0) {
            return false;
        }

        double nextX = entity.getX() + velocity.getX() * FXGL.tpf();
        double nextY = entity.getY() + velocity.getY() * FXGL.tpf();

        if (nextX < safetyMargin || nextX > mapWidth - safetyMargin * 2 || nextY < safetyMargin || nextY > mapHeight - safetyMargin * 2) {
            return true;
        }

        double w = entity.getWidth() > 0 ? entity.getWidth() : 32;
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32;

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

    public void followPath(double tpf, double speed) {
        if (pathWaypoints == null || pathWaypoints.isEmpty()) {
            stop();
            return;
        }

        Point2D currentWaypoint = pathWaypoints.get(0);
        double distToWaypoint = entity.getPosition().distance(currentWaypoint);

        if (distToWaypoint < 12.0) {
            pathWaypoints.remove(0);
            if (pathWaypoints.isEmpty()) {
                stop();
                return;
            }
            currentWaypoint = pathWaypoints.get(0);
        }

        Point2D dir = currentWaypoint.subtract(entity.getPosition());
        if (dir.magnitude() > 0.01) {
            dir = dir.normalize();
        }
        moveDirect(dir.multiply(speed), tpf);
    }

    public void wander(double tpf, double speed, Point2D initialSpawnPos, double mapWidth, double mapHeight) {
        wanderTimer += tpf;
        if (wanderTimer >= wanderDuration) {
            wanderTimer = 0.0;
            wanderDuration = 2.0 + random.nextDouble() * 3.0;

            if (random.nextDouble() < 0.4) {
                wanderDir = Point2D.ZERO;
            } else {
                double angle = random.nextDouble() * 2 * Math.PI;
                wanderDir = new Point2D(Math.cos(angle), Math.sin(angle)).normalize().multiply(speed);
            }
        }

        if (wanderDir.magnitude() > 0) {
            double nextX = entity.getX() + wanderDir.getX() * tpf;
            double nextY = entity.getY() + wanderDir.getY() * tpf;
            boolean outOfBounds = false;

            if (initialSpawnPos != null) {
                if (Math.abs(nextX - initialSpawnPos.getX()) > 300.0 || Math.abs(nextY - initialSpawnPos.getY()) > 300.0) {
                    outOfBounds = true;
                }
            }

            if (outOfBounds || isMovementBlocked(wanderDir, mapWidth, mapHeight, 32.0)) {
                wanderDir = Point2D.ZERO;
                wanderTimer = wanderDuration;
                moveDirect(Point2D.ZERO, tpf);
            } else {
                moveDirect(wanderDir, tpf);
            }
        } else {
            moveDirect(Point2D.ZERO, tpf);
        }
    }

    public Point2D getWanderDir() {
        return wanderDir;
    }

    public void forceNewDirection(double cooldown) {
        collisionCooldown = cooldown;
        wanderDir = Point2D.ZERO;
        stop();
        wanderTimer = wanderDuration; // Force recalculation
    }

    public void clampToBoundaries(double mapWidth, double mapHeight, double safetyMargin) {
        double w = entity.getWidth() > 0 ? entity.getWidth() : 32.0;
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32.0;

        double maxLegalX = mapWidth - w - safetyMargin;
        double maxLegalY = mapHeight - h - safetyMargin;

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
}

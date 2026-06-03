package Project1Game.component.farming.monster;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import Project1Game.core.EntityType;
import Project1Game.component.farming.animal.BaseAnimalComponent;
import Project1Game.component.npc.NPCBehaviorComponent;
import Project1Game.Main;
import javafx.geometry.Point2D;

import java.util.List;
import java.util.ArrayList;

/**
 * AI logic for monsters that spawn from bushes.
 * Chases players, NPCs, and animals using A* pathfinding, and returns to a bush
 * to disappear after 10 seconds.
 */
public class BushMonsterComponent extends Component {

    private enum State {
        CHASING, RETURNING
    }

    private State state = State.CHASING;
    private double lifeTimer = 10.0; // Chases for 10 seconds, then returns to a bush
    private double pathTimer = 0.0;
    private double damageCooldown = 0.0;
    private double targetScanTimer = 0.0;

    private Entity targetEntity = null;
    private List<Point2D> pathWaypoints = new ArrayList<>();
    private PhysicsComponent physics;
    private double speed = 60.0; // Slightly faster chase speed

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        // Find initial target
        targetEntity = findClosestTarget();
        recalculatePath();
    }

    @Override
    public void onUpdate(double tpf) {
        lifeTimer -= tpf;
        if (damageCooldown > 0) {
            damageCooldown -= tpf;
        }

        if (state == State.CHASING) {
            if (lifeTimer <= 0) {
                state = State.RETURNING;
                targetEntity = findClosestBush();
                recalculatePath();
                // FXGL.getNotificationService().pushNotification("Quái vật đang quay trở lại bụi cây!");
                return;
            }

            // Target scanning
            targetScanTimer += tpf;
            if (targetScanTimer >= 0.5) {
                targetScanTimer = 0.0;
                Entity newTarget = findClosestTarget();
                if (newTarget != targetEntity) {
                    targetEntity = newTarget;
                    recalculatePath();
                }
            }

            if (targetEntity == null || !targetEntity.isActive()) {
                targetEntity = findClosestTarget();
                recalculatePath();
            }

            // Pathfinding recalculation timer
            pathTimer += tpf;
            if (pathTimer >= 1.0) {
                pathTimer = 0.0;
                recalculatePath();
            }

            // Follow path and check attack
            followPath(tpf, speed);
            checkAttack();

        } else if (state == State.RETURNING) {
            if (targetEntity == null || !targetEntity.isActive()) {
                targetEntity = findClosestBush();
                recalculatePath();
            }

            // If there are no bushes at all, disappear immediately
            if (targetEntity == null) {
                entity.removeFromWorld();
                return;
            }

            // Pathfinding recalculation timer
            pathTimer += tpf;
            if (pathTimer >= 1.0) {
                pathTimer = 0.0;
                recalculatePath();
            }

            followPath(tpf, speed * 0.8);

            // If close to the bush, disappear
            if (entity.distance(targetEntity) < 32.0) {
                entity.removeFromWorld();
                System.out.println("Bush monster returned to bush and disappeared.");
            }
        }
    }

    private void followPath(double tpf, double currentSpeed) {
        if (pathWaypoints == null || pathWaypoints.isEmpty()) {
            if (targetEntity != null) {
                Point2D dir = targetEntity.getPosition().subtract(entity.getPosition()).normalize();
                move(dir.multiply(currentSpeed));
            } else {
                move(Point2D.ZERO);
            }
            return;
        }

        Point2D currentWaypoint = pathWaypoints.get(0);
        double distToWaypoint = entity.getPosition().distance(currentWaypoint);

        if (distToWaypoint < 12.0) {
            pathWaypoints.remove(0);
            if (pathWaypoints.isEmpty()) {
                move(Point2D.ZERO);
                return;
            }
            currentWaypoint = pathWaypoints.get(0);
        }

        Point2D dir = currentWaypoint.subtract(entity.getPosition()).normalize();
        move(dir.multiply(currentSpeed));
    }

    private void move(Point2D velocity) {
        if (physics != null && physics.getBody() != null) {
            physics.setVelocityX(velocity.getX());
            physics.setVelocityY(velocity.getY());
        } else {
            entity.translate(velocity.multiply(FXGL.tpf()));
        }
    }

    private void checkAttack() {
        if (targetEntity == null || damageCooldown > 0) {
            return;
        }

        double dist = entity.distance(targetEntity);
        if (dist < 50.0) {
            if (targetEntity.getType() == EntityType.ANIMAL) {
                // Consume Animal
                targetEntity.removeFromWorld();
                Project1Game.Main.pushNotification("Quái vật đã ăn thịt động vật của bạn!");
                targetEntity = null;
                damageCooldown = 1.5;
            }
        }
    }

    private void recalculatePath() {
        if (targetEntity == null) {
            pathWaypoints.clear();
            return;
        }

        double mapW = 3520;
        double mapH = 2048;
        double maxW = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                .stream()
                .mapToDouble(e -> e.getRightX()).max().orElse(3520);
        double maxH = FXGL.getGameWorld()
                .getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                .stream()
                .mapToDouble(e -> e.getBottomY()).max().orElse(2048);
        mapW = Math.max(mapW, maxW);
        mapH = Math.max(mapH, maxH);

        this.pathWaypoints = Project1Game.system.AStarPathfinder.findPath(entity.getPosition(),
                targetEntity.getPosition(), mapW, mapH);

        if (this.pathWaypoints.isEmpty()) {
            this.pathWaypoints = new ArrayList<>();
            this.pathWaypoints.add(targetEntity.getPosition());
        }
    }

    private Entity findClosestTarget() {
        Entity closest = null;
        double minDist = Double.MAX_VALUE;

        // Only scan Animals
        List<Entity> animals = FXGL.getGameWorld().getEntitiesByType(EntityType.ANIMAL);
        for (Entity animal : animals) {
            double dist = entity.distance(animal);
            if (dist < minDist) {
                minDist = dist;
                closest = animal;
            }
        }

        return closest;
    }

    private Entity findClosestBush() {
        List<Entity> bushes = FXGL.getGameWorld().getEntitiesByType(EntityType.BUSH);
        Entity closest = null;
        double minDist = Double.MAX_VALUE;
        for (Entity bush : bushes) {
            double dist = entity.distance(bush);
            if (dist < minDist) {
                minDist = dist;
                closest = bush;
            }
        }
        return closest;
    }

    public boolean isReturning() {
        return state == State.RETURNING;
    }
}

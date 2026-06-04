package Project1Game.component.farming.monster;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import Project1Game.core.EntityType;
import Project1Game.component.farming.animal.BaseAnimalComponent;
import Project1Game.component.farming.CropComponent;
import javafx.geometry.Point2D;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Base AI logic for monsters with steering, food hunting, anti-jitter hysteresis,
 * player intimidation, and pathfinding.
 */
public abstract class BaseMonsterComponent extends Component {

    public enum MonsterClassification {
        CARNIVORE, HERBIVORE
    }

    protected double fleeRadius;
    protected final MonsterClassification classification;
    
    protected boolean isAlerted = false;
    protected boolean isTemporary = false;
    protected double lifeTimer = -1.0;

    protected double pathTimer = 0.0;
    protected double damageCooldown = 0.0;
    protected double targetScanTimer = 0.0;

    protected Entity targetEntity = null;
    protected List<Point2D> pathWaypoints = new ArrayList<>();
    protected PhysicsComponent physics;
    protected double baseSpeed = 50.0;
    protected double escapeSpeed = 80.0;

    protected Point2D initialSpawnPos;
    protected double wanderTimer = 0.0;
    protected double wanderDuration = 0.0;
    protected Point2D wanderDir = Point2D.ZERO;
    protected Random random = new Random();

    // Returning to bush state for temporary monsters
    protected boolean isReturning = false;

    protected BaseMonsterComponent(double fleeRadius, MonsterClassification classification) {
        this.fleeRadius = fleeRadius;
        this.classification = classification;
    }

    public void setTemporary(double duration) {
        this.isTemporary = true;
        this.lifeTimer = duration;
    }

    @Override
    public void onAdded() {
        physics = entity.getComponentOptional(PhysicsComponent.class).orElse(null);
        initialSpawnPos = entity.getPosition();
        targetEntity = findClosestTarget();
        recalculatePath();
    }

    @Override
    public void onUpdate(double tpf) {
        if (damageCooldown > 0) {
            damageCooldown -= tpf;
        }

        // Handle temporary monster life limit (returns to bush after limit)
        if (isTemporary && !isReturning) {
            lifeTimer -= tpf;
            if (lifeTimer <= 0) {
                isReturning = true;
                targetEntity = findClosestBush();
                recalculatePath();
                // FXGL.getNotificationService().pushNotification("Quái vật đang quay trở lại bụi cây!");
            }
        }

        // Retrieve player entity safely
        Entity player = null;
        List<Entity> players = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER);
        if (!players.isEmpty()) {
            player = players.get(0);
        }

        // Hysteresis player alert logic
        if (player != null) {
            double distToPlayer = entity.getPosition().distance(player.getPosition());
            if (!isAlerted) {
                if (distToPlayer < fleeRadius) {
                    isAlerted = true;
                    // Reset targeting when alerted
                    targetEntity = null;
                    pathWaypoints.clear();
                }
            } else {
                if (distToPlayer > fleeRadius + 100.0) {
                    isAlerted = false;
                }
            }
        } else {
            isAlerted = false;
        }

        // AI behavior state branch
        if (isReturning) {
            handleReturningState(tpf);
        } else if (isAlerted && player != null) {
            handleAlertedState(tpf, player);
        } else {
            handleNormalSeekingState(tpf);
        }
    }

    private void handleReturningState(double tpf) {
        if (targetEntity == null || !targetEntity.isActive()) {
            targetEntity = findClosestBush();
            recalculatePath();
        }

        if (targetEntity == null) {
            entity.removeFromWorld();
            return;
        }

        pathTimer += tpf;
        if (pathTimer >= 1.0) {
            pathTimer = 0.0;
            recalculatePath();
        }

        followPath(tpf, baseSpeed);

        if (entity.distance(targetEntity) < 32.0) {
            entity.removeFromWorld();
            System.out.println("Monster returned to bush and vanished.");
        }
    }

    private void handleAlertedState(double tpf, Entity player) {
        // Player intimidation / chasing away
        boolean playerMovingTowards = false;
        PhysicsComponent playerPhys = player.getComponentOptional(PhysicsComponent.class).orElse(null);
        if (playerPhys != null) {
            Point2D pVel = new Point2D(playerPhys.getVelocityX(), playerPhys.getVelocityY());
            if (pVel.magnitude() > 5.0) {
                Point2D toMonster = entity.getPosition().subtract(player.getPosition());
                if (toMonster.magnitude() > 0.01) {
                    double dot = pVel.normalize().dotProduct(toMonster.normalize());
                    if (dot > 0.1) {
                        playerMovingTowards = true;
                    }
                }
            }
        }

        double currentSpeed = escapeSpeed;
        if (playerMovingTowards) {
            currentSpeed *= 1.5; // 1.5x panic speed multiplier
        }

        Point2D fleeDir = entity.getPosition().subtract(player.getPosition());
        if (fleeDir.magnitude() > 0.01) {
            fleeDir = fleeDir.normalize();
        } else {
            fleeDir = new Point2D(1, 0); // fallback
        }

        Point2D velocity = fleeDir.multiply(currentSpeed);
        if (isMovementBlocked(velocity)) {
            // Try sliding angles
            Point2D alt1 = rotateVector(fleeDir, 45).multiply(currentSpeed);
            Point2D alt2 = rotateVector(fleeDir, -45).multiply(currentSpeed);
            if (!isMovementBlocked(alt1)) {
                velocity = alt1;
            } else if (!isMovementBlocked(alt2)) {
                velocity = alt2;
            } else {
                velocity = Point2D.ZERO; // Vigilant Idle (Stay Still)
            }
        }

        move(velocity);
    }

    private void handleNormalSeekingState(double tpf) {
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

        if (targetEntity != null) {
            pathTimer += tpf;
            if (pathTimer >= 1.0) {
                pathTimer = 0.0;
                recalculatePath();
            }
            followPath(tpf, baseSpeed);
            checkAttack();
        } else {
            // No targets left in world, wander around initial spawn point
            handleWandering(tpf);
        }
    }

    private void handleWandering(double tpf) {
        wanderTimer += tpf;
        if (wanderTimer >= wanderDuration) {
            wanderTimer = 0.0;
            wanderDuration = 1.5 + random.nextDouble() * 2.0;

            if (random.nextDouble() < 0.3) {
                wanderDir = Point2D.ZERO;
            } else {
                double angle = random.nextDouble() * 2 * Math.PI;
                wanderDir = new Point2D(Math.cos(angle), Math.sin(angle)).normalize().multiply(baseSpeed * 0.6);
            }
        }

        if (wanderDir.magnitude() > 0) {
            // Keep wandering within 300px around initialSpawnPos
            double nextX = entity.getX() + wanderDir.getX() * tpf;
            double nextY = entity.getY() + wanderDir.getY() * tpf;
            boolean outOfBounds = false;
            if (initialSpawnPos != null) {
                if (Math.abs(nextX - initialSpawnPos.getX()) > 300.0 || Math.abs(nextY - initialSpawnPos.getY()) > 300.0) {
                    outOfBounds = true;
                }
            }
            if (outOfBounds || isMovementBlocked(wanderDir)) {
                wanderDir = Point2D.ZERO;
                wanderTimer = wanderDuration; // Force recalculation
                move(Point2D.ZERO);
            } else {
                move(wanderDir);
            }
        } else {
            move(Point2D.ZERO);
        }
    }

    private void followPath(double tpf, double speedToUse) {
        if (pathWaypoints == null || pathWaypoints.isEmpty()) {
            if (targetEntity != null) {
                Point2D dir = targetEntity.getPosition().subtract(entity.getPosition());
                if (dir.magnitude() > 0.01) dir = dir.normalize();
                move(dir.multiply(speedToUse));
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

        Point2D dir = currentWaypoint.subtract(entity.getPosition());
        if (dir.magnitude() > 0.01) dir = dir.normalize();
        move(dir.multiply(speedToUse));
    }

    protected void move(Point2D velocity) {
        if (physics != null && physics.getBody() != null) {
            physics.setVelocityX(velocity.getX());
            physics.setVelocityY(velocity.getY());
        } else {
            entity.translate(velocity.multiply(FXGL.tpf()));
        }
    }

    protected void checkAttack() {
        if (targetEntity == null || damageCooldown > 0) {
            return;
        }

        double dist = entity.distance(targetEntity);
        if (dist < 48.0) {
            BaseAnimalComponent bac = targetEntity.getComponentOptional(BaseAnimalComponent.class).orElse(null);
            if (classification == MonsterClassification.CARNIVORE && targetEntity.isType(EntityType.ANIMAL) && (bac == null || !bac.isFollowing())) {
                targetEntity.removeFromWorld();
                Project1Game.Main.pushNotification("Quái vật đã ăn thịt động vật của bạn!");
                targetEntity = null;
                damageCooldown = 2.0;
            } else if (classification == MonsterClassification.HERBIVORE && targetEntity.getType() instanceof EntityType && isCrop((EntityType) targetEntity.getType())) {
                targetEntity.removeFromWorld();
                Project1Game.Main.pushNotification("Quái vật đã phá hoại mùa màng của bạn!");
                targetEntity = null;
                damageCooldown = 2.0;
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

    protected Entity findClosestTarget() {
        Entity closest = null;
        double minDist = Double.MAX_VALUE;

        if (classification == MonsterClassification.CARNIVORE) {
            List<Entity> animals = FXGL.getGameWorld().getEntitiesByType(EntityType.ANIMAL);
            for (Entity animal : animals) {
                BaseAnimalComponent bac = animal.getComponentOptional(BaseAnimalComponent.class).orElse(null);
                if (bac != null && bac.isFollowing()) {
                    continue;
                }
                double dist = entity.distance(animal);
                if (dist < minDist) {
                    minDist = dist;
                    closest = animal;
                }
            }
        } else if (classification == MonsterClassification.HERBIVORE) {
            for (EntityType type : EntityType.values()) {
                if (isCrop(type)) {
                    List<Entity> crops = FXGL.getGameWorld().getEntitiesByType(type);
                    for (Entity crop : crops) {
                        double dist = entity.distance(crop);
                        if (dist < minDist) {
                            minDist = dist;
                            closest = crop;
                        }
                    }
                }
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

    private boolean isCrop(EntityType type) {
        return type == EntityType.WHEAT || type == EntityType.RADISH || type == EntityType.CABBAGE
                || type == EntityType.GRAPE || type == EntityType.CUCUMBER || type == EntityType.PEPPER
                || type == EntityType.CAULIFLOWER || type == EntityType.BEAN || type == EntityType.PINEAPPLE
                || type == EntityType.SUNFLOWER || type == EntityType.COCONUT || type == EntityType.APPLE;
    }

    private boolean isMovementBlocked(Point2D velocity) {
        if (velocity.getX() == 0 && velocity.getY() == 0) {
            return false;
        }

        double nextX = entity.getX() + velocity.getX() * FXGL.tpf();
        double nextY = entity.getY() + velocity.getY() * FXGL.tpf();

        double mapW = 3520;
        double mapH = 2048;
        if (Project1Game.Main.getInstance() != null) {
            mapW = Project1Game.Main.getInstance().getCurrentMapWidth();
            mapH = Project1Game.Main.getInstance().getCurrentMapHeight();
        }
        if (nextX < 32 || nextX > mapW - 64 || nextY < 32 || nextY > mapH - 64) {
            return true;
        }

        double w = entity.getWidth() > 0 ? entity.getWidth() : 32;
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32;
        Point2D probePos = entity.getPosition().add(velocity.normalize().multiply(16.0));
        javafx.geometry.Rectangle2D nextBox = new javafx.geometry.Rectangle2D(probePos.getX(), probePos.getY(), w, h);

        List<Entity> obstacles = FXGL.getGameWorld().getEntitiesInRange(nextBox);
        for (Entity obs : obstacles) {
            if (obs.getType() == EntityType.WALL || obs.getType() == EntityType.COLLISION) {
                return true;
            }
        }

        return false;
    }

    private Point2D rotateVector(Point2D v, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Point2D(v.getX() * cos - v.getY() * sin, v.getX() * sin + v.getY() * cos);
    }

    public boolean isReturning() {
        return isReturning;
    }

    public boolean isAlerted() {
        return isAlerted;
    }
}

package Project1Game.component.farming.monster;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import Project1Game.core.EntityType;
import Project1Game.component.farming.animal.BaseAnimalComponent;
import Project1Game.component.common.SteeringComponent;
import Project1Game.system.NotificationManager;
import javafx.geometry.Point2D;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Base AI logic for monsters with steering, food hunting, anti-jitter hysteresis,
 * player intimidation, and pathfinding.
 * Optimized for O(1) boundary updates, cached lookups, and memory efficiency.
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
    protected SteeringComponent steering;
    protected double baseSpeed = 50.0;
    protected double escapeSpeed = 80.0;

    protected Point2D initialSpawnPos;
    protected Random random = new Random();
    protected double spawnProtectionTimer = 5.0;

    protected boolean isReturning = false;

    // Map size caching for O(1) boundary updates
    private double cachedMapWidth = 3520;
    private double cachedMapHeight = 2048;
    private static final double SAFETY_MARGIN = 64.0;

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

        // Setup SteeringComponent
        steering = entity.getComponentOptional(SteeringComponent.class).orElse(null);
        if (steering == null) {
            steering = new SteeringComponent();
            entity.addComponent(steering);
        }

        // Cache map size on spawn
        if (Project1Game.Main.getInstance() != null) {
            cachedMapWidth = Project1Game.Main.getInstance().getCurrentMapWidth();
            cachedMapHeight = Project1Game.Main.getInstance().getCurrentMapHeight();
        } else {
            double maxW = FXGL.getGameWorld()
                    .getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                    .stream()
                    .mapToDouble(e -> e.getRightX()).max().orElse(3520);
            double maxH = FXGL.getGameWorld()
                    .getEntitiesByType(EntityType.FIELD, EntityType.WALL, EntityType.COLLISION)
                    .stream()
                    .mapToDouble(e -> e.getBottomY()).max().orElse(2048);
            cachedMapWidth = Math.max(3520, maxW);
            cachedMapHeight = Math.max(2048, maxH);
        }

        targetEntity = findClosestTarget();
        recalculatePath();

        System.out.println("[BaseMonsterComponent] Added monster " + getClass().getSimpleName() + " at " + initialSpawnPos
                + " | isTemporary=" + isTemporary + ", lifeTimer=" + lifeTimer + ", spawnProtectionTimer=" + spawnProtectionTimer);
    }

    @Override
    public void onUpdate(double tpf) {
        if (entity == null || !entity.isActive()) {
            return;
        }

        // Cap tpf to avoid extreme jumps
        double dt = Math.min(tpf, 0.1);

        if (damageCooldown > 0) damageCooldown -= dt;
        if (spawnProtectionTimer > 0) spawnProtectionTimer -= dt;

        if (steering != null) {
            steering.updateCooldown(dt);
            if (steering.getCollisionCooldown() > 0) {
                return;
            }
        }

        if (isTemporary && !isReturning) {
            lifeTimer -= dt;
            if (lifeTimer <= 0) {
                isReturning = true;
                targetEntity = findClosestBush();
                recalculatePath();
                System.out.println("[BaseMonsterComponent] Monster " + getClass().getSimpleName() + " life expired. Returning to closest bush: " 
                        + (targetEntity != null ? targetEntity.getPosition() : "none") + " | spawnProtectionTimer=" + spawnProtectionTimer);
            }
        }

        Entity player = null;
        List<Entity> players = FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER);
        if (!players.isEmpty()) {
            player = players.get(0);
        }

        if (player != null) {
            double distToPlayer = entity.getPosition().distance(player.getPosition());
            if (!isAlerted) {
                if (distToPlayer < fleeRadius) {
                    isAlerted = true;
                    targetEntity = null;
                    pathWaypoints.clear();
                    if (steering != null) steering.clearPath();
                }
            } else {
                if (distToPlayer > fleeRadius + 100.0) {
                    isAlerted = false;
                }
            }
        } else {
            isAlerted = false;
        }

        if (isReturning) {
            handleReturningState(dt);
        } else if (isAlerted && player != null) {
            handleAlertedState(dt, player);
        } else {
            handleNormalSeekingState(dt);
        }

        // Keep entity strictly inside boundaries
        if (steering != null) {
            steering.clampToBoundaries(cachedMapWidth, cachedMapHeight, SAFETY_MARGIN);
        }
    }

    private void handleReturningState(double tpf) {
        if (targetEntity == null || !targetEntity.isActive()) {
            targetEntity = findClosestBush();
            recalculatePath();
        }

        if (targetEntity == null) {
            System.out.println("[BaseMonsterComponent] Removed monster " + getClass().getSimpleName() + " at " + entity.getPosition() + ": closest bush target is null.");
            entity.removeFromWorld();
            return;
        }

        pathTimer += tpf;
        if (pathTimer >= 1.0) {
            pathTimer = 0.0;
            recalculatePath();
        }

        followPath(tpf, baseSpeed);

        if (entity.getCenter().distance(targetEntity.getCenter()) < 16.0) {
            if (spawnProtectionTimer <= 0) {
                System.out.println("[BaseMonsterComponent] Removed monster " + getClass().getSimpleName() + " at " + entity.getPosition() 
                        + ": returned to bush " + targetEntity.getPosition() + " | spawnProtectionTimer=" + spawnProtectionTimer);
                entity.removeFromWorld();
            }
        }
    }

    private void handleAlertedState(double tpf, Entity player) {
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
            currentSpeed *= 1.5;
        }

        Point2D fleeDir = entity.getPosition().subtract(player.getPosition());
        if (fleeDir.magnitude() > 0.01) {
            fleeDir = fleeDir.normalize();
        } else {
            fleeDir = new Point2D(1, 0);
        }

        Point2D velocity = fleeDir.multiply(currentSpeed);
        if (steering != null && steering.isMovementBlocked(velocity, cachedMapWidth, cachedMapHeight, SAFETY_MARGIN)) {
            Point2D alt1 = rotateVector(fleeDir, 45).multiply(currentSpeed);
            Point2D alt2 = rotateVector(fleeDir, -45).multiply(currentSpeed);
            if (!steering.isMovementBlocked(alt1, cachedMapWidth, cachedMapHeight, SAFETY_MARGIN)) {
                velocity = alt1;
            } else if (!steering.isMovementBlocked(alt2, cachedMapWidth, cachedMapHeight, SAFETY_MARGIN)) {
                velocity = alt2;
            } else {
                velocity = Point2D.ZERO;
            }
        }

        if (steering != null) {
            steering.moveDirect(velocity, tpf);
        }
    }

    private void handleNormalSeekingState(double tpf) {
        // Skip animal targets currently following the player
        if (targetEntity != null && targetEntity.getType() == EntityType.ANIMAL) {
            BaseAnimalComponent bac = targetEntity.getComponentOptional(BaseAnimalComponent.class).orElse(null);
            if (bac != null && bac.isFollowing()) {
                targetEntity = null;
            }
        }

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

        boolean canSeekDirectly = targetEntity != null && entity.getCenter().distance(targetEntity.getCenter()) < 90.0;
        if (targetEntity != null && (!pathWaypoints.isEmpty() || canSeekDirectly)) {
            pathTimer += tpf;
            if (pathTimer >= 1.0) {
                pathTimer = 0.0;
                recalculatePath();
            }
            followPath(tpf, baseSpeed);
            checkAttack();
        } else {
            handleWandering(tpf);
        }
    }

    private void handleWandering(double tpf) {
        if (steering == null) return;
        
        steering.wander(tpf, baseSpeed * 0.6, initialSpawnPos, cachedMapWidth, cachedMapHeight);
    }

    private void followPath(double tpf, double speedToUse) {
        if (steering == null) return;

        if (pathWaypoints == null || pathWaypoints.isEmpty()) {
            if (targetEntity != null) {
                Point2D dir = targetEntity.getPosition().subtract(entity.getPosition());
                if (dir.magnitude() > 0.01) dir = dir.normalize();
                steering.moveDirect(dir.multiply(speedToUse), tpf);
            } else {
                steering.stop();
            }
            return;
        }

        steering.setPathWaypoints(pathWaypoints);
        steering.followPath(tpf, speedToUse);
        pathWaypoints = steering.getPathWaypoints();
    }

    protected void checkAttack() {
        if (targetEntity == null || damageCooldown > 0) {
            return;
        }

        double centerDist = entity.getCenter().distance(targetEntity.getCenter());
        double entityWidthSum = (entity.getWidth() * Math.abs(entity.getScaleX()) + targetEntity.getWidth() * Math.abs(targetEntity.getScaleX()));
        double attackRange = Math.max(80.0, entityWidthSum / 2.0 + 16.0);
        
        if (centerDist <= attackRange) {
            BaseAnimalComponent bac = targetEntity.getComponentOptional(BaseAnimalComponent.class).orElse(null);
            if (classification == MonsterClassification.CARNIVORE && targetEntity.getType() == EntityType.ANIMAL) {
                if (bac == null || !bac.isFollowing()) {
                    System.out.println("[BaseMonsterComponent] CARNIVORE " + getClass().getSimpleName() + " ate animal " + targetEntity + " at " + targetEntity.getPosition());
                    targetEntity.removeFromWorld();
                    NotificationManager.pushNotification("Cảnh báo: Quái vật đã ăn thịt động vật của bạn!");
                    targetEntity = null;
                    damageCooldown = 2.0;
                }
            } else if (classification == MonsterClassification.HERBIVORE && Project1Game.core.CropRegistry.getInstance().isCrop((EntityType) targetEntity.getType())) {
                System.out.println("[BaseMonsterComponent] HERBIVORE " + getClass().getSimpleName() + " destroyed crop " + targetEntity.getType() + " at " + targetEntity.getPosition());
                targetEntity.removeFromWorld();
                NotificationManager.pushNotification("Cảnh báo: Quái vật đã phá hoại mùa màng của bạn!");
                targetEntity = null;
                damageCooldown = 2.0;
            }
        }
    }

    private void recalculatePath() {
        if (targetEntity == null) {
            pathWaypoints.clear();
            if (steering != null) steering.clearPath();
            return;
        }

        double h = entity.getHeight() > 0 ? entity.getHeight() : 32.0;
        this.pathWaypoints = Project1Game.system.AStarPathfinder.findPath(entity.getPosition(),
                targetEntity.getPosition(), cachedMapWidth, cachedMapHeight, h);
        if (steering != null) {
            steering.setPathWaypoints(pathWaypoints);
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
                double dist = entity.getCenter().distance(animal.getCenter());
                if (dist < minDist) {
                    minDist = dist;
                    closest = animal;
                }
            }
        } else if (classification == MonsterClassification.HERBIVORE) {
            for (EntityType cropType : Project1Game.core.CropRegistry.getInstance().getSupportedCrops()) {
                List<Entity> crops = FXGL.getGameWorld().getEntitiesByType(cropType);
                for (Entity crop : crops) {
                    double dist = entity.getCenter().distance(crop.getCenter());
                    if (dist < minDist) {
                        minDist = dist;
                        closest = crop;
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

    public void forceNewDirection() {
        if (isAlerted || isReturning) {
            return;
        }
        if (steering != null) {
            steering.forceNewDirection(0.5);
        }
    }

    public double getCollisionCooldown() {
        return steering != null ? steering.getCollisionCooldown() : 0.0;
    }
}
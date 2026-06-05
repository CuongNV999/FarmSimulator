package Project1Game.component.farming.monster;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import Project1Game.core.EntityType;
import Project1Game.component.farming.animal.BaseAnimalComponent;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.EnumSet;

/**
 * Base AI logic for monsters with steering, food hunting, anti-jitter hysteresis,
 * player intimidation, and pathfinding.
 * * Optimized for O(1) boundary updates, cached lookups, and memory efficiency.
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
    protected double collisionCooldown = 0.0;

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
    protected double spawnProtectionTimer = 5.0;

    protected boolean isReturning = false;

    // Bộ đệm kích thước bản đồ tránh tính toán lại O(N) liên tục
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

        // Cache lại kích thước bản đồ một lần duy nhất khi quái xuất hiện
        if (Project1Game.Main.getInstance() != null) {
            cachedMapWidth = Project1Game.Main.getInstance().getCurrentMapWidth();
            cachedMapHeight = Project1Game.Main.getInstance().getCurrentMapHeight();
        } else {
            // Fallback an toàn nếu không lấy được từ Main instance
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

        // Cap tpf to avoid extreme jumps (pause, lag spikes, admin panel init)
        double dt = Math.min(tpf, 0.1);

        if (damageCooldown > 0) damageCooldown -= dt;
        if (spawnProtectionTimer > 0) spawnProtectionTimer -= dt;

        if (collisionCooldown > 0) {
            collisionCooldown -= dt;
            if (physics != null && physics.getBody() != null) {
                physics.setVelocityX(0);
                physics.setVelocityY(0);
            }
            return;
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

        // CHỐNG VĂNG MAP: Đồng bộ hóa toàn bộ tham số biên một cách nhất quán
        double w = entity.getWidth() > 0 ? entity.getWidth() : 32.0;
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32.0;

        double maxLegalX = cachedMapWidth - w - SAFETY_MARGIN;
        double maxLegalY = cachedMapHeight - h - SAFETY_MARGIN;

        if (entity.getX() < SAFETY_MARGIN) {
            entity.setX(SAFETY_MARGIN);
            if (physics != null) physics.setVelocityX(Math.abs(physics.getVelocityX()));
        }
        if (entity.getX() > maxLegalX) {
            entity.setX(maxLegalX);
            if (physics != null) physics.setVelocityX(-Math.abs(physics.getVelocityX()));
        }
        if (entity.getY() < SAFETY_MARGIN) {
            entity.setY(SAFETY_MARGIN);
            if (physics != null) physics.setVelocityY(Math.abs(physics.getVelocityY()));
        }
        if (entity.getY() > maxLegalY) {
            entity.setY(maxLegalY);
            if (physics != null) physics.setVelocityY(-Math.abs(physics.getVelocityY()));
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

        // Sử dụng center distance ổn định hơn để quay về bụi cây
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
        if (isMovementBlocked(velocity, tpf)) {
            Point2D alt1 = rotateVector(fleeDir, 45).multiply(currentSpeed);
            Point2D alt2 = rotateVector(fleeDir, -45).multiply(currentSpeed);
            if (!isMovementBlocked(alt1, tpf)) {
                velocity = alt1;
            } else if (!isMovementBlocked(alt2, tpf)) {
                velocity = alt2;
            } else {
                velocity = Point2D.ZERO;
            }
        }

        move(velocity, tpf);
    }

    private void handleNormalSeekingState(double tpf) {
        // Bỏ qua mục tiêu là động vật đang đi theo người chơi
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

        // Sử dụng khoảng cách tâm để tối ưu hóa va chạm / di chuyển tầm gần
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
            double nextX = entity.getX() + wanderDir.getX() * tpf;
            double nextY = entity.getY() + wanderDir.getY() * tpf;
            boolean outOfBounds = false;

            if (initialSpawnPos != null) {
                if (Math.abs(nextX - initialSpawnPos.getX()) > 800.0 || Math.abs(nextY - initialSpawnPos.getY()) > 800.0) {
                    outOfBounds = true;
                }
            }
            if (outOfBounds || isMovementBlocked(wanderDir, tpf)) {
                wanderDir = Point2D.ZERO;
                wanderTimer = wanderDuration;
                move(Point2D.ZERO, tpf);
            } else {
                move(wanderDir, tpf);
            }
        } else {
            move(Point2D.ZERO, tpf);
        }
    }

    private void followPath(double tpf, double speedToUse) {
        if (pathWaypoints == null || pathWaypoints.isEmpty()) {
            if (targetEntity != null) {
                Point2D dir = targetEntity.getPosition().subtract(entity.getPosition());
                if (dir.magnitude() > 0.01) dir = dir.normalize();
                move(dir.multiply(speedToUse), tpf);
            } else {
                move(Point2D.ZERO, tpf);
            }
            return;
        }

        Point2D currentWaypoint = pathWaypoints.get(0);
        double distToWaypoint = entity.getPosition().distance(currentWaypoint);

        if (distToWaypoint < 12.0) {
            pathWaypoints.remove(0);
            if (pathWaypoints.isEmpty()) {
                move(Point2D.ZERO, tpf);
                return;
            }
            currentWaypoint = pathWaypoints.get(0);
        }

        Point2D dir = currentWaypoint.subtract(entity.getPosition());
        if (dir.magnitude() > 0.01) dir = dir.normalize();
        move(dir.multiply(speedToUse), tpf);
    }

    protected void move(Point2D velocity) {
        move(velocity, FXGL.tpf());
    }

    protected void move(Point2D velocity, double dt) {
        if (physics != null && physics.getBody() != null) {
            physics.setVelocityX(velocity.getX());
            physics.setVelocityY(velocity.getY());
        } else {
            entity.translate(velocity.multiply(dt));
        }
    }

    protected void checkAttack() {
        if (targetEntity == null || damageCooldown > 0) {
            return;
        }

        double centerDist = entity.getCenter().distance(targetEntity.getCenter());
        
        // Tính toán khoảng cách tấn công động dựa trên kích thước của quái vật và con mồi
        double entityWidthSum = (entity.getWidth() * Math.abs(entity.getScaleX()) + targetEntity.getWidth() * Math.abs(targetEntity.getScaleX()));
        double attackRange = Math.max(80.0, entityWidthSum / 2.0 + 16.0);
        
        if (centerDist <= attackRange) {
            BaseAnimalComponent bac = targetEntity.getComponentOptional(BaseAnimalComponent.class).orElse(null);
            if (classification == MonsterClassification.CARNIVORE && targetEntity.getType() == EntityType.ANIMAL) {
                if (bac == null || !bac.isFollowing()) {
                    System.out.println("[BaseMonsterComponent] CARNIVORE " + getClass().getSimpleName() + " ate animal " + targetEntity + " at " + targetEntity.getPosition());
                    targetEntity.removeFromWorld();
                    Project1Game.Main.pushNotification("Cảnh báo: Quái vật đã ăn thịt động vật của bạn!");
                    targetEntity = null;
                    damageCooldown = 2.0;
                }
            } else if (classification == MonsterClassification.HERBIVORE && Project1Game.core.CropRegistry.getInstance().isCrop((EntityType) targetEntity.getType())) {
                System.out.println("[BaseMonsterComponent] HERBIVORE " + getClass().getSimpleName() + " destroyed crop " + targetEntity.getType() + " at " + targetEntity.getPosition());
                targetEntity.removeFromWorld();
                Project1Game.Main.pushNotification("Cảnh báo: Quái vật đã phá hoại mùa màng của bạn!");
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

        // TỐI ƯU HÓA TỐC ĐỘ: Sử dụng kích thước bản đồ đã được lưu trong bộ đệm (O(1)) và truyền chiều cao thực thể
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32.0;
        this.pathWaypoints = Project1Game.system.AStarPathfinder.findPath(entity.getPosition(),
                targetEntity.getPosition(), cachedMapWidth, cachedMapHeight, h);
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
            // TỐI ƯU HÓA TỐC ĐỘ: Duyệt trực thế giới một lần rồi lọc bằng bộ hash-set chuẩn O(1)
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

    private boolean isCrop(EntityType type) {
        return Project1Game.core.CropRegistry.getInstance().isCrop(type);
    }

    private boolean isMovementBlocked(Point2D velocity) {
        return isMovementBlocked(velocity, FXGL.tpf());
    }

    private boolean isMovementBlocked(Point2D velocity, double dt) {
        if (velocity.getX() == 0 && velocity.getY() == 0) {
            return false;
        }

        double nextX = entity.getX() + velocity.getX() * dt;
        double nextY = entity.getY() + velocity.getY() * dt;

        // Đồng bộ hóa nghiêm ngặt khoảng đệm với SAFETY_MARGIN để chống Jittering
        if (nextX < SAFETY_MARGIN || nextX > cachedMapWidth - SAFETY_MARGIN || nextY < SAFETY_MARGIN || nextY > cachedMapHeight - SAFETY_MARGIN) {
            return true;
        }

        double w = entity.getWidth() > 0 ? entity.getWidth() : 32;
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32;

        // Đo đạc va chạm thông minh
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

    private Point2D rotateVector(Point2D v, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Point2D(v.getX() * cos - v.getY() * sin, v.getX() * sin + v.getY() * cos);
    }

    public boolean isReturning() {
        return isReturning;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public double getLifeTimer() {
        return lifeTimer;
    }

    public void setReturning(boolean returning) {
        this.isReturning = returning;
    }

    public double getSpawnProtectionTimer() {
        return spawnProtectionTimer;
    }

    public boolean isAlerted() {
        return isAlerted;
    }

    public void forceNewDirection() {
        if (isAlerted || isReturning) {
            return;
        }

        collisionCooldown = 0.5;
        wanderDir = Point2D.ZERO;
        if (physics != null && physics.getBody() != null) {
            physics.setVelocityX(0);
            physics.setVelocityY(0);
        }

        wanderTimer = 0;
        wanderDuration = collisionCooldown;
    }

    public double getCollisionCooldown() {
        return collisionCooldown;
    }
}
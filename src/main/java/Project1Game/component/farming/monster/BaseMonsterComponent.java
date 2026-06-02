package Project1Game.component.farming.monster;

import Project1Game.core.EntityType;
import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.animal.BaseAnimalComponent;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import java.util.List;
import java.util.Random;

/**
 * Base AI logic component for monsters.
 * Moves through PhysicsComponent and avoids fences/ponds/trees using 150px proximity lookups.
 */
public abstract class BaseMonsterComponent extends Component {
    public enum MonsterGroup {
        CARNIVORE, HERBIVORE
    }

    public final MonsterGroup group;
    protected final double fleeRadius = 200.0;
    protected final double detectRadius = 400.0;

    protected double wanderTimer = 0;
    protected double wanderDuration = 0;
    protected Point2D moveDir = Point2D.ZERO;
    protected final Random random = new Random();

    protected PhysicsComponent physics;
    protected double speed = 40.0;

    private double scanTimer = 0.5;
    private Entity cachedFood = null;

    public BaseMonsterComponent(MonsterGroup group) {
        this.group = group;
    }

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
    }

    public abstract boolean isValidPrey(Entity entity);
    public abstract void consume(Entity target);

    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
        double distToPlayer = player != null ? entity.distance(player) : Double.MAX_VALUE;

        if (distToPlayer < fleeRadius) {
            // STATE 1: FLEE PLAYER
            fleePlayer(player, tpf);
            cachedFood = null;
        } else {
            scanTimer += tpf;
            if (scanTimer >= 0.5) {
                scanTimer = 0;
                cachedFood = findClosestFood();
            }

            if (cachedFood != null && !cachedFood.isActive()) {
                cachedFood = null;
            }

            if (cachedFood != null) {
                // STATE 2: SEEK FOOD
                seekFood(cachedFood, tpf);
            } else {
                // STATE 3: WANDER
                wander(tpf);
            }
        }
    }

    protected void fleePlayer(Entity player, double tpf) {
        Point2D dir = entity.getPosition().subtract(player.getPosition()).normalize();
        double runSpeed = speed * 2.2;
        moveDir = dir;
        move(moveDir.multiply(runSpeed));
    }

    protected void seekFood(Entity food, double tpf) {
        Point2D dir = food.getPosition().subtract(entity.getPosition()).normalize();
        moveDir = dir;
        move(moveDir.multiply(speed * 1.3));
    }

    protected void wander(double tpf) {
        wanderTimer += tpf;
        if (wanderTimer >= wanderDuration) {
            wanderTimer = 0;
            wanderDuration = 2.0 + random.nextDouble() * 3.0; // 2 to 5 seconds

            if (random.nextDouble() < 0.25) {
                moveDir = Point2D.ZERO;
            } else {
                double angle = random.nextDouble() * 2 * Math.PI;
                moveDir = new Point2D(Math.cos(angle), Math.sin(angle));
            }
        }

        move(moveDir.multiply(speed * 0.7));
    }

    protected void move(Point2D velocity) {
        if (isMovementBlocked(velocity)) {
            moveDir = Point2D.ZERO;
            if (physics != null && physics.getBody() != null) {
                physics.setVelocityX(0);
                physics.setVelocityY(0);
            }
            wanderTimer = wanderDuration; // Force choice of new random direction next frame
        } else {
            if (physics != null && physics.getBody() != null) {
                physics.setVelocityX(velocity.getX());
                physics.setVelocityY(velocity.getY());
            } else {
                entity.translate(velocity.multiply(FXGL.tpf()));
            }
        }
    }

    private boolean isMovementBlocked(Point2D velocity) {
        if (velocity.getX() == 0 && velocity.getY() == 0) {
            return false;
        }

        double nextX = entity.getX() + velocity.getX() * FXGL.tpf();
        double nextY = entity.getY() + velocity.getY() * FXGL.tpf();

        double minX = 50;
        double maxX = 3470;
        double minY = 50;
        double maxY = 1998;

        if (nextX < minX || nextX > maxX || nextY < minY || nextY > maxY) {
            return true;
        }

        double w = entity.getWidth();
        double h = entity.getHeight();
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

    protected Entity findClosestFood() {
        Entity closest = null;
        double minDist = detectRadius;

        if (group == MonsterGroup.CARNIVORE) {
            List<Entity> animals = FXGL.getGameWorld().getEntitiesByType(EntityType.ANIMAL);
            for (Entity a : animals) {
                if (isValidPrey(a)) {
                    double d = entity.distance(a);
                    if (d < minDist) {
                        minDist = d;
                        closest = a;
                    }
                }
            }
        } else if (group == MonsterGroup.HERBIVORE) {
            List<Entity> crops = FXGL.getGameWorld().getEntitiesByComponent(CropComponent.class);
            for (Entity c : crops) {
                if (isValidPrey(c)) {
                    double d = entity.distance(c);
                    if (d < minDist) {
                        minDist = d;
                        closest = c;
                    }
                }
            }
        }
        return closest;
    }
}

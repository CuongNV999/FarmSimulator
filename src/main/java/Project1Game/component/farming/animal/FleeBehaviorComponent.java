package Project1Game.component.farming.animal;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import Project1Game.core.EntityType;
import Project1Game.system.SteeringComponent;
import Project1Game.system.AStarPathfinder;
import javafx.geometry.Point2D;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class FleeBehaviorComponent extends Component {
    private boolean isFleeing = false;
    private Point2D fleeTarget = null;
    private List<Point2D> fleePathWaypoints = new ArrayList<>();
    private double fleeCheckTimer = 0.0;
    private final Random random = new Random();

    private SteeringComponent steering;
    private AnimalAnimationComponent animation;

    public boolean isFleeing() {
        return isFleeing;
    }

    @Override
    public void onAdded() {
        steering = entity.getComponentOptional(SteeringComponent.class).orElse(null);
        animation = entity.getComponentOptional(AnimalAnimationComponent.class).orElse(null);
    }

    @Override
    public void onUpdate(double tpf) {
        if (steering == null || steering.getCollisionCooldown() > 0) return;

        fleeCheckTimer += tpf;
        if (fleeCheckTimer >= 0.3) {
            fleeCheckTimer = 0.0;
            checkForMonsters();
        }

        if (isFleeing) {
            handleFleeing(tpf);
        }
    }

    private void checkForMonsters() {
        boolean monsterNearby = false;
        Point2D monsterPos = null;

        List<Entity> monsters = FXGL.getGameWorld().getEntitiesByType(EntityType.MONSTER);
        for (Entity m : monsters) {
            Project1Game.component.farming.monster.BaseMonsterComponent bmc = 
                    m.getComponentOptional(Project1Game.component.farming.monster.BaseMonsterComponent.class).orElse(null);
            if (bmc != null && !bmc.isReturning()) {
                double dist = entity.distance(m);
                if (dist < 250.0) {
                    monsterNearby = true;
                    monsterPos = m.getPosition();
                    break;
                }
            }
        }

        if (monsterNearby) {
            if (!isFleeing) {
                isFleeing = true;
                chooseFleeTarget(monsterPos);
            }
        } else {
            if (isFleeing) {
                isFleeing = false;
                fleeTarget = null;
                fleePathWaypoints.clear();
                if (steering != null) {
                    steering.clearPath();
                }
                if (animation != null) {
                    animation.updateIdleAnimation();
                }
            }
        }
    }

    private void chooseFleeTarget(Point2D monsterPos) {
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

        Point2D candidate = null;
        for (int i = 0; i < 10; i++) {
            double rx = 100 + random.nextDouble() * (mapW - 200);
            double ry = 100 + random.nextDouble() * (mapH - 200);
            Point2D pt = new Point2D(rx, ry);
            if (monsterPos == null || pt.distance(monsterPos) > 400.0) {
                candidate = pt;
                break;
            }
        }
        if (candidate == null) {
            candidate = new Point2D(100 + random.nextDouble() * (mapW - 200), 100 + random.nextDouble() * (mapH - 200));
        }

        fleeTarget = candidate;
        double h = entity.getHeight() > 0 ? entity.getHeight() : 32.0;
        fleePathWaypoints = AStarPathfinder.findPath(entity.getPosition(), fleeTarget, mapW, mapH, h);
        if (fleePathWaypoints.isEmpty()) {
            fleePathWaypoints = new ArrayList<>();
            fleePathWaypoints.add(fleeTarget);
        }
        if (steering != null) {
            steering.setPathWaypoints(fleePathWaypoints);
        }
    }

    private void handleFleeing(double tpf) {
        if (steering == null) return;
        
        if (fleeTarget == null || fleePathWaypoints == null || fleePathWaypoints.isEmpty()) {
            chooseFleeTarget(null);
            return;
        }

        double fleeSpeed = 60.0;
        steering.followPath(tpf, fleeSpeed);
        fleePathWaypoints = steering.getPathWaypoints();

        if (fleePathWaypoints.isEmpty()) {
            chooseFleeTarget(null);
            return;
        }

        Point2D currentWaypoint = fleePathWaypoints.get(0);
        Point2D dir = currentWaypoint.subtract(entity.getPosition());
        if (dir.magnitude() > 0.01) {
            if (animation != null) {
                animation.updateWalkAnimation(dir.normalize());
            }
        }
    }
}

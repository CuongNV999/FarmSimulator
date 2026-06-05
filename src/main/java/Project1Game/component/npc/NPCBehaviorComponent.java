package Project1Game.component.npc;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import com.almasb.fxgl.entity.components.CollidableComponent;
import Project1Game.interaction.Interactable;
import Project1Game.interaction.InteractableComponent;
import com.almasb.fxgl.entity.Entity;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class NPCBehaviorComponent extends Component implements Interactable {
    
    public enum NPCActivityState {
        ROAMING,
        MOVING_TO_HOUSE,
        MOVING_TO_WORK,
        FLEEING
    }

    private PhysicsComponent physics;
    private NPCAnimationComponent animation;

    private Point2D spawnPosition;
    private Point2D target = null;
    private NPCActivityState state = NPCActivityState.ROAMING;
    private double speed = 15; // NPC moves slightly slower for natural look

    private final Random random = new Random();

    // Pathfinding waypoints
    private List<Point2D> pathWaypoints = new ArrayList<>();

    // Fleeing variables
    private Point2D fleeTarget = null;
    private List<Point2D> fleePathWaypoints = new ArrayList<>();
    private double fleeCheckTimer = 0.0;

    // Visibility/Active state
    private boolean isHidden = false;

    // Static list to track hidden NPCs that were removed from the world
    private static final List<com.almasb.fxgl.entity.Entity> hiddenNPCs = new java.util.ArrayList<>();

    public static List<com.almasb.fxgl.entity.Entity> getHiddenNPCs() {
        return hiddenNPCs;
    }

    public static void clearHiddenNPCs() {
        hiddenNPCs.clear();
    }

    private com.almasb.fxgl.entity.Entity homeDoorEntity = null;

    private boolean checkIntersection(com.almasb.fxgl.entity.Entity a, com.almasb.fxgl.entity.Entity b) {
        if (a == null || b == null) return false;
        double ax = a.getX();
        double ay = a.getY();
        double aw = a.getWidth() > 0 ? a.getWidth() : 32;
        double ah = a.getHeight() > 0 ? a.getHeight() : 64;
        
        double bx = b.getX();
        double by = b.getY();
        double bw = b.getWidth() > 0 ? b.getWidth() : 32;
        double bh = b.getHeight() > 0 ? b.getHeight() : 32;
        
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);
        if (entity.hasComponent(NPCAnimationComponent.class)) {
            animation = entity.getComponent(NPCAnimationComponent.class);
        }
        if (spawnPosition == null) {
            spawnPosition = entity.getPosition();
        }

        // Ensure name properties are set correctly after map loader initialization
        if (entity.isType(Project1Game.core.EntityType.GUIDER)) {
            entity.setProperty("name", "Bác Nông Dân");
        } else if (entity.isType(Project1Game.core.EntityType.TRADER)) {
            entity.setProperty("name", "Trader");
        }
        entity.addComponent(new InteractableComponent(this));
    }

    private boolean firstFrame = true;

    private double stuckTimer = 0.0;
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

            // Check if spawned during night time (8:00 PM to 6:00 AM)
            if (Project1Game.Main.getInstance() != null) {
                Project1Game.system.TimeSystem ts = Project1Game.Main.getInstance().getTimeSystem();
                if (ts != null) {
                    int currentHour = ts.getHour();
                    if (currentHour >= 20 || currentHour < 6) {
                        disappear();
                        return;
                    }
                }
            }
        }

        if (isHidden) {
            return;
        }

        // Fleeing detection & update
        fleeCheckTimer += tpf;
        if (fleeCheckTimer >= 0.3) {
            fleeCheckTimer = 0.0;
            checkForMonsters();
        }

        // Cập nhật trạng thái di chuyển cho animation
        if (physics != null && animation != null) {
            boolean moving = Math.abs(physics.getVelocityX()) > 1.0 || Math.abs(physics.getVelocityY()) > 1.0;
            animation.setMoving(moving);
        }

        // Dispatch states
        switch (state) {
            case FLEEING:
                handleFleeing(tpf);
                break;
            case MOVING_TO_HOUSE:
                handleMovingToHouse(tpf);
                break;
            case MOVING_TO_WORK:
                handleMovingToWork(tpf);
                break;
            case ROAMING:
            default:
                handleRoaming(tpf);
                break;
        }
    }

    private void handleMovingToHouse(double tpf) {
        if (target == null) return;
        
        totalHomeTimer += tpf;
        if (totalHomeTimer > 25.0) {
            disappear();
            return;
        }

        boolean touchesDoor = false;
        if (homeDoorEntity != null) {
            touchesDoor = checkIntersection(entity, homeDoorEntity)
                       || entity.getCenter().distance(homeDoorEntity.getCenter()) < 32
                       || entity.getPosition().distance(target) < 30;
        } else {
            touchesDoor = entity.getPosition().distance(target) < 30;
        }

        if (touchesDoor) {
            disappear();
            return;
        }

        double distance = entity.getPosition().distance(target);

        if (distance < 60) {
            if (entity.hasComponent(CollidableComponent.class)) {
                entity.getComponent(CollidableComponent.class).setValue(false);
            }
        }

        if (distance > 15) {
            if (pathWaypoints == null || pathWaypoints.isEmpty()) {
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

            if (distToWaypoint < 12.0) {
                pathWaypoints.remove(0);
                if (pathWaypoints.isEmpty()) {
                    physics.setVelocityX(0);
                    physics.setVelocityY(0);
                    return;
                }
                currentWaypoint = pathWaypoints.get(0);
            }

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
                if (!pathWaypoints.isEmpty()) {
                    pathWaypoints.remove(0);
                }
            }

            physics.setVelocityX(targetVelX);
            physics.setVelocityY(targetVelY);
            updateAnimation(animVelocity);

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
            disappear();
        }
    }

    private void handleMovingToWork(double tpf) {
        if (target == null) return;
        
        double distance = entity.getPosition().distance(target);

        if (distance < 60) {
            if (entity.hasComponent(CollidableComponent.class)) {
                entity.getComponent(CollidableComponent.class).setValue(false);
            }
        }

        if (distance > 15) {
            if (pathWaypoints == null || pathWaypoints.isEmpty()) {
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

            if (distToWaypoint < 12.0) {
                pathWaypoints.remove(0);
                if (pathWaypoints.isEmpty()) {
                    physics.setVelocityX(0);
                    physics.setVelocityY(0);
                    return;
                }
                currentWaypoint = pathWaypoints.get(0);
            }

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
                if (!pathWaypoints.isEmpty()) {
                    pathWaypoints.remove(0);
                }
            }

            physics.setVelocityX(targetVelX);
            physics.setVelocityY(targetVelY);
            updateAnimation(animVelocity);

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
            stopMoving();
            if (entity.hasComponent(CollidableComponent.class)) {
                entity.getComponent(CollidableComponent.class).setValue(true);
            }
            System.out.println("NPC " + entity.getString("name") + " đã hoàn thành đi bộ về nơi làm việc.");
        }
    }

    private void handleRoaming(double tpf) {
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

    public void goHome(com.almasb.fxgl.entity.Entity doorEntity) {
        this.homeDoorEntity = doorEntity;
        this.target = doorEntity.getPosition();
        this.state = NPCActivityState.MOVING_TO_HOUSE;
        this.totalHomeTimer = 0.0;
        this.stuckTimer = 0.0;
        recalculatePath();
        physics.setLinearVelocity(Point2D.ZERO);
    }

    public void goHome(Point2D doorPosition) {
        this.homeDoorEntity = null;
        this.target = doorPosition;
        this.state = NPCActivityState.MOVING_TO_HOUSE;
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
        this.state = NPCActivityState.ROAMING;
        physics.setVelocityX(0);
        physics.setVelocityY(0);
        pathWaypoints.clear();
        if (entity != null && entity.hasComponent(CollidableComponent.class)) {
            entity.getComponent(CollidableComponent.class).setValue(true);
        }
    }

    public void walkToWork() {
        this.target = spawnPosition;
        this.state = NPCActivityState.MOVING_TO_WORK;
        this.stuckTimer = 0.0;
        recalculatePath();
        physics.setLinearVelocity(Point2D.ZERO);
        System.out.println("NPC " + entity.getString("name") + " bắt đầu đi bộ về nơi làm việc tại " + spawnPosition);
    }

    public boolean isGoingHome() {
        return state == NPCActivityState.MOVING_TO_HOUSE;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void disappear() {
        stopMoving();
        isHidden = true;
        if (!hiddenNPCs.contains(entity)) {
            hiddenNPCs.add(entity);
        }
        entity.removeFromWorld(); // Safe delete from active game world
        System.out.println("NPC " + entity.getString("name") + " đã đi vào nhà và biến mất.");
    }

    public void reappear() {
        isHidden = false;
        
        Point2D spawnLoc = spawnPosition;
        if (homeDoorEntity != null) {
            spawnLoc = homeDoorEntity.getPosition();
        }
        
        if (physics != null) {
            physics.overwritePosition(spawnLoc);
        } else {
            entity.setPosition(spawnLoc);
        }
        
        entity.getViewComponent().setOpacity(1.0); // Make visible
        if (entity.hasComponent(CollidableComponent.class)) {
            entity.getComponent(CollidableComponent.class).setValue(true); // Re-enable collision
        }

        // Reset movement states & trigger walk to work
        state = NPCActivityState.ROAMING;

        walkToWork();
        System.out.println("NPC " + entity.getString("name") + " đã xuất hiện trở lại tại " + spawnLoc + " và đang đi bộ về nơi làm việc.");
    }

    private void checkForMonsters() {
        boolean monsterNearby = false;
        Point2D monsterPos = null;

        List<Entity> monsters = FXGL.getGameWorld().getEntitiesByType(Project1Game.core.EntityType.MONSTER);
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
            if (state != NPCActivityState.FLEEING) {
                state = NPCActivityState.FLEEING;
                chooseFleeTarget(monsterPos);
            }
        } else {
            if (state == NPCActivityState.FLEEING) {
                fleeTarget = null;
                fleePathWaypoints.clear();
                walkToWork();
            }
        }
    }

    private void chooseFleeTarget(Point2D monsterPos) {
        double mapW = 3520;
        double mapH = 2048;
        double maxW = FXGL.getGameWorld()
                .getEntitiesByType(Project1Game.core.EntityType.FIELD, Project1Game.core.EntityType.WALL, Project1Game.core.EntityType.COLLISION)
                .stream()
                .mapToDouble(e -> e.getRightX()).max().orElse(3520);
        double maxH = FXGL.getGameWorld()
                .getEntitiesByType(Project1Game.core.EntityType.FIELD, Project1Game.core.EntityType.WALL, Project1Game.core.EntityType.COLLISION)
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
        fleePathWaypoints = Project1Game.system.AStarPathfinder.findPath(entity.getPosition(), fleeTarget, mapW, mapH);
        if (fleePathWaypoints.isEmpty()) {
            fleePathWaypoints = new ArrayList<>();
            fleePathWaypoints.add(fleeTarget);
        }
    }

    private void handleFleeing(double tpf) {
        if (physics != null && animation != null) {
            boolean moving = Math.abs(physics.getVelocityX()) > 1.0 || Math.abs(physics.getVelocityY()) > 1.0;
            animation.setMoving(moving);
        }

        if (fleeTarget == null || fleePathWaypoints == null || fleePathWaypoints.isEmpty()) {
            chooseFleeTarget(null);
            return;
        }

        Point2D currentWaypoint = fleePathWaypoints.get(0);
        double distToWaypoint = entity.getPosition().distance(currentWaypoint);

        if (distToWaypoint < 12.0) {
            fleePathWaypoints.remove(0);
            if (fleePathWaypoints.isEmpty()) {
                chooseFleeTarget(null);
                return;
            }
            currentWaypoint = fleePathWaypoints.get(0);
        }

        double dx = currentWaypoint.getX() - entity.getX();
        double dy = currentWaypoint.getY() - entity.getY();
        double fleeSpeed = 40.0 * 15;

        double targetVelX = 0;
        double targetVelY = 0;
        Point2D animVelocity = Point2D.ZERO;

        if (Math.abs(dx) > 5) {
            targetVelX = Math.signum(dx) * fleeSpeed;
            animVelocity = new Point2D(Math.signum(dx) * 40.0, 0);
        } else if (Math.abs(dy) > 5) {
            targetVelY = Math.signum(dy) * fleeSpeed;
            animVelocity = new Point2D(0, Math.signum(dy) * 40.0);
        } else {
            if (!fleePathWaypoints.isEmpty()) {
                fleePathWaypoints.remove(0);
            }
        }

        physics.setVelocityX(targetVelX);
        physics.setVelocityY(targetVelY);
        updateAnimation(animVelocity);
    }

    @Override
    public void interact(Entity player, Entity target) {
        if (target.isType(Project1Game.core.EntityType.TRADER) && target.hasComponent(TraderComponent.class)) {
            target.getComponent(TraderComponent.class).interact(player, target);
        } else {
            String npcName = target.getProperties().keys().contains("name") ? target.getString("name") : "";
            Project1Game.quest.QuestGiver npc = Project1Game.quest.QuestManager.getInstance().getNPC(npcName);
            if (npc != null) {
                System.out.println("Tương tác với NPC: " + npcName);
                String text = npc.interact();
                
                boolean hasActiveOrCompleted = npc.getQuests().stream()
                        .anyMatch(q -> q.getStatus() == Project1Game.quest.QuestStatus.IN_PROGRESS || q.getStatus() == Project1Game.quest.QuestStatus.COMPLETED);
                
                if (!hasActiveOrCompleted) {
                    npc.acceptNextAvailableQuest();
                }
                
                npc.claimFirstCompleted(Project1Game.Main.getInstance().getInventory());
                
                Project1Game.Main.getInstance().getDialogView().setDialog(target.getString("name"), text.split("\n"));
                Project1Game.Main.getInstance().getDialogView().show();
                Project1Game.Main.getInstance().getToolbarView().updateSelection();
            }
        }
    }
}

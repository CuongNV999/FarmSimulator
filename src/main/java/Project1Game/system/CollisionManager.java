package Project1Game.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import Project1Game.core.EntityType;
import Project1Game.component.farming.animal.BaseAnimalComponent;
import Project1Game.component.farming.monster.BaseMonsterComponent;
import Project1Game.ui.view.dialog.DialogView;

public class CollisionManager {
    private Entity nearbyNPC = null;
    private Entity nearbyDoor = null;
    private Entity nearbySleep = null;

    public Entity getNearbyNPC() {
        return nearbyNPC;
    }

    public void setNearbyNPC(Entity nearbyNPC) {
        this.nearbyNPC = nearbyNPC;
    }

    public Entity getNearbyDoor() {
        return nearbyDoor;
    }

    public void setNearbyDoor(Entity nearbyDoor) {
        this.nearbyDoor = nearbyDoor;
    }

    public Entity getNearbySleep() {
        return nearbySleep;
    }

    public void setNearbySleep(Entity nearbySleep) {
        this.nearbySleep = nearbySleep;
    }

    public void clearNearby() {
        nearbyNPC = null;
        nearbyDoor = null;
        nearbySleep = null;
    }

    public void initCollisionHandlers(DialogView dialogView) {
        // Khởi tạo hệ thống vật lý và lắng nghe NPC qua System
        PhysicsSystem.init(new PhysicsSystem.NPCListener() {
            @Override
            public void onNPCNear(Entity npc) {
                nearbyNPC = npc;
            }

            @Override
            public void onNPCAway() {
                nearbyNPC = null;
            }
        }, dialogView);

        // Thêm CollisionHandler cho DOOR
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.DOOR) {
            @Override
            protected void onCollisionBegin(Entity player, Entity door) {
                nearbyDoor = door;
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity door) {
                nearbyDoor = null;
            }
        });

        // Handler cho việc đi ngủ
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.SLEEP) {
            @Override
            protected void onCollisionBegin(Entity player, Entity sleep) {
                nearbySleep = sleep;
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity sleep) {
                nearbySleep = null;
            }
        });

        // Dynamic collision avoidance (push prevention) for animals and monsters
        FXGL.getPhysicsWorld().addCollisionHandler(new CreatureAvoidanceHandler(EntityType.ANIMAL, EntityType.ANIMAL));
        FXGL.getPhysicsWorld().addCollisionHandler(new CreatureAvoidanceHandler(EntityType.ANIMAL, EntityType.MONSTER));
        FXGL.getPhysicsWorld().addCollisionHandler(new CreatureAvoidanceHandler(EntityType.MONSTER, EntityType.MONSTER));

        // Tránh chướng ngại vật cho động vật khi va chạm với các vật cản khác
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.COLLISION));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.WALL));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.PLAYER));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.NPC));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.GUIDER));
        FXGL.getPhysicsWorld().addCollisionHandler(new AnimalObstacleCollisionHandler(EntityType.TRADER));
    }

    private static class CreatureAvoidanceHandler extends CollisionHandler {
        public CreatureAvoidanceHandler(EntityType a, EntityType b) {
            super(a, b);
        }

        @Override
        protected void onCollision(Entity entityA, Entity entityB) {
            boolean shouldForceA = false;
            boolean shouldForceB = false;

            BaseAnimalComponent animalA = null;
            BaseMonsterComponent monsterA = null;
            if (entityA.isType(EntityType.ANIMAL)) {
                animalA = entityA.getComponentOptional(BaseAnimalComponent.class).orElse(null);
                if (animalA != null && animalA.getCollisionCooldown() <= 0) {
                    shouldForceA = true;
                }
            } else if (entityA.isType(EntityType.MONSTER)) {
                monsterA = entityA.getComponentOptional(BaseMonsterComponent.class).orElse(null);
                if (monsterA != null && monsterA.getCollisionCooldown() <= 0) {
                    shouldForceA = true;
                }
            }

            BaseAnimalComponent animalB = null;
            BaseMonsterComponent monsterB = null;
            if (entityB.isType(EntityType.ANIMAL)) {
                animalB = entityB.getComponentOptional(BaseAnimalComponent.class).orElse(null);
                if (animalB != null && animalB.getCollisionCooldown() <= 0) {
                    shouldForceB = true;
                }
            } else if (entityB.isType(EntityType.MONSTER)) {
                monsterB = entityB.getComponentOptional(BaseMonsterComponent.class).orElse(null);
                if (monsterB != null && monsterB.getCollisionCooldown() <= 0) {
                    shouldForceB = true;
                }
            }

            if (shouldForceA) {
                if (animalA != null)
                    animalA.forceNewDirection();
                else if (monsterA != null)
                    monsterA.forceNewDirection();
            }

            if (shouldForceB) {
                if (animalB != null)
                    animalB.forceNewDirection();
                else if (monsterB != null)
                    monsterB.forceNewDirection();
            }
        }
    }

    private static class AnimalObstacleCollisionHandler extends CollisionHandler {
        public AnimalObstacleCollisionHandler(EntityType obstacleType) {
            super(EntityType.ANIMAL, obstacleType);
        }

        @Override
        protected void onCollision(Entity animal, Entity obstacle) {
            BaseAnimalComponent animalComp = animal.getComponentOptional(BaseAnimalComponent.class).orElse(null);
            if (animalComp != null && animalComp.getCollisionCooldown() <= 0) {
                animalComp.forceNewDirection();
            }
        }
    }
}

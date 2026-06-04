package Project1Game.factory;

import Project1Game.component.farming.animal.BaseAnimalComponent;
import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.SoilComponent;
import Project1Game.component.farming.monster.*;
import Project1Game.component.player.PlayerComponent;
import Project1Game.component.npc.NPCAnimationComponent;
import Project1Game.component.npc.NPCBehaviorComponent;
import Project1Game.component.npc.TraderComponent; // Import TraderComponent
import Project1Game.core.EntityType;
import Project1Game.config.CropData;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.physics.*;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.almasb.fxgl.core.collection.UpdatableObjectProperty; // Import UpdatableObjectProperty
import Project1Game.interaction.InteractableComponent;
import Project1Game.Main;

/**
 * Factory quản lý việc tạo tất cả thực thể trong game.
 */
public class GameEntityFactory implements EntityFactory {

    /**
     * Xử lý các đối tượng không có Type trong Tiled để tránh crash game.
     */
    @Spawns("")
    public Entity spawnEmpty(SpawnData data) {
        System.err.println("Cảnh báo: Phát hiện đối tượng không có Type tại: " + data.getX() + "," + data.getY());
        return new Entity(); // Trả về entity rỗng thay vì để game crash
    }

    // ================= NHÂN VẬT CHÍNH =================
    @Spawns("Player")
    public Entity spawnPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().friction(0f).density(0.1f));
        physics.setBodyType(BodyType.DYNAMIC);

        // ĐÃ SỬA: Vì không gọi được trực tiếp từ physics, ta đợi vật lý khởi tạo xong
        // rồi truy cập vào lõi Body của Box2D để cài đặt lực cản phanh quán tính (Linear Damping)
        physics.setOnPhysicsInitialized(() -> {
            if (physics.getBody() != null) {
                physics.getBody().setLinearDamping(12.0f);   // Phanh quán tính trượt thẳng
                physics.getBody().setAngularDamping(10.0f);  // Phanh quán tính trượt vòng tròn
                physics.getBody().setFixedRotation(true);    // Khóa hướng nhìn thẳng đứng
            }
        });

        return FXGL.entityBuilder(data)
                .type(EntityType.PLAYER)
                .bbox(new HitBox(new javafx.geometry.Point2D(26, 23), BoundingShape.box(13, 26)))
                .zIndex(10)
                .with(physics)
                .with(new PlayerComponent())
                .collidable()
                .build();
    }

    // ================= NÔNG NGHIỆP =================
    @Spawns("Soil")
    public Entity spawnSoil(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.SOIL)
                .bbox(new HitBox(BoundingShape.box(32, 32)))
                .zIndex(0)
                .with(new SoilComponent())
                .build();
    }

    private Entity createCrop(SpawnData data, CropData cropInfo) {
        return FXGL.entityBuilder(data)
                .type(cropInfo.type)
                .bbox(new HitBox(BoundingShape.box(32, 32)))
                .zIndex(5)
                .with(new CropComponent(cropInfo))
                .build();
    }

    @Spawns("Wheat") public Entity spawnWheat(SpawnData d) { return createCrop(d, CropData.WHEAT); }
    @Spawns("Radish") public Entity spawnRadish(SpawnData d) { return createCrop(d, CropData.RADISH); }
    @Spawns("Cabbage") public Entity spawnCabbage(SpawnData d) { return createCrop(d, CropData.CABBAGE); }
    @Spawns("Grape") public Entity spawnGrape(SpawnData d) { return createCrop(d, CropData.GRAPE); }
    @Spawns("Cucumber") public Entity spawnCucumber(SpawnData d) { return createCrop(d, CropData.CUCUMBER); }
    @Spawns("Pepper") public Entity spawnPepper(SpawnData d) { return createCrop(d, CropData.PEPPER); }
    @Spawns("Cauliflower") public Entity spawnCauliflower(SpawnData d) { return createCrop(d, CropData.CAULIFLOWER); }
    @Spawns("Bean") public Entity spawnBean(SpawnData d) { return createCrop(d, CropData.BEAN); }
    @Spawns("Pineapple") public Entity spawnPineapple(SpawnData d) { return createCrop(d, CropData.PINEAPPLE); }
    @Spawns("Sunflower") public Entity spawnSunflower(SpawnData d) { return createCrop(d, CropData.SUNFLOWER); }
    @Spawns("Coconut") public Entity spawnCoconut(SpawnData d) { return createCrop(d, CropData.COCONUT); }
    @Spawns("Apple") public Entity spawnApple(SpawnData d) { return createCrop(d, CropData.APPLE); }

    // ================= MÔI TRƯỜNG & VẬT CẢN (SỬ DỤNG NUMBER CASTING) =================

    @Spawns("Wall")
    public Entity spawnWall(SpawnData data) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32.0;

        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .with(new PhysicsComponent())
                .collidable()
                .build();
    }

    @Spawns("Field")
    public Entity spawnField(SpawnData data) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32.0;

        return FXGL.entityBuilder(data).type(EntityType.FIELD)
                .bbox(new HitBox(BoundingShape.box(w, h))).build();
    }

    @Spawns("Collisions")
    public Entity spawnCollisions(SpawnData data) {
        double w = data.hasKey("width") ? ((Number)data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number)data.get("height")).doubleValue() : 32.0;

        return FXGL.entityBuilder(data)
                .type(EntityType.COLLISION)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .with(new PhysicsComponent())
                .collidable()
                .build();
    }

    @Spawns("Interaction")
    public Entity spawnInteraction(SpawnData data) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32.0;

        return FXGL.entityBuilder(data)
                .type(EntityType.INTERACTION)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .collidable()
                .build();
    }

    @Spawns("Door")
    public Entity spawnDoor(SpawnData data) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32.0;

        String targetMapValue = data.hasKey("targetMap") ? (String) data.get("targetMap") : "default_map.tmx";

        double targetXValue = 0.0;
        if (data.hasKey("targetX")) {
            Object obj = data.get("targetX");
            if (obj instanceof UpdatableObjectProperty) {
                obj = ((UpdatableObjectProperty<?>) obj).getValue();
            }
            if (obj instanceof Number) {
                targetXValue = ((Number) obj).doubleValue();
            } else {
                try { targetXValue = Double.parseDouble(String.valueOf(obj)); } catch (NumberFormatException e) {}
            }
        }

        double targetYValue = 0.0;
        if (data.hasKey("targetY")) {
            Object obj = data.get("targetY");
            if (obj instanceof UpdatableObjectProperty) {
                obj = ((UpdatableObjectProperty<?>) obj).getValue();
            }
            if (obj instanceof Number) {
                targetYValue = ((Number) obj).doubleValue();
            } else {
                try { targetYValue = Double.parseDouble(String.valueOf(obj)); } catch (NumberFormatException e) {}
            }
        }

        return FXGL.entityBuilder(data)
                .type(EntityType.DOOR)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .zIndex(1)
                .with("targetMap", targetMapValue)
                // ĐỔI TÊN Ở ĐÂY để tránh xung đột với Tiled
                .with("teleportX", targetXValue)
                .with("teleportY", targetYValue)
                .with(new InteractableComponent((player, target) -> {
                    Main.getInstance().handleDoorInteraction(target);
                }))
                .collidable()
                .build();
    }

    // Chuyển sang các phương thức riêng biệt gọi chung logic để tránh lỗi repeatable annotation
    @Spawns("House_in")
    public Entity spawnHouseIn(SpawnData data) {
        return spawnDoor(data);
    }

    @Spawns("House_out")
    public Entity spawnHouseOut(SpawnData data) {
        Entity door = spawnDoor(data);
        if (door.getBoundingBoxComponent() != null) {
            door.getBoundingBoxComponent().clearHitBoxes();
            door.getBoundingBoxComponent().addHitBox(new HitBox(com.almasb.fxgl.physics.BoundingShape.box(64, 48)));
        }
        return door;
    }

    @Spawns("Sleep")
    public Entity spawnSleep(SpawnData data) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32.0;

        return FXGL.entityBuilder(data)
                .type(EntityType.SLEEP)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .with(new InteractableComponent((player, target) -> {
                    Main.getInstance().handleSleepInteraction();
                }))
                .collidable()
                .build();
    }

    @Spawns("Selector")
    public Entity spawnSelector(SpawnData data) {
        Rectangle rect = new Rectangle(32, 32, Color.color(1, 1, 1, 0.2));
        rect.setStroke(Color.WHITE);
        rect.setStrokeWidth(2);
        return FXGL.entityBuilder(data).type(EntityType.SELECTOR).view(rect).zIndex(100).build();
    }

    // ================= NPCs (SỬ DỤNG NUMBER CASTING) =================

    @Spawns("Guider")
    public Entity spawnGuider(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().friction(0f).density(0.1f));
        physics.setBodyType(BodyType.KINEMATIC);
        physics.setOnPhysicsInitialized(() -> {
            if (physics.getBody() != null) {
                physics.getBody().setLinearDamping(12.0f);
                physics.getBody().setFixedRotation(true);
            }
        });
        
        return FXGL.entityBuilder(data).type(EntityType.GUIDER)
                .bbox(new HitBox(new javafx.geometry.Point2D(4, 40), BoundingShape.box(24, 24)))
                .with(physics)
                .with(new NPCAnimationComponent())
                .with(new NPCBehaviorComponent())
                .with("name", "Bác Nông Dân")
                .collidable()
                .build();
    }

    @Spawns("NPC")
    public Entity spawnNPC(SpawnData data) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32.0;

        return FXGL.entityBuilder(data)
                .type(EntityType.NPC)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .collidable()
                .build();
    }

    @Spawns("Trader")
    public Entity spawnTrader(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().friction(0f).density(0.1f));
        physics.setBodyType(BodyType.KINEMATIC);
        physics.setOnPhysicsInitialized(() -> {
            if (physics.getBody() != null) {
                physics.getBody().setLinearDamping(12.0f);
                physics.getBody().setFixedRotation(true);
            }
        });

        return FXGL.entityBuilder(data)
                .type(EntityType.TRADER)
                .bbox(new HitBox(new javafx.geometry.Point2D(4, 40), BoundingShape.box(24, 24)))
                .with(physics)
                .with(new NPCAnimationComponent("NPC/Trader/Trader.png")) // Gắn animation component cho Trader
                .with(new NPCBehaviorComponent()) // Thêm component điều khiển hành vi
                .with(new TraderComponent()) // Gắn TraderComponent vào Trader entity
                .with("name", "Trader")
                .collidable()
                .build();
    }

    @Spawns("Trader_in")
    public Entity spawnTraderIn(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.TRADER_IN).build();
    }

    @Spawns("Guider_in")
    public Entity spawnGuiderIn(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.GUIDER_IN).build();
    }

    public static int animalSpawnOffset = 0;

    private Entity createAnimal(SpawnData data, String animalType, double w, double h) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().friction(0f).density(0.1f));
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setOnPhysicsInitialized(() -> {
            if (physics.getBody() != null) {
                physics.getBody().setGravityScale(0f);
                physics.getBody().setFixedRotation(true);
            }
        });

        double x = data.getX();
        if (!data.hasKey("fromSave")) {
            x += animalSpawnOffset;
            animalSpawnOffset = (animalSpawnOffset + 32) % 128;
            data = new SpawnData(x, data.getY()).put("fromSave", false);
        }

        Entity entity = FXGL.entityBuilder(data)
                .type(EntityType.ANIMAL)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .zIndex(7)
                .with(physics)
                .with(BaseAnimalComponent.create(animalType))
                .collidable()
                .build();

        return entity;
    }

    @Spawns("Chick")
    public Entity spawnChick(SpawnData data) {
        return createAnimal(data, "chick", 24, 24);
    }

    @Spawns("Calf")
    public Entity spawnCalf(SpawnData data) {
        return createAnimal(data, "calf", 48, 48);
    }

    @Spawns("Lamb")
    public Entity spawnLamb(SpawnData data) {
        return createAnimal(data, "lamb", 32, 32);
    }

    @Spawns("Piglet")
    public Entity spawnPiglet(SpawnData data) {
        return createAnimal(data, "piglet", 32, 32);
    }

    @Spawns("Turkey")
    public Entity spawnTurkey(SpawnData data) {
        return createAnimal(data, "turkey", 32, 32);
    }

    @Spawns("Bull")
    public Entity spawnBull(SpawnData data) {
        Entity bull = createAnimal(data, "bull", 64, 64);
        BaseAnimalComponent bac = bull.getComponent(BaseAnimalComponent.class);
        if (bac != null) {
            bac.setDaysGrown(bac.getMaxGrowthDays());
            bac.initAnimation();
        }
        return bull;
    }

    private Entity createMonster(SpawnData data, String monsterType, String runTexturePath, String idleTexturePath) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().friction(0f).density(0.1f));
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setOnPhysicsInitialized(() -> {
            if (physics.getBody() != null) {
                physics.getBody().setGravityScale(0f);
                physics.getBody().setFixedRotation(true);
            }
        });

        BaseMonsterComponent bmc;
        if ("Boar".equalsIgnoreCase(monsterType)) {
            bmc = new BoarComponent();
        } else if ("Fox".equalsIgnoreCase(monsterType)) {
            bmc = new FoxComponent();
        } else if ("Deer".equalsIgnoreCase(monsterType)) {
            bmc = new DeerComponent();
        } else if ("Hare".equalsIgnoreCase(monsterType)) {
            bmc = new HareComponent();
        } else {
            bmc = new BoarComponent();
        }

        if (data.hasKey("tempBushMonster")) {
            bmc.setTemporary(10.0);
        }

        return FXGL.entityBuilder(data)
                .type(EntityType.MONSTER)
                .bbox(new HitBox(BoundingShape.box(32, 32)))
                .zIndex(8)
                .with(physics)
                .with(bmc)
                .with(new MonsterAnimationComponent(monsterType, runTexturePath, idleTexturePath))
                .with("monsterType", monsterType)
                .collidable()
                .build();
    }

    @Spawns("Boar")
    public Entity spawnBoar(SpawnData data) {
        return createMonster(data, "Boar", "monster/Boar/Boar_Walk_with_shadow.png", "monster/Boar/Boar_Idle_with_shadow.png");
    }

    @Spawns("Fox")
    public Entity spawnFox(SpawnData data) {
        return createMonster(data, "Fox", "monster/Fox/Fox_walk_with_shadow.png", "monster/Fox/Fox_Idle_with_shadow.png");
    }

    @Spawns("Deer")
    public Entity spawnDeer(SpawnData data) {
        return createMonster(data, "Deer", "monster/Deer/Deer_Run_with_shadow.png", "monster/Deer/Deer_Idle_with_shadow.png");
    }

    @Spawns("Hare")
    public Entity spawnHare(SpawnData data) {
        return createMonster(data, "Hare", "monster/Hare/Hare_Run_with_shadow.png", "monster/Hare/Hare_Idle_with_shadow.png");
    }

    @Spawns("Bush")
    public Entity spawnBush(SpawnData data) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32.0;
        return FXGL.entityBuilder(data)
                .type(EntityType.BUSH)
                .bbox(new HitBox(BoundingShape.box(w, h)))
                .collidable()
                .build();
    }

    @Spawns("BushMonster")
    public Entity spawnBushMonster(SpawnData data) {
        double spawnX = data.getX();
        double spawnY = data.getY();
        try {
            Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
            if (player != null && player.getPosition().distance(spawnX, spawnY) < 64) {
                // Offset spawn position by 96 pixels away from player
                double angle = Math.random() * 2 * Math.PI;
                spawnX = player.getX() + Math.cos(angle) * 96;
                spawnY = player.getY() + Math.sin(angle) * 96;
            }
        } catch (Exception e) {
            // Ignore if player is not found
        }

        java.util.Random rand = new java.util.Random();
        SpawnData copy = new SpawnData(spawnX, spawnY);
        copy.put("tempBushMonster", true);
        if (rand.nextBoolean()) {
            return createMonster(copy, "Boar", "monster/Boar/Boar_Walk_with_shadow.png", "monster/Boar/Boar_Idle_with_shadow.png");
        } else {
            return createMonster(copy, "Fox", "monster/Fox/Fox_walk_with_shadow.png", "monster/Fox/Fox_Idle_with_shadow.png");
        }
    }
}
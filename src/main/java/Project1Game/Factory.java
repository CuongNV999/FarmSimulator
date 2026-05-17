package Project1Game;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyDef;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;

import javafx.scene.paint.Color;

public class Factory implements EntityFactory {

    @Spawns("Player")
    public Entity spawnPlayer(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().friction(0).density(0.1f));
        BodyDef bd = new BodyDef();
        bd.setFixedRotation(true);
        bd.setType(BodyType.DYNAMIC);
        physics.setBodyDef(bd);
        physics.setBodyType(BodyType.DYNAMIC);
        return FXGL.entityBuilder(data)
                .bbox(new HitBox(new javafx.geometry.Point2D(26, 23), BoundingShape.box(13, 26)))
                .type(EntityType.PLAYER)
                .zIndex(2)
                .with(physics)
                .with(new PlayerComponent())
                .collidable()
                .build();
    }

    @Spawns("Background")
    public Entity spawnBackground(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new javafx.scene.shape.Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.valueOf("#3a9141")))
                .with(new IrremovableComponent())
                .zIndex(-100)
                .build();
    }

    @Spawns("Wall")
    public Entity spawnWall(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .collidable()
                .build();
    }

    @Spawns("Soil")
    public Entity spawnSoil(SpawnData data) {
        int width = data.hasKey("width") ? data.get("width") : 32;
        int height = data.hasKey("height") ? data.get("height") : 32;
        return FXGL.entityBuilder(data)
                .type(EntityType.SOIL)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .zIndex(0)
                .with(new SoilComponent())
                .build();
    }

    // Wheat & Corn: sprite 32x64, offset -32 để gốc khớp ô đất
    @Spawns("Wheat")
    public Entity spawnWheat(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.WHEAT)
                .bbox(new HitBox(BoundingShape.box(32, 32))).zIndex(1)
                .with(new CropComponent(0, 6 * 32, 64, -32)).build();
    }

    @Spawns("Corn")
    public Entity spawnCorn(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.CORN)
                .bbox(new HitBox(BoundingShape.box(32, 32))).zIndex(1)
                .with(new CropComponent(4, 6 * 32, 64, -32)).build();
    }

    // Radish/Cabbage/Lettuce/Tomato: sprite 32x96, offset -32
    // rowY = (hàng thực - 1) * 32 để lấy thêm phần trên
    @Spawns("Radish")
    public Entity spawnRadish(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.RADISH)
                .bbox(new HitBox(BoundingShape.box(32, 32))).zIndex(1)
                .with(new CropComponent(0, 0 * 32, 96, -32)).build();
    }

    @Spawns("Cabbage")
    public Entity spawnCabbage(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.CABBAGE)
                .bbox(new HitBox(BoundingShape.box(32, 32))).zIndex(1)
                .with(new CropComponent(0, 2 * 32, 96, -32)).build();
    }

    @Spawns("Lettuce")
    public Entity spawnLettuce(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.LETTUCE)
                .bbox(new HitBox(BoundingShape.box(32, 32))).zIndex(1)
                .with(new CropComponent(4, 2 * 32, 96, -32)).build();
    }

    @Spawns("Tomato")
    public Entity spawnTomato(SpawnData data) {
        return FXGL.entityBuilder(data).type(EntityType.TOMATO)
                .bbox(new HitBox(BoundingShape.box(32, 32))).zIndex(1)
                .with(new CropComponent(0, 4 * 32, 64, -32)).build();
    }

    @Spawns("Collisions")
    public Entity spawnCollisions(SpawnData data) {
        double width = 32, height = 32;
        if (data.hasKey("width")) { Object w = data.get("width"); width = w instanceof Number ? ((Number)w).doubleValue() : 32; }
        if (data.hasKey("height")) { Object h = data.get("height"); height = h instanceof Number ? ((Number)h).doubleValue() : 32; }
        double rotation = 0;
        if (data.hasKey("rotation")) { Object r = data.get("rotation"); rotation = r instanceof Number ? ((Number)r).doubleValue() : 0; }
        double bboxW = width, bboxH = height;
        if (Math.abs(rotation) > 0.01) {
            double rad = Math.toRadians(rotation);
            bboxW = Math.abs(width * Math.cos(rad)) + Math.abs(height * Math.sin(rad));
            bboxH = Math.abs(width * Math.sin(rad)) + Math.abs(height * Math.cos(rad));
        }
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data).type(EntityType.COLLISION)
                .bbox(new HitBox(BoundingShape.box(bboxW, bboxH)))
                .with(physics).collidable().build();
    }

    @Spawns("Interaction")
    public Entity spawnInteraction(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data).type(EntityType.INTERACTION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics).build();
    }

    @Spawns("Door")
    public Entity spawnDoor(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        return FXGL.entityBuilder(data).type(EntityType.INTERACTION)
                .bbox(new HitBox(BoundingShape.box(width, height))).collidable().build();
    }

    @Spawns("Field")
    public Entity spawnField(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        return FXGL.entityBuilder(data).type(EntityType.FIELD)
                .bbox(new HitBox(BoundingShape.box(width, height))).build();
    }

    @Spawns("NPC")
    public Entity spawnNPC(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        return FXGL.entityBuilder(data).type(EntityType.NPC)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .build();
    }

    @Spawns("Guider")
    public Entity spawnGuider(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data).type(EntityType.GUIDER)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics).collidable().build();
    }

    @Spawns("Trader")
    public Entity spawnTrader(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data).type(EntityType.TRADER)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics).collidable().build();
    }

    @Spawns("Selector")
    public Entity spawnSelector(SpawnData data) {
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(32, 32);
        rect.setFill(Color.color(1, 1, 1, 0.2));
        rect.setStroke(Color.WHITE);
        rect.setStrokeWidth(2);
        return FXGL.entityBuilder(data).type(EntityType.SELECTOR).view(rect).zIndex(10).build();
    }
}

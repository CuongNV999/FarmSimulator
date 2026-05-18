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

        return FXGL.entityBuilder(data)
                .bbox(new HitBox(new javafx.geometry.Point2D(26, 23), BoundingShape.box(13, 26)))
                .type(EntityType.PLAYER)
                .zIndex(2)
                .with(physics)
                .with(new PlayerComponent())
                .collidable()
                .build();
    }

    @Spawns("Soil")
    public Entity spawnSoil(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.SOIL)
                .bbox(new HitBox(BoundingShape.box(32, 32)))
                .zIndex(0)
                .with(new SoilComponent())
                .build();
    }

    // --- HỆ THỐNG CÂY TRỒNG (Áp dụng Data-Driven) ---

    private Entity createCrop(SpawnData data, CropData cropInfo) {
        return FXGL.entityBuilder(data)
                .type(cropInfo.type)
                .bbox(new HitBox(BoundingShape.box(32, 32)))
                .zIndex(1)
                .with(new CropComponent(cropInfo)) // Dùng chung 1 component duy nhất
                .build();
    }

    @Spawns("Wheat")
    public Entity spawnWheat(SpawnData data) { return createCrop(data, CropData.WHEAT); }

    @Spawns("Corn")
    public Entity spawnCorn(SpawnData data) { return createCrop(data, CropData.CORN); }

    @Spawns("Radish")
    public Entity spawnRadish(SpawnData data) { return createCrop(data, CropData.RADISH); }

    @Spawns("Cabbage")
    public Entity spawnCabbage(SpawnData data) { return createCrop(data, CropData.CABBAGE); }

    @Spawns("Lettuce")
    public Entity spawnLettuce(SpawnData data) { return createCrop(data, CropData.LETTUCE); }

    @Spawns("Tomato")
    public Entity spawnTomato(SpawnData data) { return createCrop(data, CropData.TOMATO); }

    // --- CÁC THỰC THỂ KHÁC ---

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

    @Spawns("Collisions")
    public Entity spawnCollisions(SpawnData data) {
        double width = data.hasKey("width") ? ((Number)data.get("width")).doubleValue() : 32;
        double height = data.hasKey("height") ? ((Number)data.get("height")).doubleValue() : 32;
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data).type(EntityType.COLLISION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics).collidable().build();
    }

    @Spawns("Interaction")
    public Entity spawnInteraction(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        return FXGL.entityBuilder(data).type(EntityType.INTERACTION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .collidable().build();
    }

    @Spawns("Field")
    public Entity spawnField(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        return FXGL.entityBuilder(data).type(EntityType.FIELD)
                .bbox(new HitBox(BoundingShape.box(width, height))).build();
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
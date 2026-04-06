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
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;

        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().friction(0).density(0.1f));
        BodyDef bd = new BodyDef();
        bd.setFixedRotation(true);
        bd.setType(BodyType.DYNAMIC);
        physics.setBodyDef(bd);
        physics.setBodyType(BodyType.DYNAMIC);

        // Bbox nhỏ hơn sprite, nằm ở phần chân - top-down collision chuẩn
        int bboxW = 13;
        int bboxH = 26;
        int offsetX = 26;   // căn giữa theo chiều ngang
        int offsetY = 23;         // nằm ở phần chân

        return FXGL.entityBuilder(data)
                .bbox(new HitBox(new javafx.geometry.Point2D(offsetX, offsetY),
                        BoundingShape.box(bboxW, bboxH)))
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
                .view(new javafx.scene.shape.Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.valueOf("#3a9141"))) // Màu cỏ xanh
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

    @Spawns("Rice")
    public Entity spawnRice(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.RICE)
                .viewWithBBox("Crops/rice_1.png") // Hình ảnh mầm lúa
                .zIndex(1)
                .with(new RiceComponent()) // Component quản lý sự trưởng thành
                .build();
    }

    @Spawns("Collisions")
    public Entity spawnCollisions(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        return FXGL.entityBuilder(data)
                .type(EntityType.COLLISION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .view(new javafx.scene.shape.Rectangle(width, height, Color.TRANSPARENT))
                .with(physics)
                .collidable()
                .build();
    }

    @Spawns("Interaction")
    public Entity spawnInteraction(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        return FXGL.entityBuilder(data)
                .type(EntityType.INTERACTION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .collidable()
                .build();
    }
    @Spawns("Door")
    public Entity spawnDoor(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;

        return FXGL.entityBuilder(data)
                .type(EntityType.INTERACTION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .collidable()
                .build();
    }

    @Spawns("Selector")
    public Entity spawnSelector(SpawnData data) {
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(32, 32);
        rect.setFill(Color.color(1, 1, 1, 0.2)); // Nền trắng mờ
        rect.setStroke(Color.WHITE); // Viền trắng
        rect.setStrokeWidth(2);

        return FXGL.entityBuilder(data)
                .type(EntityType.SELECTOR)
                .view(rect)
                .zIndex(10)
                .build();
    }
}


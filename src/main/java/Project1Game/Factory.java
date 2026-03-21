package Project1Game;
import com.almasb.fxgl.entity.components.ViewComponent;
import javafx.scene.paint.Color;
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
import javafx.geometry.Point2D;
import java.util.List;
import java.awt.*;

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
                .viewWithBBox("Player.png")
                .type(EntityType.PLAYER)
                .zIndex(2)
                .with(physics)
                .collidable()
                .build();

    }

    @Spawns("Background")
    public Entity spawnBackground(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(String.valueOf(new Rectangle()))
                .with(new IrremovableComponent())
                .zIndex(-100)
                .build();
    }

    @Spawns("Wall")
    public Entity spawnWall(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
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
                .collidable() // Cần thiết để kiểm tra Player đang đứng trên đất
                .with(new SoilComponent()) // Component quản lý trạng thái đất và texture
                .build();
    }

    @Spawns("Rice")
    public Entity spawnRice(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.RICE)
                .viewWithBBox("rice_1.png") // Hình ảnh mầm lúa
                .zIndex(1)
                .with(new RiceComponent()) // Component quản lý sự trưởng thành
                .build();
    }
}


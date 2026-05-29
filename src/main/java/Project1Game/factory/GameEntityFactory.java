package Project1Game.factory;

import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.SoilComponent;
import Project1Game.component.player.PlayerComponent;
import Project1Game.component.NPCAnimationComponent;
import Project1Game.component.NPCBehaviorComponent;
import Project1Game.component.TraderComponent; // Import TraderComponent
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
    @Spawns("Corn") public Entity spawnCorn(SpawnData d) { return createCrop(d, CropData.CORN); }
    @Spawns("Radish") public Entity spawnRadish(SpawnData d) { return createCrop(d, CropData.RADISH); }
    @Spawns("Cabbage") public Entity spawnCabbage(SpawnData d) { return createCrop(d, CropData.CABBAGE); }
    @Spawns("Lettuce") public Entity spawnLettuce(SpawnData d) { return createCrop(d, CropData.LETTUCE); }
    @Spawns("Tomato") public Entity spawnTomato(SpawnData d) { return createCrop(d, CropData.TOMATO); }

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
        return spawnDoor(data);
    }

    @Spawns("Sleep")
    public Entity spawnSleep(SpawnData data) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32.0;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32.0;

        return FXGL.entityBuilder(data)
                .type(EntityType.SLEEP)
                .bbox(new HitBox(BoundingShape.box(w, h)))
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
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setOnPhysicsInitialized(() -> {
            if (physics.getBody() != null) {
                physics.getBody().setLinearDamping(12.0f);
                physics.getBody().setFixedRotation(true);
            }
        });
        
        return FXGL.entityBuilder(data).type(EntityType.GUIDER)
                .bbox(new HitBox(BoundingShape.box(32, 64)))
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
        physics.setBodyType(BodyType.DYNAMIC);
        physics.setOnPhysicsInitialized(() -> {
            if (physics.getBody() != null) {
                physics.getBody().setLinearDamping(12.0f);
                physics.getBody().setFixedRotation(true);
            }
        });

        return FXGL.entityBuilder(data)
                .type(EntityType.TRADER)
                .bbox(new HitBox(BoundingShape.box(32, 64)))
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
}
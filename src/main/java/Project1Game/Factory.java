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

/**
 * Lớp Factory chịu trách nhiệm khởi tạo chi tiết kết cấu đồ họa, kích thước hộp va chạm
 * và các logic component đi kèm ứng với từng tên gọi chuỗi ký tự được định cấu trúc trên map TMX.
 */
public class Factory implements EntityFactory {

    /**
     * Khởi tạo thực thể Nhân vật chính (Player)
     */
    @Spawns("Player")
    public Entity spawnPlayer(SpawnData data) {
        // Cấu hình thành phần hộp Vật lý cho người chơi di chuyển có ma sát, gia tốc
        PhysicsComponent physics = new PhysicsComponent();
        physics.setFixtureDef(new FixtureDef().friction(0).density(0.1f)); // friction = 0 giúp nhân vật di chuyển trơn tru không bị kẹt khi cọ sát vào tường
        BodyDef bd = new BodyDef();
        bd.setFixedRotation(true);        // Khóa trục xoay: Giữ nhân vật luôn thẳng đứng, không bị quay vòng tròn khi va đập vật lý
        bd.setType(BodyType.DYNAMIC);    // DYNAMIC: Thực thể chịu tác động lực, di chuyển tự do và bị chặn bởi vật thể tĩnh khác
        physics.setBodyDef(bd);

        return FXGL.entityBuilder(data)
                // Đặt kích thước hộp va chạm Hitbox tùy biến ở chân nhân vật nhằm tạo chiều sâu 2.5D (Y-Sorting) dễ đi lại
                .bbox(new HitBox(new javafx.geometry.Point2D(26, 23), BoundingShape.box(13, 26)))
                .type(EntityType.PLAYER)  // Định danh nhãn nhóm thực thể
                .zIndex(2)                // Thứ tự hiển thị lớp ảnh (Lớp càng cao sẽ che khuất lớp thấp hơn như Đất hay Cây)
                .with(physics)            // Tích hợp hộp vật lý
                .with(new PlayerComponent()) // Thêm bộ điều khiển cử động/hoạt ảnh hoạt hình nhân vật
                .collidable()             // Bật tính năng lắng nghe bắt sự kiện va chạm cảm biến
                .build();
    }

    /**
     * Khởi tạo các ô đất trồng trọt cày xới ruộng vườn (Soil)
     */
    @Spawns("Soil")
    public Entity spawnSoil(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.SOIL)
                .bbox(new HitBox(BoundingShape.box(32, 32))) // Hộp va chạm kích thước ô vuông tiêu chuẩn 32x32px
                .zIndex(0) // Nằm ở tầng thấp nhất để nhân vật và cây trồng hiển thị đè lên trên ảnh ô đất
                .with(new SoilComponent()) // Quản lý logic đất khô/ướt và kết cấu thay đổi texture đất
                .build();
    }

    // ================= HỆ THỐNG CÂY TRỒNG (Áp dụng thiết kế cấu trúc Data-Driven) =================

    /**
     * Hàm phụ trợ dùng chung để đóng gói tạo lập các loại cây dựa theo dữ liệu cấu hình đầu vào CropData mẫu
     */
    private Entity createCrop(SpawnData data, CropData cropInfo) {
        return FXGL.entityBuilder(data)
                .type(cropInfo.type) // Nhãn nhóm thực thể lấy tự động tương ứng theo loại cây
                .bbox(new HitBox(BoundingShape.box(32, 32)))
                .zIndex(1) // Nằm cao hơn ô đất nền (0) nhưng nằm thấp dưới chân người chơi (2)
                .with(new CropComponent(cropInfo)) // Đổ dữ liệu thông tin thời gian lớn, hình ảnh của giống cây tương ứng vào
                .build();
    }

    // Đăng ký chuỗi kích hoạt chế tạo cụ thể từng hạt giống nông sản cây trồng
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

    // ================= CÁC THỰC THỂ HỆ THỐNG VÀ KHÔNG GIAN BẢN ĐỒ =================

    /**
     * Khởi tạo hình nền cơ bản (Background) màu xanh cỏ nếu bản đồ thiếu ảnh nền
     */
    @Spawns("Background")
    public Entity spawnBackground(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new javafx.scene.shape.Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.valueOf("#3a9141")))
                .with(new IrremovableComponent()) // Chống không cho thực thể này bị xóa ngoài ý muốn khi chuyển map
                .zIndex(-100)                      // Đẩy sâu xuống đáy đồ họa
                .build();
    }

    /**
     * Khởi tạo các bức tường vô hình/hữu hình để bao bọc chắn đường (Wall)
     */
    @Spawns("Wall")
    public Entity spawnWall(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC); // STATIC: Khối vật lý tĩnh cố định cứng, giống bức tường, không bao giờ bị dịch chuyển hay đẩy lùi
        
        return FXGL.entityBuilder(data)
                .type(EntityType.WALL)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .collidable()
                .build();
    }

    /**
     * Tạo vùng va chạm ngăn cản chặn lối đi quét từ lớp Object Layer có sẵn của Tiled Map
     */
    @Spawns("Collisions")
    public Entity spawnCollisions(SpawnData data) {
        // Hỗ trợ đọc thông số độ rộng dài linh hoạt quét tự động từ tmx map sang Java dữ liệu Number
        double width = data.hasKey("width") ? ((Number)data.get("width")).doubleValue() : 32;
        double height = data.hasKey("height") ? ((Number)data.get("height")).doubleValue() : 32;
        
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        return FXGL.entityBuilder(data).type(EntityType.COLLISION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics).collidable().build();
    }

    /**
     * Khởi tạo một vùng kích hoạt tương tác (Vùng cảm biến Trigger vô hình)
     */
    @Spawns("Interaction")
    public Entity spawnInteraction(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        
        // Khởi tạo thành phần vật lý để định vị xử lý va chạm không gian
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        
        return FXGL.entityBuilder(data).type(EntityType.INTERACTION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics)
                .collidable() // Quan trọng: Phải bật collidable để kích hoạt sự kiện va chạm khi Player bước chân vào vùng này
                .build();
    }

    /**
     * Khởi tạo Cửa di chuyển (Door) - Bản chất cũng là một ô Trigger tương tác khu vực chuyển đổi
     */
    @Spawns("Door")
    public Entity spawnDoor(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        
        return FXGL.entityBuilder(data).type(EntityType.INTERACTION)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .collidable()
                .build();
    }

    /**
     * Khởi tạo Ranh giới cánh đồng ruộng (Field) - Nơi giới hạn người chơi chỉ được dùng cuốc cày đất tại đây
     */
    @Spawns("Field")
    public Entity spawnField(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        
        return FXGL.entityBuilder(data).type(EntityType.FIELD)
                .bbox(new HitBox(BoundingShape.box(width, height))).build();
    }

    /**
     * Khởi tạo thực thể nhân vật quần chúng cơ bản (NPC)
     */
    @Spawns("NPC")
    public Entity spawnNPC(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        return FXGL.entityBuilder(data).type(EntityType.NPC)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .collidable()
                .build();
    }

    /**
     * Khởi tạo NPC Người hướng dẫn (Guider) cố định một chỗ trên map
     */
    @Spawns("Guider")
    public Entity spawnGuider(SpawnData data) {
        int width = data.hasKey("width") ? (int) data.get("width") : 32;
        int height = data.hasKey("height") ? (int) data.get("height") : 32;
        
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC); // NPC đứng im không bị người chơi xô đẩy làm lệch vị trí
        
        return FXGL.entityBuilder(data).type(EntityType.GUIDER)
                .bbox(new HitBox(BoundingShape.box(width, height)))
                .with(physics).collidable().build();
    }

    /**
     * Khởi tạo NPC Thương nhân mua bán vật phẩm nông sản (Trader)
     */
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

    /**
     * Khởi tạo ô chỉ định vị trí trỏ chuột (Selector) vẽ một khung viền trắng mờ kích thước 32x32px hình vuông
     */
    @Spawns("Selector")
    public Entity spawnSelector(SpawnData data) {
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(32, 32);
        rect.setFill(Color.color(1, 1, 1, 0.2)); // Màu nền trắng mờ đục trong suốt nhẹ (Alpha = 0.2)
        rect.setStroke(Color.WHITE);              // Màu viền khung: Màu trắng tinh
        rect.setStrokeWidth(2);                   // Độ dày nét vẽ viền: 2px
        
        return FXGL.entityBuilder(data)
                .type(EntityType.SELECTOR)
                .view(rect)     // Gán thành phần đồ họa hình chữ nhật vừa vẽ làm hình ảnh đại diện
                .zIndex(10)     // Đặt zIndex cực cao để khung chuột luôn luôn hiển thị trên cùng, không bị che khuất bởi người chơi hay cây cối
                .build();
    }
}
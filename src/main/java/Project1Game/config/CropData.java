package Project1Game.config;

import Project1Game.core.EntityType;
import javafx.util.Duration;

public enum CropData {

    // =====================================================
    // CÂY GỐC - chuyển sang dùng ảnh riêng từng stage
    // Thư mục: assets/textures/Crops/OldCrops/<tên_cây>/
    // Mỗi cây cần: seed.png, stage1.png, stage2.png, stage3.png, harvest.png
    // =====================================================
    WHEAT   (EntityType.WHEAT,   "Crops/OldCrops/wheat/",    6,  50, 3),
    RADISH  (EntityType.RADISH,  "Crops/OldCrops/radish/",  12,  40, 2),
    CABBAGE (EntityType.CABBAGE, "Crops/OldCrops/cabbage/", 15,  90, 3),

    // =====================================================
    // 9 CÂY MỚI
    // Thư mục: assets/textures/Crops/NewCrops/<tên_cây>/
    // =====================================================
    GRAPE      (EntityType.GRAPE,      "Crops/NewCrops/grape/",      14, 80,  4),
    CUCUMBER   (EntityType.CUCUMBER,   "Crops/NewCrops/cucumber/",    7, 35,  3),
    PEPPER     (EntityType.PEPPER,     "Crops/NewCrops/pepper/",     10, 55,  3),
    CAULIFLOWER(EntityType.CAULIFLOWER,"Crops/NewCrops/cauliflower/",13, 75,  2),
    BEAN       (EntityType.BEAN,       "Crops/NewCrops/bean/",        6, 30,  5),
    PINEAPPLE  (EntityType.PINEAPPLE,  "Crops/NewCrops/pineapple/",  20, 100, 2),
    SUNFLOWER  (EntityType.SUNFLOWER,  "Crops/NewCrops/sunflower/",  12, 65,  3),
    COCONUT    (EntityType.COCONUT,    "Crops/NewCrops/coconut/",    25, 120, 1),
    APPLE      (EntityType.APPLE,      "Crops/NewCrops/apple/",      18, 90,  2);

    public final EntityType type;
    public final String spriteDir;   // đường dẫn thư mục ảnh (kết thúc bằng /)
    public final Duration growthTime;
    public final int price;          // giá hạt giống
    public final int yield;          // sản lượng thu hoạch

    CropData(EntityType type, String spriteDir, double growthSeconds, int price, int yield) {
        this.type       = type;
        this.spriteDir  = spriteDir;
        this.growthTime = Duration.seconds(growthSeconds);
        this.price      = price;
        this.yield      = yield;
    }

    /**
     * Lấy path ảnh theo stage:
     *   0 = stage1.png    (cây mới trồng)
     *   1 = stage2.png    (cây đang lớn)
     *   2 = stage3.png    (cây chín - có quả, thu hoạch được)
     *   3 = harvest.png   (cây đã bị hái - trống quả, chờ lớn lại hoặc nhổ)
     */
    public String getSpriteForStage(int stage) {
        String[] names = {"stage1.png", "stage2.png", "stage3.png", "harvest.png"};
        return spriteDir + names[Math.min(stage, 3)];
    }

    /** stage3 = cây chín, có thể thu hoạch */
    public static final int STAGE_RIPE      = 2;
    /** stage harvest = cây đã bị hái, trống quả */
    public static final int STAGE_HARVESTED = 3;

    /** Lấy path ảnh hạt giống (dùng trong inventory/shop) */
    public String getSeedSprite() {
        return spriteDir + "seed.png";
    }

    public static CropData fromType(EntityType type) {
        for (CropData data : values()) {
            if (data.type == type) return data;
        }
        return WHEAT;
    }
}

package Project1Game.config;

import Project1Game.core.EntityType;
import javafx.util.Duration;

public enum CropData {
    // Định nghĩa: TYPE, colStart, rowY, spriteH, offsetY, growthSeconds, price
    WHEAT(EntityType.WHEAT, 0, 6 * 32, 64, -32, 10, 50),
    CORN(EntityType.CORN, 4, 6 * 32, 64, -32, 12, 70),
    RADISH(EntityType.RADISH, 0, 0 * 32, 96, -32, 8, 40),
    CABBAGE(EntityType.CABBAGE, 0, 2 * 32, 96, -32, 15, 90),
    LETTUCE(EntityType.LETTUCE, 4, 2 * 32, 96, -32, 7, 30),
    TOMATO(EntityType.TOMATO, 0, 4 * 32, 64, -32, 11, 60);

    public final EntityType type;
    public final int colStart;
    public final int rowY;
    public final int spriteH;
    public final int offsetY;
    public final Duration growthTime; // Đổi tên từ growthInterval thành growthTime
    public final int price; // Thêm thuộc tính giá

    CropData(EntityType type, int colStart, int rowY, int spriteH, int offsetY, double growthSeconds, int price) {
        this.type = type;
        this.colStart = colStart;
        this.rowY = rowY;
        this.spriteH = spriteH;
        this.offsetY = offsetY;
        this.growthTime = Duration.seconds(growthSeconds);
        this.price = price;
    }

    /** Tìm dữ liệu dựa trên EntityType */
    public static CropData fromType(EntityType type) {
        for (CropData data : values()) {
            if (data.type == type) return data;
        }
        return WHEAT; // mặc định
    }
}
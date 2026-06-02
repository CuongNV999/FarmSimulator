package Project1Game.config;

import Project1Game.core.EntityType;

public enum CropData {
    // Định nghĩa: TYPE, colStart, rowY, spriteH, offsetY, growthSeconds, price, yield
    WHEAT(EntityType.WHEAT, 0, 6 * 32, 64, -32, 10.0, 25, 1),
    CORN(EntityType.CORN, 4, 6 * 32, 64, -32, 80.0, 110, 2),
    RADISH(EntityType.RADISH, 0, 0 * 32, 96, -32, 25.0, 90, 1),
    CABBAGE(EntityType.CABBAGE, 0, 2 * 32, 96, -32, 40.0, 150, 1),
    LETTUCE(EntityType.LETTUCE, 4, 2 * 32, 96, -32, 15.0, 35, 1),
    TOMATO(EntityType.TOMATO, 0, 4 * 32, 64, -32, 60.0, 60, 2);

    public final EntityType type;
    public final int colStart;
    public final int rowY;
    public final int spriteH;
    public final int offsetY;
    public final javafx.util.Duration growthTime;
    public final int price; // Thêm thuộc tính giá
    public final int yield; // Thêm thuộc tính sản lượng thu hoạch

    CropData(EntityType type, int colStart, int rowY, int spriteH, int offsetY, double growthSeconds, int price, int yield) {
        this.type = type;
        this.colStart = colStart;
        this.rowY = rowY;
        this.spriteH = spriteH;
        this.offsetY = offsetY;
        this.growthTime = javafx.util.Duration.seconds(growthSeconds);
        this.price = price;
        this.yield = yield;
    }

    /** Tìm dữ liệu dựa trên EntityType */
    public static CropData fromType(EntityType type) {
        for (CropData data : values()) {
            if (data.type == type)
                return data;
        }
        return WHEAT; // mặc định
    }
}
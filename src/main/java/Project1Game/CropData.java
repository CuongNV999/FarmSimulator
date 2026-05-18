package Project1Game;

import javafx.util.Duration;

public enum CropData {
    // Định nghĩa: TYPE, colStart, rowY, spriteH, offsetY, growthSeconds
    WHEAT(EntityType.WHEAT, 0, 6 * 32, 64, -32, 10),
    CORN(EntityType.CORN, 4, 6 * 32, 64, -32, 12),
    RADISH(EntityType.RADISH, 0, 0 * 32, 96, -32, 8),
    CABBAGE(EntityType.CABBAGE, 0, 2 * 32, 96, -32, 15),
    LETTUCE(EntityType.LETTUCE, 4, 2 * 32, 96, -32, 7),
    TOMATO(EntityType.TOMATO, 0, 4 * 32, 64, -32, 11);

    public final EntityType type;
    public final int colStart;
    public final int rowY;
    public final int spriteH;
    public final int offsetY;
    public final Duration growthInterval;

    CropData(EntityType type, int colStart, int rowY, int spriteH, int offsetY, double seconds) {
        this.type = type;
        this.colStart = colStart;
        this.rowY = rowY;
        this.spriteH = spriteH;
        this.offsetY = offsetY;
        this.growthInterval = Duration.seconds(seconds);
    }

    /** Tìm dữ liệu dựa trên EntityType */
    public static CropData fromType(EntityType type) {
        for (CropData data : values()) {
            if (data.type == type) return data;
        }
        return WHEAT; // mặc định
    }
}
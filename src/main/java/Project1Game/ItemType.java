package Project1Game;

public enum ItemType {
    HOE("Cuốc", "Crops/soil_1.png", null),
    WHEAT_SEED("Hạt lúa mì", "Crops/rice_1.png", "Wheat"),
    RADISH_SEED("Hạt củ cải", "Crops/rice_1.png", "Radish"),
    CABBAGE_SEED("Hạt bắp cải", "Crops/rice_1.png", "Cabbage"),
    LETTUCE_SEED("Hạt xà lách", "Crops/rice_1.png", "Lettuce"),
    TOMATO_SEED("Hạt cà chua", "Crops/rice_1.png", "Tomato"),
    CORN_SEED("Hạt ngô", "Crops/rice_1.png", "Corn"),
    WHEAT("Lúa mì", "Crops/rice_3.png", null),
    RADISH("Củ cải", "Crops/rice_3.png", null),
    CABBAGE("Bắp cải", "Crops/rice_3.png", null),
    LETTUCE("Xà lách", "Crops/rice_3.png", null),
    TOMATO("Cà chua", "Crops/rice_3.png", null),
    CORN("Ngô", "Crops/rice_3.png", null),
    WATERING_CAN("Bình tưới", "Crops/soil_2.png", null),
    EMPTY_5("Ô trống", "", null),
    EMPTY_6("Ô trống", "", null),
    EMPTY_7("Ô trống", "", null),
    EMPTY_8("Ô trống", "", null),
    EMPTY_9("Ô trống", "", null);

    private final String displayName;
    private final String iconName;
    private final String spawnName; // tên entity để spawn khi trồng

    ItemType(String displayName, String iconName, String spawnName) {
        this.displayName = displayName;
        this.iconName = iconName;
        this.spawnName = spawnName;
    }

    public String getDisplayName() { return displayName; }
    public String getIconName() { return iconName; }
    public String getSpawnName() { return spawnName; }

    public boolean isSeed() { return spawnName != null; }
}

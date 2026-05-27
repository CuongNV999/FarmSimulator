package Project1Game.core;

public enum ItemType {
    // Định nghĩa: Tên hiển thị, Icon, Tên Entity Spawn, Giá mua, Giá bán
    HOE("Cuốc", "Crops/soil_1.png", null, 0, 0), // Không mua bán
    WATERING_CAN("Bình tưới", "Crops/soil_2.png", null, 0, 0), // Không mua bán

    // Hạt giống
    WHEAT_SEED("Hạt lúa mì", "Crops/rice_1.png", "Wheat", 10, 5),
    RADISH_SEED("Hạt củ cải", "Crops/rice_1.png", "Radish", 12, 6),
    CABBAGE_SEED("Hạt bắp cải", "Crops/rice_1.png", "Cabbage", 15, 7),
    LETTUCE_SEED("Hạt xà lách", "Crops/rice_1.png", "Lettuce", 8, 4),
    TOMATO_SEED("Hạt cà chua", "Crops/rice_1.png", "Tomato", 13, 6),
    CORN_SEED("Hạt ngô", "Crops/rice_1.png", "Corn", 11, 5),

    // Nông sản
    WHEAT("Lúa mì", "Crops/rice_3.png", null, 0, 20), // Giá mua 0 vì không bán lại cho Trader
    RADISH("Củ cải", "Crops/rice_3.png", null, 0, 25),
    CABBAGE("Bắp cải", "Crops/rice_3.png", null, 0, 30),
    LETTUCE("Xà lách", "Crops/rice_3.png", null, 0, 15),
    TOMATO("Cà chua", "Crops/rice_3.png", null, 0, 28),
    CORN("Ngô", "Crops/rice_3.png", null, 0, 22),

    // Các ô trống (có thể bỏ qua giá mua/bán)
    EMPTY_5("Ô trống", "", null, 0, 0),
    EMPTY_6("Ô trống", "", null, 0, 0),
    EMPTY_7("Ô trống", "", null, 0, 0),
    EMPTY_8("Ô trống", "", null, 0, 0),
    EMPTY_9("Ô trống", "", null, 0, 0);

    private final String displayName;
    private final String iconName;
    private final String spawnName; // tên entity để spawn khi trồng
    private final int buyPrice;
    private final int sellPrice;

    ItemType(String displayName, String iconName, String spawnName, int buyPrice, int sellPrice) {
        this.displayName = displayName;
        this.iconName = iconName;
        this.spawnName = spawnName;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public String getDisplayName() { return displayName; }
    public String getIconName() { return iconName; }
    public String getSpawnName() { return spawnName; }
    public int getBuyPrice() { return buyPrice; }
    public int getSellPrice() { return sellPrice; }

    public boolean isSeed() { return spawnName != null; }
}
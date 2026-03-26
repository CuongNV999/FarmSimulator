package Project1Game;

public enum ItemType {
    HOE("Cuốc", "soil_1.png"),
    RICE_SEED("Hạt lúa", "rice_1.png"),
    RICE("Lúa", "rice_3.png"),
    WATERING_CAN("Bình tưới", "soil_2.png"),
    EMPTY_5("Ô trống", ""),
    EMPTY_6("Ô trống", ""),
    EMPTY_7("Ô trống", ""),
    EMPTY_8("Ô trống", ""),
    EMPTY_9("Ô trống", "");

    private final String displayName;
    private final String iconName;

    ItemType(String displayName, String iconName) {
        this.displayName = displayName;
        this.iconName = iconName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconName() {
        return iconName;
    }
}

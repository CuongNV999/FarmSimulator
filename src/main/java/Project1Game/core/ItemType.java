package Project1Game.core;

import Project1Game.model.item.Usable;
import Project1Game.model.item.HoeAction;
import Project1Game.model.item.WateringCanAction;
import Project1Game.model.item.PlantCropAction;
import Project1Game.model.item.PlaceAnimalAction;
import com.almasb.fxgl.entity.Entity;

public enum ItemType implements Usable {
    // Định nghĩa: Tên hiển thị, Icon, Tên Entity Spawn, Giá mua, Giá bán, Hành động sử dụng
    HOE("Cuốc", "Crops/soil_1.png", null, 0, 0, new HoeAction()),
    WATERING_CAN("Bình tưới", "Crops/soil_2.png", null, 0, 0, new WateringCanAction()),

    // Hạt giống
    WHEAT_SEED("Hạt lúa mì", "Crops/rice_1.png", "Wheat", 10, 5, new PlantCropAction()),
    RADISH_SEED("Hạt củ cải", "Crops/rice_1.png", "Radish", 40, 20, new PlantCropAction()),
    CABBAGE_SEED("Hạt bắp cải", "Crops/rice_1.png", "Cabbage", 60, 30, new PlantCropAction()),
    LETTUCE_SEED("Hạt xà lách", "Crops/rice_1.png", "Lettuce", 5, 2, new PlantCropAction()),
    TOMATO_SEED("Hạt cà chua", "Crops/rice_1.png", "Tomato", 25, 12, new PlantCropAction()),
    CORN_SEED("Hạt ngô", "Crops/rice_1.png", "Corn", 15, 7, new PlantCropAction()),

    // Nông sản
    WHEAT("Lúa mì", "Crops/rice_3.png", null, 0, 10, (player, target) -> {}),
    RADISH("Củ cải", "Crops/rice_3.png", null, 0, 45, (player, target) -> {}),
    CABBAGE("Bắp cải", "Crops/rice_3.png", null, 0, 50, (player, target) -> {}),
    LETTUCE("Xà lách", "Crops/rice_3.png", null, 0, 8, (player, target) -> {}),
    TOMATO("Cà chua", "Crops/rice_3.png", null, 0, 20, (player, target) -> {}),
    CORN("Ngô", "Crops/rice_3.png", null, 0, 15, (player, target) -> {}),

    // Baby Animals (Buyable)
    CHICK("Gà con", "Animal/Chick_animation_with_shadow.png", "Chick", 50, 0, new PlaceAnimalAction("Chick")),
    CALF("Bê", "Animal/Calf_animation_with_shadow.png", "Calf", 300, 0, new PlaceAnimalAction("Calf")),
    LAMB("Cừu non", "Animal/Lamb_animation_with_shadow.png", "Lamb", 150, 0, new PlaceAnimalAction("Lamb")),
    PIGLET("Heo con", "Animal/Piglet_animation_with_shadow.png", "Piglet", 200, 0, new PlaceAnimalAction("Piglet")),
    TURKEY("Gà tây", "Animal/Turkey_animation_with_shadow.png", "Turkey", 100, 220, new PlaceAnimalAction("Turkey")),

    // Mature Animals (Harvested and Sellable)
    ROOSTER("Gà trống", "Animal/Rooster_animation_with_shadow.png", null, 0, 120, (player, target) -> {}),
    BULL("Bò đực", "Animal/Bull_animation_with_shadow.png", null, 0, 650, (player, target) -> {}),
    SHEEP("Cừu trưởng thành", "Animal/Sheep_animation_with_shadow.png", null, 0, 350, (player, target) -> {}),
    PIG("Heo trưởng thành", "Animal/Piglet_animation_with_shadow.png", null, 0, 480, (player, target) -> {}),

    // Các ô trống (có thể bỏ qua giá mua/bán)
    EMPTY_5("Ô trống", "", null, 0, 0, (player, target) -> {}),
    EMPTY_6("Ô trống", "", null, 0, 0, (player, target) -> {}),
    EMPTY_7("Ô trống", "", null, 0, 0, (player, target) -> {}),
    EMPTY_8("Ô trống", "", null, 0, 0, (player, target) -> {}),
    EMPTY_9("Ô trống", "", null, 0, 0, (player, target) -> {});

    private final String displayName;
    private final String iconName;
    private final String spawnName; // tên entity để spawn khi trồng
    private final int buyPrice;
    private final int sellPrice;
    private final Usable action;

    ItemType(String displayName, String iconName, String spawnName, int buyPrice, int sellPrice, Usable action) {
        this.displayName = displayName;
        this.iconName = iconName;
        this.spawnName = spawnName;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.action = action;
    }

    public String getDisplayName() { return displayName; }
    public String getIconName() { return iconName; }
    public String getSpawnName() { return spawnName; }
    public int getBuyPrice() { return buyPrice; }
    public int getSellPrice() { return sellPrice; }

    public boolean isSeed() { return spawnName != null && name().endsWith("_SEED"); }

    @Override
    public void use(Entity player, Entity target) {
        action.use(player, target);
    }
}
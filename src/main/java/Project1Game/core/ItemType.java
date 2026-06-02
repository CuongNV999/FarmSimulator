package Project1Game.core;

import Project1Game.model.item.Usable;
import Project1Game.model.item.HoeAction;
import Project1Game.model.item.WateringCanAction;
import Project1Game.model.item.PlantCropAction;
import Project1Game.model.item.PlaceAnimalAction;
import com.almasb.fxgl.entity.Entity;

public enum ItemType implements Usable {

    // ===== CÔNG CỤ =====
    HOE          ("Cuốc",      "Crops/soil_1.png", null, 0, 0, new HoeAction()),
    WATERING_CAN ("Bình tưới", "Crops/soil_2.png", null, 0, 0, new WateringCanAction()),

    // ===== HẠT GIỐNG (6 cây gốc) =====
    // icon: Crops/OldCrops/<tên>/seed.png
    WHEAT_SEED   ("Hạt lúa mì",  "Crops/OldCrops/wheat/seed.png",   "Wheat",   10,  5, new PlantCropAction()),
    RADISH_SEED  ("Hạt củ cải",  "Crops/OldCrops/radish/seed.png",  "Radish",  40, 20, new PlantCropAction()),
    CABBAGE_SEED ("Hạt bắp cải", "Crops/OldCrops/cabbage/seed.png", "Cabbage", 60, 30, new PlantCropAction()),
    LETTUCE_SEED ("Hạt xà lách", "Crops/OldCrops/lettuce/seed.png", "Lettuce",  5,  2, new PlantCropAction()),
    TOMATO_SEED  ("Hạt cà chua", "Crops/OldCrops/tomato/seed.png",  "Tomato",  25, 12, new PlantCropAction()),
    CORN_SEED    ("Hạt ngô",     "Crops/OldCrops/corn/seed.png",    "Corn",    15,  7, new PlantCropAction()),

    // ===== HẠT GIỐNG (9 cây mới) =====
    // icon: Crops/NewCrops/<tên>/seed.png
    GRAPE_SEED      ("Hạt nho",       "Crops/NewCrops/grape/seed.png",       "Grape",       50, 25, new PlantCropAction()),
    CUCUMBER_SEED   ("Hạt dưa chuột", "Crops/NewCrops/cucumber/seed.png",    "Cucumber",    12,  6, new PlantCropAction()),
    PEPPER_SEED     ("Hạt ớt",        "Crops/NewCrops/pepper/seed.png",      "Pepper",      20, 10, new PlantCropAction()),
    CAULIFLOWER_SEED("Hạt súp lơ",    "Crops/NewCrops/cauliflower/seed.png", "Cauliflower", 30, 15, new PlantCropAction()),
    BEAN_SEED       ("Hạt đỗ",        "Crops/NewCrops/bean/seed.png",        "Bean",         8,  4, new PlantCropAction()),
    PINEAPPLE_SEED  ("Hạt dứa",       "Crops/NewCrops/pineapple/seed.png",   "Pineapple",   60, 30, new PlantCropAction()),
    SUNFLOWER_SEED  ("Hạt hướng dương","Crops/NewCrops/sunflower/seed.png",  "Sunflower",   25, 12, new PlantCropAction()),
    COCONUT_SEED    ("Hạt dừa",       "Crops/NewCrops/coconut/seed.png",     "Coconut",     80, 40, new PlantCropAction()),
    APPLE_SEED      ("Hạt táo",       "Crops/NewCrops/apple/seed.png",       "Apple",       45, 22, new PlantCropAction()),

    // ===== NÔNG SẢN (6 cây gốc) =====
    WHEAT   ("Lúa mì",  "Crops/OldCrops/wheat/harvest.png",   null, 0,  10, (p, t) -> {}),
    RADISH  ("Củ cải",  "Crops/OldCrops/radish/harvest.png",  null, 0,  45, (p, t) -> {}),
    CABBAGE ("Bắp cải", "Crops/OldCrops/cabbage/harvest.png", null, 0,  50, (p, t) -> {}),
    LETTUCE ("Xà lách", "Crops/OldCrops/lettuce/harvest.png", null, 0,   8, (p, t) -> {}),
    TOMATO  ("Cà chua", "Crops/OldCrops/tomato/harvest.png",  null, 0,  20, (p, t) -> {}),
    CORN    ("Ngô",     "Crops/OldCrops/corn/harvest.png",    null, 0,  15, (p, t) -> {}),

    // ===== NÔNG SẢN (9 cây mới) =====
    GRAPE      ("Nho",        "Crops/NewCrops/grape/harvest.png",       null, 0,  60, (p, t) -> {}),
    CUCUMBER   ("Dưa chuột",  "Crops/NewCrops/cucumber/harvest.png",    null, 0,  18, (p, t) -> {}),
    PEPPER     ("Ớt",         "Crops/NewCrops/pepper/harvest.png",      null, 0,  30, (p, t) -> {}),
    CAULIFLOWER("Súp lơ",     "Crops/NewCrops/cauliflower/harvest.png", null, 0,  55, (p, t) -> {}),
    BEAN       ("Đỗ",         "Crops/NewCrops/bean/harvest.png",        null, 0,  12, (p, t) -> {}),
    PINEAPPLE  ("Dứa",        "Crops/NewCrops/pineapple/harvest.png",   null, 0,  80, (p, t) -> {}),
    SUNFLOWER  ("Hướng dương","Crops/NewCrops/sunflower/harvest.png",   null, 0,  45, (p, t) -> {}),
    COCONUT    ("Dừa",        "Crops/NewCrops/coconut/harvest.png",     null, 0, 100, (p, t) -> {}),
    APPLE      ("Táo",        "Crops/NewCrops/apple/harvest.png",       null, 0,  70, (p, t) -> {}),

    // ===== ĐỘNG VẬT =====
    CHICK  ("Gà con",  "Animal/Chick_animation_with_shadow.png",  "Chick",   50,   0, new PlaceAnimalAction("Chick")),
    CALF   ("Bê",      "Animal/Calf_animation_with_shadow.png",   "Calf",   300,   0, new PlaceAnimalAction("Calf")),
    LAMB   ("Cừu non", "Animal/Lamb_animation_with_shadow.png",   "Lamb",   150,   0, new PlaceAnimalAction("Lamb")),
    PIGLET ("Heo con", "Animal/Piglet_animation_with_shadow.png", "Piglet", 200,   0, new PlaceAnimalAction("Piglet")),
    TURKEY ("Gà tây",  "Animal/Turkey_animation_with_shadow.png", "Turkey", 100, 220, new PlaceAnimalAction("Turkey")),

    ROOSTER("Gà trống",         "Animal/Rooster_animation_with_shadow.png", null, 0, 120, (p, t) -> {}),
    BULL   ("Bò đực",           "Animal/Bull_animation_with_shadow.png",    null, 0, 650, (p, t) -> {}),
    SHEEP  ("Cừu trưởng thành", "Animal/Sheep_animation_with_shadow.png",   null, 0, 350, (p, t) -> {}),
    PIG    ("Heo trưởng thành", "Animal/Piglet_animation_with_shadow.png",  null, 0, 480, (p, t) -> {}),

    // ===== Ô TRỐNG =====
    EMPTY_5("", "", null, 0, 0, (p, t) -> {}),
    EMPTY_6("", "", null, 0, 0, (p, t) -> {}),
    EMPTY_7("", "", null, 0, 0, (p, t) -> {}),
    EMPTY_8("", "", null, 0, 0, (p, t) -> {}),
    EMPTY_9("", "", null, 0, 0, (p, t) -> {});

    private final String displayName;
    private final String iconName;
    private final String spawnName;
    private final int buyPrice;
    private final int sellPrice;
    private final Usable action;

    ItemType(String displayName, String iconName, String spawnName,
             int buyPrice, int sellPrice, Usable action) {
        this.displayName = displayName;
        this.iconName    = iconName;
        this.spawnName   = spawnName;
        this.buyPrice    = buyPrice;
        this.sellPrice   = sellPrice;
        this.action      = action;
    }

    public String getDisplayName() { return displayName; }
    public String getIconName()    { return iconName; }
    public String getSpawnName()   { return spawnName; }
    public int getBuyPrice()       { return buyPrice; }
    public int getSellPrice()      { return sellPrice; }
    public boolean isSeed()        { return spawnName != null && name().endsWith("_SEED"); }

    @Override
    public void use(Entity player, Entity target) { action.use(player, target); }
}

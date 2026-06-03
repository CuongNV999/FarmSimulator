package Project1Game.core;

import Project1Game.model.item.Usable;
import Project1Game.model.item.HoeAction;
import Project1Game.model.item.WateringCanAction;
import Project1Game.model.item.PlantCropAction;
import Project1Game.model.item.PlaceAnimalAction;
import Project1Game.model.item.EatAction;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public enum ItemType implements Usable {
    // Định nghĩa: Tên hiển thị, Icon, Tên Entity Spawn, Giá mua, Giá bán, Hành động
    // sử dụng
    HOE("Cuốc", "Tool/hoe.png", null, 0, 0, new HoeAction()),
    WATERING_CAN("Bình tưới", "Tool/watering_can.png", null, 0, 0, new WateringCanAction()),

    // Hạt giống (6 cây gốc)
    WHEAT_SEED("Hạt lúa mì", "Crops/OldCrops/wheat/seed.png", "Wheat", 10, 5, new PlantCropAction()),
    RADISH_SEED("Hạt củ cải", "Crops/OldCrops/radish/seed.png", "Radish", 40, 20, new PlantCropAction()),
    CABBAGE_SEED("Hạt bắp cải", "Crops/OldCrops/cabbage/seed.png", "Cabbage", 60, 30, new PlantCropAction()),

    // Hạt giống (9 cây mới)
    GRAPE_SEED("Hạt nho", "Crops/NewCrops/grape/seed.png", "Grape", 50, 25, new PlantCropAction()),
    CUCUMBER_SEED("Hạt dưa chuột", "Crops/NewCrops/cucumber/seed.png", "Cucumber", 12, 6, new PlantCropAction()),
    PEPPER_SEED("Hạt ớt", "Crops/NewCrops/pepper/seed.png", "Pepper", 20, 10, new PlantCropAction()),
    CAULIFLOWER_SEED("Hạt súp lơ", "Crops/NewCrops/cauliflower/seed.png", "Cauliflower", 30, 15, new PlantCropAction()),
    BEAN_SEED("Hạt đỗ", "Crops/NewCrops/bean/seed.png", "Bean", 8, 4, new PlantCropAction()),
    PINEAPPLE_SEED("Hạt dứa", "Crops/NewCrops/pineapple/seed.png", "Pineapple", 60, 30, new PlantCropAction()),
    SUNFLOWER_SEED("Hạt hướng dương", "Crops/NewCrops/sunflower/seed.png", "Sunflower", 25, 12, new PlantCropAction()),
    COCONUT_SEED("Hạt dừa", "Crops/NewCrops/coconut/seed.png", "Coconut", 80, 40, new PlantCropAction()),
    APPLE_SEED("Hạt táo", "Crops/NewCrops/apple/seed.png", "Apple", 45, 22, new PlantCropAction()),

    // Nông sản (6 cây gốc)
    WHEAT("Lúa mì", "Crops/OldCrops/wheat/harvest.png", null, 0, 25, (player, target) -> {
    }),
    RADISH("Củ cải", "Crops/OldCrops/radish/harvest.png", null, 0, 90, (player, target) -> {
    }),
    CABBAGE("Bắp cải", "Crops/OldCrops/cabbage/harvest.png", null, 0, 150, (player, target) -> {
    }),

    // Nông sản (9 cây mới)
    GRAPE("Nho", "Crops/NewCrops/grape/harvest.png", null, 0, 60, (player, target) -> {
    }),
    CUCUMBER("Dưa chuột", "Crops/NewCrops/cucumber/harvest.png", null, 0, 18, (player, target) -> {
    }),
    PEPPER("Ớt", "Crops/NewCrops/pepper/harvest.png", null, 0, 30, (player, target) -> {
    }),
    CAULIFLOWER("Súp lơ", "Crops/NewCrops/cauliflower/harvest.png", null, 0, 55, (player, target) -> {
    }),
    BEAN("Đỗ", "Crops/NewCrops/bean/harvest.png", null, 0, 12, (player, target) -> {
    }),
    PINEAPPLE("Dứa", "Crops/NewCrops/pineapple/harvest.png", null, 0, 80, (player, target) -> {
    }),
    SUNFLOWER("Hướng dương", "Crops/NewCrops/sunflower/harvest.png", null, 0, 45, (player, target) -> {
    }),
    COCONUT("Dừa", "Crops/NewCrops/coconut/harvest.png", null, 0, 100, (player, target) -> {
    }),
    APPLE("Táo", "Crops/NewCrops/apple/harvest.png", null, 0, 70, (player, target) -> {
    }),

    // Bánh mì (Thực phẩm)
    BREAD_SLICE("Bánh mì lát", "food:20:1", null, 10, 4, new EatAction(8, 1)),
    BAGUETTE("Bánh mì dài", "food:20:3", null, 25, 12, new EatAction(20, 2)),
    BREAD_LOAF("Ổ bánh mì", "food:20:2", null, 40, 20, new EatAction(35, 4)),
    BREAD_BUN("Bánh mì tròn", "food:20:5", null, 15, 7, new EatAction(12, 1)),
    CROISSANT("Bánh sừng bò", "food:20:7", null, 20, 9, new EatAction(16, 2)),
    PRETZEL("Bánh Pretzel", "food:20:11", null, 18, 8, new EatAction(14, 2)),
    DONUT("Bánh Donut", "food:20:17", null, 12, 5, new EatAction(10, 1)),
    PANCAKE("Bánh Pancake", "food:20:18", null, 30, 15, new EatAction(25, 3)),

    // Thịt (Thực phẩm)
    COOKED_DRUMSTICK("Đùi gà chín", "food:25:1", null, 40, 22, new EatAction(25, 3)),

    COOKED_CHICKEN("Thịt gà chín", "food:25:2", null, 90, 50, new EatAction(45, 6)),

    COOKED_MEAT("Thịt bò chín", "food:25:5", null, 120, 75, new EatAction(60, 8)),

    SAUSAGE("Xúc xích", "food:25:6", null, 45, 25, new EatAction(30, 4)),

    // Baby Animals (Buyable)
    CHICK("Gà con", "Animal/Chick_animation_with_shadow.png", "Chick", 50, 0, new PlaceAnimalAction("Chick")),
    CALF("Bê", "Animal/Calf_animation_with_shadow.png", "Calf", 300, 0, new PlaceAnimalAction("Calf")),
    LAMB("Cừu non", "Animal/Lamb_animation_with_shadow.png", "Lamb", 150, 0, new PlaceAnimalAction("Lamb")),
    PIGLET("Heo con", "Animal/Piglet_animation_with_shadow.png", "Piglet", 200, 0, new PlaceAnimalAction("Piglet")),
    TURKEY("Gà tây", "Animal/Turkey_animation_with_shadow.png", "Turkey", 100, 220, new PlaceAnimalAction("Turkey")),

    // Mature Animals (Harvested and Sellable)
    ROOSTER("Gà trống", "Animal/Rooster_animation_with_shadow.png", null, 0, 120, (player, target) -> {
    }),
    BULL("Bò đực", "Animal/Bull_animation_with_shadow.png", "Bull", 800, 650, new PlaceAnimalAction("Bull")),
    SHEEP("Cừu trưởng thành", "Animal/Sheep_animation_with_shadow.png", null, 0, 350, (player, target) -> {
    }),
    PIG("Heo trưởng thành", "Animal/Piglet_animation_with_shadow.png", null, 0, 480, (player, target) -> {
    }),

    // Các ô trống (có thể bỏ qua giá mua/bán)
    EMPTY_5("Ô trống", "", null, 0, 0, (player, target) -> {
    }),
    EMPTY_6("Ô trống", "", null, 0, 0, (player, target) -> {
    }),
    EMPTY_7("Ô trống", "", null, 0, 0, (player, target) -> {
    }),
    EMPTY_8("Ô trống", "", null, 0, 0, (player, target) -> {
    }),
    EMPTY_9("Ô trống", "", null, 0, 0, (player, target) -> {
    });

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

    public String getDisplayName() {
        return displayName;
    }

    public String getIconName() {
        return iconName;
    }

    public String getSpawnName() {
        return spawnName;
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public boolean isSeed() {
        return spawnName != null && name().endsWith("_SEED");
    }

    public static Image extractFoodImage(String iconName) {
        try {
            String[] parts = iconName.split(":");
            int rowIdx = Integer.parseInt(parts[1]);
            int colIdx = Integer.parseInt(parts[2]);
            Image fullImage = FXGL.image("Crops/food.png");
            int cellSize = 32;
            int x = colIdx * cellSize;
            int y = rowIdx * cellSize;
            return new WritableImage(fullImage.getPixelReader(), x, y, cellSize, cellSize);
        } catch (Exception e) {
            System.err.println("Error extracting food image for iconName " + iconName + ": " + e.getMessage());
            return FXGL.image("empty.png");
        }
    }

    @Override
    public void use(Entity player, Entity target) {
        action.use(player, target);
    }
}
package Project1Game.config;

import Project1Game.component.farming.animal.BaseAnimalComponent.AnimalType;
import Project1Game.core.ItemType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AnimalConfigRegistry {
    private static AnimalConfigRegistry instance;
    private final Map<String, AnimalConfig> registry = new HashMap<>();

    private AnimalConfigRegistry() {
        // Register Chicken
        AnimalConfig chickenConfig = new AnimalConfig(
            AnimalType.CHICKEN, "Gà", "Gà con", "Rooster", 4,
            "Animal/Chick_animation_with_shadow.png", "Animal/Rooster_animation_with_shadow.png",
            ItemType.ROOSTER, 16, 16, 32, 32
        );
        register("chicken", chickenConfig);
        register("chick", chickenConfig);
        register("rooster", chickenConfig);

        // Register Cow
        AnimalConfig cowConfig = new AnimalConfig(
            AnimalType.COW, "Bò", "Bê", "Bull", 7,
            "Animal/Calf_animation_with_shadow.png", "Animal/Bull_animation_with_shadow.png",
            ItemType.BULL, 64, 64, 64, 64
        );
        register("cow", cowConfig);
        register("calf", cowConfig);
        register("bull", cowConfig);

        // Register Sheep
        AnimalConfig sheepConfig = new AnimalConfig(
            AnimalType.SHEEP, "Cừu", "Cừu non", "Sheep", 5,
            "Animal/Lamb_animation_with_shadow.png", "Animal/Sheep_animation_with_shadow.png",
            ItemType.SHEEP, 32, 32, 32, 32
        );
        register("sheep", sheepConfig);
        register("lamb", sheepConfig);

        // Register Pig
        AnimalConfig pigConfig = new AnimalConfig(
            AnimalType.PIG, "Heo", "Heo con", "Pig", 6,
            "Animal/Piglet_animation_with_shadow.png", "Animal/Piglet_animation_with_shadow.png",
            ItemType.PIG, 32, 32, 32, 32
        );
        register("pig", pigConfig);
        register("piglet", pigConfig);

        // Register Turkey
        AnimalConfig turkeyConfig = new AnimalConfig(
            AnimalType.TURKEY, "Gà tây", "Gà tây con", "Gà tây trưởng thành", 3,
            "Animal/Turkey_animation_with_shadow.png", "Animal/Turkey_animation_with_shadow.png",
            ItemType.TURKEY, 32, 32, 32, 32
        );
        register("turkey", turkeyConfig);
        register("turkeychick", turkeyConfig);
    }

    public static synchronized AnimalConfigRegistry getInstance() {
        if (instance == null) {
            instance = new AnimalConfigRegistry();
        }
        return instance;
    }

    public void register(String key, AnimalConfig config) {
        registry.put(key.toLowerCase(), config);
    }

    public Optional<AnimalConfig> getConfig(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(registry.get(key.toLowerCase()));
    }
}

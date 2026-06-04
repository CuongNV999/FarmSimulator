package Project1Game.core;

import java.util.EnumSet;
import java.util.Set;
import java.util.Collections;

/**
 * Centralized registry for all crop entity types in the game.
 * Resolves the Open/Closed Principle violation by providing a single source of truth.
 */
public class CropRegistry {
    private static CropRegistry instance;
    private final Set<EntityType> cropTypes;

    private CropRegistry() {
        cropTypes = EnumSet.of(
            EntityType.WHEAT, EntityType.RADISH, EntityType.CABBAGE,
            EntityType.GRAPE, EntityType.CUCUMBER, EntityType.PEPPER,
            EntityType.CAULIFLOWER, EntityType.BEAN, EntityType.PINEAPPLE,
            EntityType.SUNFLOWER, EntityType.COCONUT, EntityType.APPLE
        );
    }

    public static synchronized CropRegistry getInstance() {
        if (instance == null) {
            instance = new CropRegistry();
        }
        return instance;
    }

    public Set<EntityType> getSupportedCrops() {
        return Collections.unmodifiableSet(cropTypes);
    }

    public boolean isCrop(EntityType type) {
        return cropTypes.contains(type);
    }
}

package Project1Game.config;

import Project1Game.component.farming.animal.BaseAnimalComponent;

import Project1Game.core.ItemType;

public record AnimalConfig(
    BaseAnimalComponent.AnimalType type,
    String animalName,
    String babyName,
    String adultName,
    int maxGrowthDays,
    String babyTexture,
    String adultTexture,
    ItemType adultItem,
    int babyWidth,
    int babyHeight,
    int adultWidth,
    int adultHeight
) {}

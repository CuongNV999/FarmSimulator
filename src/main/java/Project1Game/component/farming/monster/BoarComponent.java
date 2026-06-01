package Project1Game.component.farming.monster;

import Project1Game.component.farming.animal.BaseAnimalComponent;
import Project1Game.component.farming.animal.BaseAnimalComponent.AnimalType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

/**
 * Boar (Lợn lòi) - Carnivore.
 */
public class BoarComponent extends BaseMonsterComponent {
    public BoarComponent() {
        super(MonsterGroup.CARNIVORE);
        this.speed = 50.0;
    }

    @Override
    public boolean isValidPrey(Entity animal) {
        BaseAnimalComponent bac = animal.getComponentOptional(BaseAnimalComponent.class).orElse(null);
        if (bac != null) {
            AnimalType type = bac.getType();
            return type == AnimalType.CHICKEN || type == AnimalType.TURKEY || type == AnimalType.PIG;
        }
        return false;
    }

    @Override
    public void consume(Entity target) {
        target.removeFromWorld();
        FXGL.getNotificationService().pushNotification("Một con Lợn lòi (Boar) đã ăn thịt động vật của bạn!");
        System.gc();
    }
}

package Project1Game.component.farming.monster;

import Project1Game.component.farming.CropComponent;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

/**
 * Deer (Nai) - Herbivore.
 */
public class DeerComponent extends BaseMonsterComponent {
    public DeerComponent() {
        super(MonsterGroup.HERBIVORE);
        this.speed = 45.0;
    }

    @Override
    public boolean isValidPrey(Entity crop) {
        CropComponent cc = crop.getComponentOptional(CropComponent.class).orElse(null);
        return cc != null;
    }

    @Override
    public void consume(Entity target) {
        target.removeFromWorld();
        FXGL.getNotificationService().pushNotification("Một con Nai (Deer) đã ăn trộm nông sản!");
        System.gc();
    }
}

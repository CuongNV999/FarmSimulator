package Project1Game.component.farming.monster;

import Project1Game.component.farming.CropComponent;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

/**
 * Hare (Thỏ rừng) - Herbivore.
 */
public class HareComponent extends BaseMonsterComponent {
    public HareComponent() {
        super(MonsterGroup.HERBIVORE);
        this.speed = 55.0;
    }

    @Override
    public boolean isValidPrey(Entity crop) {
        return crop.hasComponent(CropComponent.class);
    }

    @Override
    public void consume(Entity target) {
        target.removeFromWorld();
        FXGL.getNotificationService().pushNotification("Một con Thỏ (Hare) đã gặm mất cây trồng!");
        System.gc();
    }
}

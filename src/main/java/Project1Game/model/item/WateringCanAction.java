package Project1Game.model.item;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import Project1Game.core.EntityType;
import Project1Game.component.farming.SoilComponent;
import Project1Game.quest.QuestContext;
import Project1Game.quest.QuestManager;

public class WateringCanAction implements Usable {
    @Override
    public void use(Entity player, Entity target) {
        if (target == null || target.getViewComponent().getOpacity() < 1.0) return;
        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(target.getPosition()) < 15)
                .findFirst().ifPresent(soil -> {
                    soil.getComponent(SoilComponent.class).setWet(true);
                    QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.WATER, null));
                    if (Project1Game.Main.getInstance() != null) {
                        Project1Game.Main.getInstance().drainHungerForWork(1.0);
                    }
                });
    }
}

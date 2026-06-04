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
        if (Project1Game.Main.getInstance() != null) {
            Project1Game.Main.getInstance().getFarmingSystem().useWateringCan(target.getPosition());
        }
    }
}

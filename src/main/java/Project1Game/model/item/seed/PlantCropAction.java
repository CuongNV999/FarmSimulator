package Project1Game.model.item.seed;

import Project1Game.model.item.Usable;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import Project1Game.core.EntityType;
import Project1Game.core.ItemType;
import Project1Game.component.farming.SoilComponent;
import Project1Game.quest.QuestContext;
import Project1Game.quest.QuestManager;

public class PlantCropAction implements Usable {
    @Override
    public void use(Entity player, Entity target) {
        if (target == null || target.getViewComponent().getOpacity() < 1.0) return;
        
        Project1Game.model.Inventory inventory = Project1Game.Main.getInstance().getInventory();
        if (inventory == null) return;

        ItemType seedType = inventory.getSelectedItem();
        if (seedType == null || !seedType.isSeed() || inventory.getCount(seedType) <= 0) return;

        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(target.getPosition()) < 10
                        && s.getComponent(SoilComponent.class).canPlant())
                .findFirst().ifPresent(soil -> {
                    FXGL.spawn(seedType.getSpawnName(), soil.getX(), soil.getY());
                    soil.getComponent(SoilComponent.class).setHasPlant(true);
                    inventory.removeItem(seedType, 1);
                    QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.PLANT, seedType));
                });
    }
}

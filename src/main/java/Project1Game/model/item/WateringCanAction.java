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
        
        System.out.println("WateringCan used at target position: " + target.getPosition());
        
        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> {
                    double distance = s.getPosition().distance(target.getPosition());
                    System.out.println("Soil at " + s.getPosition() + ", distance: " + distance);
                    return distance < 15;
                })
                .findFirst().ifPresent(soil -> {
                    System.out.println("Found soil to water at: " + soil.getPosition());
                    SoilComponent soilComp = soil.getComponent(SoilComponent.class);
                    if (soilComp != null) {
                        System.out.println("Before watering - isWet: " + soilComp.isWet());
                        soilComp.setWet(true);
                        System.out.println("After watering - isWet: " + soilComp.isWet());
                    } else {
                        System.out.println("ERROR: Soil entity has no SoilComponent!");
                    }
                    QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.WATER, null));
                    if (Project1Game.Main.getInstance() != null) {
                        Project1Game.Main.getInstance().drainHungerForWork(1.0);
                    }
                });
    }
}

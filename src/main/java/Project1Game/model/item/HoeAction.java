package Project1Game.model.item;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import Project1Game.core.EntityType;
import Project1Game.component.farming.SoilComponent;
import javafx.geometry.Point2D;

public class HoeAction implements Usable {
    @Override
    public void use(Entity player, Entity target) {
        if (target == null || target.getViewComponent().getOpacity() < 1.0) return;

        double selX = target.getX();
        double selY = target.getY();

        boolean inField = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD).stream()
                .anyMatch(f -> f.getX() <= selX && selX < f.getX() + f.getWidth()
                        && f.getY() <= selY && selY < f.getY() + f.getHeight());

        if (inField) {
            boolean hasSoil = FXGL.getGameWorld().getEntitiesAt(new Point2D(selX + 16, selY + 16)).stream()
                    .anyMatch(e -> e.isType(EntityType.SOIL));

            if (!hasSoil) {
                FXGL.spawn("Soil", selX, selY);
                FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL)
                        .forEach(s -> s.getComponent(SoilComponent.class).updateTexture());
                if (Project1Game.Main.getInstance() != null) {
                    Project1Game.Main.getInstance().drainHungerForWork(1.0);
                }
            }
        }
    }
}

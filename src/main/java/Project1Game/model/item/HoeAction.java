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
            boolean hasSoil = FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                    .anyMatch(s -> Math.round(s.getX() / 32.0) == Math.round(selX / 32.0)
                            && Math.round(s.getY() / 32.0) == Math.round(selY / 32.0));

            if (!hasSoil) {
                FXGL.spawn("Soil", selX, selY);
                if (Project1Game.Main.getInstance() != null) {
                    Project1Game.Main.getInstance().drainHungerForWork(1.0);
                }
            }
        }
    }
}

package Project1Game.component.farming;

import Project1Game.Main;
import Project1Game.config.CropData;
import Project1Game.core.EntityType;
import Project1Game.system.DayNightEvent;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.TimerAction;

public class CropComponent extends Component {
    private int stage = 0;
    private boolean canGrow = true;
    private TimerAction growTimer;

    private final CropData data;

    private javafx.event.EventHandler<DayNightEvent> nightHandler;
    private javafx.event.EventHandler<DayNightEvent> dayHandler;

    public CropComponent(CropData data) {
        this.data = data;
    }

    @Override
    public void onAdded() {
        updateView();
        growTimer = FXGL.getGameTimer().runAtInterval(this::grow, data.growthTime);
        canGrow = Main.isDayTime();

        nightHandler = e -> canGrow = false;
        dayHandler   = e -> canGrow = true;

        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_NIGHT, nightHandler);
        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_DAY,   dayHandler);
    }

    @Override
    public void onRemoved() {
        if (growTimer != null)    growTimer.expire();
        if (nightHandler != null) FXGL.getEventBus().removeEventHandler(DayNightEvent.SET_NIGHT, nightHandler);
        if (dayHandler != null)   FXGL.getEventBus().removeEventHandler(DayNightEvent.SET_DAY,   dayHandler);
    }

    private void updateView() {
        Texture frame = FXGL.texture(data.getSpriteForStage(stage));
        getEntity().getViewComponent().clearChildren();
        getEntity().getViewComponent().addChild(frame);
    }

    public void grow() {
        if (!canGrow) return;
        if (stage >= CropData.STAGE_RIPE) return;

        Entity soil = FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(entity.getPosition()) < 5)
                .findFirst()
                .orElse(null);
        if (soil == null) return;
        SoilComponent sc = soil.getComponent(SoilComponent.class);
        if (!sc.isWet()) return;

        if (Project1Game.system.WeatherSystem.getCurrentWeather()
                == Project1Game.system.WeatherSystem.Weather.DROUGHT) {
            if (Math.random() < 0.3) return; // Reduced from 50% to 30%
        }

        stage++;
        updateView();
        boolean isRaining = (Project1Game.system.WeatherSystem.getCurrentWeather()
                == Project1Game.system.WeatherSystem.Weather.RAINY);
        if (!isRaining) sc.setWet(false);
    }

    /** Thu hoạch: trả về nông sản và chuyển cây sang trạng thái đã hái (harvest.png) */
    public void harvest() {
        if (stage == CropData.STAGE_HARVESTED) return;
        stage = CropData.STAGE_HARVESTED;
        updateView();

        // Notify soil it's empty
        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(entity.getPosition()) < 10)
                .forEach(s -> s.getComponent(SoilComponent.class).setHasPlant(false));

        // Immediately remove entity to prevent re-clicking
        FXGL.runOnce(() -> {
            if (entity != null && entity.isActive()) {
                entity.removeFromWorld();
            }
        }, javafx.util.Duration.seconds(0.5));
    }

    public int getStage()           { return stage; }
    public void setStage(int s)     { this.stage = s; updateView(); }
    /** Cây chín = stage3, có thể thu hoạch */
    public boolean isRipe()         { return stage == CropData.STAGE_RIPE; }
    /** Cây đã bị hái rồi */
    public boolean isHarvested()    { return stage == CropData.STAGE_HARVESTED; }
    public CropData getData()       { return data; }
}

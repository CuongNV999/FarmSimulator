package Project1Game.component.farming;

import com.almasb.fxgl.entity.component.Component;
import Project1Game.component.farming.state.SoilState;
import Project1Game.component.farming.state.TilledDryState;
import Project1Game.component.farming.state.TilledWetState;

public class SoilComponent extends Component {
    private boolean hasPlant = false;
    private SoilState currentState = new TilledDryState();

    public boolean canPlant() {
        return !hasPlant && currentState.canPlant();
    }

    public void setHasPlant(boolean hasPlant) {
        this.hasPlant = hasPlant;
    }

    public boolean isHasPlant() {
        return hasPlant;
    }

    public boolean isWet() {
        return currentState.isWet() || (Project1Game.system.WeatherSystem.getCurrentWeather() == Project1Game.system.WeatherSystem.Weather.RAINY);
    }

    public void setWet(boolean wet) {
        if (wet) {
            currentState = currentState.water();
        } else {
            currentState = new TilledDryState();
        }
        updateTexture(); // Cập nhật lại hình ảnh ngay khi tưới
    }

    public void updateTexture() {
        if (entity == null) return;
        entity.getViewComponent().clearChildren();
        if (isWet()) {
            entity.getViewComponent().addChild(new Project1Game.component.farming.state.TilledWetState().getTexture(entity.getX()));
        } else {
            entity.getViewComponent().addChild(currentState.getTexture(entity.getX()));
        }
    }

    private javafx.event.EventHandler<Project1Game.system.DayNightEvent> dayHandler;

    @Override
    public void onAdded() {
        updateTexture();

        dayHandler = e -> {
            boolean isRaining = (Project1Game.system.WeatherSystem.getCurrentWeather() == Project1Game.system.WeatherSystem.Weather.RAINY);
            if (!isRaining && !hasPlant) {
                setWet(false);
            }
        };
        com.almasb.fxgl.dsl.FXGL.getEventBus().addEventHandler(Project1Game.system.DayNightEvent.SET_DAY, dayHandler);
    }

    @Override
    public void onRemoved() {
        if (dayHandler != null) {
            com.almasb.fxgl.dsl.FXGL.getEventBus().removeEventHandler(Project1Game.system.DayNightEvent.SET_DAY, dayHandler);
        }
    }
}
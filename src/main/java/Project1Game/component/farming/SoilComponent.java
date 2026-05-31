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
        entity.getViewComponent().addChild(currentState.getTexture(entity.getX()));
    }

    @Override
    public void onAdded() {
        updateTexture();
    }
}
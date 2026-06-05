package Project1Game.component.farming;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.effect.ColorAdjust;

public class SoilComponent extends Component {
    public enum State {
        TILLED_DRY,
        TILLED_WET
    }

    private boolean hasPlant = false;
    private State state = State.TILLED_DRY;

    public boolean canPlant() {
        return !hasPlant;
    }

    public void setHasPlant(boolean hasPlant) {
        this.hasPlant = hasPlant;
    }

    public boolean isHasPlant() {
        return hasPlant;
    }

    public boolean isWet() {
        return state == State.TILLED_WET || (Project1Game.system.WeatherSystem.getCurrentWeather() == Project1Game.system.WeatherSystem.Weather.RAINY);
    }

    public void setWet(boolean wet) {
        if (wet) {
            state = State.TILLED_WET;
            System.out.println("Soil watered: State changed to TILLED_WET");
        } else {
            state = State.TILLED_DRY;
        }
        updateTexture();
    }

    public void updateTexture() {
        if (entity == null) return;
        entity.getViewComponent().clearChildren();

        int gridX = (int) Math.round(entity.getX() / 32.0);
        String textureName = (gridX % 2 == 0) ? "Crops/soil_1.png" : "Crops/soil_2.png";
        Texture texture = FXGL.texture(textureName);

        if (isWet()) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.4);
            darken.setContrast(0.2);
            texture.setEffect(darken);
        }
        entity.getViewComponent().addChild(texture);
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
        FXGL.getEventBus().addEventHandler(Project1Game.system.DayNightEvent.SET_DAY, dayHandler);
    }

    @Override
    public void onRemoved() {
        if (dayHandler != null) {
            FXGL.getEventBus().removeEventHandler(Project1Game.system.DayNightEvent.SET_DAY, dayHandler);
        }
    }
}
package Project1Game.component.farming.state;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.effect.ColorAdjust;

public class TilledWetState implements SoilState {
    @Override
    public boolean isWet() {
        return true;
    }

    @Override
    public boolean canPlant() {
        return true;
    }

    @Override
    public Texture getTexture(double entityX) {
        int gridX = (int) Math.round(entityX / 32.0);
        String textureName = (gridX % 2 == 0) ? "Crops/soil_1.png" : "Crops/soil_2.png";
        Texture texture = FXGL.texture(textureName);
        ColorAdjust darken = new ColorAdjust();
        darken.setBrightness(-0.4); // Giảm độ sáng
        darken.setContrast(0.2);    // Tăng tương phản
        texture.setEffect(darken);
        return texture;
    }

    @Override
    public SoilState water() {
        return this;
    }

    @Override
    public SoilState till() {
        return this;
    }
}

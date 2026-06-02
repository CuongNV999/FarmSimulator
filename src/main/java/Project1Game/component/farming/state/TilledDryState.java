package Project1Game.component.farming.state;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;

public class TilledDryState implements SoilState {
    @Override
    public boolean isWet() {
        return false;
    }

    @Override
    public boolean canPlant() {
        return true;
    }

    @Override
    public Texture getTexture(double entityX) {
        int gridX = (int) Math.round(entityX / 32.0);
        String textureName = (gridX % 2 == 0) ? "Crops/soil_1.png" : "Crops/soil_2.png";
        return FXGL.texture(textureName);
    }

    @Override
    public SoilState water() {
        return new TilledWetState();
    }

    @Override
    public SoilState till() {
        return this;
    }
}

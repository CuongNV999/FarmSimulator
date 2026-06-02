package Project1Game.component.farming.state;

import com.almasb.fxgl.texture.Texture;

public interface SoilState {
    boolean isWet();
    boolean canPlant();
    Texture getTexture(double entityX);
    SoilState water();
    SoilState till();
}

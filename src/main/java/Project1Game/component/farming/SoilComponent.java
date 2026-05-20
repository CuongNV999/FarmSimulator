package Project1Game.component.farming;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.effect.ColorAdjust;

public class SoilComponent extends Component {
    private boolean hasPlant = false;
    private boolean isWet = false; // Trạng thái đất ướt

    public boolean canPlant() { return !hasPlant; }
    public void setHasPlant(boolean hasPlant) { this.hasPlant = hasPlant; }
    public boolean isHasPlant() { return hasPlant; }
    public boolean isWet() { return isWet; }

    public void setWet(boolean wet) {
        this.isWet = wet;
        updateTexture(); // Cập nhật lại hình ảnh ngay khi tưới
    }

    public void updateTexture() {
        int tileSize = 32;
        int gridX = (int) Math.round(entity.getX() / tileSize);

        String textureName = (gridX % 2 == 0) ? "Crops/soil_1.png" : "Crops/soil_2.png";
        Texture texture = FXGL.texture(textureName);

        // Nếu đất ướt, ta dùng ColorAdjust để làm ảnh tối và xanh hơn một chút (giả lập đất ướt)
        if (isWet) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.4); // Giảm độ sáng
            darken.setContrast(0.2);    // Tăng tương phản
            texture.setEffect(darken);
        }

        entity.getViewComponent().clearChildren();
        entity.getViewComponent().addChild(texture);
    }
}
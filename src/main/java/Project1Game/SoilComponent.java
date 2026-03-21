package Project1Game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Rectangle2D;

import java.util.List;

public class SoilComponent extends Component {
    private boolean hasPlant = false;

    public boolean canPlant() {
        return !hasPlant;
    }

    public void setHasPlant(boolean hasPlant) {
        this.hasPlant = hasPlant;
    }

    @Override
    public void onAdded() {
        // Không gọi updateTexture() ở đây vì entity có thể chưa có position chính xác
        // updateTexture() sẽ được gọi từ Main.initGame() sau khi tất cả entity đã được tạo
    }

    public void updateTexture() {
        int tileSize = 32;

        // Dùng Math.round để tránh lỗi làm tròn floating point
        int gridX = (int) Math.round(entity.getX() / tileSize);
        int gridY = (int) Math.round(entity.getY() / tileSize);

        // Xen kẽ soil_1 và soil_2 kiểu bàn cờ
        String textureName;
        if (gridX % 2 == 0) {
            textureName = "soil_1.png";
        } else {
            textureName = "soil_2.png";
        }

        Texture texture = FXGL.texture(textureName);

        entity.getViewComponent().clearChildren();
        entity.getViewComponent().addChild(texture);
    }
}

package Project1Game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.TimerAction;
import javafx.geometry.Rectangle2D;

public class CropComponent extends Component {
    private int stage = 0;
    private final int MAX_STAGE = 3;
    private TimerAction growTimer;

    private static final String CROPS_FILE = "Crops/vectoraith_tileset_farmingsims_crops_dense_spring_32x32.png";
    private static final int TILE = 32;

    private final CropData data;

    public CropComponent(CropData data) {
        this.data = data;
    }

    @Override
    public void onAdded() {
        updateView();
        // Sử dụng thời gian lớn riêng biệt của từng loại cây
        growTimer = FXGL.getGameTimer().runAtInterval(this::grow, data.growthInterval);
    }

    @Override
    public void onRemoved() {
        if (growTimer != null) growTimer.expire();
    }

    private void updateView() {
        Texture frame = FXGL.texture(CROPS_FILE)
                .subTexture(new Rectangle2D((data.colStart + stage) * TILE, data.rowY, TILE, data.spriteH));
        
        if (data.offsetY != 0) {
            frame.setTranslateY(data.offsetY);
        }
        
        getEntity().getViewComponent().clearChildren();
        getEntity().getViewComponent().addChild(frame);
    }

    public void grow() {
        if (stage < MAX_STAGE) {
            // Tìm thực thể SOIL tại cùng vị trí với cây
            FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                    .filter(s -> s.getPosition().distance(entity.getPosition()) < 5) // Tìm ô đất khớp vị trí
                    .findFirst()
                    .ifPresent(soil -> {
                        SoilComponent sc = soil.getComponent(SoilComponent.class);
                        
                        // Chỉ lớn lên nếu đất ĐANG ƯỚT
                        if (sc.isWet()) {
                            stage++;
                            updateView();
                            
                            // Sau khi cây hút nước để lớn, đất sẽ KHÔ đi
                            sc.setWet(false); 
                            System.out.println(data.type + " đã lớn thêm 1 bậc nhờ có nước!");
                        } else {
                            System.out.println(data.type + " không thể lớn vì đất khô!");
                        }
                    });
        }
    }

    public boolean isRipe() {
        return stage == MAX_STAGE;
    }
}
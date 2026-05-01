package Project1Game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.TimerAction;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

/**
 * Generic crop component — dùng chung cho tất cả loại nông sản.
 * Mỗi loại chỉ khác nhau ở vị trí sprite trong tileset.
 */
public class CropComponent extends Component {
    private int stage = 0;
    private final int MAX_STAGE = 3;
    private TimerAction growTimer;

    private static final String CROPS_FILE = "Crops/vectoraith_tileset_farmingsims_crops_dense_spring_32x32.png";
    private static final int TILE = 32;

    private final int colStart;
    private final int rowY;
    private final int spriteH;
    private final int offsetY; // dịch dọc sprite so với entity position

    public CropComponent(int colStart, int rowY, int spriteH, int offsetY) {
        this.colStart = colStart;
        this.rowY = rowY;
        this.spriteH = spriteH;
        this.offsetY = offsetY;
    }

    /** Convenience constructor không cần offset (offsetY = 0) */
    public CropComponent(int colStart, int rowY, int spriteH) {
        this(colStart, rowY, spriteH, 0);
    }

    @Override
    public void onAdded() {
        updateView();
        growTimer = FXGL.getGameTimer().runAtInterval(this::grow, Duration.seconds(10));
    }

    @Override
    public void onRemoved() {
        if (growTimer != null) growTimer.expire();
    }

    private void updateView() {
        Texture frame = FXGL.texture(CROPS_FILE)
                .subTexture(new Rectangle2D((colStart + stage) * TILE, rowY, TILE, spriteH));
        if (offsetY != 0) frame.setTranslateY(offsetY);
        getEntity().getViewComponent().clearChildren();
        getEntity().getViewComponent().addChild(frame);
    }

    public void grow() {
        if (stage < MAX_STAGE) {
            stage++;
            updateView();
        }
    }

    public boolean isRipe() {
        return stage == MAX_STAGE;
    }
}

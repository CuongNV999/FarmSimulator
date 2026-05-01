package Project1Game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.TimerAction;

import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

public class WheatComponent extends Component {
    private int stage = 0; // 0-3, 4 giai đoạn
    private final int MAX_STAGE = 3;
    private TimerAction growTimer;

    private static final String CROPS_FILE = "Crops/vectoraith_tileset_farmingsims_crops_dense_spring_32x32.png";
    private static final int TILE = 32;
    private static final int ROW_Y = 6 * TILE; // bắt đầu từ hàng 6 để lấy đủ 2 hàng cao
    private static final int SPRITE_W = 32;
    private static final int SPRITE_H = 64;    // sprite cao 2 tile
    private static final int COL_START = 0;

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
                .subTexture(new Rectangle2D((COL_START + stage) * SPRITE_W, ROW_Y, SPRITE_W, SPRITE_H));
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

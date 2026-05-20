package Project1Game.component.farming;

import Project1Game.Main;
import Project1Game.config.CropData;
import Project1Game.core.EntityType;
import Project1Game.system.DayNightEvent;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.TimerAction;
import javafx.geometry.Rectangle2D;

public class CropComponent extends Component {
    private int stage = 0;
    private final int MAX_STAGE = 3;
    private TimerAction growTimer;

    private boolean canGrow = true;

    private static final String CROPS_FILE = "Crops/vectoraith_tileset_farmingsims_crops_dense_spring_32x32.png";
    private static final int TILE = 32;

    private final CropData data;

    public CropComponent(CropData data) {
        this.data = data;
    }

    @Override
    public void onAdded() {
        updateView();
        growTimer = FXGL.getGameTimer().runAtInterval(this::grow, data.growthInterval);

        // Đảm bảo ban đầu kiểm tra đúng trạng thái ngày đêm
        canGrow = Main.isDayTime();

        // Lắng nghe sự kiện từ EventBus
        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_NIGHT, e -> {
            canGrow = false;
            System.out.println(data.type + " ngừng lớn vì trời tối.");
        });

        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_DAY, e -> {
            canGrow = true;
            System.out.println(data.type + " bắt đầu lớn lại vì có nắng.");
        });
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
        if (!canGrow) {
            return;
        }

        if (stage < MAX_STAGE) {
            FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                    .filter(s -> s.getPosition().distance(entity.getPosition()) < 5)
                    .findFirst()
                    .ifPresent(soil -> {
                        SoilComponent sc = soil.getComponent(SoilComponent.class);
                        if (sc.isWet()) {
                            stage++;
                            updateView();
                            sc.setWet(false);
                        }
                    });
        }
    }
    public int getStage() { return stage; }
    public void setStage(int stage) {
        this.stage = stage;
        updateView();
    }
    public boolean isRipe() {
        return stage == MAX_STAGE;
    }
}
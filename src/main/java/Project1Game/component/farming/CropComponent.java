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

    private javafx.event.EventHandler<DayNightEvent> nightHandler;
    private javafx.event.EventHandler<DayNightEvent> dayHandler;

    public CropComponent(CropData data) {
        this.data = data;
    }

    @Override
    public void onAdded() {
        updateView();
        
        // Khởi chạy timer lớn lên theo thời gian thực của cây
        growTimer = FXGL.getGameTimer().runAtInterval(this::grow, data.growthTime);

        // Đảm bảo ban đầu kiểm tra đúng trạng thái ngày đêm
        canGrow = Main.isDayTime();

        // Định nghĩa handler
        nightHandler = e -> {
            canGrow = false;
            System.out.println(data.type + " ngừng lớn vì trời tối.");
        };

        dayHandler = e -> {
            canGrow = true;
            System.out.println(data.type + " bắt đầu lớn lại vì có nắng.");
        };

        // Lắng nghe sự kiện từ EventBus
        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_NIGHT, nightHandler);
        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_DAY, dayHandler);
    }

    @Override
    public void onRemoved() {
        if (growTimer != null) {
            growTimer.expire();
        }
        if (nightHandler != null) {
            FXGL.getEventBus().removeEventHandler(DayNightEvent.SET_NIGHT, nightHandler);
        }
        if (dayHandler != null) {
            FXGL.getEventBus().removeEventHandler(DayNightEvent.SET_DAY, dayHandler);
        }
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

        // Hạn hán làm giảm 50% tốc độ lớn
        if (Project1Game.system.WeatherSystem.getCurrentWeather() == Project1Game.system.WeatherSystem.Weather.DROUGHT) {
            if (Math.random() < 0.5) {
                System.out.println(data.type + " không phát triển nhịp này do hạn hán.");
                return;
            }
        }

        if (stage < MAX_STAGE) {
            FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                    .filter(s -> s.getPosition().distance(entity.getPosition()) < 5)
                    .findFirst()
                    .ifPresent(soil -> {
                        SoilComponent sc = soil.getComponent(SoilComponent.class);
                        boolean isRaining = (Project1Game.system.WeatherSystem.getCurrentWeather() == Project1Game.system.WeatherSystem.Weather.RAINY);
                        // Cây chỉ lớn khi đất ẩm (đất được tưới nước hoặc trời đang mưa)
                        if (sc.isWet() || isRaining) {
                            stage++;
                            updateView();
                            System.out.println(data.type + " đã lớn lên giai đoạn " + stage);

                            // Làm khô đất sau khi lớn lên 1 giai đoạn (trừ khi trời đang mưa)
                            if (!isRaining) {
                                sc.setWet(false);
                            }
                        } else {
                            System.out.println(data.type + " không lớn được do đất khô.");
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

    public CropData getData() {
        return data;
    }
}
package Project1Game;
import javafx.geometry.Rectangle2D;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.time.TimerAction;
import javafx.util.Duration;
import java.awt.*;

public class RiceComponent extends Component {
    private int stage = 1;
    private final int MAX_STAGE = 3; // Giả sử bạn có 3 file ảnh
    private TimerAction growTimer;

    @Override
    public void onAdded() {
        updateView();
        // Tự động đổi stage sau mỗi 10 giây
        growTimer = FXGL.getGameTimer().runAtInterval(() -> {
            grow();
        }, Duration.seconds(10));
    }

    @Override
    public void onRemoved() {
        if (growTimer != null) {
            growTimer.expire();
        }
    }

    private void updateView() {
        // Tạo tên file dựa trên biến stage (ví dụ: rice_1.png, rice_2.png...)
        String imageName = "rice_" + stage + ".png";

        // Load ảnh trực tiếp từ thư mục textures
        Texture texture = FXGL.texture(imageName);

        // Phóng to rice lớn hơn ô soil để trông tự nhiên
        double targetSize = 48.0;
        double scale = targetSize / Math.max(texture.getWidth(), texture.getHeight());
        texture.setFitWidth(texture.getWidth() * scale);
        texture.setFitHeight(texture.getHeight() * scale);

        // Căn giữa rice trong ô soil 32x32 (rice sẽ tràn ra ngoài ô)
        double offsetX = (32 - texture.getFitWidth()) / 2;
        double offsetY = (32 - texture.getFitHeight());
        texture.setTranslateX(offsetX);
        texture.setTranslateY(offsetY);

        // Xóa hình cũ, thêm hình mới
        getEntity().getViewComponent().clearChildren();
        getEntity().getViewComponent().addChild(texture);
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
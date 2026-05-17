package Project1Game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class StatusBarsView extends Parent {

    private static final double BAR_WIDTH = 300;
    private static final double BAR_HEIGHT = 28;
    private static final double SPACING = 12;
    private static final int NUM_HEARTS = 9;

    private final DoubleProperty health = new SimpleDoubleProperty(18);
    private final DoubleProperty maxHealth = new SimpleDoubleProperty(18);
    private final DoubleProperty hunger = new SimpleDoubleProperty(100);
    private final DoubleProperty maxHunger = new SimpleDoubleProperty(100);

    private final HBox heartsContainer;
    private final List<Texture> heartTextures = new ArrayList<>();
    private final Texture fullHeart;
    private final Texture halfHeart;
    private final Texture emptyHeart;
    private final Rectangle hungerFill;
    private final Text healthText;
    private final Text hungerText;

    public StatusBarsView() {
        double y = 0;

        // --- Thanh máu (Dạng 9 trái tim) ---
        heartsContainer = new HBox(4);
        heartsContainer.setTranslateX(0);
        heartsContainer.setTranslateY(y - 4);

        // Load heart sprite (cấu trúc 3 frame: [0]=đầy, [1]=nửa, [2]=rỗng)
        // Kích thước tệp là 96x32, mỗi khung hình 32x32
        fullHeart = FXGL.texture("UI/heart_sprite.png").subTexture(new javafx.geometry.Rectangle2D(0, 0, 32, 32));
        halfHeart = FXGL.texture("UI/heart_sprite.png").subTexture(new javafx.geometry.Rectangle2D(32, 0, 32, 32));
        emptyHeart = FXGL.texture("UI/heart_sprite.png").subTexture(new javafx.geometry.Rectangle2D(64, 0, 32, 32));

        for (int i = 0; i < NUM_HEARTS; i++) {
            Texture heart = new Texture(fullHeart.getImage());
            heart.setFitWidth(32);
            heart.setFitHeight(32);
            heartTextures.add(heart);
            heartsContainer.getChildren().add(heart);
        }

        healthText = new Text("18 / 18");
        healthText.setFont(Font.font("Arial", 16));
        healthText.setFill(Color.WHITE);
        healthText.setTranslateX(NUM_HEARTS * (32 + 4) + 10);
        healthText.setTranslateY(y + 20);

        // --- Thanh thức ăn ---
        y += BAR_HEIGHT + SPACING;

        Text hungerLabel = new Text("\uD83C\uDF5E Food");
        hungerLabel.setFont(Font.font("Arial", 18));
        hungerLabel.setFill(Color.WHITE);
        hungerLabel.setTranslateY(y + 20);

        Rectangle hungerBg = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
        hungerBg.setFill(Color.rgb(60, 60, 60, 0.8));
        hungerBg.setArcWidth(8);
        hungerBg.setArcHeight(8);
        hungerBg.setTranslateX(70);
        hungerBg.setTranslateY(y);

        hungerFill = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
        hungerFill.setFill(Color.rgb(200, 150, 30));
        hungerFill.setArcWidth(8);
        hungerFill.setArcHeight(8);
        hungerFill.setTranslateX(70);
        hungerFill.setTranslateY(y);

        hungerText = new Text("100 / 100");
        hungerText.setFont(Font.font("Arial", 16));
        hungerText.setFill(Color.WHITE);
        hungerText.setTranslateX(70 + BAR_WIDTH / 2 - 35);
        hungerText.setTranslateY(y + 20);

        getChildren().addAll(
                heartsContainer, healthText,
                hungerBg, hungerFill, hungerLabel, hungerText
        );

        // Bind fill width to property
        health.addListener((obs, oldVal, newVal) -> updateBars());
        maxHealth.addListener((obs, oldVal, newVal) -> updateBars());
        hunger.addListener((obs, oldVal, newVal) -> updateBars());
        maxHunger.addListener((obs, oldVal, newVal) -> updateBars());
    }

    private void updateBars() {
        // Mỗi tim có 2 nấc (đầy = 2, nửa = 1, rỗng = 0)
        // Với 9 tim, tổng cộng có 18 nấc.
        // Tỉ lệ máu sẽ được nhân với 18 để biết tổng số nấc hiện tại.
        double hpRatio = Math.max(0, Math.min(1, health.get() / maxHealth.get()));
        double totalTicks = hpRatio * (NUM_HEARTS * 2);

        for (int i = 0; i < NUM_HEARTS; i++) {
            // Xác định trạng thái của tim thứ i (0-indexed)
            // Tim thứ i đại diện cho nấc i*2 + 1 và i*2 + 2
            double heartVal = totalTicks - (i * 2);

            if (heartVal >= 2) {
                // Tim đầy (đủ 2 nấc)
                heartTextures.get(i).setImage(fullHeart.getImage());
            } else if (heartVal >= 0.5) {
                // Tim nửa (ít nhất nửa nấc, dùng ngưỡng 0.5 để làm tròn cho đẹp)
                heartTextures.get(i).setImage(halfHeart.getImage());
            } else {
                // Tim rỗng
                heartTextures.get(i).setImage(emptyHeart.getImage());
            }
        }
        healthText.setText((int) health.get() + " / " + (int) maxHealth.get());

        double foodRatio = Math.max(0, Math.min(1, hunger.get() / maxHunger.get()));
        hungerFill.setWidth(BAR_WIDTH * foodRatio);
        hungerText.setText((int) hunger.get() + " / " + (int) maxHunger.get());
    }

    // --- Getters / Setters ---

    public double getHealth() { return health.get(); }
    public void setHealth(double value) { health.set(value); }
    public DoubleProperty healthProperty() { return health; }

    public double getMaxHealth() { return maxHealth.get(); }
    public void setMaxHealth(double value) { maxHealth.set(value); }

    public double getHunger() { return hunger.get(); }
    public void setHunger(double value) { hunger.set(value); }
    public DoubleProperty hungerProperty() { return hunger; }

    public double getMaxHunger() { return maxHunger.get(); }
    public void setMaxHunger(double value) { maxHunger.set(value); }
}

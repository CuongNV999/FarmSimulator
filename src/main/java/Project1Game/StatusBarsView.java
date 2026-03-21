package Project1Game;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class StatusBarsView extends Parent {

    private static final double BAR_WIDTH = 200;
    private static final double BAR_HEIGHT = 20;
    private static final double SPACING = 8;

    private final DoubleProperty health = new SimpleDoubleProperty(100);
    private final DoubleProperty maxHealth = new SimpleDoubleProperty(100);
    private final DoubleProperty hunger = new SimpleDoubleProperty(100);
    private final DoubleProperty maxHunger = new SimpleDoubleProperty(100);

    private final Rectangle healthFill;
    private final Rectangle hungerFill;
    private final Text healthText;
    private final Text hungerText;

    public StatusBarsView() {
        double y = 0;

        // --- Thanh máu ---
        Text healthLabel = new Text("❤ HP");
        healthLabel.setFont(Font.font("Arial", 14));
        healthLabel.setFill(Color.WHITE);
        healthLabel.setTranslateY(y + 14);

        Rectangle healthBg = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
        healthBg.setFill(Color.rgb(60, 60, 60, 0.8));
        healthBg.setArcWidth(6);
        healthBg.setArcHeight(6);
        healthBg.setTranslateX(50);
        healthBg.setTranslateY(y);

        healthFill = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
        healthFill.setFill(Color.rgb(220, 40, 40));
        healthFill.setArcWidth(6);
        healthFill.setArcHeight(6);
        healthFill.setTranslateX(50);
        healthFill.setTranslateY(y);

        healthText = new Text("100 / 100");
        healthText.setFont(Font.font("Arial", 12));
        healthText.setFill(Color.WHITE);
        healthText.setTranslateX(50 + BAR_WIDTH / 2 - 25);
        healthText.setTranslateY(y + 14);

        // --- Thanh thức ăn ---
        y += BAR_HEIGHT + SPACING;

        Text hungerLabel = new Text("\uD83C\uDF5E Food");
        hungerLabel.setFont(Font.font("Arial", 14));
        hungerLabel.setFill(Color.WHITE);
        hungerLabel.setTranslateY(y + 14);

        Rectangle hungerBg = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
        hungerBg.setFill(Color.rgb(60, 60, 60, 0.8));
        hungerBg.setArcWidth(6);
        hungerBg.setArcHeight(6);
        hungerBg.setTranslateX(50);
        hungerBg.setTranslateY(y);

        hungerFill = new Rectangle(BAR_WIDTH, BAR_HEIGHT);
        hungerFill.setFill(Color.rgb(200, 150, 30));
        hungerFill.setArcWidth(6);
        hungerFill.setArcHeight(6);
        hungerFill.setTranslateX(50);
        hungerFill.setTranslateY(y);

        hungerText = new Text("100 / 100");
        hungerText.setFont(Font.font("Arial", 12));
        hungerText.setFill(Color.WHITE);
        hungerText.setTranslateX(50 + BAR_WIDTH / 2 - 25);
        hungerText.setTranslateY(y + 14);

        getChildren().addAll(
                healthBg, healthFill, healthLabel, healthText,
                hungerBg, hungerFill, hungerLabel, hungerText
        );

        // Bind fill width to property
        health.addListener((obs, oldVal, newVal) -> updateBars());
        maxHealth.addListener((obs, oldVal, newVal) -> updateBars());
        hunger.addListener((obs, oldVal, newVal) -> updateBars());
        maxHunger.addListener((obs, oldVal, newVal) -> updateBars());
    }

    private void updateBars() {
        double hpRatio = Math.max(0, Math.min(1, health.get() / maxHealth.get()));
        healthFill.setWidth(BAR_WIDTH * hpRatio);
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

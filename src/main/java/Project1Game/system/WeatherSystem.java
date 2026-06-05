package Project1Game.system;

import Project1Game.ui.utility.GameFont;

import com.almasb.fxgl.dsl.FXGL;
import Project1Game.Main;
import Project1Game.component.farming.SoilComponent;
import Project1Game.core.EntityType;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;

/**
 * Weather system that manages weather cycles (Sunny, Rainy, Drought) every 4 hours,
 * updates visual filters/particles, and modifies soil/crop growth rules.
 */
public class WeatherSystem {
    public enum Weather {
        SUNNY("Nắng", Color.GOLD),
        RAINY("Mưa", Color.CYAN),
        DROUGHT("Hạn hán", Color.ORANGE);

        public final String displayName;
        public final Color color;

        Weather(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
        }
    }

    private static WeatherSystem instance;
    private static Weather currentWeather = Weather.SUNNY;

    private int lastWeatherHourGroup = -1;

    private Text weatherText;
    private Rectangle weatherOverlay;
    private Pane rainPane;

    private final int MAX_RAIN_DROPS = 80;
    private final Line[] rainDrops = new Line[MAX_RAIN_DROPS];
    private final double[] rainSpeeds = new double[MAX_RAIN_DROPS];

    private boolean visualsEnabled = true;

    public void setVisualsEnabled(boolean enabled) {
        this.visualsEnabled = enabled;
        if (!enabled) {
            // Delay setting overlay/weather visual states by 0.2s for smoother transition
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.2));
            pause.setOnFinished(e -> {
                if (!visualsEnabled) {
                    applyVisualsState();
                }
            });
            pause.play();
        } else {
            applyVisualsState();
        }
    }

    private void applyVisualsState() {
        if (weatherText != null) {
            weatherText.setVisible(visualsEnabled);
        }
        if (weatherOverlay != null) {
            if (visualsEnabled) {
                if (currentWeather == Weather.RAINY) {
                    weatherOverlay.setFill(Color.rgb(60, 75, 90, 0.25));
                } else if (currentWeather == Weather.DROUGHT) {
                    weatherOverlay.setFill(Color.rgb(240, 130, 40, 0.20));
                } else {
                    weatherOverlay.setFill(Color.TRANSPARENT);
                }
            } else {
                weatherOverlay.setFill(Color.TRANSPARENT);
            }
        }
        if (rainPane != null) {
            rainPane.setVisible(visualsEnabled && currentWeather == Weather.RAINY);
        }
    }

    private WeatherSystem() {}

    public static WeatherSystem getInstance() {
        if (instance == null) {
            instance = new WeatherSystem();
        }
        return instance;
    }

    public static Weather getCurrentWeather() {
        return currentWeather;
    }

    public void init() {
        // Clean up old UI nodes to prevent duplicates upon game restart / main menu reload
        if (weatherText != null) {
            try {
                if (weatherText.getParent() instanceof javafx.scene.layout.VBox) {
                    ((javafx.scene.layout.VBox) weatherText.getParent()).getChildren().remove(weatherText);
                } else {
                    FXGL.getGameScene().removeUINode(weatherText);
                }
            } catch (Exception e) {}
        }
        if (weatherOverlay != null) {
            try { FXGL.getGameScene().removeUINode(weatherOverlay); } catch (Exception e) {}
        }
        if (rainPane != null) {
            try { FXGL.getGameScene().removeUINode(rainPane); } catch (Exception e) {}
        }

        // Initialize UI Nodes
        weatherText = new Text("Thời tiết: Nắng");
        weatherText.setFont(Font.font(Project1Game.ui.utility.GameFont.GAME_FONT, FontWeight.BOLD, 18));
        weatherText.setStroke(Color.BLACK);
        weatherText.setStrokeWidth(0.3);
        weatherText.setFill(Color.GOLD);

        // Register to Main HUD Container
        if (Main.getInstance() != null) {
            Main.getInstance().registerWeatherText(weatherText);
        }

        // Screen color overlay
        weatherOverlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Color.TRANSPARENT);
        weatherOverlay.setMouseTransparent(true);

        // Rain panel
        rainPane = new Pane();
        rainPane.setMouseTransparent(true);
        rainPane.setVisible(false);

        for (int i = 0; i < MAX_RAIN_DROPS; i++) {
            Line line = new Line(0, 0, 1.5, 12);
            line.setStroke(Color.rgb(160, 200, 255, 0.45));
            line.setStrokeWidth(1.5);
            rainDrops[i] = line;
            rainSpeeds[i] = 400 + Math.random() * 200;
            line.setTranslateX(Math.random() * FXGL.getAppWidth());
            line.setTranslateY(Math.random() * FXGL.getAppHeight());
            rainPane.getChildren().add(line);
        }

        // Add to game scene graph (only overlay and rainPane)
        FXGL.getGameScene().addUINodes(weatherOverlay, rainPane);

        // Set initial weather visuals
        changeWeather(currentWeather);
        
        // Reset time group checks
        lastWeatherHourGroup = -1;
    }

    public void onUpdate(double tpf) {
        if (currentWeather == Weather.RAINY && rainPane != null && rainPane.isVisible()) {
            for (int i = 0; i < MAX_RAIN_DROPS; i++) {
                Line drop = rainDrops[i];
                double newY = drop.getTranslateY() + rainSpeeds[i] * tpf;
                double newX = drop.getTranslateX() - 40 * tpf; // slight drift

                if (newY > FXGL.getAppHeight()) {
                    newY = -12;
                    newX = Math.random() * FXGL.getAppWidth();
                }
                drop.setTranslateX(newX);
                drop.setTranslateY(newY);
            }
        }
    }

    public void updateTime(double gameTime) {
        int group = (int) (gameTime / 240); // 0 to 5 (240 mins = 4 hours)
        if (group != lastWeatherHourGroup) {
            if (lastWeatherHourGroup != -1) {
                changeWeatherRandomly();
            }
            lastWeatherHourGroup = group;
        }
    }

    public void changeWeatherRandomly() {
        double r = Math.random();
        if (r < 0.55) {
            changeWeather(Weather.SUNNY);
        } else if (r < 0.85) {
            changeWeather(Weather.RAINY);
        } else {
            changeWeather(Weather.DROUGHT);
        }
    }

    public void changeWeather(Weather newWeather) {
        currentWeather = newWeather;

        if (weatherText != null) {
            weatherText.setText("Thời tiết: " + newWeather.displayName);
            weatherText.setFill(newWeather.color);
        }

        applyVisualsState();

        // Trigger texture updates on all soils in the world
        try {
            if (FXGL.getGameWorld() != null) {
                FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(s -> {
                    SoilComponent sc = s.getComponent(SoilComponent.class);
                    if (sc != null) {
                        if (newWeather == Weather.RAINY) {
                            sc.setWet(true);
                        } else {
                            sc.updateTexture();
                        }
                    }
                });
            }
        } catch (Exception e) {
            // Ignore if world is loading/unloading
        }

        System.out.println("[WeatherSystem] Thời tiết chuyển thành: " + newWeather.displayName);
    }
}

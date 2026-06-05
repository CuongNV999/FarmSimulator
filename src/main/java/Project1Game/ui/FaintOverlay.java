package Project1Game.ui;

import com.almasb.fxgl.dsl.FXGL;
import Project1Game.Main;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class FaintOverlay extends StackPane {
    private final Runnable onRespawn;
    private final boolean isGameOver;

    public FaintOverlay(boolean isGameOver, Runnable onRespawn) {
        this.onRespawn = onRespawn;
        this.isGameOver = isGameOver;
        
        // Ensure overlay prevents mouse-click propagation down to underlying game entities
        setPickOnBounds(true);
        setOnMousePressed(e -> e.consume());
        setOnMouseReleased(e -> e.consume());
        setOnMouseDragged(e -> e.consume());
        setOnMouseClicked(e -> e.consume());

        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);

        if (!isGameOver) {
            // Scenario A: Non-lethal faint
            setStyle("-fx-background-color: rgba(60, 10, 10, 0.88);"); // Dark translucent red overlay background
            setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

            Text title = new Text("BẠN ĐÃ NGẤT XỈU");
            title.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 48));
            title.setFill(Color.web("#d9383a"));

            Text details = new Text("Bạn đã bị kiệt sức! Bác nông dân đã đưa bạn về nhà.\nPhạt viện phí: 100 G. Sức khỏe phục hồi 50%.");
            details.setFont(Font.font(GameFont.GAME_FONT, FontWeight.NORMAL, 18));
            details.setFill(Color.WHITE);
            details.setStyle("-fx-text-alignment: center;");
            details.setWrappingWidth(FXGL.getAppWidth() * 0.7);

            Button respawnBtn = new Button("TỈNH DẬY (RESPAWN)");
            respawnBtn.setStyle("-fx-background-color: #d9383a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
            respawnBtn.setOnAction(e -> {
                // Fade out overlay when respawning
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.6), this);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(ev -> {
                    FXGL.getGameScene().removeUINode(this);
                    if (onRespawn != null) {
                        onRespawn.run();
                    }
                });
                fadeOut.play();
            });

            content.getChildren().addAll(title, details, respawnBtn);
        } else {
            // Scenario B: Permanent bankruptcy / death game over
            setStyle("-fx-background-color: rgba(20, 0, 0, 0.95);"); // Extremely dark background
            setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());

            Text title = new Text("BẠN ĐÃ CHẾT!");
            title.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 54));
            title.setFill(Color.web("#b00e0e"));

            Text details = new Text("Bạn đã bị kiệt sức nhưng không đủ tiền trả viện phí (100 G). Bác nông dân không thể đưa bạn về nhà được nữa... Hệ thống sẽ tải bản lưu tự động (autosave.dat) mới nhất.");
            details.setFont(Font.font(GameFont.GAME_FONT, FontWeight.NORMAL, 18));
            details.setFill(Color.WHITE);
            details.setStyle("-fx-text-alignment: center;");
            details.setWrappingWidth(FXGL.getAppWidth() * 0.7);

            Button loadSaveBtn = new Button("Tải bản lưu gần nhất");
            loadSaveBtn.setStyle("-fx-background-color: #2b4c3f; -fx-text-fill: #a2e8c2; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
            loadSaveBtn.setOnAction(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.6), this);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(ev -> {
                    FXGL.getGameScene().removeUINode(this);
                    Main app = Main.getInstance();
                    if (app != null) {
                        app.loadGame();
                        app.getPlayerStateManager().setDead(false);
                    }
                });
                fadeOut.play();
            });

            Button exitMenuBtn = new Button("Thoát ra Menu chính");
            exitMenuBtn.setStyle("-fx-background-color: #3e3e4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
            exitMenuBtn.setOnAction(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.6), this);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(ev -> {
                    FXGL.getGameScene().removeUINode(this);
                    Main app = Main.getInstance();
                    if (app != null) {
                        app.getPlayerStateManager().setDead(false);
                    }
                    FXGL.getGameController().gotoMainMenu();
                });
                fadeOut.play();
            });

            content.getChildren().addAll(title, details, loadSaveBtn, exitMenuBtn);
        }

        getChildren().add(content);
        setOpacity(0.0);
    }

    public void show() {
        if (!FXGL.getGameScene().getUINodes().contains(this)) {
            FXGL.getGameScene().addUINode(this);
        }
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
}

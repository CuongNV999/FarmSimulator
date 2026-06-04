package Project1Game.ui;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.notification.Notification;
import com.almasb.fxgl.notification.view.NotificationView;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class CustomNotificationView extends NotificationView {

    private final StackPane container;
    private final Text messageText;
    private final Rectangle bg;

    public CustomNotificationView() {
        container = new StackPane();
        container.setAlignment(Pos.CENTER);

        // dark background: semi-transparent charcoal with a light border
        bg = new Rectangle();
        bg.setFill(Color.rgb(30, 30, 30, 0.85));
        bg.setArcWidth(12);
        bg.setArcHeight(12);
        bg.setStroke(Color.rgb(255, 255, 255, 0.15));
        bg.setStrokeWidth(1.2);

        messageText = new Text();
        messageText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        messageText.setFill(Color.rgb(240, 240, 240));

        // Adjust background size when text bounds change
        messageText.boundsInLocalProperty().addListener((obs, oldVal, newVal) -> {
            bg.setWidth(newVal.getWidth() + 32);
            bg.setHeight(newVal.getHeight() + 16);
        });

        container.getChildren().addAll(bg, messageText);
        getChildren().add(container);

        // Center horizontally dynamically based on actual application width
        container.translateXProperty().bind(
                bg.widthProperty().multiply(-0.5).add(FXGL.getAppWidth() / 2.0));

        // Hide initially
        container.setOpacity(0.0);
    }

    @Override
    public void push(Notification notification) {
        String msg = notification.getMessage();
        messageText.setText(msg);
        container.setVisible(true);
    }

    @Override
    public void playInAnimation() {
        double targetY = 35.0; // Aligned with the center of the blue-circled area (approx Y=35)
        container.setTranslateY(targetY - 30); // Start slightly above

        TranslateTransition translate = new TranslateTransition(Duration.seconds(0.35), container);
        translate.setToY(targetY);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.3), container);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ParallelTransition anim = new ParallelTransition(translate, fade);
        anim.play();
    }

    @Override
    public void playOutAnimation() {
        double targetY = 35.0; 
        TranslateTransition translate = new TranslateTransition(Duration.seconds(0.3), container);
        translate.setToY(targetY - 30);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.25), container);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition anim = new ParallelTransition(translate, fade);
        anim.play();
    }

    @Override
    public void onUpdate(double tpf) {
        // No-op, required by Updatable interface
    }
}

package Project1Game.ui;

import Project1Game.Main;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;

public class DeadScreenView extends Parent {
    private final Text hintText;
    private final DeadScreenButton btnLoad;

    public DeadScreenView() {
        double width = FXGL.getAppWidth();
        double height = FXGL.getAppHeight();

        // 1. Background gradient (Crimson-to-Black translucent)
        LinearGradient bgGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0.12, 0.02, 0.02, 0.92)),
                new Stop(1, Color.color(0.01, 0.005, 0.005, 0.97))
        );
        Rectangle bg = new Rectangle(width, height, bgGrad);

        // 2. Title Text
        Text titleText = new Text("BẠN ĐÃ CHẾT!");
        titleText.setFont(GameFont.font(FontWeight.EXTRA_BOLD, 64));
        titleText.setFill(Color.web("#ff3b30")); // Neon Red
        
        DropShadow titleShadow = new DropShadow(30, Color.rgb(255, 50, 50, 0.8));
        titleText.setEffect(titleShadow);

        // 3. Subtitle / Description Text
        Text descText = new Text("Bạn đã bị kiệt sức nhưng không đủ tiền trả viện phí (100 G).\nBác nông dân không thể đưa bạn về nhà được nữa...");
        descText.setFont(GameFont.font(FontWeight.BOLD, 18));
        descText.setFill(Color.web("#ffb3b3")); // Soft pastel red/pink
        descText.setWrappingWidth(600);
        descText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Hint text showing details of save files
        hintText = new Text("Đang kiểm tra các bản lưu...");
        hintText.setFont(GameFont.font(FontWeight.NORMAL, 14));
        hintText.setFill(Color.LIGHTGRAY);

        // 4. Buttons Container
        HBox buttonsBox = new HBox(30);
        buttonsBox.setAlignment(Pos.CENTER);

        // Button: Load last save
        btnLoad = new DeadScreenButton("Tải bản lưu gần nhất", () -> {
            Main.getInstance().loadLastSave();
        });

        // Button: Return to main menu
        DeadScreenButton btnMenu = new DeadScreenButton("Thoát ra Menu chính", () -> {
            FXGL.getGameController().gotoMainMenu();
            hide(); // Hide dead screen in case they go back later
        });

        buttonsBox.getChildren().addAll(btnLoad, btnMenu);

        // 5. Layout Container
        VBox vbox = new VBox(25, titleText, descText, hintText, buttonsBox);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(40));
        vbox.setPrefSize(width, height);

        getChildren().addAll(bg, vbox);
        setVisible(false);
    }

    public void show() {
        setVisible(true);
        updateButtonsState();
    }

    public void hide() {
        setVisible(false);
    }

    public void updateButtonsState() {
        boolean canLoad = Main.getInstance().hasAutosave() || Main.getInstance().hasSaveGame();
        btnLoad.setDisable(!canLoad);
        if (!canLoad) {
            btnLoad.setOpacity(0.4);
            btnLoad.setMouseTransparent(true);
            hintText.setText("Không tìm thấy bản lưu trước đó. Hãy thoát ra Menu chính.");
        } else {
            btnLoad.setOpacity(1.0);
            btnLoad.setMouseTransparent(false);
            if (Main.getInstance().hasAutosave()) {
                hintText.setText("Hệ thống sẽ tải bản tự động lưu (autosave.dat) mới nhất.");
            } else {
                hintText.setText("Hệ thống sẽ tải bản lưu thủ công (save_game.dat) gần nhất.");
            }
        }
    }

    // Custom Button for Dead Screen
    private static class DeadScreenButton extends StackPane {
        private final Rectangle bg;
        private final Text text;
        private final Runnable action;

        public DeadScreenButton(String name, Runnable action) {
            this.action = action;

            bg = new Rectangle(260, 48);
            bg.setArcWidth(20);
            bg.setArcHeight(20);
            bg.setFill(Color.color(0.12, 0.04, 0.04, 0.6)); // Glass dark red
            bg.setStroke(Color.color(0.35, 0.1, 0.1, 0.6));
            bg.setStrokeWidth(2.0);

            text = new Text(name);
            text.setFont(GameFont.font(FontWeight.BOLD, 16));
            text.setFill(Color.web("#ffebeb"));

            DropShadow shadow = new DropShadow(8, Color.color(0, 0, 0, 0.5));
            setEffect(shadow);

            setOnMouseEntered(e -> {
                bg.setFill(Color.color(0.3, 0.05, 0.05, 0.8));
                bg.setStroke(Color.web("#ff453a")); // Neon red glow
                text.setFill(Color.WHITE);
                setScaleX(1.05);
                setScaleY(1.05);
                shadow.setColor(Color.color(0.4, 0.1, 0.1, 0.4));
            });

            setOnMouseExited(e -> {
                bg.setFill(Color.color(0.12, 0.04, 0.04, 0.6));
                bg.setStroke(Color.color(0.35, 0.1, 0.1, 0.6));
                text.setFill(Color.web("#ffebeb"));
                setScaleX(1.0);
                setScaleY(1.0);
                shadow.setColor(Color.color(0, 0, 0, 0.5));
            });

            setOnMousePressed(e -> {
                bg.setFill(Color.color(0.4, 0.08, 0.08, 0.95));
                setScaleX(0.96);
                setScaleY(0.96);
            });

            setOnMouseReleased(e -> {
                bg.setFill(Color.color(0.3, 0.05, 0.05, 0.8));
                setScaleX(1.05);
                setScaleY(1.05);
                if (action != null) {
                    action.run();
                }
            });

            setAlignment(Pos.CENTER);
            getChildren().addAll(bg, text);
        }
    }
}

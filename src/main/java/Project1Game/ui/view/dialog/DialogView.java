package Project1Game.ui.view.dialog;

import Project1Game.ui.utility.GameFont;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Hộp thoại NPC — hiển thị tên và nội dung hội thoại.
 * Dùng setDialog(name, lines) để cập nhật nội dung.
 * Nhấn R để đóng/mở.
 */
public class DialogView extends Parent {
    private static final double WIDTH = 600;
    private static final double HEIGHT = 120;

    private final Text nameText;
    private final Text contentText;

    public DialogView(double screenW, double screenH) {
        Rectangle bg = new Rectangle(WIDTH, HEIGHT);
        bg.setFill(Color.rgb(20, 15, 10, 0.92));
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        bg.setStroke(Color.rgb(180, 140, 60));
        bg.setStrokeWidth(2);

        nameText = new Text("NPC");
        nameText.setFont(Font.font(GameFont.GAME_FONT, 15));
        nameText.setFill(Color.GOLD);

        contentText = new Text("...");
        contentText.setFont(Font.font(GameFont.GAME_FONT, 14));
        contentText.setFill(Color.WHITE);
        contentText.setWrappingWidth(WIDTH - 24);

        Text hint = new Text("[R] Đóng");
        hint.setFont(Font.font(GameFont.GAME_FONT, 11));
        hint.setFill(Color.GRAY);

        VBox box = new VBox(6, nameText, contentText, hint);
        box.setPadding(new Insets(10, 12, 8, 12));
        box.setAlignment(Pos.TOP_LEFT);
        box.setPrefSize(WIDTH, HEIGHT);

        getChildren().addAll(bg, box);

        // Đặt ở giữa phía dưới màn hình
        setTranslateX((screenW - WIDTH) / 2);
        setTranslateY(screenH - HEIGHT - 80);
        setVisible(false);
    }

    /** Cập nhật nội dung hội thoại */
    public void setDialog(String npcName, String... lines) {
        nameText.setText(npcName);
        contentText.setText(String.join("\n", lines));
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public void toggle() {
        if (isVisible()) hide(); else show();
    }

    public boolean isOpen() { return isVisible(); }
}

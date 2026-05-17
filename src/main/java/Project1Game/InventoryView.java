package Project1Game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class InventoryView extends VBox {
    private static final int SLOT_SIZE = 64;
    private static final int SLOT_GAP = 4;
    private static final int COLS = Inventory.COLS;
    private static final int ROWS = Inventory.ROWS;

    private final Inventory inventory;
    private boolean visible2 = false;

    public InventoryView(Inventory inventory) {
        this.inventory = inventory;

        // Nền tối
        Rectangle bg = new Rectangle(COLS * (SLOT_SIZE + SLOT_GAP) + 20, ROWS * (SLOT_SIZE + SLOT_GAP) + 50);
        bg.setFill(Color.rgb(30, 20, 10, 0.92));
        bg.setArcWidth(12);
        bg.setArcHeight(12);

        Text title = new Text("Inventory");
        title.setFont(Font.font("Arial", 16));
        title.setFill(Color.GOLD);

        GridPane grid = new GridPane();
        grid.setHgap(SLOT_GAP);
        grid.setVgap(SLOT_GAP);
        grid.setPadding(new Insets(8));

        // Bỏ qua 9 slot hotbar đầu, lấy 27 slot còn lại
        ItemType[] all = inventory.getAllSlots();
        int startIdx = Inventory.HOTBAR_SIZE;
        for (int i = 0; i < ROWS * COLS; i++) {
            int idx = startIdx + i;
            ItemType type = idx < all.length ? all[idx] : null;
            StackPane slot = createSlot(type);
            grid.add(slot, i % COLS, i / COLS);
        }

        setAlignment(Pos.CENTER);
        setSpacing(6);
        setPadding(new Insets(10));
        getChildren().addAll(title, grid);

        // Đặt nền phía sau
        setStyle("-fx-background-color: rgba(30,20,10,0.92); -fx-background-radius: 12;");
        setVisible(false);
    }

    private StackPane createSlot(ItemType type) {
        StackPane pane = new StackPane();
        pane.setPrefSize(SLOT_SIZE, SLOT_SIZE);

        Rectangle bg = new Rectangle(SLOT_SIZE, SLOT_SIZE);
        bg.setFill(Color.rgb(60, 40, 20, 0.85));
        bg.setStroke(Color.rgb(139, 90, 43));
        bg.setStrokeWidth(1.5);
        bg.setArcWidth(6);
        bg.setArcHeight(6);
        pane.getChildren().add(bg);

        if (type != null && type.getIconName() != null && !type.getIconName().isEmpty()) {
            Texture icon = FXGL.texture(type.getIconName());
            icon.setFitWidth(40);
            icon.setFitHeight(40);
            icon.setPreserveRatio(true);
            pane.getChildren().add(icon);

            Text countText = new Text();
            countText.setFont(Font.font("Arial", 14));
            countText.setFill(Color.WHITE);
            countText.textProperty().bind(
                Bindings.createStringBinding(
                    () -> {
                        int count = inventory.countProperty(type).get();
                        return count > 0 ? String.valueOf(count) : "";
                    },
                    inventory.countProperty(type)
                )
            );
            StackPane.setAlignment(countText, Pos.BOTTOM_RIGHT);
            countText.setTranslateX(-3);
            countText.setTranslateY(-2);
            pane.getChildren().add(countText);
        }

        return pane;
    }

    public void toggle() {
        visible2 = !visible2;
        setVisible(visible2);
    }

    public boolean isOpen() { return visible2; }
}

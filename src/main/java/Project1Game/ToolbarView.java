package Project1Game;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ToolbarView extends HBox {
    private static final int SLOT_SIZE = 64;
    private static final int SLOT_GAP = 4;
    private final Inventory inventory;
    private final StackPane[] slotPanes;

    public ToolbarView(Inventory inventory) {
        this.inventory = inventory;
        this.slotPanes = new StackPane[inventory.getSlots().length];

        setSpacing(SLOT_GAP);
        setAlignment(Pos.CENTER);

        ItemType[] slots = inventory.getSlots();
        for (int i = 0; i < slots.length; i++) {
            StackPane slotPane = createSlot(slots[i], i);
            slotPanes[i] = slotPane;
            getChildren().add(slotPane);
        }

        updateSelection();
    }

    private StackPane createSlot(ItemType itemType, int index) {
        StackPane pane = new StackPane();
        pane.setPrefSize(SLOT_SIZE, SLOT_SIZE);

        // Nền ô
        Rectangle bg = new Rectangle(SLOT_SIZE, SLOT_SIZE);
        bg.setFill(Color.rgb(60, 40, 20, 0.85));
        bg.setStroke(Color.rgb(139, 90, 43));
        bg.setStrokeWidth(2);
        bg.setArcWidth(8);
        bg.setArcHeight(8);

        // Icon vật phẩm
        Texture icon = FXGL.texture(itemType.getIconName());
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);

        // Số lượng
        Text countText = new Text();
        countText.setFont(Font.font("Arial", 14));
        countText.setFill(Color.WHITE);
        countText.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            int count = inventory.countProperty(itemType).get();
                            return count > 0 ? String.valueOf(count) : "";
                        },
                        inventory.countProperty(itemType)
                )
        );
        StackPane.setAlignment(countText, Pos.BOTTOM_RIGHT);
        countText.setTranslateX(-4);
        countText.setTranslateY(-2);

        // Tên vật phẩm (hiển thị nhỏ phía trên)
        Text nameText = new Text(itemType.getDisplayName());
        nameText.setFont(Font.font("Arial", 9));
        nameText.setFill(Color.LIGHTGRAY);
        StackPane.setAlignment(nameText, Pos.TOP_CENTER);
        nameText.setTranslateY(2);

        // Phím tắt
        Text keyText = new Text(String.valueOf(index + 1));
        keyText.setFont(Font.font("Arial", 10));
        keyText.setFill(Color.YELLOW);
        StackPane.setAlignment(keyText, Pos.TOP_LEFT);
        keyText.setTranslateX(4);
        keyText.setTranslateY(2);

        pane.getChildren().addAll(bg, icon, countText, nameText, keyText);
        return pane;
    }

    public void updateSelection() {
        for (int i = 0; i < slotPanes.length; i++) {
            Rectangle bg = (Rectangle) slotPanes[i].getChildren().get(0);
            if (i == inventory.getSelectedSlot()) {
                bg.setStroke(Color.GOLD);
                bg.setStrokeWidth(3);
                bg.setFill(Color.rgb(80, 60, 30, 0.95));
            } else {
                bg.setStroke(Color.rgb(139, 90, 43));
                bg.setStrokeWidth(2);
                bg.setFill(Color.rgb(60, 40, 20, 0.85));
            }
        }
    }
}

package Project1Game.ui;

import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.model.InventorySlot;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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

        // Lấy tất cả các slot từ inventory
        InventorySlot[] allSlots = inventory.getSlots();
        // Bắt đầu từ sau các slot hotbar
        int inventoryStartIndex = Inventory.HOTBAR_SIZE;

        for (int i = 0; i < ROWS * COLS; i++) {
            int fullIndex = inventoryStartIndex + i; // Chỉ số đầy đủ trong mảng slots của Inventory
            InventorySlot slot = allSlots[fullIndex];
            StackPane slotPane = createSlot(slot, fullIndex);
            grid.add(slotPane, i % COLS, i / COLS);
        }

        setAlignment(Pos.CENTER);
        setSpacing(6);
        setPadding(new Insets(10));
        getChildren().addAll(title, grid);

        // Đặt nền phía sau
        setStyle("-fx-background-color: rgba(30,20,10,0.92); -fx-background-radius: 12;");
        setVisible(false);
    }

    private StackPane createSlot(InventorySlot inventorySlot, int fullIndex) {
        StackPane pane = new StackPane();
        pane.setPrefSize(SLOT_SIZE, SLOT_SIZE);

        Rectangle bg = new Rectangle(SLOT_SIZE, SLOT_SIZE);
        bg.setFill(Color.rgb(60, 40, 20, 0.85));
        bg.setStroke(Color.rgb(139, 90, 43));
        bg.setStrokeWidth(1.5);
        bg.setArcWidth(6);
        bg.setArcHeight(6);
        pane.getChildren().add(bg);

        // Icon vật phẩm
        Texture icon = new Texture(FXGL.image("empty.png")); // Icon mặc định trống
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);
        pane.getChildren().add(icon);

        // Liên kết icon với itemTypeProperty của InventorySlot
        icon.imageProperty().bind(
                Bindings.createObjectBinding(() -> {
                    ItemType itemType = inventorySlot.getItemType();
                    if (itemType != null && itemType.getIconName() != null && !itemType.getIconName().isEmpty()) {
                        if (itemType.getIconName().startsWith("Animal/")) {
                            return Project1Game.component.farming.BaseAnimalComponent.extractFaceDownIdleImage(itemType.getIconName());
                        } else {
                            return FXGL.image(itemType.getIconName());
                        }
                    }
                    return FXGL.image("empty.png"); // Hình ảnh trống
                }, inventorySlot.itemTypeProperty())
        );

        // Số lượng
        Text countText = new Text();
        countText.setFont(Font.font("Arial", 14));
        countText.setFill(Color.WHITE);
        countText.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            int count = inventorySlot.getCount();
                            return count > 0 ? String.valueOf(count) : "";
                        },
                        inventorySlot.countProperty()
                )
        );
        StackPane.setAlignment(countText, Pos.BOTTOM_RIGHT);
        countText.setTranslateX(-3);
        countText.setTranslateY(-2);
        pane.getChildren().add(countText);

        // --- Drag and Drop Source ---
        pane.setOnDragDetected(event -> {
            if (!inventorySlot.isEmpty()) {
                Dragboard db = pane.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(fullIndex)); // Lưu chỉ mục đầy đủ của slot nguồn
                db.setContent(content);
                db.setDragView(icon.snapshot(null, null)); // Đặt hình ảnh kéo là icon vật phẩm
                event.consume();
            }
        });

        // --- Drag and Drop Target ---
        pane.setOnDragOver(event -> {
            if (event.getGestureSource() != pane && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        pane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int sourceIndex = Integer.parseInt(db.getString());
                int targetIndex = fullIndex; // Chỉ mục đầy đủ của slot hiện tại

                // Di chuyển vật phẩm trong inventory
                inventory.moveItem(sourceIndex, targetIndex);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return pane;
    }

    public void toggle() {
        visible2 = !visible2;
        setVisible(visible2);
    }

    public boolean isOpen() { return visible2; }
}

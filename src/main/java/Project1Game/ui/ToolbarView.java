package Project1Game.ui;

import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.model.InventorySlot;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ToolbarView extends HBox {
    private static final int SLOT_SIZE = 80;
    private static final int SLOT_GAP = 6;
    private final Inventory inventory;
    private final StackPane[] slotPanes;

    public ToolbarView(Inventory inventory) {
        this.inventory = inventory;
        this.slotPanes = new StackPane[Inventory.HOTBAR_SIZE]; // Kích thước hotbar

        setSpacing(SLOT_GAP);
        setAlignment(Pos.CENTER);

        InventorySlot[] hotbarSlots = inventory.getHotbarSlots(); // Lấy các slot hotbar
        for (int i = 0; i < Inventory.HOTBAR_SIZE; i++) {
            StackPane slotPane = createSlot(hotbarSlots[i], i);
            slotPanes[i] = slotPane;
            getChildren().add(slotPane);
        }

        updateSelection();
    }

    private StackPane createSlot(InventorySlot inventorySlot, int index) {
        StackPane pane = new StackPane();
        pane.setPrefSize(SLOT_SIZE, SLOT_SIZE);

        // Nền ô
        Rectangle bg = new Rectangle(SLOT_SIZE, SLOT_SIZE);
        bg.setFill(Color.rgb(60, 40, 20, 0.85));
        bg.setStroke(Color.rgb(139, 90, 43));
        bg.setStrokeWidth(2);
        bg.setArcWidth(8);
        bg.setArcHeight(8);

        pane.getChildren().add(bg);

        // Icon vật phẩm
        Texture icon = new Texture(FXGL.image("empty.png")); // Icon mặc định trống
        icon.setFitWidth(50);
        icon.setFitHeight(50);
        icon.setPreserveRatio(true);
        pane.getChildren().add(icon);

        // Liên kết icon với itemTypeProperty của InventorySlot
        icon.imageProperty().bind(
                Bindings.createObjectBinding(() -> {
                    ItemType itemType = inventorySlot.getItemType();
                    if (itemType != null && itemType.getIconName() != null && !itemType.getIconName().isEmpty()) {
                        if (itemType.getIconName().startsWith("Animal/")) {
                            return Project1Game.component.farming.animal.BaseAnimalComponent.extractFaceDownIdleImage(itemType.getIconName());
                        } else {
                            return FXGL.image(itemType.getIconName());
                        }
                    }
                    return FXGL.image("empty.png"); // Hình ảnh trống
                }, inventorySlot.itemTypeProperty())
        );

        // Số lượng
        Text countText = new Text();
        countText.setFont(Font.font("Arial", 18));
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
        countText.setTranslateX(-4);
        countText.setTranslateY(-2);

        // Tên vật phẩm (hiển thị nhỏ phía trên)
        Text nameText = new Text();
        nameText.setFont(Font.font("Arial", 11));
        nameText.setFill(Color.LIGHTGRAY);
        nameText.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            ItemType itemType = inventorySlot.getItemType();
                            return itemType != null && !itemType.getIconName().isEmpty() ? itemType.getDisplayName() : "";
                        },
                        inventorySlot.itemTypeProperty()
                )
        );
        StackPane.setAlignment(nameText, Pos.TOP_CENTER);
        nameText.setTranslateY(2);

        // Phím tắt
        Text keyText = new Text(String.valueOf(index + 1));
        keyText.setFont(Font.font("Arial", 12));
        keyText.setFill(Color.YELLOW);
        StackPane.setAlignment(keyText, Pos.TOP_LEFT);
        keyText.setTranslateX(4);
        keyText.setTranslateY(2);

        pane.getChildren().addAll(countText, nameText, keyText);

        // --- Drag and Drop Source ---
        pane.setOnDragDetected(event -> {
            if (!inventorySlot.isEmpty()) {
                System.out.println("Toolbar: Drag detected from slot " + index + " (Item: " + inventorySlot.getItemType() + ")");
                Dragboard db = pane.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(index)); // Lưu chỉ mục của slot nguồn
                db.setContent(content);
                db.setDragView(icon.snapshot(null, null)); // Đặt hình ảnh kéo là icon vật phẩm
                event.consume();
            }
        });

        // --- Drag and Drop Target ---
        pane.setOnDragOver(event -> {
            if (event.getGestureSource() != pane && event.getDragboard().hasString()) {
                System.out.println("Toolbar: Drag over slot " + index);
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        pane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int sourceIndex = Integer.parseInt(db.getString());
                int targetIndex = index; // Chỉ mục của slot hiện tại

                System.out.println("Toolbar: Item dropped from " + sourceIndex + " to " + targetIndex);
                // Di chuyển vật phẩm trong inventory
                inventory.moveItem(sourceIndex, targetIndex);
                updateSelection(); // Cập nhật lại viền chọn
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return pane;
    }

    public void updateSelection() {
        for (int i = 0; i < Inventory.HOTBAR_SIZE; i++) {
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

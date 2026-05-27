package Project1Game.ui;

import Project1Game.component.player.PlayerComponent;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;

public class TradingView extends VBox {

    private static final int ITEM_SLOT_SIZE = 64;
    private static final int ITEM_SLOT_GAP = 5;

    private final Inventory inventory;
    private final PlayerComponent playerComponent;
    private boolean visible = false;

    // Danh sách các vật phẩm có thể mua (hạt giống)
    private final List<ItemType> buyableItems = Arrays.asList(
            ItemType.WHEAT_SEED, ItemType.CORN_SEED, ItemType.RADISH_SEED,
            ItemType.CABBAGE_SEED, ItemType.LETTUCE_SEED, ItemType.TOMATO_SEED
    );

    // Danh sách các vật phẩm có thể bán (nông sản)
    private final List<ItemType> sellableItems = Arrays.asList(
            ItemType.WHEAT, ItemType.CORN, ItemType.RADISH,
            ItemType.CABBAGE, ItemType.LETTUCE, ItemType.TOMATO
    );

    public TradingView(Inventory inventory, PlayerComponent playerComponent) {
        this.inventory = inventory;
        this.playerComponent = playerComponent;

        // Cấu hình nền và viền cho TradingView
        setPrefSize(FXGL.getAppWidth() * 0.7, FXGL.getAppHeight() * 0.8);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));
        setSpacing(15);
        setStyle("-fx-background-color: rgba(30, 20, 10, 0.95); -fx-background-radius: 15; -fx-border-color: #8B4513; -fx-border-width: 3; -fx-border-radius: 15;");

        Text title = new Text("Cửa hàng của Trader");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.GOLD);

        // Hiển thị tiền của người chơi
        Text moneyDisplay = new Text();
        moneyDisplay.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        moneyDisplay.setFill(Color.LIGHTGREEN);
        moneyDisplay.textProperty().bind(
                Bindings.concat("Tiền của bạn: ", playerComponent.moneyProperty(), " G")
        );

        // Phần mua hàng
        VBox buySection = createBuySection();
        // Phần bán hàng
        VBox sellSection = createSellSection();

        HBox mainContent = new HBox(30, buySection, sellSection);
        mainContent.setAlignment(Pos.CENTER);

        getChildren().addAll(title, moneyDisplay, mainContent);
        setVisible(false); // Mặc định ẩn
    }

    private VBox createBuySection() {
        Text buyTitle = new Text("Mua Hạt giống");
        buyTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        buyTitle.setFill(Color.LIGHTBLUE);

        GridPane buyGrid = new GridPane();
        buyGrid.setHgap(ITEM_SLOT_GAP);
        buyGrid.setVgap(ITEM_SLOT_GAP);
        buyGrid.setPadding(new Insets(10));

        int col = 0;
        int row = 0;
        for (ItemType item : buyableItems) {
            buyGrid.add(createTradingItemSlot(item, true), col, row);
            col++;
            if (col >= 3) { // 3 cột
                col = 0;
                row++;
            }
        }
        return new VBox(10, buyTitle, buyGrid);
    }

    private VBox createSellSection() {
        Text sellTitle = new Text("Bán Nông sản");
        sellTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        sellTitle.setFill(Color.ORANGE);

        GridPane sellGrid = new GridPane();
        sellGrid.setHgap(ITEM_SLOT_GAP);
        sellGrid.setVgap(ITEM_SLOT_GAP);
        sellGrid.setPadding(new Insets(10));

        int col = 0;
        int row = 0;
        for (ItemType item : sellableItems) {
            sellGrid.add(createTradingItemSlot(item, false), col, row);
            col++;
            if (col >= 3) { // 3 cột
                col = 0;
                row++;
            }
        }
        return new VBox(10, sellTitle, sellGrid);
    }

    private StackPane createTradingItemSlot(ItemType itemType, boolean isBuying) {
        StackPane pane = new StackPane();
        pane.setPrefSize(ITEM_SLOT_SIZE * 1.5, ITEM_SLOT_SIZE * 2); // Tăng kích thước để chứa thông tin

        Rectangle bg = new Rectangle(ITEM_SLOT_SIZE * 1.5, ITEM_SLOT_SIZE * 2);
        bg.setFill(Color.rgb(60, 40, 20, 0.8));
        bg.setStroke(Color.rgb(139, 90, 43));
        bg.setStrokeWidth(2);
        bg.setArcWidth(8);
        bg.setArcHeight(8);
        pane.getChildren().add(bg);

        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);

        // Icon vật phẩm
        if (itemType.getIconName() != null && !itemType.getIconName().isEmpty()) {
            Texture icon = FXGL.texture(itemType.getIconName());
            icon.setFitWidth(ITEM_SLOT_SIZE);
            icon.setFitHeight(ITEM_SLOT_SIZE);
            icon.setPreserveRatio(true);
            content.getChildren().add(icon);
        }

        // Tên vật phẩm
        Text nameText = new Text(itemType.getDisplayName());
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameText.setFill(Color.WHITE);
        content.getChildren().add(nameText);

        // Giá
        Text priceText = new Text();
        priceText.setFont(Font.font("Arial", 11));
        priceText.setFill(Color.YELLOW);
        if (isBuying) {
            priceText.setText("Giá mua: " + itemType.getBuyPrice() + " G");
        } else {
            priceText.setText("Giá bán: " + itemType.getSellPrice() + " G");
        }
        content.getChildren().add(priceText);

        // Nút mua/bán
        Text actionButton = new Text(isBuying ? "MUA" : "BÁN");
        actionButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        actionButton.setFill(isBuying ? Color.LIMEGREEN : Color.RED);
        actionButton.setStroke(Color.BLACK);
        actionButton.setStrokeWidth(0.5);
        actionButton.setOnMouseClicked(e -> {
            if (isBuying) {
                handleBuyItem(itemType);
            } else {
                handleSellItem(itemType);
            }
        });
        content.getChildren().add(actionButton);

        pane.getChildren().add(content);
        return pane;
    }

    private void handleBuyItem(ItemType itemType) {
        if (playerComponent.removeMoney(itemType.getBuyPrice())) {
            inventory.addItem(itemType, 1);
            FXGL.getNotificationService().pushNotification("Đã mua " + itemType.getDisplayName() + "!");
        } else {
            FXGL.getNotificationService().pushNotification("Không đủ tiền để mua " + itemType.getDisplayName() + "!");
        }
    }

    private void handleSellItem(ItemType itemType) {
        if (inventory.removeItem(itemType, 1)) {
            playerComponent.addMoney(itemType.getSellPrice());
            FXGL.getNotificationService().pushNotification("Đã bán " + itemType.getDisplayName() + "!");
        } else {
            FXGL.getNotificationService().pushNotification("Không có " + itemType.getDisplayName() + " để bán!");
        }
    }

    public void toggle() {
        visible = !visible;
        setVisible(visible);
        if (visible) {
            // Cập nhật vị trí để nó luôn ở giữa màn hình
            setTranslateX((FXGL.getAppWidth() - getPrefWidth()) / 2);
            setTranslateY((FXGL.getAppHeight() - getPrefHeight()) / 2);
        }
    }

    public boolean isOpen() {
        return visible;
    }
}
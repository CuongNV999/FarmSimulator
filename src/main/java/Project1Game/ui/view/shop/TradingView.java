package Project1Game.ui.view.shop;

import Project1Game.ui.presenter.TradingPresenter;
import Project1Game.ui.utility.GameFont;

import Project1Game.component.player.PlayerComponent;
import Project1Game.system.NotificationManager;
import Project1Game.system.TradeEvent;
import Project1Game.component.npc.TraderComponent;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.model.InventorySlot;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TradingView extends VBox {

    private static final int ITEM_SLOT_SIZE = 64;
    private static final int ITEM_SLOT_GAP = 5;

    private final Inventory inventory;
    private final PlayerComponent playerComponent;
    private boolean visible = false;

    private TraderComponent currentTrader;
    private final VBox buySectionContainer = new VBox();
    private final VBox sellSectionContainer = new VBox();
    private final VBox cartSectionContainer = new VBox();
    private final Text relationshipText = new Text();

    // --- THƯƠNG LƯỢNG ---
    private final HBox negotiationPanel = new HBox();
    private Slider negotiationSlider;
    private Text sliderValueText;
    private Text negotiationResultText;
    private Text negotiationHistoryText;

    // Shopping Cart list
    private final List<CartItem> cartItems = new ArrayList<>();

    private final List<ItemType> buyableItems = new ArrayList<>();
    private final List<ItemType> sellableItems = new ArrayList<>();

    // Danh sách các vật phẩm có thể mua (thực phẩm ăn được)
    private final List<ItemType> buyableFood = Arrays.asList(
            ItemType.BREAD_SLICE, ItemType.BAGUETTE, ItemType.BREAD_LOAF, ItemType.BREAD_BUN,
            ItemType.CROISSANT, ItemType.PRETZEL, ItemType.DONUT, ItemType.PANCAKE,
            ItemType.COOKED_DRUMSTICK, ItemType.COOKED_CHICKEN,
            ItemType.COOKED_MEAT, ItemType.SAUSAGE
    );

    // Danh sách các vật phẩm có thể bán (thực phẩm ăn được)
    private final List<ItemType> sellableFood = Arrays.asList(
            ItemType.BREAD_SLICE, ItemType.BAGUETTE, ItemType.BREAD_LOAF, ItemType.BREAD_BUN,
            ItemType.CROISSANT, ItemType.PRETZEL, ItemType.DONUT, ItemType.PANCAKE,
            ItemType.COOKED_DRUMSTICK, ItemType.COOKED_CHICKEN,
            ItemType.COOKED_MEAT, ItemType.SAUSAGE
    );

    // Category Tabs definition
    public enum ShopTab {
        CROPS_AND_SEEDS,
        ANIMALS,
        FOOD
    }

    private ShopTab currentTab = ShopTab.CROPS_AND_SEEDS;
    private StackPane cropsTabBtn;
    private StackPane animalsTabBtn;
    private StackPane foodTabBtn;

    private final List<ItemType> buyableAnimals = Arrays.asList(
            ItemType.CHICK, ItemType.CALF, ItemType.LAMB, ItemType.PIGLET, ItemType.TURKEY_CHICK
    );

    private final List<ItemType> sellableAnimals = Arrays.asList(
            ItemType.ROOSTER, ItemType.BULL, ItemType.SHEEP, ItemType.PIG, ItemType.TURKEY
    );

    // Inner class representing items in cart
    public static class CartItem {
        public final ItemType itemType;
        public final boolean isBuying;
        public int quantity;

        public CartItem(ItemType itemType, boolean isBuying, int quantity) {
            this.itemType = itemType;
            this.isBuying = isBuying;
            this.quantity = quantity;
        }
    }

    public TradingView(Inventory inventory, PlayerComponent playerComponent) {
        this.inventory = inventory;
        this.playerComponent = playerComponent;

        // Khởi tạo danh sách cây từ CropRegistry để tránh vi phạm Open/Closed Principle
        for (Project1Game.core.EntityType cropType : Project1Game.core.CropRegistry.getInstance().getSupportedCrops()) {
            try {
                buyableItems.add(ItemType.valueOf(cropType.name() + "_SEED"));
            } catch (IllegalArgumentException e) {
                System.err.println("[TradingView] Warning: seed not found for crop " + cropType);
            }
            try {
                sellableItems.add(ItemType.valueOf(cropType.name()));
            } catch (IllegalArgumentException e) {
                System.err.println("[TradingView] Warning: harvest item not found for crop " + cropType);
            }
        }

        // Cấu hình nền và viền cho TradingView - Mở rộng chiều ngang
        setPrefSize(FXGL.getAppWidth() * 0.9, FXGL.getAppHeight() * 0.85);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(15));
        setSpacing(10);
        setStyle("-fx-background-color: rgba(30, 20, 10, 0.95); -fx-background-radius: 15; -fx-border-color: #8B4513; -fx-border-width: 3; -fx-border-radius: 15;");

        Text title = new Text("Cửa hàng của Trader");
        title.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 28));
        title.setFill(Color.GOLD);

        // Hiển thị tiền của người chơi
        Text moneyDisplay = new Text();
        moneyDisplay.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 20));
        moneyDisplay.setFill(Color.LIGHTGREEN);
        moneyDisplay.textProperty().bind(
                Bindings.concat("Tiền của bạn: ", playerComponent.moneyProperty(), " G")
        );

        // Text hiển thị quan hệ
        relationshipText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 18));
        relationshipText.setFill(Color.LIGHTGOLDENRODYELLOW);

        // Khởi tạo thanh trượt thương lượng
        buildNegotiationPanel();

        // Cấu hình Cart container
        cartSectionContainer.setPrefWidth(350);
        cartSectionContainer.setPadding(new Insets(10));
        cartSectionContainer.setStyle("-fx-background-color: rgba(20, 15, 10, 0.8); -fx-background-radius: 10; -fx-border-color: #5C2E0B; -fx-border-width: 2; -fx-border-radius: 10;");
        cartSectionContainer.setSpacing(10);

        // Khởi tạo Tab bar
        cropsTabBtn = createTabButton("HẠT GIỐNG & NÔNG SẢN", ShopTab.CROPS_AND_SEEDS);
        animalsTabBtn = createTabButton("ĐỘNG VẬT / THÚ NUÔI", ShopTab.ANIMALS);
        foodTabBtn = createTabButton("THỰC PHẨM & ĐỒ ĂN", ShopTab.FOOD);
        HBox tabBar = new HBox(20, cropsTabBtn, animalsTabBtn, foodTabBtn);
        tabBar.setAlignment(Pos.CENTER);
        tabBar.setPadding(new Insets(5, 0, 5, 0));
        updateTabButtonVisuals();

        HBox mainContent = new HBox(30, buySectionContainer, sellSectionContainer, cartSectionContainer);
        mainContent.setAlignment(Pos.CENTER);

        getChildren().addAll(title, moneyDisplay, relationshipText, negotiationPanel, tabBar, mainContent);
        setVisible(false); // Mặc định ẩn
    }

    /**
     * Xây dựng thanh trượt thương lượng với slider, nút, và kết quả.
     */
    private void buildNegotiationPanel() {
        negotiationPanel.setAlignment(Pos.CENTER);
        negotiationPanel.setSpacing(15);
        negotiationPanel.setPadding(new Insets(8, 15, 8, 15));
        negotiationPanel.setStyle("-fx-background-color: rgba(50, 35, 20, 0.9); -fx-background-radius: 10; -fx-border-color: #D4A017; -fx-border-width: 2; -fx-border-radius: 10;");

        // Label thương lượng
        Text negotiateLabel = new Text("Thương lượng:");
        negotiateLabel.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 16));
        negotiateLabel.setFill(Color.GOLD);

        // Slider từ 10% đến 30%
        negotiationSlider = new Slider(10, 30, 10);
        negotiationSlider.setShowTickLabels(true);
        negotiationSlider.setShowTickMarks(true);
        negotiationSlider.setMajorTickUnit(5);
        negotiationSlider.setMinorTickCount(4);
        negotiationSlider.setBlockIncrement(1);
        negotiationSlider.setSnapToTicks(true);
        negotiationSlider.setPrefWidth(200);
        negotiationSlider.setStyle(
            "-fx-control-inner-background: #3E2723; " +
            "-fx-accent: #FFD700;"
        );

        // Text hiển thị giá trị slider hiện tại
        sliderValueText = new Text("10%");
        sliderValueText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 18));
        sliderValueText.setFill(Color.LIMEGREEN);

        // Cập nhật text khi kéo slider
        negotiationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int pct = newVal.intValue();
            sliderValueText.setText(pct + "%");

            // Đổi màu theo mức rủi ro
            if (pct <= 15) {
                sliderValueText.setFill(Color.LIMEGREEN);
            } else if (pct <= 22) {
                sliderValueText.setFill(Color.YELLOW);
            } else {
                sliderValueText.setFill(Color.TOMATO);
            }
        });

        // Thanh hiển thị mức rủi ro bằng gradient
        VBox riskIndicator = new VBox(2);
        riskIndicator.setAlignment(Pos.CENTER);

        Rectangle riskBar = new Rectangle(200, 8);
        riskBar.setArcWidth(4);
        riskBar.setArcHeight(4);
        riskBar.setFill(new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.LIMEGREEN),
            new Stop(0.4, Color.YELLOW),
            new Stop(0.7, Color.ORANGE),
            new Stop(1, Color.RED)
        ));

        Text riskLabel = new Text("An toàn ← → Rủi ro");
        riskLabel.setFont(Font.font(GameFont.GAME_FONT, 10));
        riskLabel.setFill(Color.LIGHTGRAY);
        riskIndicator.getChildren().addAll(riskBar, riskLabel);

        // Nút THƯƠNG LƯỢNG
        StackPane negotiateBtn = createStyledButton("THƯƠNG LƯỢNG", Color.DARKGOLDENROD, Color.WHITE, () -> {
            handleNegotiation();
        });

        // Text hiển thị kết quả thương lượng
        negotiationResultText = new Text("");
        negotiationResultText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));
        negotiationResultText.setFill(Color.WHITE);

        // Text hiển thị số lần thương lượng
        negotiationHistoryText = new Text("");
        negotiationHistoryText.setFont(Font.font(GameFont.GAME_FONT, 11));
        negotiationHistoryText.setFill(Color.LIGHTGRAY);

        VBox resultBox = new VBox(3, negotiationResultText, negotiationHistoryText);
        resultBox.setAlignment(Pos.CENTER_LEFT);
        resultBox.setPrefWidth(200);

        VBox sliderBox = new VBox(5, negotiationSlider, riskIndicator);
        sliderBox.setAlignment(Pos.CENTER);

        negotiationPanel.getChildren().addAll(negotiateLabel, sliderBox, sliderValueText, negotiateBtn, resultBox);
    }

    /**
     * Xử lý khi người chơi nhấn nút "THƯƠNG LƯỢNG"
     */
    private void handleNegotiation() {
        if (currentTrader == null) {
            NotificationManager.pushNotification("Không có Trader nào để thương lượng!");
            return;
        }

        int requestedPercent = (int) negotiationSlider.getValue();
        FXGL.getEventBus().fireEvent(new TradeEvent(TradeEvent.NEGOTIATE, currentTrader, requestedPercent));
    }

    public void displayNegotiationResult(boolean success, int requestedPercent) {
        if (currentTrader == null) return;

        if (success) {
            negotiationResultText.setText("✓ Thành công! Giảm " + requestedPercent + "%");
            negotiationResultText.setFill(Color.LIMEGREEN);
            NotificationManager.pushNotification("Thương lượng thành công! Giảm " + requestedPercent + "% giá!");
        } else {
            int penalty = currentTrader.getNegotiationBonusPercent();
            negotiationResultText.setText("✗ Thất bại! Phạt " + Math.abs(penalty) + "%");
            negotiationResultText.setFill(Color.TOMATO);
            NotificationManager.pushNotification("Thương lượng thất bại! Bị phạt " + Math.abs(penalty) + "% giá!");
        }

        // Cập nhật lịch sử thương lượng
        negotiationHistoryText.setText("Đã thương lượng " + currentTrader.getNegotiationCount() + " lần tổng cộng");

        // Cập nhật text quan hệ (có thể đã bị giảm)
        relationshipText.setText("Mức quan hệ với Trader: " + currentTrader.getRelationship().name());

        // Disable slider sau khi đã thương lượng
        negotiationSlider.setDisable(true);

        // Làm mới giá trên các item slots (giá đã thay đổi)
        buySectionContainer.getChildren().clear();
        buySectionContainer.getChildren().add(createBuySection());
        sellSectionContainer.getChildren().clear();
        sellSectionContainer.getChildren().add(createSellSection());

        // Cập nhật lại giỏ hàng (giá đã thay đổi)
        updateCartUI();
    }

    public void open(TraderComponent tc) {
        open(tc, true);
    }

    public void open(TraderComponent tc, boolean resetTab) {
        if (resetTab) {
            // Mở với tab Hạt Giống làm mặc định
            this.currentTab = ShopTab.CROPS_AND_SEEDS;
        }
        updateTabButtonVisuals();

        // Nếu thay đổi trader, xóa giỏ hàng cũ
        if (this.currentTrader != tc) {
            this.cartItems.clear();
        }
        this.currentTrader = tc;

        // Cập nhật text quan hệ
        if (currentTrader != null) {
            relationshipText.setText("Mức quan hệ với Trader: " + currentTrader.getRelationship().name());
        } else {
            relationshipText.setText("");
        }

        // Cập nhật trạng thái thanh trượt thương lượng
        updateNegotiationPanelState();

        // Tái tạo nội dung mua/bán với giá đã điều chỉnh
        buySectionContainer.getChildren().clear();
        buySectionContainer.getChildren().add(createBuySection());

        sellSectionContainer.getChildren().clear();
        sellSectionContainer.getChildren().add(createSellSection());

        // Cập nhật giao diện giỏ hàng
        updateCartUI();

        visible = true;
        setVisible(true);
        // Cập nhật vị trí để nó luôn ở giữa màn hình
        setTranslateX((FXGL.getAppWidth() - getPrefWidth()) / 2);
        setTranslateY((FXGL.getAppHeight() - getPrefHeight()) / 2);
    }

    /**
     * Cập nhật trạng thái giao diện thanh trượt thương lượng khi mở shop
     */
    private void updateNegotiationPanelState() {
        if (currentTrader == null) {
            negotiationPanel.setVisible(false);
            return;
        }

        negotiationPanel.setVisible(true);

        if (currentTrader.hasNegotiatedThisSession()) {
            // Đã thương lượng trong phiên này
            negotiationSlider.setDisable(true);
            int bonus = currentTrader.getNegotiationBonusPercent();
            if (bonus > 0) {
                negotiationResultText.setText("✓ Đã giảm " + bonus + "%");
                negotiationResultText.setFill(Color.LIMEGREEN);
            } else if (bonus < 0) {
                negotiationResultText.setText("✗ Bị phạt " + Math.abs(bonus) + "%");
                negotiationResultText.setFill(Color.TOMATO);
            } else {
                negotiationResultText.setText("");
            }
        } else {
            // Chưa thương lượng, cho phép kéo slider
            negotiationSlider.setDisable(false);
            negotiationSlider.setValue(10); // Reset về 10%
            sliderValueText.setText("10%");
            sliderValueText.setFill(Color.LIMEGREEN);
            negotiationResultText.setText("");
        }

        // Cập nhật lịch sử
        int count = currentTrader.getNegotiationCount();
        if (count > 0) {
            negotiationHistoryText.setText("Đã thương lượng " + count + " lần tổng cộng");
            // Cảnh báo nếu thương lượng quá nhiều
            if (count >= 3) {
                negotiationHistoryText.setFill(Color.ORANGE);
            } else {
                negotiationHistoryText.setFill(Color.LIGHTGRAY);
            }
        } else {
            negotiationHistoryText.setText("Chưa thương lượng lần nào");
            negotiationHistoryText.setFill(Color.LIGHTGRAY);
        }
    }

    private VBox createBuySection() {
        String titleText;
        if (currentTab == ShopTab.CROPS_AND_SEEDS) {
            titleText = "Mua Hạt giống";
        } else if (currentTab == ShopTab.ANIMALS) {
            titleText = "Mua Thú nuôi (Con non)";
        } else {
            titleText = "Mua Thực phẩm & Đồ ăn";
        }
        Text buyTitle = new Text(titleText);
        buyTitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 22));
        buyTitle.setFill(Color.LIGHTBLUE);

        GridPane buyGrid = new GridPane();
        buyGrid.setHgap(ITEM_SLOT_GAP);
        buyGrid.setVgap(ITEM_SLOT_GAP);
        buyGrid.setPadding(new Insets(10));

        List<ItemType> items;
        if (currentTab == ShopTab.CROPS_AND_SEEDS) {
            items = buyableItems;
        } else if (currentTab == ShopTab.ANIMALS) {
            items = buyableAnimals;
        } else {
            items = buyableFood;
        }

        int col = 0;
        int row = 0;
        for (ItemType item : items) {
            buyGrid.add(createTradingItemSlot(item, true), col, row);
            col++;
            if (col >= 3) { // 3 cột
                col = 0;
                row++;
            }
        }

        ScrollPane scroll = new ScrollPane(buyGrid);
        scroll.setPrefHeight(320);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-viewport-background: transparent; -fx-bar-color: #5C2E0B;");

        return new VBox(10, buyTitle, scroll);
    }

    private VBox createSellSection() {
        String titleText;
        if (currentTab == ShopTab.CROPS_AND_SEEDS) {
            titleText = "Bán Nông sản";
        } else if (currentTab == ShopTab.ANIMALS) {
            titleText = "Bán Động vật (Trưởng thành)";
        } else {
            titleText = "Bán Thực phẩm & Đồ ăn";
        }
        Text sellTitle = new Text(titleText);
        sellTitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 22));
        sellTitle.setFill(Color.ORANGE);

        GridPane sellGrid = new GridPane();
        sellGrid.setHgap(ITEM_SLOT_GAP);
        sellGrid.setVgap(ITEM_SLOT_GAP);
        sellGrid.setPadding(new Insets(10));

        List<ItemType> items;
        if (currentTab == ShopTab.CROPS_AND_SEEDS) {
            items = sellableItems;
        } else if (currentTab == ShopTab.ANIMALS) {
            items = sellableAnimals;
        } else {
            items = sellableFood;
        }

        int col = 0;
        int row = 0;
        for (ItemType item : items) {
            sellGrid.add(createTradingItemSlot(item, false), col, row);
            col++;
            if (col >= 3) { // 3 cột
                col = 0;
                row++;
            }
        }

        ScrollPane scroll = new ScrollPane(sellGrid);
        scroll.setPrefHeight(320);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-viewport-background: transparent; -fx-bar-color: #5C2E0B;");

        return new VBox(10, sellTitle, scroll);
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
            Texture icon;
            if (itemType.getIconName().startsWith("food:")) {
                icon = new Texture(ItemType.extractFoodImage(itemType.getIconName()));
            } else if (itemType.getIconName().startsWith("Animal/")) {
                icon = new Texture(Project1Game.component.farming.animal.BaseAnimalComponent.extractFaceDownIdleImage(itemType.getIconName()));
            } else {
                icon = FXGL.texture(itemType.getIconName());
            }
            icon.setFitWidth(ITEM_SLOT_SIZE);
            icon.setFitHeight(ITEM_SLOT_SIZE);
            icon.setPreserveRatio(true);
            content.getChildren().add(icon);
        }

        // Tên vật phẩm
        Text nameText = new Text(itemType.getDisplayName());
        nameText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 12));
        nameText.setFill(Color.WHITE);
        content.getChildren().add(nameText);

        // Giá đã được điều chỉnh dựa vào quan hệ
        int basePrice = isBuying ? itemType.getBuyPrice() : itemType.getSellPrice();
        int adjustedPrice = basePrice;
        if (currentTrader != null) {
            adjustedPrice = currentTrader.getAdjustedPrice(basePrice, isBuying);
        }

        // Giá hiển thị
        Text priceText = new Text();
        priceText.setFont(Font.font(GameFont.GAME_FONT, 11));
        priceText.setFill(Color.YELLOW);
        if (isBuying) {
            priceText.setText("Giá mua: " + adjustedPrice + " G");
        } else {
            priceText.setText("Giá bán: " + adjustedPrice + " G");
        }
        content.getChildren().add(priceText);

        // Nút mua/bán -> Chuyển thành thêm vào giỏ hàng (sử dụng StackPane để tạo nút rõ nét, không bị mờ)
        StackPane actionButton = new StackPane();
        
        Rectangle btnBg = new Rectangle(75, 22);
        btnBg.setArcWidth(6);
        btnBg.setArcHeight(6);
        btnBg.setFill(isBuying ? Color.rgb(46, 125, 50) : Color.rgb(198, 40, 40));
        btnBg.setStroke(isBuying ? Color.rgb(27, 94, 32) : Color.rgb(183, 28, 28));
        btnBg.setStrokeWidth(1);
        
        Text btnText = new Text(isBuying ? "MUA (+)" : "BÁN (+)");
        btnText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 12));
        btnText.setFill(Color.WHITE);
        
        actionButton.getChildren().addAll(btnBg, btnText);
        actionButton.setStyle("-fx-cursor: hand;");
        actionButton.setOnMouseClicked(e -> {
            addToCart(itemType, isBuying);
        });
        
        actionButton.setOnMouseEntered(e -> {
            btnBg.setFill(isBuying ? Color.rgb(56, 142, 60) : Color.rgb(211, 47, 47));
        });
        actionButton.setOnMouseExited(e -> {
            btnBg.setFill(isBuying ? Color.rgb(46, 125, 50) : Color.rgb(198, 40, 40));
        });
        
        content.getChildren().add(actionButton);

        pane.getChildren().add(content);
        return pane;
    }

    private void addToCart(ItemType itemType, boolean isBuying) {
        if (currentTrader != null && currentTrader.willRefuseTrade()) {
            NotificationManager.pushNotification("Trader đang bực bội và từ chối giao dịch!");
            return;
        }

        // Kiểm tra xem đã có trong giỏ hàng chưa
        for (CartItem ci : cartItems) {
            if (ci.itemType == itemType && ci.isBuying == isBuying) {
                if (!isBuying) {
                    int inventoryCount = inventory.getCount(itemType);
                    if (ci.quantity >= inventoryCount) {
                        NotificationManager.pushNotification("Không thể bán nhiều hơn số lượng trong kho đồ!");
                        return;
                    }
                }
                ci.quantity++;
                updateCartUI();
                return;
            }
        }

        // Nếu là bán, kiểm tra xem có ít nhất 1 vật phẩm trong kho đồ không
        if (!isBuying) {
            int inventoryCount = inventory.getCount(itemType);
            if (inventoryCount <= 0) {
                NotificationManager.pushNotification("Không có " + itemType.getDisplayName() + " để bán!");
                return;
            }
        }

        // Thêm mới vào giỏ hàng
        cartItems.add(new CartItem(itemType, isBuying, 1));
        updateCartUI();
    }

    private void updateCartUI() {
        cartSectionContainer.getChildren().clear();

        Text cartTitle = new Text("Giỏ Hàng");
        cartTitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 22));
        cartTitle.setFill(Color.GOLD);
        cartSectionContainer.getChildren().add(cartTitle);

        VBox itemsBox = new VBox(5);
        itemsBox.setMinWidth(330);

        int totalBuyCost = 0;
        int totalSellIncome = 0;

        if (cartItems.isEmpty()) {
            Text emptyText = new Text("Giỏ hàng trống");
            emptyText.setFont(Font.font(GameFont.GAME_FONT, 14));
            emptyText.setFill(Color.GRAY);
            itemsBox.getChildren().add(emptyText);
        } else {
            for (CartItem ci : cartItems) {
                int basePrice = ci.isBuying ? ci.itemType.getBuyPrice() : ci.itemType.getSellPrice();
                int adjustedPrice = basePrice;
                if (currentTrader != null) {
                    adjustedPrice = currentTrader.getAdjustedPrice(basePrice, ci.isBuying);
                }

                int totalItemPrice = adjustedPrice * ci.quantity;
                if (ci.isBuying) {
                    totalBuyCost += totalItemPrice;
                } else {
                    totalSellIncome += totalItemPrice;
                }

                HBox row = createCartItemRow(ci, adjustedPrice);
                itemsBox.getChildren().add(row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setPrefHeight(220);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-viewport-background: transparent; -fx-bar-color: #5C2E0B;");
        cartSectionContainer.getChildren().add(scrollPane);

        // Summary
        VBox summaryBox = new VBox(5);
        summaryBox.setPadding(new Insets(10, 0, 10, 0));
        summaryBox.setStyle("-fx-border-color: #5C2E0B; -fx-border-width: 1 0 0 0;");

        Text buyCostText = new Text("Tổng mua: " + totalBuyCost + " G");
        buyCostText.setFont(Font.font(GameFont.GAME_FONT, 12));
        buyCostText.setFill(Color.LIGHTBLUE);

        Text sellIncomeText = new Text("Tổng bán: " + totalSellIncome + " G");
        sellIncomeText.setFont(Font.font(GameFont.GAME_FONT, 12));
        sellIncomeText.setFill(Color.ORANGE);

        int netCost = totalBuyCost - totalSellIncome;
        Text netText = new Text();
        netText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));
        if (netCost > 0) {
            netText.setText("Tổng thanh toán: " + netCost + " G");
            netText.setFill(Color.TOMATO);
        } else if (netCost < 0) {
            netText.setText("Tổng nhận lại: " + (-netCost) + " G");
            netText.setFill(Color.LIMEGREEN);
        } else {
            netText.setText("Tổng thanh toán: 0 G");
            netText.setFill(Color.WHITE);
        }

        summaryBox.getChildren().addAll(buyCostText, sellIncomeText, netText);
        cartSectionContainer.getChildren().add(summaryBox);

        // Buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);

        final int finalNetCost = netCost;
        StackPane checkoutBtn = createStyledButton("THANH TOÁN", Color.GREEN, Color.WHITE, () -> {
            handleCheckout(finalNetCost);
        });

        StackPane clearBtn = createStyledButton("XÓA GIỎ", Color.FIREBRICK, Color.WHITE, () -> {
            cartItems.clear();
            updateCartUI();
        });

        actionButtons.getChildren().addAll(checkoutBtn, clearBtn);
        cartSectionContainer.getChildren().add(actionButtons);
    }

    private HBox createCartItemRow(CartItem cartItem, int unitPrice) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));
        row.setStyle("-fx-background-color: rgba(50, 30, 15, 0.5); -fx-background-radius: 5;");
        row.setPrefWidth(310);

        // Icon
        Texture icon;
        if (cartItem.itemType.getIconName() != null && cartItem.itemType.getIconName().startsWith("food:")) {
            icon = new Texture(ItemType.extractFoodImage(cartItem.itemType.getIconName()));
        } else if (cartItem.itemType.getIconName() != null && cartItem.itemType.getIconName().startsWith("Animal/")) {
            icon = new Texture(Project1Game.component.farming.animal.BaseAnimalComponent.extractFaceDownIdleImage(cartItem.itemType.getIconName()));
        } else if (cartItem.itemType.getIconName() != null && !cartItem.itemType.getIconName().isEmpty()) {
            icon = FXGL.texture(cartItem.itemType.getIconName());
        } else {
            icon = new Texture(FXGL.image("empty.png"));
        }
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        icon.setPreserveRatio(true);

        // Info
        VBox info = new VBox(2);
        info.setPrefWidth(100);
        Text nameText = new Text(cartItem.itemType.getDisplayName());
        nameText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 11));
        nameText.setFill(Color.WHITE);

        Text typeText = new Text(cartItem.isBuying ? "Mua" : "Bán");
        typeText.setFont(Font.font(GameFont.GAME_FONT, 9));
        typeText.setFill(cartItem.isBuying ? Color.LIMEGREEN : Color.ORANGE);
        info.getChildren().addAll(nameText, typeText);

        // Price
        Text priceText = new Text(unitPrice + " G");
        priceText.setFont(Font.font(GameFont.GAME_FONT, 10));
        priceText.setFill(Color.LIGHTGRAY);
        StackPane pricePane = new StackPane(priceText);
        pricePane.setPrefWidth(35);
        pricePane.setAlignment(Pos.CENTER_LEFT);

        // Quantity controls
        HBox qtyControls = new HBox(4);
        qtyControls.setAlignment(Pos.CENTER);
        qtyControls.setPrefWidth(60);

        Text minusBtn = new Text(" - ");
        minusBtn.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));
        minusBtn.setFill(Color.TOMATO);
        minusBtn.setStyle("-fx-cursor: hand;");
        minusBtn.setOnMouseClicked(e -> {
            if (cartItem.quantity > 1) {
                cartItem.quantity--;
                updateCartUI();
            }
        });

        Text qtyText = new Text(String.valueOf(cartItem.quantity));
        qtyText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 11));
        qtyText.setFill(Color.YELLOW);

        Text plusBtn = new Text(" + ");
        plusBtn.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));
        plusBtn.setFill(Color.LIMEGREEN);
        plusBtn.setStyle("-fx-cursor: hand;");
        plusBtn.setOnMouseClicked(e -> {
            if (!cartItem.isBuying) {
                int inventoryCount = inventory.getCount(cartItem.itemType);
                if (cartItem.quantity >= inventoryCount) {
                    NotificationManager.pushNotification("Không thể bán nhiều hơn số lượng trong kho đồ!");
                    return;
                }
            }
            cartItem.quantity++;
            updateCartUI();
        });
        qtyControls.getChildren().addAll(minusBtn, qtyText, plusBtn);

        // Total row price
        Text totalRowPrice = new Text((unitPrice * cartItem.quantity) + " G");
        totalRowPrice.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 11));
        totalRowPrice.setFill(Color.GOLD);
        StackPane totalPane = new StackPane(totalRowPrice);
        totalPane.setPrefWidth(45);
        totalPane.setAlignment(Pos.CENTER_LEFT);

        // Remove button
        Text removeBtn = new Text("✕");
        removeBtn.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 12));
        removeBtn.setFill(Color.TOMATO);
        removeBtn.setStyle("-fx-cursor: hand;");
        removeBtn.setOnMouseClicked(e -> {
            cartItems.remove(cartItem);
            updateCartUI();
        });

        row.getChildren().addAll(icon, info, pricePane, qtyControls, totalPane, removeBtn);
        return row;
    }

    private StackPane createStyledButton(String label, Color bgColor, Color textColor, Runnable action) {
        StackPane btn = new StackPane();
        btn.setPadding(new Insets(5, 10, 5, 10));
        Rectangle bg = new Rectangle(100, 30);
        bg.setFill(bgColor);
        bg.setStroke(bgColor.darker());
        bg.setStrokeWidth(1);
        bg.setArcWidth(5);
        bg.setArcHeight(5);

        Text text = new Text(label);
        text.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 12));
        text.setFill(textColor);

        btn.getChildren().addAll(bg, text);
        btn.setStyle("-fx-cursor: hand;");
        btn.setOnMouseClicked(e -> action.run());

        btn.setOnMouseEntered(e -> bg.setOpacity(0.8));
        btn.setOnMouseExited(e -> bg.setOpacity(1.0));

        return btn;
    }

    private void handleCheckout(int netCost) {
        FXGL.getEventBus().fireEvent(new TradeEvent(TradeEvent.CHECKOUT, currentTrader, cartItems, netCost));
    }

    public void clearCart() {
        cartItems.clear();
    }

    public void refreshAfterTrade() {
        if (currentTrader != null) {
            relationshipText.setText("Mức quan hệ với Trader: " + currentTrader.getRelationship().name());
        }
        updateNegotiationPanelState();
        refreshTabs();
        updateCartUI();
    }

    public void toggle() {
        visible = !visible;
        setVisible(visible);
        if (visible) {
            open(null); // Mặc định không truyền TraderComponent, mở với giá gốc
        } else {
            currentTrader = null;
            cartItems.clear(); // Xóa sạch giỏ hàng khi đóng shop
        }
    }

    public boolean isOpen() {
        return visible;
    }

    // Helper methods for category tab UI
    private StackPane createTabButton(String label, ShopTab tab) {
        StackPane btn = new StackPane();
        btn.setPadding(new Insets(6, 12, 6, 12));
        
        Rectangle bg = new Rectangle(200, 32);
        bg.setArcWidth(6);
        bg.setArcHeight(6);
        
        Text text = new Text(label);
        text.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 12));
        
        btn.getChildren().addAll(bg, text);
        btn.setStyle("-fx-cursor: hand;");
        
        btn.setOnMouseClicked(e -> {
            if (currentTab != tab) {
                currentTab = tab;
                refreshTabs();
            }
        });
        
        return btn;
    }

    private void refreshTabs() {
        updateTabButtonVisuals();
        
        buySectionContainer.getChildren().clear();
        buySectionContainer.getChildren().add(createBuySection());
        
        sellSectionContainer.getChildren().clear();
        sellSectionContainer.getChildren().add(createSellSection());
    }

    private void updateTabButtonVisuals() {
        setTabBtnState(cropsTabBtn, currentTab == ShopTab.CROPS_AND_SEEDS);
        setTabBtnState(animalsTabBtn, currentTab == ShopTab.ANIMALS);
        setTabBtnState(foodTabBtn, currentTab == ShopTab.FOOD);
    }

    private void setTabBtnState(StackPane btn, boolean isActive) {
        if (btn == null) return;
        Rectangle bg = (Rectangle) btn.getChildren().get(0);
        Text text = (Text) btn.getChildren().get(1);
        if (isActive) {
            bg.setFill(Color.rgb(139, 90, 43, 0.9));
            bg.setStroke(Color.GOLD);
            bg.setStrokeWidth(2.5);
            text.setFill(Color.GOLD);
        } else {
            bg.setFill(Color.rgb(60, 40, 20, 0.6));
            bg.setStroke(Color.rgb(100, 70, 40));
            bg.setStrokeWidth(1.5);
            text.setFill(Color.LIGHTGRAY);
        }
    }
}

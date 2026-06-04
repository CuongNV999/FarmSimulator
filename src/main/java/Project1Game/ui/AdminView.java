package Project1Game.ui;

import Project1Game.component.player.PlayerComponent;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;

/**
 * Admin Console View.
 * Press ~ (BACK_QUOTE) to open, unlocked with key "1111".
 */
public class AdminView extends VBox {

    private final Inventory inventory;
    private final PlayerComponent playerComponent;
    private boolean visible = false;
    private boolean authenticated = false;

    private final VBox container = new VBox(15);
    private final TextField passcodeField = new TextField();
    private final Text errorText = new Text("");

    public AdminView(Inventory inventory, PlayerComponent playerComponent) {
        this.inventory = inventory;
        this.playerComponent = playerComponent;

        // Base styles for the admin console panel overlay
        setPrefSize(FXGL.getAppWidth() * 0.75, FXGL.getAppHeight() * 0.85);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));
        setSpacing(15);
        setStyle("-fx-background-color: rgba(18, 18, 24, 0.96); -fx-background-radius: 15; -fx-border-color: #d9383a; -fx-border-width: 3; -fx-border-radius: 15;");

        container.setAlignment(Pos.CENTER);
        getChildren().add(container);
        setVisible(false);

        showPasscodeScreen();
    }

    private void showPasscodeScreen() {
        container.getChildren().clear();

        Text title = new Text("ADMIN PANEL ACCESS");
        title.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 28));
        title.setFill(Color.web("#d9383a"));

        Text subtitle = new Text("Enter Authorization Passcode");
        subtitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.NORMAL, 16));
        subtitle.setFill(Color.LIGHTGRAY);

        passcodeField.setPromptText("Enter key...");
        passcodeField.setPrefWidth(200);
        passcodeField.setMaxWidth(200);
        passcodeField.setAlignment(Pos.CENTER);
        passcodeField.setStyle("-fx-background-color: #22222b; -fx-text-fill: white; -fx-border-color: #444; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 16px;");

        // Action when pressing enter key
        passcodeField.setOnAction(e -> attemptAuthentication());

        Button submitBtn = new Button("AUTHORIZE");
        submitBtn.setStyle("-fx-background-color: #d9383a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> attemptAuthentication());

        errorText.setFill(Color.RED);
        errorText.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));

        container.getChildren().addAll(title, subtitle, passcodeField, submitBtn, errorText);
    }

    private void attemptAuthentication() {
        String code = passcodeField.getText().trim();
        if ("1111".equals(code)) {
            authenticated = true;
            errorText.setText("");
            passcodeField.clear();
            showAdminControls();
        } else {
            errorText.setText("Access Denied: Invalid Passcode!");
            passcodeField.clear();
        }
    }

    private void showAdminControls() {
        container.getChildren().clear();

        Text title = new Text("SYSTEM ADMINISTRATOR CONSOLE");
        title.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 26));
        title.setFill(Color.web("#d9383a"));

        HBox columns = new HBox(30);
        columns.setAlignment(Pos.TOP_CENTER);
        columns.setPrefHeight(400);

        // --- SECTION 1: Player configuration adjustments ---
        VBox statsCol = new VBox(15);
        statsCol.setPrefWidth(280);
        statsCol.setAlignment(Pos.TOP_LEFT);
        statsCol.setPadding(new Insets(10));
        statsCol.setStyle("-fx-background-color: rgba(30, 30, 40, 0.5); -fx-background-radius: 10; -fx-border-color: #444; -fx-border-width: 1; -fx-border-radius: 10;");

        Text statsTitle = new Text("Player Configuration");
        statsTitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 18));
        statsTitle.setFill(Color.web("#eccb58"));

        // Adjusting gold field
        Text goldLabel = new Text("Gold:");
        goldLabel.setFill(Color.WHITE);
        goldLabel.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));
        
        TextField goldInput = new TextField(String.valueOf(playerComponent.getMoney()));
        goldInput.setPrefWidth(120);
        goldInput.setStyle("-fx-background-color: #1a1a24; -fx-text-fill: gold; -fx-border-color: #555; -fx-border-radius: 4; -fx-font-weight: bold;");
        
        Button applyGold = new Button("Set");
        applyGold.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-cursor: hand;");
        applyGold.setOnAction(e -> {
            try {
                int gold = Integer.parseInt(goldInput.getText().trim());
                playerComponent.setMoney(gold);
            } catch (NumberFormatException ignored) {}
        });

        HBox goldRow = new HBox(10, goldLabel, goldInput, applyGold);
        goldRow.setAlignment(Pos.CENTER_LEFT);

        // Shortcut buttons for adding money
        Button add1k = new Button("+1K Gold");
        add1k.setStyle("-fx-background-color: #2b4c3f; -fx-text-fill: #a2e8c2; -fx-font-weight: bold; -fx-cursor: hand;");
        add1k.setOnAction(e -> {
            playerComponent.addMoney(1000);
            goldInput.setText(String.valueOf(playerComponent.getMoney()));
        });

        Button add10k = new Button("+10K Gold");
        add10k.setStyle("-fx-background-color: #1e3a47; -fx-text-fill: #92d4f5; -fx-font-weight: bold; -fx-cursor: hand;");
        add10k.setOnAction(e -> {
            playerComponent.addMoney(10000);
            goldInput.setText(String.valueOf(playerComponent.getMoney()));
        });

        HBox quickGoldRow = new HBox(10, add1k, add10k);

        // Skin Selection Panel
        Text skinTitle = new Text("Choose Player Skin:");
        skinTitle.setFill(Color.WHITE);
        skinTitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));

        GridPane skinGrid = new GridPane();
        skinGrid.setHgap(8);
        skinGrid.setVgap(8);

        String[] skinNames = {"Default", "Skeleton", "Male"};
        String[] skinPaths = {"Player", "Player_Skeleton", "Player_Male"};

        for (int i = 0; i < skinNames.length; i++) {
            final String path = skinPaths[i];
            Button btnSkin = new Button(skinNames[i]);
            btnSkin.setPrefWidth(80);
            btnSkin.setStyle("-fx-background-color: #3e3e4a; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px;");
            btnSkin.setOnAction(e -> {
                playerComponent.changeSkin(path);
            });
            skinGrid.add(btnSkin, i % 3, i / 3);
        }

        // Time Speed Presets Panel
        Text timeTitle = new Text("Time Speed Multiplier:");
        timeTitle.setFill(Color.WHITE);
        timeTitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));

        HBox timeSpeedButtons = new HBox(6);
        timeSpeedButtons.setAlignment(Pos.CENTER_LEFT);

        double[] presets = {1.0, 10.0, 50.0, 100.0};
        String[] presetLabels = {"1x", "10x", "50x", "100x"};

        for (int i = 0; i < presets.length; i++) {
            final double val = presets[i];
            Button btnTime = new Button(presetLabels[i]);
            btnTime.setPrefWidth(55);
            btnTime.setStyle("-fx-background-color: #3e3e4a; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px;");
            btnTime.setOnAction(e -> {
                if (Project1Game.Main.getInstance() != null && Project1Game.Main.getInstance().getTimeSystem() != null) {
                    Project1Game.Main.getInstance().getTimeSystem().setTimeSpeedMultiplier(val);
                }
            });
            timeSpeedButtons.getChildren().add(btnTime);
        }

        // Cheat button
        Text cheatsTitle = new Text("Cheats:");
        cheatsTitle.setFill(Color.WHITE);
        cheatsTitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 14));

        Button btnMatureAll = new Button("Instant Mature All");
        btnMatureAll.setPrefWidth(160);
        btnMatureAll.setStyle("-fx-background-color: #eccb58; -fx-text-fill: #12121c; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 13px;");
        btnMatureAll.setOnAction(e -> {
            if (Project1Game.Main.getInstance() != null) {
                Project1Game.Main.getInstance().matureAllCropsAndAnimals();
            }
        });

        Button btnSpawnBushMonster = new Button("Spawn Bush Monster");
        btnSpawnBushMonster.setPrefWidth(160);
        btnSpawnBushMonster.setStyle("-fx-background-color: #d9383a; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 13px;");
        btnSpawnBushMonster.setOnAction(e -> {
            if (Project1Game.Main.getInstance() != null) {
                Project1Game.Main.getInstance().spawnBushMonsterAdmin();
            }
        });

        statsCol.getChildren().addAll(statsTitle, goldRow, quickGoldRow, skinTitle, skinGrid, timeTitle, timeSpeedButtons, cheatsTitle, btnMatureAll, btnSpawnBushMonster);

        // --- SECTION 2: Crop & inventory Adjusters scrollpane ---
        VBox itemsCol = new VBox(10);
        itemsCol.setPrefWidth(450);
        itemsCol.setAlignment(Pos.TOP_LEFT);

        Text itemsTitle = new Text("Crop & Inventory Adjuster");
        itemsTitle.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 18));
        itemsTitle.setFill(Color.web("#eccb58"));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefViewportHeight(320);
        scrollPane.setPrefViewportWidth(440);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        GridPane itemGrid = new GridPane();
        itemGrid.setHgap(10);
        itemGrid.setVgap(10);
        itemGrid.setPadding(new Insets(10));
        itemGrid.setStyle("-fx-background-color: rgba(20, 20, 30, 0.6); -fx-background-radius: 10;");

        List<ItemType> items = Arrays.asList(
                ItemType.WHEAT_SEED, ItemType.RADISH_SEED,
                ItemType.CABBAGE_SEED,
                ItemType.GRAPE_SEED, ItemType.CUCUMBER_SEED, ItemType.PEPPER_SEED,
                ItemType.CAULIFLOWER_SEED, ItemType.BEAN_SEED, ItemType.PINEAPPLE_SEED,
                ItemType.SUNFLOWER_SEED, ItemType.COCONUT_SEED, ItemType.APPLE_SEED,
                ItemType.WHEAT, ItemType.RADISH,
                ItemType.CABBAGE,
                ItemType.GRAPE, ItemType.CUCUMBER, ItemType.PEPPER,
                ItemType.CAULIFLOWER, ItemType.BEAN, ItemType.PINEAPPLE,
                ItemType.SUNFLOWER, ItemType.COCONUT, ItemType.APPLE,
                ItemType.BREAD_SLICE, ItemType.BAGUETTE, ItemType.BREAD_LOAF, ItemType.BREAD_BUN,
                ItemType.CROISSANT, ItemType.PRETZEL, ItemType.DONUT, ItemType.PANCAKE,
                ItemType.COOKED_DRUMSTICK, ItemType.COOKED_CHICKEN,
                ItemType.COOKED_MEAT, ItemType.SAUSAGE,
                ItemType.CHICK, ItemType.CALF, ItemType.LAMB, ItemType.PIGLET, ItemType.TURKEY,
                ItemType.ROOSTER, ItemType.BULL, ItemType.SHEEP, ItemType.PIG
        );

        int row = 0;
        for (ItemType item : items) {
            HBox itemRow = createItemAdjustRow(item);
            itemGrid.add(itemRow, 0, row);
            row++;
        }

        scrollPane.setContent(itemGrid);
        itemsCol.getChildren().addAll(itemsTitle, scrollPane);

        columns.getChildren().addAll(statsCol, itemsCol);

        // Bottom control close button
        Button closeBtn = new Button("CLOSE CONSOLE");
        closeBtn.setStyle("-fx-background-color: #d9383a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 25; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> toggle());

        container.getChildren().addAll(title, columns, closeBtn);
    }

    private HBox createItemAdjustRow(ItemType type) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 8, 4, 8));
        row.setStyle("-fx-background-color: rgba(40, 40, 50, 0.4); -fx-background-radius: 6;");
        row.setPrefWidth(420);

        // Icon
        Texture icon;
        if (type.getIconName() != null && type.getIconName().startsWith("food:")) {
            icon = new Texture(ItemType.extractFoodImage(type.getIconName()));
        } else if (type.getIconName() != null && type.getIconName().startsWith("Animal/")) {
            icon = new Texture(Project1Game.component.farming.animal.BaseAnimalComponent.extractFaceDownIdleImage(type.getIconName()));
        } else if (type.getIconName() != null && !type.getIconName().isEmpty()) {
            icon = FXGL.texture(type.getIconName());
        } else {
            icon = new Texture(FXGL.image("empty.png"));
        }
        icon.setFitWidth(32);
        icon.setFitHeight(32);

        // Name
        Text name = new Text(type.getDisplayName());
        name.setFont(Font.font(GameFont.GAME_FONT, FontWeight.BOLD, 13));
        name.setFill(Color.WHITE);
        name.setWrappingWidth(110);

        // Quantity count label
        Text qty = new Text("Qty: " + inventory.getCount(type));
        qty.setFont(Font.font(GameFont.GAME_FONT, FontWeight.NORMAL, 13));
        qty.setFill(Color.LIGHTGREEN);
        qty.setWrappingWidth(60);

        // Buttons
        Button minus5 = new Button("-5");
        minus5.setStyle("-fx-background-color: #d9383a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        minus5.setOnAction(e -> {
            inventory.removeItem(type, 5);
            qty.setText("Qty: " + inventory.getCount(type));
        });

        Button minus1 = new Button("-1");
        minus1.setStyle("-fx-background-color: #b05c5c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        minus1.setOnAction(e -> {
            inventory.removeItem(type, 1);
            qty.setText("Qty: " + inventory.getCount(type));
        });

        Button plus1 = new Button("+1");
        plus1.setStyle("-fx-background-color: #5c9eb0; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        plus1.setOnAction(e -> {
            inventory.addItem(type, 1);
            qty.setText("Qty: " + inventory.getCount(type));
        });

        Button plus5 = new Button("+5");
        plus5.setStyle("-fx-background-color: #38a6d9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");
        plus5.setOnAction(e -> {
            inventory.addItem(type, 5);
            qty.setText("Qty: " + inventory.getCount(type));
        });

        row.getChildren().addAll(icon, name, qty, minus5, minus1, plus1, plus5);
        return row;
    }

    public void toggle() {
        visible = !visible;
        setVisible(visible);
        if (visible) {
            if (authenticated) {
                showAdminControls();
            } else {
                showPasscodeScreen();
            }
            setTranslateX((FXGL.getAppWidth() - getPrefWidth()) / 2);
            setTranslateY((FXGL.getAppHeight() - getPrefHeight()) / 2);
        } else {
            passcodeField.clear();
            errorText.setText("");
        }
    }

    public boolean isOpen() {
        return visible;
    }
}

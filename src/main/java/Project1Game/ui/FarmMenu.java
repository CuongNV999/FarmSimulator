package Project1Game.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import Project1Game.Main;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Custom Main and Pause Menu for Farm Simulator.
 */
public class FarmMenu extends FXGLMenu {

    public FarmMenu(MenuType type) {
        super(type);

        // 1. Create a beautiful dark green nature-themed gradient background
        LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0e2413")), // Dark green forest tone
                new Stop(1, Color.web("#050806"))  // Deep charcoal/black
        );
        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), grad);
        
        // 2. Decorative background elements (semi-transparent glowing circles)
        Rectangle glassOverlay = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0, 0, 0, 0.25));

        // 3. Create Title Container
        VBox titleContainer = new VBox(10);
        titleContainer.setAlignment(Pos.CENTER);
        titleContainer.setTranslateY(80);
        titleContainer.setPrefWidth(getAppWidth());

        // 4. Create VBox for Menu Buttons
        VBox menuBox = new VBox(15);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setTranslateY(260);
        menuBox.setPrefWidth(getAppWidth());

        // Access the main application casted instance
        Main app = FXGL.getAppCast();

        if (type == MenuType.MAIN_MENU) {
            showMainMenuScreen(menuBox, titleContainer, app);
        } else {
            // GAME (PAUSE) MENU
            Text titleText = new Text("GAME PAUSED");
            titleText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 60));
            titleText.setFill(Color.web("#eccb58")); // Rich gold color
            titleText.setEffect(new DropShadow(25, Color.rgb(100, 255, 120, 0.6)));

            Text subtitleText = new Text("Press ESC to resume");
            subtitleText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            subtitleText.setFill(Color.web("#a0bfa7")); // Pastel green
            titleContainer.getChildren().addAll(titleText, subtitleText);

            MenuButton btnResume = new MenuButton("Resume Game", this::fireResume);

            MenuButton btnSave = new MenuButton("Save Game", () -> {
                app.saveGame();
            });
            // Attach unique action for saving to update button state
            btnSave.setOnMouseClicked(e -> {
                app.saveGame();
                btnSave.setTemporaryText("Game Saved!", "Save Game");
            });

            MenuButton btnSaveExit = new MenuButton("Save & Exit", () -> {
                app.saveGame();
                fireExitToMainMenu();
            });

            MenuButton btnExitMenu = new MenuButton("Exit to Main Menu", this::fireExitToMainMenu);

            MenuButton btnExit = new MenuButton("Exit Game", this::fireExit);

            menuBox.getChildren().addAll(btnResume, btnSave, btnSaveExit, btnExitMenu, btnExit);
        }

        // Add everything to scene graph
        getContentRoot().getChildren().addAll(bg, glassOverlay, titleContainer, menuBox);
    }

    private void showMainMenuScreen(VBox menuBox, VBox titleContainer, Main app) {
        menuBox.getChildren().clear();
        titleContainer.getChildren().clear();

        Text titleText = new Text("FARM SIMULATOR");
        titleText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 60));
        titleText.setFill(Color.web("#eccb58")); // Rich gold color
        titleText.setEffect(new DropShadow(25, Color.rgb(100, 255, 120, 0.6)));

        Text subtitleText = new Text("Version 2.5 - Professional Edition");
        subtitleText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        subtitleText.setFill(Color.web("#a0bfa7")); // Pastel green
        titleContainer.getChildren().addAll(titleText, subtitleText);

        MenuButton btnNewGame = new MenuButton("New Game", () -> {
            app.setShouldLoadSaveOnStart(false);
            fireNewGame();
        });

        // Load last game button
        MenuButton btnLoadGame = new MenuButton("Load Last Game", () -> {
            app.setShouldLoadSaveOnStart(true);
            fireNewGame();
        });

        // Check if save game exists
        boolean hasSave = app.hasSaveGame();
        if (!hasSave) {
            btnLoadGame.setDisable(true);
            btnLoadGame.setOpacity(0.35);
        }

        MenuButton btnChooseSkin = new MenuButton("Choose Skin", () -> {
            showSkinSelectionScreen(menuBox, titleContainer, app);
        });

        MenuButton btnExit = new MenuButton("Exit Game", this::fireExit);

        menuBox.getChildren().addAll(btnNewGame, btnLoadGame, btnChooseSkin, btnExit);
    }

    private void showSkinSelectionScreen(VBox menuBox, VBox titleContainer, Main app) {
        menuBox.getChildren().clear();
        titleContainer.getChildren().clear();

        Text titleText = new Text("SELECT CHARACTER SKIN");
        titleText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 42));
        titleText.setFill(Color.web("#eccb58"));
        titleText.setEffect(new DropShadow(20, Color.rgb(255, 200, 100, 0.5)));

        Text subtitleText = new Text("Select a character class/style before starting your farm");
        subtitleText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        subtitleText.setFill(Color.web("#a0bfa7"));
        titleContainer.getChildren().addAll(titleText, subtitleText);

        // HBox for the skins
        javafx.scene.layout.HBox skinsRow = new javafx.scene.layout.HBox(15);
        skinsRow.setAlignment(Pos.CENTER);

        String[] skinNames = {"Default", "HUST", "Farmer", "Cowboy", "Knight", "Witch"};
        String[] skinPaths = {"Player", "Player_HUST", "Player_Farmer", "Player_Cowboy", "Player_Knight", "Player_Witch"};
        String[] skinDescs = {"Classic farmer", "BK Student", "Plaid & overalls", "West outlaw", "Iron warrior", "Wizard robe"};

        for (int i = 0; i < skinNames.length; i++) {
            final String path = skinPaths[i];
            final String name = skinNames[i];
            
            VBox skinBox = new VBox(8);
            skinBox.setAlignment(Pos.CENTER);
            skinBox.setPadding(new Insets(10));
            skinBox.setPrefSize(140, 180);
            
            // Check if active
            boolean isSelected = Project1Game.component.player.PlayerComponent.SELECTED_SKIN.equals(path);
            String borderStyle = isSelected 
                ? "-fx-border-color: #eccb58; -fx-border-width: 3; -fx-border-radius: 10;" 
                : "-fx-border-color: #355c45; -fx-border-width: 1.5; -fx-border-radius: 10;";
            
            skinBox.setStyle("-fx-background-color: rgba(10, 25, 15, 0.75); -fx-background-radius: 10; " + borderStyle);

            // Sprite preview
            com.almasb.fxgl.texture.Texture preview = FXGL.texture(path + "/player.png");
            preview.setFitWidth(48);
            preview.setFitHeight(48);

            Text nameText = new Text(name);
            nameText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            nameText.setFill(Color.WHITE);

            Text descText = new Text(skinDescs[i]);
            descText.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            descText.setFill(Color.LIGHTGRAY);

            Button selectBtn = new Button(isSelected ? "Selected" : "Select");
            selectBtn.setPrefWidth(90);
            if (isSelected) {
                selectBtn.setStyle("-fx-background-color: #eccb58; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5;");
            } else {
                selectBtn.setStyle("-fx-background-color: #254c35; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
            }

            selectBtn.setOnAction(e -> {
                Project1Game.component.player.PlayerComponent.SELECTED_SKIN = path;
                // Re-draw screen to update selections
                showSkinSelectionScreen(menuBox, titleContainer, app);
            });

            // Hover effects for the boxes
            skinBox.setOnMouseEntered(e -> {
                if (!Project1Game.component.player.PlayerComponent.SELECTED_SKIN.equals(path)) {
                    skinBox.setStyle("-fx-background-color: rgba(20, 50, 30, 0.85); -fx-background-radius: 10; -fx-border-color: #5be584; -fx-border-width: 1.5; -fx-border-radius: 10;");
                }
            });
            skinBox.setOnMouseExited(e -> {
                boolean active = Project1Game.component.player.PlayerComponent.SELECTED_SKIN.equals(path);
                String activeB = active 
                    ? "-fx-border-color: #eccb58; -fx-border-width: 3; -fx-border-radius: 10;" 
                    : "-fx-border-color: #355c45; -fx-border-width: 1.5; -fx-border-radius: 10;";
                skinBox.setStyle("-fx-background-color: rgba(10, 25, 15, 0.75); -fx-background-radius: 10; " + activeB);
            });

            skinBox.getChildren().addAll(preview, nameText, descText, selectBtn);
            skinsRow.getChildren().add(skinBox);
        }

        // Adjust position slightly to fit HBox
        menuBox.setTranslateY(210);

        MenuButton btnBack = new MenuButton("Back to Menu", () -> {
            menuBox.setTranslateY(260); // Restore original position
            showMainMenuScreen(menuBox, titleContainer, app);
        });

        menuBox.getChildren().addAll(skinsRow, btnBack);
    }

    /**
     * Premium glassmorphic button with smooth hover scaling and glow transitions.
     */
    private static class MenuButton extends StackPane {
        private final Rectangle bg;
        private final Text text;
        private final Runnable action;

        public MenuButton(String name, Runnable action) {
            this.action = action;

            bg = new Rectangle(280, 48);
            bg.setArcWidth(20);
            bg.setArcHeight(20);
            bg.setFill(Color.color(0.08, 0.16, 0.1, 0.55)); // Glass dark green
            bg.setStroke(Color.color(0.25, 0.55, 0.35, 0.7));
            bg.setStrokeWidth(2.0);

            text = new Text(name);
            text.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            text.setFill(Color.web("#e2efe5"));

            // Drop shadow for the button container
            DropShadow buttonShadow = new DropShadow(8, Color.color(0, 0, 0, 0.4));
            setEffect(buttonShadow);

            // Hover and Interaction events
            setOnMouseEntered(e -> {
                bg.setFill(Color.color(0.12, 0.32, 0.18, 0.8));
                bg.setStroke(Color.web("#5be584")); // Bright neon green glow
                text.setFill(Color.WHITE);
                setScaleX(1.05);
                setScaleY(1.05);
                buttonShadow.setColor(Color.color(0.1, 0.4, 0.2, 0.3));
            });

            setOnMouseExited(e -> {
                bg.setFill(Color.color(0.08, 0.16, 0.1, 0.55));
                bg.setStroke(Color.color(0.25, 0.55, 0.35, 0.7));
                text.setFill(Color.web("#e2efe5"));
                setScaleX(1.0);
                setScaleY(1.0);
                buttonShadow.setColor(Color.color(0, 0, 0, 0.4));
            });

            setOnMousePressed(e -> {
                bg.setFill(Color.color(0.15, 0.45, 0.25, 0.95));
                setScaleX(0.96);
                setScaleY(0.96);
            });

            setOnMouseReleased(e -> {
                bg.setFill(Color.color(0.12, 0.32, 0.18, 0.8));
                setScaleX(1.05);
                setScaleY(1.05);
                if (action != null) {
                    action.run();
                }
            });

            setAlignment(Pos.CENTER);
            getChildren().addAll(bg, text);
        }

        public void setTemporaryText(String tempText, String originalText) {
            text.setText(tempText);
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> text.setText(originalText));
            pause.play();
        }
    }
}

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
    private final MenuType type;

    public FarmMenu(MenuType type) {
        super(type);
        this.type = type;

        // 1. Background choice: background.jpg for Main Menu, semi-translucent gradient for Pause Menu
        javafx.scene.Node bgNode;
        if (type == MenuType.MAIN_MENU) {
            try {
                com.almasb.fxgl.texture.Texture bgTexture = FXGL.texture("UI/background.jpg");
                bgTexture.setFitWidth(getAppWidth());
                bgTexture.setFitHeight(getAppHeight());
                bgNode = bgTexture;
            } catch (Exception e) {
                System.err.println("Could not load background.jpg: " + e.getMessage());
                LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#0e2413")),
                        new Stop(1, Color.web("#050806"))
                );
                bgNode = new Rectangle(getAppWidth(), getAppHeight(), grad);
            }
        } else {
            LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(0.05, 0.14, 0.07, 0.75)), // Translucent dark green
                    new Stop(1, Color.color(0.02, 0.03, 0.02, 0.85))  // Translucent black
            );
            bgNode = new Rectangle(getAppWidth(), getAppHeight(), grad);
        }
        
        // 2. Glass overlay
        Rectangle glassOverlay = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0, 0, 0, 0.2));

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
            showPauseMenuScreen(menuBox, titleContainer, app);
        }

        // Add everything to scene graph
        getContentRoot().getChildren().addAll(bgNode, glassOverlay, titleContainer, menuBox);
    }

    private void showMainMenuScreen(VBox menuBox, VBox titleContainer, Main app) {
        menuBox.getChildren().clear();
        titleContainer.getChildren().clear();

        Text titleText = new Text("FARM SIMULATOR");
        titleText.setFont(GameFont.font(FontWeight.EXTRA_BOLD, 60));
        titleText.setFill(Color.web("#eccb58")); // Rich gold color
        titleText.setEffect(new DropShadow(25, Color.rgb(100, 255, 120, 0.6)));

        Text subtitleText = new Text("Version 2.5 - Professional Edition");
        subtitleText.setFont(GameFont.font(FontWeight.BOLD, 16));
        subtitleText.setFill(Color.web("#a0bfa7")); // Pastel green
        titleContainer.getChildren().addAll(titleText, subtitleText);

        MenuButton btnNewGame = new MenuButton("New Game", () -> {
            showSkinSelectionScreen(menuBox, titleContainer, app, true);
        });

        // Load last game button
        MenuButton btnLoadGame = new MenuButton("Load Last Game", () -> {
            app.setShouldLoadSaveOnStart(true);
            fireNewGame();
        });

        // Check if save game exists
        boolean hasSave = app.hasSaveGame();

        MenuButton btnOptions = new MenuButton("Options", () -> {
            showOptionsScreen(menuBox, titleContainer);
        });

        MenuButton btnExit = new MenuButton("Exit Game", this::fireExit);

        if (hasSave) {
            menuBox.getChildren().addAll(btnNewGame, btnLoadGame, btnOptions, btnExit);
        } else {
            menuBox.getChildren().addAll(btnNewGame, btnOptions, btnExit);
        }
    }

    private void showPauseMenuScreen(VBox menuBox, VBox titleContainer, Main app) {
        menuBox.getChildren().clear();
        titleContainer.getChildren().clear();

        Text titleText = new Text("GAME PAUSED");
        titleText.setFont(GameFont.font(FontWeight.EXTRA_BOLD, 60));
        titleText.setFill(Color.web("#eccb58")); // Rich gold color
        titleText.setEffect(new DropShadow(25, Color.rgb(100, 255, 120, 0.6)));

        Text subtitleText = new Text("Press ESC to resume");
        subtitleText.setFont(GameFont.font(FontWeight.BOLD, 16));
        subtitleText.setFill(Color.web("#a0bfa7")); // Pastel green
        titleContainer.getChildren().addAll(titleText, subtitleText);

        MenuButton btnResume = new MenuButton("Resume Game", this::fireResume);

        MenuButton btnSave = new MenuButton("Save Game", () -> {
            app.saveGame();
        });
        btnSave.setOnMouseClicked(e -> {
            app.saveGame();
            btnSave.setTemporaryText("Game Saved!", "Save Game");
        });

        MenuButton btnOptions = new MenuButton("Options", () -> {
            showOptionsScreen(menuBox, titleContainer);
        });

        MenuButton btnExitMenu = new MenuButton("Exit to Main Menu", this::fireExitToMainMenu);

        menuBox.getChildren().addAll(btnResume, btnSave, btnOptions, btnExitMenu);
    }

    private void showOptionsScreen(VBox menuBox, VBox titleContainer) {
        menuBox.getChildren().clear();
        titleContainer.getChildren().clear();

        Text titleText = new Text("GAME OPTIONS");
        titleText.setFont(GameFont.font(FontWeight.EXTRA_BOLD, 48));
        titleText.setFill(Color.web("#eccb58"));
        titleText.setEffect(new DropShadow(20, Color.rgb(255, 200, 100, 0.5)));

        Text subtitleText = new Text("Adjust your game settings below");
        subtitleText.setFont(GameFont.font(FontWeight.BOLD, 16));
        subtitleText.setFill(Color.web("#a0bfa7"));
        titleContainer.getChildren().addAll(titleText, subtitleText);

        Main app = FXGL.getAppCast();

        // 1. HP Depletion Toggle
        MenuButton[] btnHpDepletion = new MenuButton[1];
        btnHpDepletion[0] = new MenuButton("", () -> {
            app.toggleHPDepletion();
            btnHpDepletion[0].setText("HP Depletion: " + (app.isHPDepletionEnabled() ? "ON" : "OFF"));
        });
        btnHpDepletion[0].setText("HP Depletion: " + (app.isHPDepletionEnabled() ? "ON" : "OFF"));

        // 2. Music Volume Button
        MenuButton[] btnMusic = new MenuButton[1];
        btnMusic[0] = new MenuButton("", () -> {
            double currentVol = FXGL.getSettings().getGlobalMusicVolume();
            double newVol = currentVol >= 1.0 ? 0.0 : currentVol + 0.25;
            FXGL.getSettings().setGlobalMusicVolume(newVol);
            btnMusic[0].setText("Music Volume: " + (int)Math.round(newVol * 100) + "%");
        });
        btnMusic[0].setText("Music Volume: " + (int)Math.round(FXGL.getSettings().getGlobalMusicVolume() * 100) + "%");

        // 3. Sound Volume Button
        MenuButton[] btnSound = new MenuButton[1];
        btnSound[0] = new MenuButton("", () -> {
            double currentVol = FXGL.getSettings().getGlobalSoundVolume();
            double newVol = currentVol >= 1.0 ? 0.0 : currentVol + 0.25;
            FXGL.getSettings().setGlobalSoundVolume(newVol);
            btnSound[0].setText("Sound Volume: " + (int)Math.round(newVol * 100) + "%");
        });
        btnSound[0].setText("Sound Volume: " + (int)Math.round(FXGL.getSettings().getGlobalSoundVolume() * 100) + "%");

        // 4. Fullscreen Toggle
        MenuButton[] btnFullscreen = new MenuButton[1];
        btnFullscreen[0] = new MenuButton("", () -> {
            javafx.stage.Stage stage = FXGL.getPrimaryStage();
            boolean isFull = stage.isFullScreen();
            stage.setFullScreen(!isFull);
            btnFullscreen[0].setText("Fullscreen: " + (!isFull ? "ON" : "OFF"));
        });
        btnFullscreen[0].setText("Fullscreen: " + (FXGL.getPrimaryStage().isFullScreen() ? "ON" : "OFF"));

        // 5. Back Button
        MenuButton btnBack = new MenuButton("Back", () -> {
            if (this.type == MenuType.MAIN_MENU) {
                showMainMenuScreen(menuBox, titleContainer, app);
            } else {
                showPauseMenuScreen(menuBox, titleContainer, app);
            }
        });

        menuBox.getChildren().addAll(btnHpDepletion[0], btnMusic[0], btnSound[0], btnFullscreen[0], btnBack);
    }

    private void showSkinSelectionScreen(VBox menuBox, VBox titleContainer, Main app, boolean isNewGame) {
        menuBox.getChildren().clear();
        titleContainer.getChildren().clear();

        // Always reset to default skin for new game (only Default is unlocked)
        if (isNewGame) {
            Project1Game.component.player.PlayerComponent.SELECTED_SKIN = "Player";
        }

        Text titleText = new Text("SELECT CHARACTER SKIN");
        titleText.setFont(GameFont.font(FontWeight.EXTRA_BOLD, 42));
        titleText.setFill(Color.web("#eccb58"));
        titleText.setEffect(new DropShadow(20, Color.rgb(255, 200, 100, 0.5)));

        Text subtitleText = new Text("Select a character class/style before starting your farm");
        subtitleText.setFont(GameFont.font(FontWeight.BOLD, 16));
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
            final boolean isLocked = false; // All skins are unlocked by default
            
            VBox skinBox = new VBox(8);
            skinBox.setAlignment(Pos.CENTER);
            skinBox.setPadding(new Insets(10));
            skinBox.setPrefSize(140, 180);
            
            // Check if active
            boolean isSelected = Project1Game.component.player.PlayerComponent.SELECTED_SKIN.equals(path);
            String borderStyle = isSelected 
                ? "-fx-border-color: #eccb58; -fx-border-width: 3; -fx-border-radius: 10;" 
                : "-fx-border-color: #355c45; -fx-border-width: 1.5; -fx-border-radius: 10;";
            
            String bgColor = isLocked
                ? "rgba(5, 10, 8, 0.85)"
                : "rgba(10, 25, 15, 0.75)";
            skinBox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; " + borderStyle);

            // Sprite preview (greyed out if locked)
            com.almasb.fxgl.texture.Texture preview = FXGL.texture(path + "/player.png");
            preview.setFitWidth(48);
            preview.setFitHeight(48);
            if (isLocked) {
                preview.setOpacity(0.3);
            }

            Text nameText = new Text(name);
            nameText.setFont(GameFont.font(FontWeight.BOLD, 15));
            nameText.setFill(isLocked ? Color.web("#555555") : Color.WHITE);

            Text descText = new Text(skinDescs[i]);
            descText.setFont(GameFont.font(FontWeight.NORMAL, 10));
            descText.setFill(isLocked ? Color.web("#444444") : Color.LIGHTGRAY);

            if (isLocked) {
                // Show lock icon instead of select button
                Text lockIcon = new Text("🔒");
                lockIcon.setFont(GameFont.font(FontWeight.BOLD, 22));
                lockIcon.setOpacity(0.7);

                skinBox.getChildren().addAll(preview, nameText, descText, lockIcon);
            } else {
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
                    showSkinSelectionScreen(menuBox, titleContainer, app, isNewGame);
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
            }
            skinsRow.getChildren().add(skinBox);
        }

        // Adjust position slightly to fit HBox
        menuBox.setTranslateY(210);

        javafx.scene.layout.HBox buttonsBox = new javafx.scene.layout.HBox(20);
        buttonsBox.setAlignment(Pos.CENTER);

        if (isNewGame) {
            MenuButton btnStartGame = new MenuButton("Start Game", () -> {
                app.setShouldLoadSaveOnStart(false);
                Project1Game.system.TutorialSystem.getInstance().resetTutorial();
                fireNewGame();
            });
            MenuButton btnBack = new MenuButton("Back to Menu", () -> {
                menuBox.setTranslateY(260); // Restore original position
                showMainMenuScreen(menuBox, titleContainer, app);
            });
            buttonsBox.getChildren().addAll(btnStartGame, btnBack);
        } else {
            MenuButton btnBack = new MenuButton("Back to Menu", () -> {
                menuBox.setTranslateY(260); // Restore original position
                showMainMenuScreen(menuBox, titleContainer, app);
            });
            buttonsBox.getChildren().add(btnBack);
        }

        menuBox.getChildren().addAll(skinsRow, buttonsBox);
    }

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
            text.setFont(GameFont.font(FontWeight.BOLD, 18));
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

        public void setText(String name) {
            text.setText(name);
        }

        public void setTemporaryText(String tempText, String originalText) {
            text.setText(tempText);
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> text.setText(originalText));
            pause.play();
        }
    }
}

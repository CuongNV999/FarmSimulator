package Project1Game.ui;

import Project1Game.Main;
import Project1Game.component.player.PlayerComponent;
import Project1Game.ui.view.hud.*;
import Project1Game.ui.view.dialog.*;
import Project1Game.ui.view.shop.*;
import Project1Game.ui.view.admin.*;
import Project1Game.ui.view.inventory.*;
import Project1Game.ui.view.overlay.*;
import Project1Game.ui.utility.GameFont;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class HUDManager {

    private ToolbarView toolbarView;
    private InventoryView inventoryView;
    private StatusBarsView statusBarsView;
    private DialogView dialogView;
    private MinimapView minimap;
    private Text moneyText;
    private TradingView tradingView;
    private AdminView adminView;
    private QuestJournalView questJournalView;

    private NightLightingOverlay nightOverlay;
    private Text clockText;
    private VBox hudContainer;

    private IntegerProperty boundMoneyProperty = null;
    private ChangeListener<Number> moneyListener = null;

    public void initializeHUD(Main app) {
        toolbarView = new ToolbarView(app.getInventory());
        toolbarView.setLayoutX((FXGL.getAppWidth() - 9 * 86) / 2.0);
        toolbarView.setLayoutY(FXGL.getAppHeight() - 100);

        inventoryView = new InventoryView(app.getInventory());
        inventoryView.setLayoutX((FXGL.getAppWidth() - 620) / 2.0);
        inventoryView.setLayoutY((FXGL.getAppHeight() - 300) / 2.0);

        dialogView = new DialogView(FXGL.getAppWidth(), FXGL.getAppHeight());

        statusBarsView = new StatusBarsView();
        statusBarsView.setLayoutX(20);
        statusBarsView.setLayoutY(20);

        minimap = new MinimapView();
        double minimapHeight = 150;
        minimap.setLayoutX(10);
        minimap.setLayoutY(FXGL.getAppHeight() - minimapHeight - 10);

        nightOverlay = new NightLightingOverlay(FXGL.getAppWidth(), FXGL.getAppHeight());

        clockText = new Text();
        clockText.setFont(GameFont.font(FontWeight.BOLD, 20));
        clockText.setStroke(Color.BLACK);
        clockText.setStrokeWidth(0.5);

        moneyText = new Text();
        moneyText.setFont(GameFont.font(FontWeight.BOLD, 18));
        moneyText.setFill(Color.GOLD);
        moneyText.setStroke(Color.BLACK);
        moneyText.setStrokeWidth(0.3);

        double hudContainerWidth = 220;
        hudContainer = new VBox(6);
        hudContainer.setPadding(new Insets(10, 15, 10, 15));
        hudContainer.setPrefWidth(hudContainerWidth);
        hudContainer.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.45); -fx-background-radius: 8; -fx-border-color: rgba(255, 255, 255, 0.15); -fx-border-width: 1; -fx-border-radius: 8;");
        hudContainer.setAlignment(Pos.TOP_RIGHT);
        hudContainer.setLayoutX(FXGL.getAppWidth() - hudContainerWidth - 15);
        hudContainer.setLayoutY(15);
        hudContainer.getChildren().addAll(clockText, moneyText);

        questJournalView = new QuestJournalView();
        questJournalView.layoutXProperty().bind(hudContainer.layoutXProperty());
        questJournalView.layoutYProperty().bind(hudContainer.layoutYProperty().add(hudContainer.heightProperty()).add(15));

        FXGL.getGameScene().addUINodes(nightOverlay, toolbarView, inventoryView, dialogView, statusBarsView, minimap,
                hudContainer, questJournalView);
    }

    public void bindPlayerUI(Entity player) {
        if (player == null || !player.isActive() || !player.hasComponent(PlayerComponent.class) || moneyText == null) {
            return;
        }
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        if (boundMoneyProperty != null && moneyListener != null) {
            boundMoneyProperty.removeListener(moneyListener);
        }
        boundMoneyProperty = playerComponent.moneyProperty();
        moneyListener = (obs, old, newV) -> {
            moneyText.setText("Tiền: " + newV + " G");
        };
        boundMoneyProperty.addListener(moneyListener);
        moneyText.setText("Tiền: " + playerComponent.getMoney() + " G");
    }

    public void registerWeatherText(Text weatherText) {
        if (hudContainer != null && weatherText != null) {
            if (!hudContainer.getChildren().contains(weatherText)) {
                int moneyIndex = hudContainer.getChildren().indexOf(moneyText);
                if (moneyIndex >= 0) {
                    hudContainer.getChildren().add(moneyIndex, weatherText);
                } else {
                    hudContainer.getChildren().add(weatherText);
                }
            }
        }
    }

    public void refresh() {
        if (minimap != null) {
            minimap.update();
        }
        if (questJournalView != null) {
            questJournalView.refresh();
        }
    }

    // --- Getters / Setters ---

    public ToolbarView getToolbarView() {
        return toolbarView;
    }

    public InventoryView getInventoryView() {
        return inventoryView;
    }

    public StatusBarsView getStatusBarsView() {
        return statusBarsView;
    }

    public DialogView getDialogView() {
        return dialogView;
    }

    public MinimapView getMinimap() {
        return minimap;
    }

    public Text getMoneyText() {
        return moneyText;
    }

    public TradingView getTradingView() {
        return tradingView;
    }

    public void setTradingView(TradingView tradingView) {
        this.tradingView = tradingView;
    }

    public AdminView getAdminView() {
        return adminView;
    }

    public void setAdminView(AdminView adminView) {
        this.adminView = adminView;
    }

    public QuestJournalView getQuestJournalView() {
        return questJournalView;
    }

    public NightLightingOverlay getNightOverlay() {
        return nightOverlay;
    }

    public Text getClockText() {
        return clockText;
    }

    public VBox getHudContainer() {
        return hudContainer;
    }
}

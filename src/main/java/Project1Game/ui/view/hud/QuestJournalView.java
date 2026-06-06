package Project1Game.ui.view.hud;

import Project1Game.quest.Quest;
import Project1Game.quest.QuestManager;
import Project1Game.quest.QuestObjective;
import Project1Game.quest.QuestStatus;
import Project1Game.ui.utility.GameFont;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

public class QuestJournalView extends VBox {

    private final Text titleText;
    private final Text questNameText;
    private final VBox objectivesContainer;

    public QuestJournalView() {
        super(6);
        setAlignment(Pos.TOP_LEFT);

        // Styling as an independent dark retro box matching console aesthetics
        setPadding(new Insets(10, 15, 10, 15));
        setPrefWidth(220);
        setStyle(
            "-fx-background-color: rgba(18, 18, 24, 0.85); " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #eccb58; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10;"
        );

        // Header Title (increased font scale to 18px for better readability)
        titleText = new Text("Nhiệm vụ:");
        titleText.setFont(GameFont.font(FontWeight.BOLD, 18));
        titleText.setFill(Color.web("#eccb58"));
        titleText.setStroke(Color.BLACK);
        titleText.setStrokeWidth(0.4);

        // Quest Name Text (increased font scale to 16px)
        questNameText = new Text();
        questNameText.setFont(GameFont.font(FontWeight.BOLD, 16));
        questNameText.setFill(Color.WHITE);
        questNameText.setStroke(Color.BLACK);
        questNameText.setStrokeWidth(0.3);

        // Objectives Container
        objectivesContainer = new VBox(4);
        objectivesContainer.setAlignment(Pos.TOP_LEFT);

        getChildren().addAll(titleText, questNameText, objectivesContainer);
        refresh();
    }

    public void refresh() {
        objectivesContainer.getChildren().clear();

        List<Quest> activeQuests = QuestManager.getInstance().getActiveQuests();

        if (!activeQuests.isEmpty()) {
            Quest activeQuest = activeQuests.get(0);

            // Make the entire container visible and managed in layouts
            this.setVisible(true);
            this.setManaged(true);

            titleText.setVisible(true);
            titleText.setManaged(true);

            questNameText.setText(activeQuest.getTitle());
            questNameText.setVisible(true);
            questNameText.setManaged(true);

            if (activeQuest.getStatus() == QuestStatus.COMPLETED) {
                Text complText = new Text("Chờ nộp cho NPC...");
                complText.setFont(GameFont.font(FontWeight.BOLD, 14));
                complText.setFill(Color.LIGHTGREEN);
                complText.setStroke(Color.BLACK);
                complText.setStrokeWidth(0.2);
                objectivesContainer.getChildren().add(complText);
            } else {
                for (QuestObjective obj : activeQuest.getObjectives()) {
                    Text objText = new Text(obj.getProgressText());
                    objText.setFont(GameFont.font(FontWeight.BOLD, 14));
                    objText.setFill(Color.LIGHTGRAY);
                    objText.setStroke(Color.BLACK);
                    objText.setStrokeWidth(0.2);
                    objectivesContainer.getChildren().add(objText);
                }
            }
        } else {
            // No active quests - hide completely and collapse layout bounds
            this.setVisible(false);
            this.setManaged(false);
        }
    }
}

package Project1Game.quest;

import Project1Game.model.Inventory;

import java.util.Collections;
import java.util.List;

/**
 * Đại diện cho một Quest trong game.
 */
public class Quest {

    private final String id;
    private final String title;
    private final String description;   // lời thoại NPC giao quest
    private final String completionText; // lời NPC khi người chơi nộp

    private QuestStatus status = QuestStatus.NOT_STARTED;
    private final List<QuestObjective> objectives;
    private final QuestReward reward;

    public Quest(String id, String title, String description, String completionText, 
                 List<QuestObjective> objectives, QuestReward reward) {
        this.id             = id;
        this.title          = title;
        this.description    = description;
        this.completionText = completionText;
        this.objectives     = objectives;
        this.reward         = reward;
    }

    /** Người chơi chấp nhận quest từ NPC. */
    public void start() {
        if (status != QuestStatus.NOT_STARTED)
            throw new IllegalStateException("Quest đã được bắt đầu hoặc hoàn thành.");
        status = QuestStatus.IN_PROGRESS;
        System.out.println("[Quest] Bắt đầu: " + title);
    }

    /**
     * Gọi mỗi khi có sự kiện game.
     * Cập nhật tiến độ và chuyển sang COMPLETED nếu xong tất cả objectives.
     */
    public void onEvent(QuestContext ctx) {
        if (status != QuestStatus.IN_PROGRESS) return;
        for (QuestObjective obj : objectives) {
            if (!obj.isDone()) obj.checkProgress(ctx);
        }
        if (objectives.stream().allMatch(QuestObjective::isDone)) {
            status = QuestStatus.COMPLETED;
            System.out.println("[Quest] Hoàn thành: " + title + " – hãy nộp cho NPC!");
        }
    }

    /**
     * Người chơi quay lại nộp quest cho NPC.
     * @return phần thưởng đã trao (để UI hiển thị)
     */
    public QuestReward claimReward(Inventory inventory) {
        if (status != QuestStatus.COMPLETED)
            throw new IllegalStateException("Quest chưa hoàn thành.");
        reward.grantTo(inventory);
        status = QuestStatus.REWARDED;
        System.out.println("[Quest] Đã nhận thưởng: " + title);
        return reward;
    }

    public String getId()               { return id; }
    public String getTitle()            { return title; }
    public String getDescription()      { return description; }
    public String getCompletionText()   { return completionText; }
    public QuestStatus getStatus()      { return status; }
    public QuestReward getReward()      { return reward; }

    public List<QuestObjective> getObjectives() {
        return Collections.unmodifiableList(objectives);
    }

    /** Tóm tắt tiến độ tất cả objectives. */
    public String getProgressSummary() {
        if (status == QuestStatus.NOT_STARTED) return "Chưa bắt đầu";
        if (status == QuestStatus.REWARDED)    return "Đã hoàn thành";
        StringBuilder sb = new StringBuilder();
        for (QuestObjective obj : objectives) {
            sb.append("  • ").append(obj.getProgressText()).append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "[" + status + "] " + title;
    }
}

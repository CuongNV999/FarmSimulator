package Project1Game.quest;

import Project1Game.model.Inventory;

import java.util.Collections;
import java.util.List;

/**
 * Lớp cơ sở trừu tượng cho mọi Quest trong game.
 *
 * <p>Mỗi quest con phải định nghĩa:
 * <ul>
 *   <li>{@link #buildObjectives()} – danh sách điều kiện cần hoàn thành</li>
 *   <li>{@link #buildReward()} – phần thưởng khi nộp quest</li>
 * </ul>
 *
 * Vòng đời: NOT_STARTED → IN_PROGRESS → COMPLETED → REWARDED
 */
public abstract class Quest {

    private final String id;
    private final String title;
    private final String description;   // lời thoại NPC giao quest
    private final String completionText; // lời NPC khi người chơi nộp

    private QuestStatus status = QuestStatus.NOT_STARTED;
    private List<QuestObjective> objectives;
    private QuestReward reward;

    protected Quest(String id, String title, String description, String completionText) {
        this.id             = id;
        this.title          = title;
        this.description    = description;
        this.completionText = completionText;
    }

    // ------------------------------------------------------------------ //
    //  Abstract factory methods (Template Method)                         //
    // ------------------------------------------------------------------ //

    /** Trả về danh sách các điều kiện của quest này. */
    protected abstract List<QuestObjective> buildObjectives();

    /** Trả về phần thưởng của quest này. */
    protected abstract QuestReward buildReward();

    // ------------------------------------------------------------------ //
    //  Lifecycle                                                           //
    // ------------------------------------------------------------------ //

    /** Người chơi chấp nhận quest từ NPC. */
    public void start() {
        if (status != QuestStatus.NOT_STARTED)
            throw new IllegalStateException("Quest đã được bắt đầu hoặc hoàn thành.");
        objectives = buildObjectives();
        reward     = buildReward();
        status     = QuestStatus.IN_PROGRESS;
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

    // ------------------------------------------------------------------ //
    //  Getters                                                             //
    // ------------------------------------------------------------------ //

    public String getId()               { return id; }
    public String getTitle()            { return title; }
    public String getDescription()      { return description; }
    public String getCompletionText()   { return completionText; }
    public QuestStatus getStatus()      { return status; }
    public QuestReward getReward()      { return reward != null ? reward : buildReward(); }

    /** Trả về danh sách objectives (chỉ sau khi start()). */
    public List<QuestObjective> getObjectives() {
        return objectives != null ? Collections.unmodifiableList(objectives) : Collections.emptyList();
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

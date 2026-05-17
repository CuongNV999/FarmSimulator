package Project1Game.quest;

import Project1Game.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * NPC giao quest cho người chơi.
 *
 * <p>NPC nắm giữ một danh sách quest (tất cả do constructor đăng ký).
 * Người chơi tương tác theo luồng:</p>
 * <ol>
 *   <li>Gọi {@link #interact()} → NPC trả về lời thoại giới thiệu quest tiếp theo
 *       còn chờ (NOT_STARTED) hoặc ghi nhận quest đã COMPLETED để trao thưởng.</li>
 *   <li>Người chơi gọi {@link #acceptQuest(String)} để nhận quest.</li>
 *   <li>Khi người chơi hoàn thành điều kiện, gọi {@link #claimReward(String, Inventory)}
 *       để nhận thưởng.</li>
 * </ol>
 *
 * QuestManager sẽ đẩy sự kiện game vào mọi quest đang IN_PROGRESS qua
 * {@link #notifyEvent(QuestContext)}.
 */
public class NPC {

    private final String name;       // tên NPC
    private final String greeting;   // lời chào khi không có quest nào
    private final List<Quest> quests;

    public NPC(String name, String greeting) {
        this.name     = name;
        this.greeting = greeting;
        this.quests   = new ArrayList<>();
    }

    /** Đăng ký quest vào danh sách của NPC (gọi khi khởi tạo game). */
    public void registerQuest(Quest quest) {
        quests.add(quest);
    }

    // ------------------------------------------------------------------ //
    //  Tương tác                                                           //
    // ------------------------------------------------------------------ //

    /**
     * Người chơi đến nói chuyện với NPC.
     * @return Lời thoại NPC trả về (tuỳ trạng thái hiện tại).
     */
    public String interact() {
        // Ưu tiên 1: nếu có quest đã COMPLETED chờ nộp
        Optional<Quest> toReward = quests.stream()
                .filter(q -> q.getStatus() == QuestStatus.COMPLETED)
                .findFirst();
        if (toReward.isPresent()) {
            Quest q = toReward.get();
            return name + ": \"" + q.getCompletionText() + "\"\n" +
                   "[Nhấn E để nhận thưởng: " + q.getTitle() + "]";
        }

        // Ưu tiên 2: quest đang làm – nhắc nhở
        Optional<Quest> inProgress = quests.stream()
                .filter(q -> q.getStatus() == QuestStatus.IN_PROGRESS)
                .findFirst();
        if (inProgress.isPresent()) {
            Quest q = inProgress.get();
            return name + ": \"Nhớ hoàn thành nhiệm vụ nhé cháu!\"\n"
                   + "Tiến độ:\n" + q.getProgressSummary();
        }

        // Ưu tiên 3: quest mới chờ giao
        Optional<Quest> available = quests.stream()
                .filter(q -> q.getStatus() == QuestStatus.NOT_STARTED)
                .findFirst();
        if (available.isPresent()) {
            Quest q = available.get();
            return name + ": \"" + q.getDescription() + "\"\n" +
                   "[Nhiệm vụ: " + q.getTitle() + " – nhấn E để nhận]";
        }

        // Không còn quest nào
        return name + ": \"" + greeting + "\"";
    }

    /**
     * Người chơi chấp nhận quest theo ID.
     * @return true nếu thành công
     */
    public boolean acceptQuest(String questId) {
        for (Quest q : quests) {
            if (q.getId().equals(questId) && q.getStatus() == QuestStatus.NOT_STARTED) {
                q.start();
                return true;
            }
        }
        return false;
    }

    /** Bắt đầu quest tiếp theo còn NOT_STARTED (tiện dụng cho UI đơn giản). */
    public Optional<Quest> acceptNextAvailableQuest() {
        return quests.stream()
                .filter(q -> q.getStatus() == QuestStatus.NOT_STARTED)
                .findFirst()
                .map(q -> { q.start(); return q; });
    }

    /**
     * Nộp quest đã COMPLETED và nhận thưởng.
     * @return phần thưởng nếu thành công, rỗng nếu không
     */
    public Optional<QuestReward> claimReward(String questId, Inventory inventory) {
        for (Quest q : quests) {
            if (q.getId().equals(questId) && q.getStatus() == QuestStatus.COMPLETED) {
                return Optional.of(q.claimReward(inventory));
            }
        }
        return Optional.empty();
    }

    /** Nộp quest COMPLETED đầu tiên tìm thấy. */
    public Optional<QuestReward> claimFirstCompleted(Inventory inventory) {
        return quests.stream()
                .filter(q -> q.getStatus() == QuestStatus.COMPLETED)
                .findFirst()
                .map(q -> q.claimReward(inventory));
    }

    /**
     * Đẩy sự kiện game tới tất cả quest đang IN_PROGRESS.
     * Gọi từ Main hoặc GameEventBus mỗi khi người chơi làm gì đó.
     */
    public void notifyEvent(QuestContext ctx) {
        quests.stream()
              .filter(q -> q.getStatus() == QuestStatus.IN_PROGRESS)
              .forEach(q -> q.onEvent(ctx));
    }

    // ------------------------------------------------------------------ //
    //  Getters                                                             //
    // ------------------------------------------------------------------ //

    public String getName()           { return name; }
    public List<Quest> getQuests()    { return Collections.unmodifiableList(quests); }

    /** Tất cả quest đang IN_PROGRESS. */
    public List<Quest> getActiveQuests() {
        return quests.stream()
                .filter(q -> q.getStatus() == QuestStatus.IN_PROGRESS)
                .toList();
    }

    /** Tất cả quest đã REWARDED. */
    public List<Quest> getCompletedQuests() {
        return quests.stream()
                .filter(q -> q.getStatus() == QuestStatus.REWARDED)
                .toList();
    }
}

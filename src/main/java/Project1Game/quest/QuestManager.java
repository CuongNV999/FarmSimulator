package Project1Game.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton quản lý toàn bộ hệ thống Quest và NPC.
 *
 * <p>Cách sử dụng trong {@code Main.java}:</p>
 * <pre>
 * // 1. Khởi tạo một lần khi game bắt đầu
 * QuestManager qm = QuestManager.getInstance();
 * qm.init();
 *
 * // 2. Khi người chơi thu hoạch (ví dụ trong Main.java):
 * qm.broadcast(new QuestContext(QuestContext.EventType.HARVEST, ItemType.WHEAT));
 *
 * // 3. Khi người chơi nói chuyện với NPC:
 * NPC npc = qm.getNPC("Bác Nông Dân");
 * System.out.println(npc.interact());
 * npc.acceptNextAvailableQuest();
 * </pre>
 */
public class QuestManager {

    private static QuestManager instance;

    private final List<NPC> npcs = new ArrayList<>();

    private QuestManager() {}

    public static QuestManager getInstance() {
        if (instance == null) instance = new QuestManager();
        return instance;
    }

    /**
     * Khởi tạo toàn bộ NPC và đăng ký quest cho họ.
     * Gọi một lần duy nhất trong {@code initGame()} của Main.
     */
    public void init() {
        npcs.clear();

        // --- NPC: Bác Nông Dân ---
        NPC bacNongDan = new NPC(
            "Bác Nông Dân",
            "Mùa màng bội thu là nhờ công cháu đấy! Cảm ơn cháu nhiều lắm."
        );
        bacNongDan.registerQuest(new FarmQuests.FirstMorningQuest());
        bacNongDan.registerQuest(new FarmQuests.FirstHarvestQuest());
        bacNongDan.registerQuest(new FarmQuests.SummerFeastQuest());
        bacNongDan.registerQuest(new FarmQuests.DiligentFarmerQuest());
        bacNongDan.registerQuest(new FarmQuests.WinterPreparationQuest());

        npcs.add(bacNongDan);

        System.out.println("[QuestManager] Đã khởi tạo " + npcs.size() + " NPC.");
    }

    /**
     * Phát sóng sự kiện game tới tất cả NPC (và qua đó tới mọi quest đang chạy).
     */
    public void broadcast(QuestContext ctx) {
        for (NPC npc : npcs) {
            npc.notifyEvent(ctx);
        }
    }

    /** Lấy NPC theo tên (trả null nếu không tìm thấy). */
    public NPC getNPC(String name) {
        return npcs.stream()
                   .filter(n -> n.getName().equals(name))
                   .findFirst()
                   .orElse(null);
    }

    public List<NPC> getAllNPCs() {
        return Collections.unmodifiableList(npcs);
    }

    /** Tóm tắt trạng thái tất cả quest trong toàn game (debug). */
    public void printAllQuestStatus() {
        System.out.println("===== QUEST STATUS =====");
        for (NPC npc : npcs) {
            System.out.println("NPC: " + npc.getName());
            for (Quest q : npc.getQuests()) {
                System.out.println("  " + q);
                if (q.getStatus() == QuestStatus.IN_PROGRESS) {
                    System.out.println("    " + q.getProgressSummary());
                }
            }
        }
        System.out.println("========================");
    }
}

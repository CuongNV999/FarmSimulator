package Project1Game.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton quản lý toàn bộ hệ thống Quest và NPC.
 */
public class QuestManager {

    private static QuestManager instance;

    private final List<NPC> npcs = new ArrayList<>();

    private QuestManager() {}

    public static QuestManager getInstance() {
        if (instance == null) instance = new QuestManager();
        return instance;
    }

    public void reset() {
        npcs.clear();
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
        bacNongDan.registerQuest(FarmQuests.createFirstMorningQuest());
        bacNongDan.registerQuest(FarmQuests.createFirstHarvestQuest());
        bacNongDan.registerQuest(FarmQuests.createSummerFeastQuest());
        bacNongDan.registerQuest(FarmQuests.createDiligentFarmerQuest());
        bacNongDan.registerQuest(FarmQuests.createWinterPreparationQuest());

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

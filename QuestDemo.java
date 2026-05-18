package Project1Game.quest;

import Project1Game.Inventory;
import Project1Game.ItemType;

/**
 * Demo chạy độc lập (không cần FXGL) để kiểm tra hệ thống Quest.
 * Chạy bằng: javac + java, hoặc copy vào main() của game để test.
 */
public class QuestDemo {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   FARM SIMULATOR – QUEST SYSTEM DEMO  ");
        System.out.println("========================================\n");

        // 1. Khởi tạo
        QuestManager qm = QuestManager.getInstance();
        qm.init();

        NPC bacNongDan = qm.getNPC("Bác Nông Dân");
        Inventory playerInventory = new Inventory();

        // 2. Người chơi đến gặp NPC
        System.out.println(">> Người chơi đến gặp NPC:");
        System.out.println(bacNongDan.interact());
        System.out.println();

        // 3. Nhận quest đầu tiên
        bacNongDan.acceptNextAvailableQuest()
                  .ifPresent(q -> System.out.println(">> Đã nhận quest: " + q.getTitle()));
        System.out.println();

        // 4. Mô phỏng người chơi hành động
        System.out.println(">> Người chơi gieo 5 hạt lúa mì...");
        for (int i = 0; i < 5; i++) {
            qm.broadcast(new QuestContext(QuestContext.EventType.PLANT, ItemType.WHEAT_SEED));
        }

        System.out.println(">> Người chơi tưới nước 5 lần...");
        for (int i = 0; i < 5; i++) {
            qm.broadcast(new QuestContext(QuestContext.EventType.WATER, null));
        }
        System.out.println();

        // 5. In trạng thái
        qm.printAllQuestStatus();

        // 6. Người chơi quay lại nộp thưởng
        System.out.println(">> Người chơi quay lại NPC:");
        System.out.println(bacNongDan.interact());
        System.out.println();

        bacNongDan.claimFirstCompleted(playerInventory).ifPresent(reward -> {
            System.out.println(">> Đã nhận thưởng: " + reward);
            System.out.println("   Túi đồ – Hạt củ cải: " + playerInventory.getCount(ItemType.RADISH_SEED));
            System.out.println("   Túi đồ – Hạt ngô:    " + playerInventory.getCount(ItemType.CORN_SEED));
        });
        System.out.println();

        // 7. Quest tiếp theo tự động xuất hiện
        System.out.println(">> Người chơi gặp NPC lần 2 – quest mới:");
        bacNongDan.acceptNextAvailableQuest()
                  .ifPresent(q -> System.out.println(">> Đã nhận quest: " + q.getTitle()));
        System.out.println(bacNongDan.interact());
    }
}

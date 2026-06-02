package Project1Game.quest;

import Project1Game.core.ItemType;

import java.util.Arrays;
import java.util.List;

/**
 * Tập hợp 5 Quest cụ thể do NPC Bác Nông Dân giao cho người chơi.
 */
public final class FarmQuests {

    private FarmQuests() {}

    /* ================================================================== */
    /*  QUEST 1 – Buổi Sáng Đầu Tiên                                      */
    /* ================================================================== */

    public static Quest createFirstMorningQuest() {
        return new Quest(
            "quest_first_morning",
            "Buổi Sáng Đầu Tiên",
            "Cháu ơi, mảnh ruộng đằng kia bỏ hoang lâu rồi. " +
            "Bác có ít hạt lúa mì đây, cháu giúp bác gieo " +
            "5 hạt xuống đất được không? " +
            "Đừng quên tưới nước nhé!",
            "Tốt lắm! Vài ngày nữa lúa sẽ lên xanh. " +
            "Đây, bác thưởng cháu một ít hạt giống thêm.",
            Arrays.asList(
                new Objectives.PlantObjective(ItemType.WHEAT_SEED, 5),
                new Objectives.WaterObjective(5)
            ),
            new QuestReward.Builder()
                .gold(20)
                .item(ItemType.RADISH_SEED, 5)
                .item(ItemType.CORN_SEED, 3)
                .build()
        );
    }

    /* ================================================================== */
    /*  QUEST 2 – Mùa Thu Hoạch Đầu Tiên                                  */
    /* ================================================================== */

    public static Quest createFirstHarvestQuest() {
        return new Quest(
            "quest_first_harvest",
            "Mùa Thu Hoạch Đầu Tiên",
            "Lúa mì đã chín rồi kìa cháu! " +
            "Hãy gặt về cho bác 8 bông lúa mì đi. " +
            "Bác cần bột mì để làm bánh cho cả làng.",
            "Ồ thơm quá! Bác sẽ làm bánh ngay. " +
            "Cháu thật là người nông dân tài ba!",
            List.of(
                new Objectives.HarvestObjective(ItemType.WHEAT, 8)
            ),
            new QuestReward.Builder()
                .gold(50)
                .item(ItemType.WHEAT_SEED, 10)
                .item(ItemType.TOMATO_SEED, 5)
                .build()
        );
    }

    /* ================================================================== */
    /*  QUEST 3 – Bữa Tiệc Mùa Hè                                         */
    /* ================================================================== */

    public static Quest createSummerFeastQuest() {
        return new Quest(
            "quest_summer_feast",
            "Bữa Tiệc Mùa Hè",
            "Cuối tuần này làng mình tổ chức tiệc mùa hè! " +
            "Bác cần cà chua, ngô và xà lách để nấu salad lớn. " +
            "Cháu thu hoạch giúp bác 5 cái mỗi loại được không?",
            "Tuyệt vời! Bữa tiệc sẽ rất thịnh soạn nhờ cháu. " +
            "Bác tặng cháu bộ hạt giống mới nhất vừa nhập về!",
            Arrays.asList(
                new Objectives.HarvestObjective(ItemType.TOMATO,  5),
                new Objectives.HarvestObjective(ItemType.CORN,    5),
                new Objectives.HarvestObjective(ItemType.LETTUCE, 5)
            ),
            new QuestReward.Builder()
                .gold(120)
                .item(ItemType.CABBAGE_SEED, 8)
                .item(ItemType.LETTUCE_SEED, 8)
                .item(ItemType.TOMATO_SEED,  8)
                .build()
        );
    }

    /* ================================================================== */
    /*  QUEST 4 – Người Nông Dân Cần Mẫn                                  */
    /* ================================================================== */

    public static Quest createDiligentFarmerQuest() {
        return new Quest(
            "quest_diligent_farmer",
            "Người Nông Dân Cần Mẫn",
            "Bác thấy cháu có vẻ chăm chỉ đấy! " +
            "Nhưng làm nông thật sự cần rất nhiều kiên nhẫn. " +
            "Hãy thử gieo 20 hạt và tưới nước 20 lần " +
            "để rèn luyện đôi tay cháu đi!",
            "Ha ha! Đôi tay cháu cứng cáp hẳn rồi. " +
            "Đây là phần thưởng xứng đáng cho sự cần cù!",
            Arrays.asList(
                new Objectives.PlantObjective(20),
                new Objectives.WaterObjective(20)
            ),
            new QuestReward.Builder()
                .gold(80)
                .item(ItemType.WHEAT_SEED,   5)
                .item(ItemType.RADISH_SEED,  5)
                .item(ItemType.CABBAGE_SEED, 5)
                .item(ItemType.CORN_SEED,    5)
                .build()
        );
    }

    /* ================================================================== */
    /*  QUEST 5 – Mùa Đông Sắp Đến                                        */
    /* ================================================================== */

    public static Quest createWinterPreparationQuest() {
        return new Quest(
            "quest_winter_prep",
            "Mùa Đông Sắp Đến",
            "Bác nghe thời tiết sắp trở lạnh rồi cháu ơi! " +
            "Bác cần dự trữ củ cải và bắp cải để qua đông. " +
            "Cháu có thể gặt cho bác 10 củ cải và 10 bắp cải không? " +
            "Nhanh lên không thì sương giá xuống mất!",
            "Kho lương thực đầy ắp rồi! " +
            "Cảm ơn cháu, bác sẽ không lo đói rét mùa đông này. " +
            "Bác tặng cháu bộ hạt giống cao cấp để vụ xuân tới nhé.",
            Arrays.asList(
                new Objectives.HarvestObjective(ItemType.RADISH,  10),
                new Objectives.HarvestObjective(ItemType.CABBAGE, 10)
            ),
            new QuestReward.Builder()
                .gold(200)
                .item(ItemType.WHEAT_SEED,   15)
                .item(ItemType.TOMATO_SEED,  10)
                .item(ItemType.CORN_SEED,    10)
                .item(ItemType.LETTUCE_SEED, 10)
                .build()
        );
    }
}

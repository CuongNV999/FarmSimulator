package Project1Game.quest;

import Project1Game.core.ItemType;

/**
 * Tập hợp các {@link QuestObjective} cụ thể dùng trong game.
 *
 * <p>Mỗi lớp inner-static là một loại điều kiện riêng biệt,
 * tuân theo nguyên tắc <b>Open/Closed</b>: thêm loại mới không cần
 * sửa code cũ.</p>
 */
public final class Objectives {

    private Objectives() {} // utility class

    /* ================================================================== */
    /*  1. Thu hoạch N cây của một loại                                   */
    /* ================================================================== */

    /**
     * Hoàn thành khi người chơi thu hoạch đủ {@code required} cây
     * của loại {@code targetItem}.
     */
    public static class HarvestObjective extends QuestObjective {

        private final ItemType targetItem;

        public HarvestObjective(ItemType targetItem, int required) {
            super("Thu hoạch " + required + "x " + targetItem.getDisplayName(), required);
            this.targetItem = targetItem;
        }

        @Override
        public void checkProgress(QuestContext ctx) {
            if (ctx.getEventType() == QuestContext.EventType.HARVEST
                    && ctx.getItemType() == targetItem) {
                addProgress(ctx.getAmount());
            }
        }
    }

    /* ================================================================== */
    /*  2. Gieo hạt N lần                                                  */
    /* ================================================================== */

    /**
     * Hoàn thành khi người chơi gieo đủ {@code required} hạt
     * (bất kỳ loại, hoặc một loại cụ thể nếu truyền {@code seedType}).
     */
    public static class PlantObjective extends QuestObjective {

        private final ItemType seedType; // null = chấp nhận mọi loại hạt

        /** Gieo bất kỳ loại hạt nào */
        public PlantObjective(int required) {
            super("Gieo " + required + " hạt (bất kỳ)", required);
            this.seedType = null;
        }

        /** Gieo một loại hạt cụ thể */
        public PlantObjective(ItemType seedType, int required) {
            super("Gieo " + required + "x " + seedType.getDisplayName(), required);
            this.seedType = seedType;
        }

        @Override
        public void checkProgress(QuestContext ctx) {
            if (ctx.getEventType() != QuestContext.EventType.PLANT) return;
            if (seedType == null || ctx.getItemType() == seedType) {
                addProgress(ctx.getAmount());
            }
        }
    }

    /* ================================================================== */
    /*  3. Tưới nước N lần                                                 */
    /* ================================================================== */

    public static class WaterObjective extends QuestObjective {

        public WaterObjective(int required) {
            super("Tưới nước " + required + " lần", required);
        }

        @Override
        public void checkProgress(QuestContext ctx) {
            if (ctx.getEventType() == QuestContext.EventType.WATER) {
                addProgress(ctx.getAmount());
            }
        }
    }

    /* ================================================================== */
    /*  4. Gom đủ N item vào túi                                           */
    /* ================================================================== */

    /**
     * Hoàn thành khi người chơi có trong túi ít nhất {@code required}
     * đơn vị của {@code targetItem}.  Khác HarvestObjective ở chỗ
     * dùng sự kiện {@link QuestContext.EventType#COLLECT}.
     */
    public static class CollectObjective extends QuestObjective {

        private final ItemType targetItem;

        public CollectObjective(ItemType targetItem, int required) {
            super("Sở hữu " + required + "x " + targetItem.getDisplayName(), required);
            this.targetItem = targetItem;
        }

        @Override
        public void checkProgress(QuestContext ctx) {
            if (ctx.getEventType() == QuestContext.EventType.COLLECT
                    && ctx.getItemType() == targetItem) {
                addProgress(ctx.getAmount());
            }
        }
    }
}

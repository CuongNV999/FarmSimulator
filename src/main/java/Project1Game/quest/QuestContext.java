package Project1Game.quest;

import Project1Game.ItemType;

/**
 * Bản ghi sự kiện tức thời trong game, được tạo ra mỗi khi có hành động
 * đáng chú ý (thu hoạch, gieo hạt, tưới nước, …) và truyền tới
 * {@link QuestObjective#checkProgress(QuestContext)}.
 *
 * <p>Dùng pattern <b>Value Object</b>: bất biến sau khi tạo.</p>
 */
public final class QuestContext {

    /** Loại sự kiện xảy ra trong game */
    public enum EventType {
        HARVEST,    // Thu hoạch nông sản
        PLANT,      // Gieo hạt
        WATER,      // Tưới nước
        COLLECT,    // Nhặt vật phẩm vào túi
        SELL        // Bán hàng (khi có chợ)
    }

    private final EventType eventType;
    private final ItemType  itemType;   // null nếu sự kiện không liên quan đến item
    private final int       amount;     // số lượng (mặc định 1)

    public QuestContext(EventType eventType, ItemType itemType, int amount) {
        this.eventType = eventType;
        this.itemType  = itemType;
        this.amount    = amount;
    }

    /** Constructor tiện lợi – amount mặc định 1 */
    public QuestContext(EventType eventType, ItemType itemType) {
        this(eventType, itemType, 1);
    }

    public EventType getEventType() { return eventType; }
    public ItemType  getItemType()  { return itemType; }
    public int       getAmount()    { return amount; }
}

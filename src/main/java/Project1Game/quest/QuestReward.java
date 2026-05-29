package Project1Game.quest;

import Project1Game.model.Inventory;
import Project1Game.core.ItemType;
import Project1Game.core.EntityType;
import Project1Game.component.player.PlayerComponent;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Phần thưởng khi hoàn thành một Quest.
 * Có thể tặng vật phẩm và/hoặc tiền vàng (gold).
 *
 * <pre>
 * QuestReward reward = new QuestReward.Builder()
 *         .gold(50)
 *         .item(ItemType.WHEAT_SEED, 5)
 *         .item(ItemType.CORN_SEED, 3)
 *         .build();
 * </pre>
 */
public class QuestReward {

    private final int gold;
    private final Map<ItemType, Integer> items;

    private QuestReward(Builder builder) {
        this.gold  = builder.gold;
        this.items = Collections.unmodifiableMap(new LinkedHashMap<>(builder.items));
    }

    /** Trao phần thưởng vào túi đồ của người chơi. */
    public void grantTo(Inventory inventory) {
        for (Map.Entry<ItemType, Integer> entry : items.entrySet()) {
            inventory.addItem(entry.getKey(), entry.getValue());
        }
        
        // Thêm tiền vàng (gold) vào tài khoản người chơi nếu có
        if (gold > 0) {
            try {
                Entity player = FXGL.getGameWorld().getSingleton(EntityType.PLAYER);
                PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
                if (playerComponent != null) {
                    playerComponent.addMoney(gold);
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi trao phần thưởng tiền vàng: " + e.getMessage());
            }
        }
    }

    public int getGold()                       { return gold; }
    public Map<ItemType, Integer> getItems()   { return items; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (gold > 0) sb.append(gold).append(" vàng");
        items.forEach((type, qty) -> {
            if (sb.length() > 0) sb.append(", ");
            sb.append(qty).append("x ").append(type.getDisplayName());
        });
        return sb.length() > 0 ? sb.toString() : "Không có";
    }

    /* ------------------------------------------------------------------ */
    /*  Builder                                                             */
    /* ------------------------------------------------------------------ */
    public static class Builder {
        private int gold = 0;
        private final Map<ItemType, Integer> items = new LinkedHashMap<>();

        public Builder gold(int amount)                        { this.gold = amount; return this; }
        public Builder item(ItemType type, int qty)            { items.merge(type, qty, Integer::sum); return this; }
        public QuestReward build()                             { return new QuestReward(this); }
    }
}

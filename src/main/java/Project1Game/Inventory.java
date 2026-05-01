package Project1Game;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Inventory {
    public static final int HOTBAR_SIZE = 9;
    public static final int ROWS = 3;
    public static final int COLS = 9;
    public static final int TOTAL_SIZE = HOTBAR_SIZE + ROWS * COLS; // 9 + 27 = 36

    private final Map<ItemType, IntegerProperty> items = new LinkedHashMap<>();
    private int selectedSlot = 0;
    private final ItemType[] allSlots;   // tất cả ItemType
    private final ItemType[] hotbar;     // 9 slot đầu = toolbar

    public Inventory() {
        allSlots = ItemType.values();
        hotbar = new ItemType[HOTBAR_SIZE];
        // 9 slot đầu của ItemType làm hotbar
        for (int i = 0; i < HOTBAR_SIZE && i < allSlots.length; i++) {
            hotbar[i] = allSlots[i];
        }
        for (ItemType type : allSlots) {
            items.put(type, new SimpleIntegerProperty(0));
        }
        addItem(ItemType.HOE, 1);
        addItem(ItemType.WHEAT_SEED, 10);
        addItem(ItemType.RADISH_SEED, 10);
        addItem(ItemType.CABBAGE_SEED, 10);
        addItem(ItemType.LETTUCE_SEED, 10);
        addItem(ItemType.CORN_SEED, 10);
        addItem(ItemType.WATERING_CAN, 1);
    }

    public void addItem(ItemType type, int amount) {
        items.get(type).set(items.get(type).get() + amount);
    }

    public boolean removeItem(ItemType type, int amount) {
        int current = items.get(type).get();
        if (current >= amount) {
            items.get(type).set(current - amount);
            return true;
        }
        return false;
    }

    public int getCount(ItemType type) {
        return items.get(type).get();
    }

    public IntegerProperty countProperty(ItemType type) {
        return items.get(type);
    }

    public ItemType getSelectedItem() {
        return hotbar[selectedSlot];
    }

    public int getSelectedSlot() { return selectedSlot; }

    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < HOTBAR_SIZE) selectedSlot = slot;
    }

    public void selectNext() {
        selectedSlot = (selectedSlot + 1) % HOTBAR_SIZE;
    }

    public void selectPrevious() {
        selectedSlot = (selectedSlot - 1 + HOTBAR_SIZE) % HOTBAR_SIZE;
    }

    /** 9 slot hotbar dùng cho ToolbarView */
    public ItemType[] getSlots() { return hotbar; }

    /** Tất cả slot dùng cho InventoryView */
    public ItemType[] getAllSlots() { return allSlots; }
}

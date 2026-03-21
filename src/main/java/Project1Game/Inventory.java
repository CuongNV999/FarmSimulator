package Project1Game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.LinkedHashMap;
import java.util.Map;

public class Inventory {
    private final Map<ItemType, IntegerProperty> items = new LinkedHashMap<>();
    private int selectedSlot = 0;
    private final ItemType[] slots;

    public Inventory() {
        slots = ItemType.values();
        for (ItemType type : slots) {
            items.put(type, new SimpleIntegerProperty(0));
        }
        // Bắt đầu với một số vật phẩm mặc định
        addItem(ItemType.HOE, 1);
        addItem(ItemType.RICE_SEED, 10);
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
        return slots[selectedSlot];
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < slots.length) {
            this.selectedSlot = slot;
        }
    }

    public void selectNext() {
        selectedSlot = (selectedSlot + 1) % slots.length;
    }

    public void selectPrevious() {
        selectedSlot = (selectedSlot - 1 + slots.length) % slots.length;
    }

    public ItemType[] getSlots() {
        return slots;
    }
}

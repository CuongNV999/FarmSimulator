package Project1Game.model;

import Project1Game.core.ItemType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Inventory {
    public static final int HOTBAR_SIZE = 9;
    public static final int ROWS = 3;
    public static final int COLS = 9;
    public static final int TOTAL_SIZE = HOTBAR_SIZE + ROWS * COLS; // 9 + 27 = 36

    private final InventorySlot[] slots;
    private int selectedSlot = 0;

    public Inventory() {
        slots = new InventorySlot[TOTAL_SIZE];
        for (int i = 0; i < TOTAL_SIZE; i++) {
            slots[i] = new InventorySlot();
        }

        // Vật phẩm khởi đầu
        addItem(ItemType.HOE, 1);
        addItem(ItemType.WHEAT_SEED, 10);
        addItem(ItemType.RADISH_SEED, 10);
        addItem(ItemType.CABBAGE_SEED, 10);
        addItem(ItemType.LETTUCE_SEED, 10);
        addItem(ItemType.TOMATO_SEED, 10);
        addItem(ItemType.CORN_SEED, 10);
        addItem(ItemType.WATERING_CAN, 1);
    }

    // Thêm vật phẩm vào kho đồ (tìm slot trống hoặc slot có sẵn cùng loại)
    public void addItem(ItemType type, int amount) {
        // Tìm slot có sẵn cùng loại
        for (InventorySlot slot : slots) {
            if (slot.getItemType() == type) {
                slot.add(type, amount);
                return;
            }
        }
        // Nếu không tìm thấy, tìm slot trống
        for (InventorySlot slot : slots) {
            if (slot.isEmpty()) {
                slot.add(type, amount);
                return;
            }
        }
        System.out.println("Kho đồ đầy, không thể thêm " + type);
    }

    // Xóa vật phẩm khỏi kho đồ (hỗ trợ nhiều ô chứa)
    public boolean removeItem(ItemType type, int amount) {
        if (getCount(type) < amount) {
            return false; // Không đủ vật phẩm trong toàn bộ kho đồ
        }

        int remaining = amount;
        for (InventorySlot slot : slots) {
            if (slot.getItemType() == type) {
                int count = slot.getCount();
                if (count >= remaining) {
                    slot.remove(remaining);
                    remaining = 0;
                    break;
                } else {
                    slot.clear();
                    remaining -= count;
                }
            }
        }
        return remaining == 0;
    }

    // Xóa toàn bộ vật phẩm khỏi kho đồ
    public void clear() {
        for (InventorySlot slot : slots) {
            slot.clear();
        }
    }

    // Lấy số lượng vật phẩm theo loại
    public int getCount(ItemType type) {
        int total = 0;
        for (InventorySlot slot : slots) {
            if (slot.getItemType() == type) {
                total += slot.getCount();
            }
        }
        return total;
    }

    // Lấy Property của số lượng vật phẩm (chỉ cho slot đầu tiên tìm thấy)
    public IntegerProperty countProperty(ItemType type) {
        for (InventorySlot slot : slots) {
            if (slot.getItemType() == type) {
                return slot.countProperty();
            }
        }
        // Trả về một SimpleIntegerProperty mới nếu không tìm thấy để tránh lỗi null
        return new SimpleIntegerProperty(0);
    }

    // Lấy ItemType của slot đang chọn
    public ItemType getSelectedItem() {
        return slots[selectedSlot].getItemType();
    }

    // Lấy chỉ số slot đang chọn
    public int getSelectedSlot() {
        return selectedSlot;
    }

    // Đặt slot đang chọn
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < HOTBAR_SIZE) { // Chỉ cho phép chọn trong hotbar
            selectedSlot = slot;
        }
    }

    // Chọn slot tiếp theo trong hotbar
    public void selectNext() {
        selectedSlot = (selectedSlot + 1) % HOTBAR_SIZE;
    }

    // Chọn slot trước đó trong hotbar
    public void selectPrevious() {
        selectedSlot = (selectedSlot - 1 + HOTBAR_SIZE) % HOTBAR_SIZE;
    }

    // Lấy tất cả các slot (dùng cho InventoryView)
    public InventorySlot[] getSlots() {
        return slots;
    }

    // Lấy các slot hotbar (dùng cho ToolbarView)
    public InventorySlot[] getHotbarSlots() {
        InventorySlot[] hotbarSlots = new InventorySlot[HOTBAR_SIZE];
        System.arraycopy(slots, 0, hotbarSlots, 0, HOTBAR_SIZE);
        return hotbarSlots;
    }

    // Lấy một slot cụ thể
    public InventorySlot getSlot(int index) {
        if (index >= 0 && index < TOTAL_SIZE) {
            return slots[index];
        }
        return null;
    }

    /**
     * Hoán đổi nội dung của hai slot.
     * @param index1 Chỉ số của slot thứ nhất.
     * @param index2 Chỉ số của slot thứ hai.
     */
    public void swapSlots(int index1, int index2) {
        if (index1 < 0 || index1 >= TOTAL_SIZE || index2 < 0 || index2 >= TOTAL_SIZE) {
            System.err.println("Chỉ số slot không hợp lệ.");
            return;
        }

        InventorySlot slot1 = slots[index1];
        InventorySlot slot2 = slots[index2];

        // Tạo bản sao tạm thời của slot1
        InventorySlot tempSlot = slot1.copy();

        // Di chuyển nội dung của slot2 sang slot1
        slot1.setItemType(slot2.getItemType());
        slot1.setCount(slot2.getCount());

        // Di chuyển nội dung của tempSlot (ban đầu là slot1) sang slot2
        slot2.setItemType(tempSlot.getItemType());
        slot2.setCount(tempSlot.getCount());
    }

    /**
     * Di chuyển vật phẩm từ một slot sang một slot khác.
     * Nếu slot đích trống, vật phẩm sẽ được di chuyển hoàn toàn.
     * Nếu slot đích có cùng loại vật phẩm, chúng sẽ được gộp lại.
     * Nếu slot đích có loại vật phẩm khác, chúng sẽ được hoán đổi.
     * @param fromIndex Chỉ số của slot nguồn.
     * @param toIndex Chỉ số của slot đích.
     */
    public void moveItem(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= TOTAL_SIZE || toIndex < 0 || toIndex >= TOTAL_SIZE) {
            System.err.println("Chỉ số slot không hợp lệ.");
            return;
        }

        InventorySlot fromSlot = slots[fromIndex];
        InventorySlot toSlot = slots[toIndex];

        if (fromSlot.isEmpty()) {
            return; // Không có gì để di chuyển
        }

        if (toSlot.isEmpty()) {
            // Di chuyển hoàn toàn nếu slot đích trống
            toSlot.setItemType(fromSlot.getItemType());
            toSlot.setCount(fromSlot.getCount());
            fromSlot.clear();
        } else if (toSlot.getItemType() == fromSlot.getItemType()) {
            // Gộp vật phẩm nếu cùng loại
            toSlot.setCount(toSlot.getCount() + fromSlot.getCount());
            fromSlot.clear();
        } else {
            // Hoán đổi nếu khác loại
            swapSlots(fromIndex, toIndex);
        }
    }
}

package Project1Game.model;

import Project1Game.core.ItemType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class InventorySlot {
    private final ObjectProperty<ItemType> itemType = new SimpleObjectProperty<>(null);
    private final IntegerProperty count = new SimpleIntegerProperty(0);

    public ItemType getItemType() {
        return itemType.get();
    }

    public ObjectProperty<ItemType> itemTypeProperty() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType.set(itemType);
    }

    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }

    public void setCount(int count) {
        this.count.set(count);
    }

    public boolean isEmpty() {
        return getItemType() == null || getCount() == 0;
    }

    public void clear() {
        setItemType(null);
        setCount(0);
    }

    // Thêm vật phẩm vào slot
    public void add(ItemType type, int amount) {
        if (isEmpty()) {
            setItemType(type);
            setCount(amount);
        } else if (getItemType() == type) {
            setCount(getCount() + amount);
        } else {
            // Xử lý lỗi hoặc không làm gì nếu cố gắng thêm loại vật phẩm khác vào slot đã có
            System.err.println("Cannot add " + type + " to slot containing " + getItemType());
        }
    }

    // Xóa vật phẩm khỏi slot
    public boolean remove(int amount) {
        if (getCount() >= amount) {
            setCount(getCount() - amount);
            if (getCount() == 0) {
                clear();
            }
            return true;
        }
        return false;
    }

    // Tạo một bản sao của slot
    public InventorySlot copy() {
        InventorySlot newSlot = new InventorySlot();
        newSlot.setItemType(this.getItemType());
        newSlot.setCount(this.getCount());
        return newSlot;
    }
}

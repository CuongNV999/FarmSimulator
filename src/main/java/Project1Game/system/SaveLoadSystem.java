package Project1Game.system;

import Project1Game.component.farming.SoilComponent;
import Project1Game.core.EntityType;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.model.SaveData;
import Project1Game.ui.StatusBarsView;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

public class SaveLoadSystem {
    private final Inventory inventory;
    private final StatusBarsView statusBarsView;
    private final TimeSystem timeSystem;

    public SaveLoadSystem(Inventory inventory, StatusBarsView statusBarsView, TimeSystem timeSystem) {
        this.inventory = inventory;
        this.statusBarsView = statusBarsView;
        this.timeSystem = timeSystem;
    }

    public void save() {
        SaveData data = new SaveData();
        data.gameTime = timeSystem.getGameTime(); // Lấy từ TimeSystem
        data.health = statusBarsView.getHealth();
        data.hunger = statusBarsView.getHunger();

        // Lưu Inventory
        for (ItemType t : ItemType.values()) {
            if (inventory.getCount(t) > 0) {
                data.inventoryItems.put(t.name(), inventory.getCount(t));
            }
        }

        // Lưu Đất (Soil)
        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).forEach(s -> {
            SaveData.SoilData sd = new SaveData.SoilData();
            sd.x = s.getX();
            sd.y = s.getY();
            sd.isWet = s.getComponent(SoilComponent.class).isWet();
            sd.hasPlant = s.getComponent(SoilComponent.class).isHasPlant();
            data.soils.add(sd);
        });

        // Thực thi ghi file
        FXGL.getFileSystemService().writeDataTask(data, "save_game.dat").run();
        System.out.println("Đã lưu game thành công!");
    }

    public void load() {
        FXGL.getFileSystemService().<SaveData>readDataTask("save_game.dat").onSuccess(data -> {
            timeSystem.setGameTime(data.gameTime); // Gán lại cho TimeSystem
            statusBarsView.setHealth(data.health);
            statusBarsView.setHunger(data.hunger);

            // Xóa thực thể cũ
            FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL, EntityType.WHEAT, EntityType.CORN)
                    .forEach(Entity::removeFromWorld);

            // Tái tạo ô đất
            for (SaveData.SoilData sd : data.soils) {
                Entity s = FXGL.getGameWorld().spawn("Soil", sd.x, sd.y);
                s.getComponent(SoilComponent.class).setWet(sd.isWet);
                s.getComponent(SoilComponent.class).setHasPlant(sd.hasPlant);
            }

            System.out.println("Đã tải game thành công!");
        }).run();
    }
}
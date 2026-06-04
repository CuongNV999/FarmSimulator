package Project1Game.system;

import Project1Game.component.farming.CropComponent;
import Project1Game.component.farming.SoilComponent;
import Project1Game.config.CropData; // Import CropData
import Project1Game.core.EntityType;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.quest.QuestContext;
import Project1Game.quest.QuestManager;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Point2D;

public class FarmingSystem {
    private final Inventory inventory;

    public FarmingSystem(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Cập nhật vị trí và độ mờ của con trỏ (Selector) dựa trên vị trí chuột và nhân vật.
     */
    public void updateSelector(com.almasb.fxgl.entity.Entity selector, com.almasb.fxgl.entity.Entity player) {
        if (selector == null || player == null) return;

        // 1. Lấy tọa độ chuột trong thế giới game
        double mouseX = com.almasb.fxgl.dsl.FXGL.getInput().getMouseXWorld();
        double mouseY = com.almasb.fxgl.dsl.FXGL.getInput().getMouseYWorld();

        // 2. Thuật toán Snap vào lưới ô vuông 32x32
        double x = Math.floor(mouseX / 32) * 32;
        double y = Math.floor(mouseY / 32) * 32;
        selector.setPosition(x, y);

        // 3. Tính khoảng cách giữa tâm nhân vật và ô đang trỏ tới
        // (x+16, y+16) là tâm của ô 32x32
        double distance = player.getCenter().distance(x + 16, y + 16);

        // 4. Thiết lập độ mờ: Gần (<= 250px) thì hiện rõ, xa thì mờ đi
        selector.getViewComponent().setOpacity(distance <= 250 ? 1.0 : 0.3);
    }



    /** Logic tưới nước */
    public void useWateringCan(Entity selector) {
        if (inventory.getCount(ItemType.WATERING_CAN) <= 0 || selector.getViewComponent().getOpacity() < 1.0) return;
        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(selector.getPosition()) < 15)
                .findFirst().ifPresent(soil -> {
                    soil.getComponent(SoilComponent.class).setWet(true);
                    QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.WATER, null));
                });
    }

    /** Logic thu hoạch */
    public void handleHarvest(Entity selector) {
        if (selector.getViewComponent().getOpacity() < 1.0) return;

        for (EntityType t : Project1Game.core.CropRegistry.getInstance().getSupportedCrops()) {
            FXGL.getGameWorld().getEntitiesByType(t).stream()
                    .filter(c -> c.getPosition().distance(selector.getPosition()) < 10
                            && c.getComponent(CropComponent.class).isRipe())
                    .findFirst().ifPresent(c -> {
                        ItemType res = ItemType.valueOf(t.name());
                        CropData cropData = c.getComponent(CropComponent.class).getData(); // Lấy CropData từ CropComponent

                        c.removeFromWorld();
                        int yield = cropData.yield;
                        inventory.addItem(res, yield);
                        System.out.println("Thu hoạch " + res.getDisplayName() + " với số lượng " + yield + "!"); // In ra sản lượng
                        QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.HARVEST, res));
                    });
        }
    }
}
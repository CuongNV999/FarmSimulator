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

        // 4. Thiết lập độ mờ: Gần (<= 96px) thì hiện rõ, xa thì mờ đi
        selector.getViewComponent().setOpacity(distance <= 96 ? 1.0 : 0.3);
    }

    /** Logic sử dụng Cuốc: Cày cỏ thành đất */
    public void useHoe(Entity selector) {
        if (selector.getViewComponent().getOpacity() < 1.0) return;

        double selX = selector.getX();
        double selY = selector.getY();

        boolean inField = FXGL.getGameWorld().getEntitiesByType(EntityType.FIELD).stream()
                .anyMatch(f -> f.getX() <= selX && selX < f.getX() + f.getWidth()
                        && f.getY() <= selY && selY < f.getY() + f.getHeight());

        if (inField) {
            boolean hasSoil = FXGL.getGameWorld().getEntitiesAt(new Point2D(selX + 16, selY + 16)).stream()
                    .anyMatch(e -> e.isType(EntityType.SOIL));

            if (!hasSoil) {
                FXGL.spawn("Soil", selX, selY);
                FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL)
                        .forEach(s -> s.getComponent(SoilComponent.class).updateTexture());
            }
        }
    }

    /** Logic gieo hạt */
    public void plantCrop(Entity selector, ItemType seed) {
        if (inventory.getCount(seed) <= 0 || selector.getViewComponent().getOpacity() < 1.0) return;

        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                .filter(s -> s.getPosition().distance(selector.getPosition()) < 10
                        && s.getComponent(SoilComponent.class).canPlant())
                .findFirst().ifPresent(soil -> {
                    FXGL.spawn(seed.getSpawnName(), soil.getX(), soil.getY());
                    soil.getComponent(SoilComponent.class).setHasPlant(true);
                    inventory.removeItem(seed, 1);
                    QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.PLANT, seed));
                });
    }

    /** Logic tưới nước */
    public void useWateringCan(Entity selector) {
        if (selector.getViewComponent().getOpacity() < 1.0) return;
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
        EntityType[] cropTypes = {EntityType.WHEAT, EntityType.RADISH, EntityType.CABBAGE,
                EntityType.LETTUCE, EntityType.TOMATO, EntityType.CORN};

        for (EntityType t : cropTypes) {
            FXGL.getGameWorld().getEntitiesByType(t).stream()
                    .filter(c -> c.getPosition().distance(selector.getPosition()) < 10
                            && c.getComponent(CropComponent.class).isRipe())
                    .findFirst().ifPresent(c -> {
                        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                                .filter(s -> s.getPosition().distance(c.getPosition()) < 5)
                                .findFirst().ifPresent(s -> s.getComponent(SoilComponent.class).setHasPlant(false));

                        ItemType res = ItemType.valueOf(t.name());
                        CropData cropData = c.getComponent(CropComponent.class).getData(); // Lấy CropData từ CropComponent
                        int cropPrice = cropData.price; // Lấy giá của cây trồng

                        c.removeFromWorld();
                        inventory.addItem(res, 1);
                        System.out.println("Thu hoạch " + res.getDisplayName() + " với giá " + cropPrice + "!"); // In ra giá
                        // TODO: Thêm logic cộng tiền vào đây
                        QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.HARVEST, res));
                    });
        }
    }
}
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

        // 1. Lấy tọa độ chuột trong thế giới game, cộng thêm vận tốc của nhân vật để bù 1 frame viewport lag
        double vx = 0;
        double vy = 0;
        com.almasb.fxgl.physics.PhysicsComponent physics = player.getComponentOptional(com.almasb.fxgl.physics.PhysicsComponent.class).orElse(null);
        if (physics != null) {
            vx = physics.getVelocityX();
            vy = physics.getVelocityY();
        }
        double tpf = com.almasb.fxgl.dsl.FXGL.tpf();
        double mouseX = com.almasb.fxgl.dsl.FXGL.getInput().getMouseXWorld() + vx * tpf;
        double mouseY = com.almasb.fxgl.dsl.FXGL.getInput().getMouseYWorld() + vy * tpf;

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

    /** Logic sử dụng Cuốc: Cày cỏ thành đất */
    public void useHoe(Point2D worldPoint) {
        double x = Math.floor(worldPoint.getX() / 32) * 32;
        double y = Math.floor(worldPoint.getY() / 32) * 32;

        // O(1) single-pass lookup of entities at the tile coordinates
        java.util.List<Entity> localEntities = FXGL.getGameWorld().getEntitiesAt(new Point2D(x + 16, y + 16));
        boolean inField = false;
        boolean hasSoil = false;
        for (Entity e : localEntities) {
            if (e.isType(EntityType.FIELD)) {
                inField = true;
            } else if (e.isType(EntityType.SOIL)) {
                hasSoil = true;
            }
        }

        if (inField && !hasSoil) {
            Entity s = FXGL.spawn("Soil", x, y);
            s.getComponent(SoilComponent.class).updateTexture();
            if (Project1Game.Main.getInstance() != null) {
                Project1Game.Main.getInstance().drainHungerForWork(1.0);
            }
        }
    }

    /** Logic gieo hạt */
    public void plantCrop(Entity selector, ItemType seed) {
        if (selector.getViewComponent().getOpacity() < 1.0) return;
        synchronized (inventory) {
            if (inventory.getCount(seed) <= 0) return;

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
    }

    /** Logic tưới nước */
    public void useWateringCan(Point2D worldPoint) {
        if (inventory.getCount(ItemType.WATERING_CAN) <= 0) return;

        double x = Math.floor(worldPoint.getX() / 32) * 32;
        double y = Math.floor(worldPoint.getY() / 32) * 32;

        java.util.List<Entity> localEntities = FXGL.getGameWorld().getEntitiesAt(new Point2D(x + 16, y + 16));
        for (Entity e : localEntities) {
            if (e.isType(EntityType.SOIL)) {
                SoilComponent sc = e.getComponent(SoilComponent.class);
                if (!sc.isWet()) {
                    sc.setWet(true);
                    QuestManager.getInstance().broadcast(new QuestContext(QuestContext.EventType.WATER, null));
                    if (Project1Game.Main.getInstance() != null) {
                        Project1Game.Main.getInstance().drainHungerForWork(1.0);
                    }
                }
                break;
            }
        }
    }

    /** Logic thu hoạch */
    public void handleHarvest(Entity selector) {
        if (selector.getViewComponent().getOpacity() < 1.0) return;
        EntityType[] cropTypes = {
                EntityType.WHEAT, EntityType.RADISH, EntityType.CABBAGE,
                EntityType.GRAPE, EntityType.CUCUMBER, EntityType.PEPPER,
                EntityType.CAULIFLOWER, EntityType.BEAN, EntityType.PINEAPPLE,
                EntityType.SUNFLOWER, EntityType.COCONUT, EntityType.APPLE
        };

        for (EntityType t : cropTypes) {
            FXGL.getGameWorld().getEntitiesByType(t).stream()
                    .filter(c -> c.getPosition().distance(selector.getPosition()) < 10
                            && c.getComponent(CropComponent.class).isRipe())
                    .findFirst().ifPresent(c -> {
                        FXGL.getGameWorld().getEntitiesByType(EntityType.SOIL).stream()
                                .filter(s -> s.getPosition().distance(c.getPosition()) < 20)
                                .findFirst().ifPresent(s -> s.getComponent(SoilComponent.class).setHasPlant(false));

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
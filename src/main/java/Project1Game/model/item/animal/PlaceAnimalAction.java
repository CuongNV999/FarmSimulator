package Project1Game.model.item.animal;

import Project1Game.model.item.Usable;
import Project1Game.config.AnimalConfig;

import Project1Game.Main;
import Project1Game.core.ItemType;
import Project1Game.system.NotificationManager;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;

public class PlaceAnimalAction implements Usable {
    private final String spawnName;

    public PlaceAnimalAction(String spawnName) {
        this.spawnName = spawnName;
    }

    @Override
    public void use(Entity player, Entity target) {
        if (target == null) return;

        // House Check: prevent placing animals inside the main house map
        if ("Main_house.tmx".equals(Main.getInstance().getCurrentMap())) {
            NotificationManager.pushNotification("Không thể thả động vật trong nhà!");
            return;
        }

        Project1Game.model.Inventory inventory = Project1Game.Main.getInstance().getInventory();
        if (inventory == null) return;

        ItemType animalItem = inventory.getSelectedItem();
        if (animalItem == null || inventory.getCount(animalItem) <= 0) return;

        // Spawn animal at selector position
        FXGL.spawn(spawnName, target.getX(), target.getY());
        inventory.removeItem(animalItem, 1);
        NotificationManager.pushNotification("Đã thả " + animalItem.getDisplayName() + "!");
    }
}

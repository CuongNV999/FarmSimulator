package Project1Game.ui;

import Project1Game.Main;
import Project1Game.component.player.PlayerComponent;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;

public class AdminPresenter {
    private final Inventory inventory;
    private final PlayerComponent playerComponent;

    public AdminPresenter(Inventory inventory, PlayerComponent playerComponent) {
        this.inventory = inventory;
        this.playerComponent = playerComponent;
    }

    public boolean verifyPasscode(String code) {
        return "1111".equals(code);
    }

    public void setGold(int amount) {
        playerComponent.setMoney(amount);
    }

    public void addGold(int amount) {
        playerComponent.addMoney(amount);
    }

    public void changeSkin(String path) {
        playerComponent.changeSkin(path);
    }

    public void setTimeSpeedMultiplier(double speed) {
        Main app = Main.getInstance();
        if (app != null && app.getTimeSystem() != null) {
            app.getTimeSystem().setTimeSpeedMultiplier(speed);
        }
    }

    public void triggerMatureAllCropsAndAnimals() {
        Main app = Main.getInstance();
        if (app != null) {
            app.matureAllCropsAndAnimals();
        }
    }

    public void triggerSpawnBushMonsterAdmin() {
        Main app = Main.getInstance();
        if (app != null) {
            app.spawnBushMonsterAdmin();
        }
    }

    public void addItem(ItemType type, int amount) {
        inventory.addItem(type, amount);
    }

    public void removeItem(ItemType type, int amount) {
        inventory.removeItem(type, amount);
    }
}

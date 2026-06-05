package Project1Game.model.item.food;

import Project1Game.model.item.Usable;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import Project1Game.Main;
import Project1Game.core.ItemType;
import Project1Game.ui.view.hud.StatusBarsView;
import Project1Game.system.NotificationManager;

public class EatAction implements Usable {
    private final int hungerRestore;
    private final int healthRestore;

    public EatAction(int hungerRestore, int healthRestore) {
        this.hungerRestore = hungerRestore;
        this.healthRestore = healthRestore;
    }

    @Override
    public void use(Entity player, Entity target) {
        Main main = Main.getInstance();
        if (main == null) return;

        StatusBarsView statusBars = main.getStatusBarsView();
        if (statusBars == null) return;

        ItemType foodType = main.getInventory().getSelectedItem();
        if (foodType == null || main.getInventory().getCount(foodType) <= 0) return;

        double currentHunger = statusBars.getHunger();
        double maxHunger = statusBars.getMaxHunger();
        double currentHealth = statusBars.getHealth();
        double maxHealth = statusBars.getMaxHealth();

        if (currentHunger >= maxHunger && currentHealth >= maxHealth) {
            NotificationManager.pushNotification("Bạn đã đầy bụng và khỏe mạnh, không cần ăn!");
            return;
        }

        // Hồi phục chỉ số
        statusBars.setHunger(Math.min(maxHunger, currentHunger + hungerRestore));
        statusBars.setHealth(Math.min(maxHealth, currentHealth + healthRestore));

        // Tiêu hao 1 vật phẩm khỏi inventory
        main.getInventory().removeItem(foodType, 1);

        // Phát tín hiệu thông báo
        NotificationManager.pushNotification("Đã ăn " + foodType.getDisplayName() + " (+" + hungerRestore + " Đói, +" + healthRestore + " HP)");
    }
}

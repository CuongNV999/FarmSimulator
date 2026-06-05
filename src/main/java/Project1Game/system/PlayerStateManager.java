package Project1Game.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import Project1Game.Main;
import Project1Game.component.player.PlayerComponent;
import Project1Game.ui.StatusBarsView;
import Project1Game.ui.DialogView;

public class PlayerStateManager {
    private double lastHungerDrainTime = -1;
    private double lastStarveHPTime = -1;
    private boolean isHPDepletionEnabled = true;

    public void toggleHPDepletion() {
        this.isHPDepletionEnabled = !this.isHPDepletionEnabled;
        NotificationManager.pushNotification("HP Depletion: " + (isHPDepletionEnabled ? "ON" : "OFF"));
    }

    public boolean isHPDepletionEnabled() {
        return isHPDepletionEnabled;
    }

    public void drainHungerForWork(double amount, StatusBarsView statusBarsView) {
        if (statusBarsView != null) {
            statusBarsView.setHunger(Math.max(0, statusBarsView.getHunger() - amount));
        }
    }

    public void updatePlayerStats(double tpf, TimeSystem timeSystem, StatusBarsView statusBarsView, Entity player, DialogView dialogView, LevelManager levelManager) {
        if (timeSystem != null && statusBarsView != null) {
            double currentMins = timeSystem.getGameTime();

            if (lastHungerDrainTime == -1) {
                lastHungerDrainTime = currentMins;
            }

            double hungerDiff = currentMins - lastHungerDrainTime;
            if (hungerDiff < 0) {
                hungerDiff += 1440;
            }
            if (hungerDiff >= 30) {
                int intervals = (int) (hungerDiff / 30);
                double newHunger = Math.max(0, statusBarsView.getHunger() - intervals);
                statusBarsView.setHunger(newHunger);
                lastHungerDrainTime = (lastHungerDrainTime + intervals * 30) % 1440;
            }

            if (statusBarsView.getHunger() <= 0) {
                if (isHPDepletionEnabled) {
                    if (lastStarveHPTime == -1) {
                        lastStarveHPTime = currentMins;
                    }
                    double starveDiff = currentMins - lastStarveHPTime;
                    if (starveDiff < 0) {
                        starveDiff += 1440;
                    }
                    if (starveDiff >= 5) {
                        int intervals = (int) (starveDiff / 5);
                        double newHP = Math.max(0, statusBarsView.getHealth() - (intervals * 1.0));
                        statusBarsView.setHealth(newHP);
                        lastStarveHPTime = (lastStarveHPTime + intervals * 5) % 1440;
                    }
                } else {
                    lastStarveHPTime = -1;
                }
            } else {
                lastStarveHPTime = -1;
            }

            if (statusBarsView.getHealth() <= 0) {
                handlePlayerFaint(timeSystem, player, statusBarsView, dialogView, levelManager);
            }
        }
    }

    public void handlePlayerFaint(TimeSystem timeSystem, Entity player, StatusBarsView statusBarsView, DialogView dialogView, LevelManager levelManager) {
        System.out.println("--- Player Fainted! ---");
        if (timeSystem != null) {
            timeSystem.advanceToNextDay();
            // Cache the guider and trader reappearance at 6:00 AM morning
            levelManager.getPendingNPCSpawns().clear();
            levelManager.getPendingNPCSpawns().add(new LevelManager.NPCSpawnConfig("Guider", 1792, 1024, false));
            levelManager.getPendingNPCSpawns().add(new LevelManager.NPCSpawnConfig("Trader", 1600, 1024, false));
            System.out.println("[NPC Cache] Cached morning transitions via faint: NPCs are visible");
        }

        if (player != null) {
            PlayerComponent pc = player.getComponent(PlayerComponent.class);
            if (pc != null) {
                int newMoney = Math.max(0, pc.getMoney() - 100);
                pc.setMoney(newMoney);
            }
        }

        if (statusBarsView != null) {
            statusBarsView.setHealth(statusBarsView.getMaxHealth() * 0.5);
            statusBarsView.setHunger(statusBarsView.getMaxHunger() * 0.5);
        }

        lastHungerDrainTime = -1;
        lastStarveHPTime = -1;

        if (dialogView != null && dialogView.isOpen()) {
            dialogView.hide();
        }
        
        Main app = Main.getInstance();
        app.updateLevel("Main_house.tmx", 550, 350);

        if (dialogView != null) {
            dialogView.setDialog("Thông báo", "Bạn đã bị kiệt sức và ngất xỉu!", "Bác nông dân đã đưa bạn về nhà.",
                    "Phạt viện phí: 100 G. Sức khỏe phục hồi 50%.");
            dialogView.show();
        }
    }

    public void handleSleepInteraction(TimeSystem timeSystem, StatusBarsView statusBarsView, DialogView dialogView, LevelManager levelManager) {
        System.out.println("--- Main: Bắt đầu đi ngủ ---");
        if (timeSystem != null) {
            timeSystem.advanceToNextDay();
            if (statusBarsView != null) {
                statusBarsView.setHealth(statusBarsView.getMaxHealth());
                statusBarsView.setHunger(statusBarsView.getMaxHunger());
            }

            // Cache the guider and trader reappearance at 6:00 AM morning
            if (levelManager != null && levelManager.getCurrentMap().equals("Main_house.tmx")) {
                levelManager.getPendingNPCSpawns().clear();
                levelManager.getPendingNPCSpawns().add(new LevelManager.NPCSpawnConfig("Guider", 1792, 1024, false));
                levelManager.getPendingNPCSpawns().add(new LevelManager.NPCSpawnConfig("Trader", 1600, 1024, false));
                System.out.println("[NPC Cache] Cached morning transitions via sleep: NPCs are visible");
            }

            if (dialogView != null) {
                dialogView.setDialog("Thông báo", "Bạn đã ngủ một giấc thật ngon.", "Sức khỏe đã được hồi phục!");
                dialogView.show();
            }
            System.out.println("Nhân vật đã đi ngủ.");
        }
        System.out.println("--- Main: Kết thúc đi ngủ ---");
    }
}

package Project1Game.ui.presenter;

import Project1Game.Main;
import Project1Game.component.player.PlayerComponent;
import Project1Game.core.ItemType;
import Project1Game.model.Inventory;
import Project1Game.system.CheatEvent;
import com.almasb.fxgl.dsl.FXGL;

public class AdminPresenter {
    private final Inventory inventory;
    private final PlayerComponent playerComponent;

    public AdminPresenter(Inventory inventory, PlayerComponent playerComponent) {
        this.inventory = inventory;
        this.playerComponent = playerComponent;
        registerHandlers();
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

    private void registerHandlers() {
        FXGL.getEventBus().addEventHandler(CheatEvent.ANY, e -> {
            switch (e.getCheatType()) {
                case SET_GOLD:
                    setGold(e.getIntVal());
                    break;
                case ADD_GOLD:
                    addGold(e.getIntVal());
                    break;
                case CHANGE_SKIN:
                    changeSkin(e.getStringVal());
                    break;
                case SET_TIME_SPEED:
                    setTimeSpeedMultiplier(e.getDoubleVal());
                    break;
                case MATURE_ALL:
                    triggerMatureAllCropsAndAnimals();
                    break;
                case SPAWN_MONSTER:
                    triggerSpawnBushMonsterAdmin();
                    break;
                case CHANGE_WEATHER:
                    Project1Game.system.WeatherSystem.getInstance().changeWeather(e.getWeather());
                    break;
                case SET_TIME:
                    Main app = Main.getInstance();
                    if (app != null && app.getTimeSystem() != null) {
                        app.getTimeSystem().setGameTime(e.getDoubleVal());
                    }
                    break;
                case RESTORE_STATS:
                    Main appStats = Main.getInstance();
                    if (appStats != null && appStats.getStatusBarsView() != null) {
                        appStats.getStatusBarsView().setHealth(appStats.getStatusBarsView().getMaxHealth());
                        appStats.getStatusBarsView().setHunger(appStats.getStatusBarsView().getMaxHunger());
                    }
                    break;
                case DRAIN_HP:
                    Main appDrain = Main.getInstance();
                    if (appDrain != null && appDrain.getStatusBarsView() != null) {
                        appDrain.getStatusBarsView().setHealth(0);
                    }
                    break;
                case ACCEPT_QUESTS:
                    Project1Game.quest.QuestManager.getInstance().getAllNPCs().forEach(npc -> {
                        npc.getQuests().forEach(q -> {
                            if (q.getStatus() == Project1Game.quest.QuestStatus.NOT_STARTED) {
                                q.start();
                            }
                        });
                    });
                    Project1Game.system.NotificationManager.pushNotification("All NOT_STARTED quests have been accepted!");
                    break;
                case COMPLETE_OBJECTIVES:
                    Project1Game.quest.QuestManager.getInstance().getAllNPCs().forEach(npc -> {
                        npc.getQuests().forEach(q -> {
                            if (q.getStatus() == Project1Game.quest.QuestStatus.IN_PROGRESS) {
                                q.getObjectives().forEach(obj -> {
                                    obj.setCurrent(obj.getRequired());
                                });
                                q.setStatus(Project1Game.quest.QuestStatus.COMPLETED);
                            }
                        });
                    });
                    Project1Game.system.NotificationManager.pushNotification("All objectives completed!");
                    break;
                case ADD_ITEM:
                    addItem(e.getItemType(), e.getIntVal());
                    break;
                case REMOVE_ITEM:
                    removeItem(e.getItemType(), e.getIntVal());
                    break;
                case TELEPORT:
                    Main appTeleport = Main.getInstance();
                    if (appTeleport != null) {
                        appTeleport.updateLevel(e.getStringVal(), e.getDoubleVal(), e.getDoubleValY());
                    }
                    break;
            }
        });
    }
}

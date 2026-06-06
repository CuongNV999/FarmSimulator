package Project1Game.system;

import Project1Game.Main;
import Project1Game.model.SaveData;
import Project1Game.config.AnimalConfig;
import Project1Game.config.AnimalConfigRegistry;
import com.almasb.fxgl.dsl.FXGL;
import javafx.event.EventHandler;

public class OfflineWorldGrowthManager {
    private EventHandler<DayNightEvent> dayNightHandler;

    public void init() {
        // Clean up any existing handler first to prevent duplicate registrations
        cleanUp();

        dayNightHandler = e -> {
            Main app = Main.getInstance();
            if (app == null || app.getLevelManager() == null) {
                return;
            }

            for (SaveData state : app.getLevelManager().getMapStates().values()) {
                if (state.animals != null) {
                    for (SaveData.AnimalSaveData asd : state.animals) {
                        if (asd.type != null) {
                            int maxDays = AnimalConfigRegistry.getInstance().getConfig(asd.type)
                                    .map(AnimalConfig::maxGrowthDays)
                                    .orElse(0);

                            if (maxDays > 0 && asd.daysGrown < maxDays) {
                                asd.daysGrown++;
                                System.out.println("[OfflineGrowth] Inactive animal " + asd.type + " grew to " + asd.daysGrown
                                        + "/" + maxDays);
                            }
                        }
                    }
                }
            }
        };
        FXGL.getEventBus().addEventHandler(DayNightEvent.SET_DAY, dayNightHandler);
    }

    public void cleanUp() {
        if (dayNightHandler != null) {
            FXGL.getEventBus().removeEventHandler(DayNightEvent.SET_DAY, dayNightHandler);
            dayNightHandler = null;
        }
    }
}

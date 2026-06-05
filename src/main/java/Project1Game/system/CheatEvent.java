package Project1Game.system;

import Project1Game.core.ItemType;
import Project1Game.system.WeatherSystem.Weather;
import javafx.event.Event;
import javafx.event.EventType;

public class CheatEvent extends Event {
    public static final EventType<CheatEvent> ANY = new EventType<>(Event.ANY, "CHEAT_EVENT");
    
    public enum CheatType {
        SET_GOLD,
        ADD_GOLD,
        CHANGE_SKIN,
        SET_TIME_SPEED,
        MATURE_ALL,
        SPAWN_MONSTER,
        CHANGE_WEATHER,
        SET_TIME,
        RESTORE_STATS,
        DRAIN_HP,
        ACCEPT_QUESTS,
        COMPLETE_OBJECTIVES,
        ADD_ITEM,
        REMOVE_ITEM,
        TELEPORT
    }

    private final CheatType cheatType;
    private final int intVal;
    private final double doubleVal;
    private final double doubleValY;
    private final String stringVal;
    private final ItemType itemType;
    private final Weather weather;

    public CheatEvent(CheatType cheatType) {
        super(ANY);
        this.cheatType = cheatType;
        this.intVal = 0;
        this.doubleVal = 0;
        this.doubleValY = 0;
        this.stringVal = null;
        this.itemType = null;
        this.weather = null;
    }

    public CheatEvent(CheatType cheatType, int intVal) {
        super(ANY);
        this.cheatType = cheatType;
        this.intVal = intVal;
        this.doubleVal = 0;
        this.doubleValY = 0;
        this.stringVal = null;
        this.itemType = null;
        this.weather = null;
    }

    public CheatEvent(CheatType cheatType, double doubleVal) {
        super(ANY);
        this.cheatType = cheatType;
        this.intVal = 0;
        this.doubleVal = doubleVal;
        this.doubleValY = 0;
        this.stringVal = null;
        this.itemType = null;
        this.weather = null;
    }

    public CheatEvent(CheatType cheatType, String stringVal) {
        super(ANY);
        this.cheatType = cheatType;
        this.intVal = 0;
        this.doubleVal = 0;
        this.doubleValY = 0;
        this.stringVal = stringVal;
        this.itemType = null;
        this.weather = null;
    }

    public CheatEvent(CheatType cheatType, ItemType itemType, int amount) {
        super(ANY);
        this.cheatType = cheatType;
        this.intVal = amount;
        this.doubleVal = 0;
        this.doubleValY = 0;
        this.stringVal = null;
        this.itemType = itemType;
        this.weather = null;
    }

    public CheatEvent(CheatType cheatType, Weather weather) {
        super(ANY);
        this.cheatType = cheatType;
        this.intVal = 0;
        this.doubleVal = 0;
        this.doubleValY = 0;
        this.stringVal = null;
        this.itemType = null;
        this.weather = weather;
    }

    public CheatEvent(CheatType cheatType, String mapName, double x, double y) {
        super(ANY);
        this.cheatType = cheatType;
        this.stringVal = mapName;
        this.doubleVal = x;
        this.doubleValY = y;
        this.intVal = 0;
        this.itemType = null;
        this.weather = null;
    }

    public CheatType getCheatType() { return cheatType; }
    public int getIntVal() { return intVal; }
    public double getDoubleVal() { return doubleVal; }
    public double getDoubleValY() { return doubleValY; }
    public String getStringVal() { return stringVal; }
    public ItemType getItemType() { return itemType; }
    public Weather getWeather() { return weather; }
}

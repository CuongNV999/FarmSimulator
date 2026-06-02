package Project1Game.system;

import javafx.event.Event;
import javafx.event.EventType;

public class DayNightEvent extends Event {
    public static final EventType<DayNightEvent> ANY = new EventType<>(Event.ANY, "DAY_NIGHT_EVENT");
    public static final EventType<DayNightEvent> SET_DAY = new EventType<>(ANY, "SET_DAY");
    public static final EventType<DayNightEvent> SET_NIGHT = new EventType<>(ANY, "SET_NIGHT");

    public DayNightEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
}
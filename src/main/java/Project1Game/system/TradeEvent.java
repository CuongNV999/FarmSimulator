package Project1Game.system;

import Project1Game.ui.view.shop.TradingView.CartItem;

import Project1Game.ui.view.shop.TradingView;

import Project1Game.component.npc.TraderComponent;

import javafx.event.Event;
import javafx.event.EventType;
import java.util.List;

public class TradeEvent extends Event {
    public static final EventType<TradeEvent> ANY = new EventType<>(Event.ANY, "TRADE_EVENT");
    public static final EventType<TradeEvent> CHECKOUT = new EventType<>(ANY, "TRADE_CHECKOUT");
    public static final EventType<TradeEvent> NEGOTIATE = new EventType<>(ANY, "TRADE_NEGOTIATE");

    private final TraderComponent trader;
    private final List<CartItem> cartItems;
    private final int netCost;
    private final int requestedPercent;

    // For CHECKOUT
    public TradeEvent(EventType<TradeEvent> eventType, TraderComponent trader, List<CartItem> cartItems, int netCost) {
        super(eventType);
        this.trader = trader;
        this.cartItems = cartItems;
        this.netCost = netCost;
        this.requestedPercent = 0;
    }

    // For NEGOTIATE
    public TradeEvent(EventType<TradeEvent> eventType, TraderComponent trader, int requestedPercent) {
        super(eventType);
        this.trader = trader;
        this.cartItems = null;
        this.netCost = 0;
        this.requestedPercent = requestedPercent;
    }

    public TraderComponent getTrader() { return trader; }
    public List<CartItem> getCartItems() { return cartItems; }
    public int getNetCost() { return netCost; }
    public int getRequestedPercent() { return requestedPercent; }
}

package Project1Game.ui.presenter;

import Project1Game.ui.view.shop.TradingView;

import Project1Game.component.player.PlayerComponent;
import Project1Game.component.npc.TraderComponent;
import Project1Game.model.Inventory;
import Project1Game.system.TradeService;
import Project1Game.system.NotificationManager;
import Project1Game.system.TradeEvent;
import com.almasb.fxgl.dsl.FXGL;

import java.util.List;

public class TradingPresenter {
    private final TradingView view;
    private final Inventory inventory;
    private final PlayerComponent playerComponent;

    public TradingPresenter(TradingView view, Inventory inventory, PlayerComponent playerComponent) {
        this.view = view;
        this.inventory = inventory;
        this.playerComponent = playerComponent;
        registerHandlers();
    }

    private TradingView getActiveView() {
        Project1Game.Main app = Project1Game.Main.getInstance();
        if (app != null) {
            TradingView v = app.getTradingView();
            if (v != null) {
                return v;
            }
        }
        return this.view;
    }

    private PlayerComponent getActivePlayerComponent() {
        Project1Game.Main app = Project1Game.Main.getInstance();
        if (app != null && app.getPlayer() != null) {
            PlayerComponent pc = app.getPlayer().getComponent(PlayerComponent.class);
            if (pc != null) {
                return pc;
            }
        }
        return this.playerComponent;
    }

    public void checkout(TraderComponent currentTrader, List<TradingView.CartItem> cartItems, int netCost) {
        TradingView activeView = getActiveView();
        PlayerComponent activePlayer = getActivePlayerComponent();

        TradeService.CheckoutResult result = TradeService.executeCheckout(
            inventory, activePlayer, currentTrader, cartItems, netCost
        );

        NotificationManager.pushNotification(result.message);

        if (result.updateRelationshipState && currentTrader != null) {
            currentTrader.updateRelationship(result.successStatus, netCost > 0);
        }

        if (result.success) {
            activeView.clearCart();
        }

        activeView.refreshAfterTrade();
    }

    public void negotiate(TraderComponent currentTrader, int requestedPercent) {
        if (currentTrader == null) {
            NotificationManager.pushNotification("Không có Trader nào để thương lượng!");
            return;
        }

        if (currentTrader.hasNegotiatedThisSession()) {
            NotificationManager.pushNotification("Bạn đã thương lượng rồi! Mỗi lần vào shop chỉ được thương lượng 1 lần.");
            return;
        }

        boolean success = currentTrader.tryNegotiateWithSlider(requestedPercent);
        getActiveView().displayNegotiationResult(success, requestedPercent);
    }

    private void registerHandlers() {
        FXGL.getEventBus().addEventHandler(TradeEvent.CHECKOUT, e -> {
            checkout(e.getTrader(), e.getCartItems(), e.getNetCost());
        });
        FXGL.getEventBus().addEventHandler(TradeEvent.NEGOTIATE, e -> {
            negotiate(e.getTrader(), e.getRequestedPercent());
        });
    }
}

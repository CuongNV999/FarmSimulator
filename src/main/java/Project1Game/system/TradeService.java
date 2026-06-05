package Project1Game.system;

import Project1Game.ui.view.shop.TradingView.CartItem;

import Project1Game.ui.view.shop.TradingView;

import Project1Game.component.player.PlayerComponent;
import Project1Game.component.npc.TraderComponent;
import Project1Game.model.Inventory;
import Project1Game.model.InventorySlot;


import java.util.List;

public class TradeService {

    public static class CheckoutResult {
        public final boolean success;
        public final String message;
        public final boolean updateRelationshipState;
        public final boolean successStatus;

        public CheckoutResult(boolean success, String message, boolean updateRelationshipState, boolean successStatus) {
            this.success = success;
            this.message = message;
            this.updateRelationshipState = updateRelationshipState;
            this.successStatus = successStatus;
        }
    }

    public static CheckoutResult executeCheckout(
            Inventory inventory,
            PlayerComponent playerComponent,
            TraderComponent currentTrader,
            List<CartItem> cartItems,
            int netCost) {

        if (cartItems.isEmpty()) {
            return new CheckoutResult(false, "Giỏ hàng của bạn đang trống!", false, false);
        }

        if (currentTrader != null && currentTrader.willRefuseTrade()) {
            return new CheckoutResult(false, "Trader đang bực bội và từ chối giao dịch!", false, false);
        }

        // 1. Kiểm tra không gian kho đồ thông qua giả lập
        InventorySlot[] simulatedSlots = new InventorySlot[inventory.getSlots().length];
        for (int i = 0; i < simulatedSlots.length; i++) {
            simulatedSlots[i] = inventory.getSlots()[i].copy();
        }

        // Giả lập xóa hàng bán khỏi kho đồ
        for (CartItem ci : cartItems) {
            if (!ci.isBuying) {
                int remaining = ci.quantity;
                for (InventorySlot slot : simulatedSlots) {
                    if (slot.getItemType() == ci.itemType) {
                        int count = slot.getCount();
                        if (count >= remaining) {
                            slot.remove(remaining);
                            remaining = 0;
                            break;
                        } else {
                            slot.clear();
                            remaining -= count;
                        }
                    }
                }
                if (remaining > 0) {
                    return new CheckoutResult(false, "Không đủ " + ci.itemType.getDisplayName() + " trong kho đồ!", false, false);
                }
            }
        }

        // Giả lập thêm hàng mua vào kho đồ
        for (CartItem ci : cartItems) {
            if (ci.isBuying) {
                int remaining = ci.quantity;
                // Thử gộp vào slot đã chứa loại hạt giống đó
                for (InventorySlot slot : simulatedSlots) {
                    if (slot.getItemType() == ci.itemType) {
                        slot.add(ci.itemType, remaining);
                        remaining = 0;
                        break;
                    }
                }
                // Nếu chưa gộp hết, thử thêm vào các slot trống
                if (remaining > 0) {
                    for (InventorySlot slot : simulatedSlots) {
                        if (slot.isEmpty()) {
                            slot.add(ci.itemType, remaining);
                            remaining = 0;
                            break;
                        }
                    }
                }
                if (remaining > 0) {
                    return new CheckoutResult(false, "Kho đồ không đủ chỗ trống để chứa " + ci.itemType.getDisplayName() + "!", false, false);
                }
            }
        }

        // 2. Kiểm tra tiền người chơi
        if (netCost > 0) {
            if (playerComponent.getMoney() < netCost) {
                return new CheckoutResult(false, "Bạn không đủ tiền để thực hiện giao dịch!", true, false);
            }
        }

        // 3. Thực thi giao dịch thực tế
        // Xóa hàng bán
        for (CartItem ci : cartItems) {
            if (!ci.isBuying) {
                inventory.removeItem(ci.itemType, ci.quantity);
            }
        }

        // Thêm hàng mua
        for (CartItem ci : cartItems) {
            if (ci.isBuying) {
                inventory.addItem(ci.itemType, ci.quantity);
            }
        }

        // Cộng/Trừ tiền
        if (netCost > 0) {
            playerComponent.removeMoney(netCost);
        } else if (netCost < 0) {
            playerComponent.addMoney(-netCost);
        }

        return new CheckoutResult(true, "Giao dịch thành công!", true, true);
    }
}

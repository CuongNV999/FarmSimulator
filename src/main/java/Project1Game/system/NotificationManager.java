package Project1Game.system;

import com.almasb.fxgl.dsl.FXGL;

public class NotificationManager {

    public static boolean isAllowedNotification(String message) {
        if (message == null) {
            return false;
        }
        String msg = message.toLowerCase();

        // Ăn (Eat)
        if (msg.contains("ăn") || msg.contains("đầy bụng") || msg.contains("đói") || msg.contains("thức ăn")
                || msg.contains("eat")) {
            return true;
        }

        // Mua / Bán / Giao dịch (Buy / Sell / Trade / Negotiate)
        if (msg.contains("giao dịch") || msg.contains("tiền") || msg.contains("mua") ||
                msg.contains("bán") || msg.contains("kho đồ") || msg.contains("giỏ hàng") ||
                msg.contains("trader") || msg.contains("thương lượng") || msg.contains("giá") || msg.contains("shop")) {
            return true;
        }

        // Ngủ (Sleep)
        if (msg.contains("ngủ") || msg.contains("sleep") || msg.contains("hồi phục") || msg.contains("ngon")) {
            return true;
        }

        return false;
    }

    public static void pushNotification(String message) {
        if (isAllowedNotification(message)) {
            FXGL.getNotificationService().pushNotification(message);
        }
    }
}

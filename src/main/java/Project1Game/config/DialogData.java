package Project1Game.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý nội dung hội thoại của tất cả NPC.
 *
 * Quy ước đặt key: "TênNPC.tình_huống"
 *   - "Guider.intro"    : lần đầu gặp
 *   - "Guider.default"  : hội thoại thông thường
 *   - "Trader.default"  : hội thoại thông thường
 *   - "Trader.no_item"  : khi player không có hàng
 *
 * Cách dùng trong Main.java:
 *   nearbyNPCLines = DialogData.getLines("Guider.intro");
 *
 * Cách chọn key theo trường hợp (ví dụ trong collision handler):
 *   String key = hasMetGuider ? "Guider.default" : "Guider.intro";
 *   nearbyNPCLines = DialogData.getLines(key);
 */
public class DialogData {

    private static final Map<String, String[]> DIALOGS = new HashMap<>();

    static {
        // ===================== GUIDER =====================
        DIALOGS.put("Guider.intro", new String[]{
            "Chào mừng đến với nông trại!",
            "Tôi sẽ hướng dẫn bạn cách trồng trọt.",
            "Dùng cuốc (F) để đào đất, rồi trồng hạt giống.",
            "Nhấn E để thu hoạch khi cây đã chín."
        });

        DIALOGS.put("Guider.default", new String[]{
            "Có gì cần giúp không?",
            "Nhớ tưới nước cho cây mỗi ngày nhé!"
        });

        // ===================== TRADER =====================
        DIALOGS.put("Trader.default", new String[]{
            "Chào! Tôi mua bán nông sản.",
            "Mang lúa mì, ngô hay rau củ đến đây nhé."
        });

        DIALOGS.put("Trader.no_item", new String[]{
            "Bạn chưa có gì để bán.",
            "Hãy thu hoạch thêm rồi quay lại!"
        });
    }

    /** Lấy nội dung hội thoại theo key. Trả về ["..."] nếu không tìm thấy. */
    public static String[] getLines(String key) {
        return DIALOGS.getOrDefault(key, new String[]{"..."});
    }

    /** Kiểm tra key có tồn tại không */
    public static boolean has(String key) {
        return DIALOGS.containsKey(key);
    }
}

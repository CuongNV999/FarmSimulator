package Project1Game.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý nội dung hội thoại của tất cả NPC.
 * Để thêm/sửa hội thoại, chỉnh sửa trong class này.
 *
 * Cách dùng:
 *   String[] lines = DialogData.getLines("Guider");
 */
public class DialogData {

    // Map: tên NPC -> mảng các dòng hội thoại
    private static final Map<String, String[]> DIALOGS = new HashMap<>();

    static {
        // ===================== GUIDER =====================
        DIALOGS.put("Guider", new String[]{
            "Chào mừng đến với nông trại!",
            "Dùng cuốc (F) để đào đất, rồi trồng hạt giống.",
            "Nhấn E để thu hoạch khi cây đã chín."
        });

        // ===================== TRADER =====================
        DIALOGS.put("Trader", new String[]{
            "Chào! Tôi mua bán nông sản.",
            "Mang lúa mì, ngô hay rau củ đến đây nhé."
        });
    }

    /** Lấy nội dung hội thoại theo tên NPC. Trả về mảng rỗng nếu không tìm thấy. */
    public static String[] getLines(String npcName) {
        return DIALOGS.getOrDefault(npcName, new String[]{"..."});
    }

    /** Kiểm tra NPC có hội thoại không */
    public static boolean has(String npcName) {
        return DIALOGS.containsKey(npcName);
    }
}

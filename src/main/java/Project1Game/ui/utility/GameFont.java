package Project1Game.ui.utility;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameFont {
    public static String GAME_FONT = "Arial";

    static {
        try {
            java.io.InputStream stream = GameFont.class.getResourceAsStream("/assets/font/SHPinscher-Regular.otf");
            if (stream != null) {
                Font loaded = Font.loadFont(stream, 12);
                if (loaded != null) {
                    GAME_FONT = loaded.getFamily();
                    System.out.println("--- [GameFont] Loaded custom font: " + loaded.getName() + " (Family: "
                            + GAME_FONT + ") ---");
                } else {
                    System.err.println("--- [GameFont] Font loading returned null! ---");
                }
            } else {
                System.err.println("--- [GameFont] Font resource stream is null! ---");
            }
        } catch (Exception e) {
            System.err.println("--- [GameFont] Error loading font: " + e.getMessage() + " ---");
            e.printStackTrace();
        }
    }

    public static Font font(double size) {
        return Font.font(GAME_FONT, size);
    }

    public static Font font(FontWeight weight, double size) {
        return Font.font(GAME_FONT, weight, size);
    }
}

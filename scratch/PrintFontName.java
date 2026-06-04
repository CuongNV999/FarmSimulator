import javafx.scene.text.Font;
import java.io.File;
import java.io.FileInputStream;

public class PrintFontName {
    public static void main(String[] args) {
        try {
            File file = new File("src/main/resources/assets/font/VCR_OSD_MONO_1.001.ttf");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                Font font = Font.loadFont(fis, 12);
                if (font != null) {
                    System.out.println("FONT_FAMILY:" + font.getFamily());
                    System.out.println("FONT_NAME:" + font.getName());
                } else {
                    System.out.println("Font failed to load (returned null)");
                }
            } else {
                System.out.println("Font file not found at: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}

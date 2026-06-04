import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class AnalyzeAppleTexture {
    public static void main(String[] args) throws Exception {
        String base = "d:/GameDevelop/Project1/src/main/resources/assets/textures/Crops/NewCrops/apple/";
        String[] files = {"seed.png", "stage1.png", "stage2.png", "stage3.png", "harvest.png"};
        for (String d : files) {
            File f = new File(base + d);
            if (f.exists()) {
                BufferedImage img = ImageIO.read(f);
                int bg = img.getRGB(0, 0);
                System.out.printf("%s size %dx%d, top-left color: %08x\n", d, img.getWidth(), img.getHeight(), bg);
            }
        }
    }
}

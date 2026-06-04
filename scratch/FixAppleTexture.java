import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class FixAppleTexture {
    public static void main(String[] args) throws Exception {
        String dir = "d:/GameDevelop/Project1/src/main/resources/assets/textures/Crops/NewCrops/apple/";
        String[] files = {"seed.png", "stage1.png", "stage2.png", "stage3.png", "harvest.png"};
        for (String f : files) {
            File in = new File(dir + f);
            if (!in.exists()) {
                System.out.println("File not found: " + f);
                continue;
            }
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                System.out.println("Failed to read: " + f);
                continue;
            }
            
            // Assume the top-left pixel is the background color
            int bg = img.getRGB(0, 0);
            System.out.println(f + " bg color: " + Integer.toHexString(bg));
            
            // If the background is already fully transparent, skip
            if ((bg >> 24) == 0) {
                System.out.println("Background already transparent for " + f);
                // Actually, let's still check in case there is some green. 
                // Let's define the grass green we want to remove.
            }
            
            BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int replaced = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int p = img.getRGB(x, y);
                    // Match exact background color, OR match common green grass colors if alpha is solid
                    // Let's just remove the exact top-left pixel color if it is opaque.
                    if (p == bg && ((bg >> 24) & 0xff) > 0) {
                        out.setRGB(x, y, 0x00000000);
                        replaced++;
                    } else {
                        out.setRGB(x, y, p);
                    }
                }
            }
            ImageIO.write(out, "png", in);
            System.out.println("Processed " + f + ", replaced " + replaced + " pixels.");
        }
    }
}

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class DrawAndAnalyzeCircle {
    public static void main(String[] args) throws Exception {
        int width = 400, height = 400;

        // 1. Create a blank white image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // 2. Draw a red circle
        g2d.setColor(Color.RED);
        int circleDiameter = 200;
        int x = (width - circleDiameter) / 2;
        int y = (height - circleDiameter) / 2;
        g2d.drawOval(x, y, circleDiameter, circleDiameter);
        g2d.fillOval(x, y, circleDiameter, circleDiameter);
        g2d.dispose();

        // 3. Save the drawn circle
        ImageIO.write(image, "PNG", new File("drawn_circle.png"));
        System.out.println("Circle drawn and saved to 'drawn_circle.png'.");
    }
}
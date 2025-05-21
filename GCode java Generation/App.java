import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException {
        // Load image
        BufferedImage image = ImageIO.read(new File("drawn_circle.png"));
        int width = image.getWidth();
        int height = image.getHeight();

        // Convert to binary (black/white)
        BufferedImage binary = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = binary.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // Detect drawn pixels (non-white)
        List<Point> drawnPixels = new ArrayList<>();
        boolean wasWhite = true;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (binary.getRGB(x, y) != Color.WHITE.getRGB()) {
                    drawnPixels.add(new Point(x, y));
                    wasWhite = false;
                } else {
                    if (!wasWhite) {
                        // System.out.println("Pixel at (" + x + ", " + y + ") is white.");
                        // System.out.println("LIFT");
                    }
                    wasWhite = true;
                }
            }
        }

        // Highlight detected pixels (for visualization)
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = output.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.setColor(Color.GREEN);
        for (Point p : drawnPixels) {
            output.setRGB(p.x, p.y, Color.GREEN.getRGB());
        }
        g2.dispose();

        // Save result
        ImageIO.write(output, "PNG", new File("detected_circle_java.png"));
        generateGCode(drawnPixels, "output.gcode");
        System.out.println("Detected pixels saved to 'detected_circle_java.png'.");
        System.out.println("Done! Detected " + drawnPixels.size() + " drawn pixels.");

    }

    public static String generateGCode(List<Point> drawnPixels, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write("G21 ; Set units to millimeters\n");
            writer.write("G90 ; Use absolute positioning\n");
            writer.write("G0 Z5 ; Lift pen/tool\n");
    
            Point lastPoint = null;
            boolean penDown = false;
    
            for (int i = 0; i < drawnPixels.size(); i++) {
                Point currentPoint = drawnPixels.get(i);
    
                // If the current point is not adjacent to the last point, lift the pen and move
                if (lastPoint == null || !isAdjacent(lastPoint, currentPoint)) {
                    if (penDown) {
                        writer.write("G0 Z5\n");
                        penDown = false;
                    }
                    writer.write(String.format("G0 X%d Y%d\n", currentPoint.x, currentPoint.y));
                    writer.write("G0 Z0\n");
                    penDown = true;
                }
    
                // Check for collinear points and group them
                while (i + 1 < drawnPixels.size() && isCollinear(lastPoint, currentPoint, drawnPixels.get(i + 1))) {
                    currentPoint = drawnPixels.get(++i); // Move to the next point in the line
                }
    
                // Draw to the current point
                writer.write(String.format("G1 X%d Y%d \n", currentPoint.x, currentPoint.y));
                lastPoint = currentPoint;
            }
    
            // Lift the pen at the end
            if (penDown) {
                writer.write("G0 Z5 ; Lift pen/tool\n");
            }
        }
        System.out.println("G-code generated and saved to " + outputFilePath);
        return outputFilePath;
    }
    
    private static boolean isAdjacent(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) <= 1 && Math.abs(p1.y - p2.y) <= 1;
    }
    
    private static boolean isCollinear(Point p1, Point p2, Point p3) {
        if (p1 == null || p2 == null || p3 == null) return false;
        // Check if three points are in a straight line using the slope formula
        return (p2.y - p1.y) * (p3.x - p2.x) == (p3.y - p2.y) * (p2.x - p1.x);
    }
}

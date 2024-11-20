package org.sortingalgorithms;

import javafx.scene.Node;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SaveImage {

    public static void saveChartAsImage(Node chart) {
        // Capture the chart as a WritableImage
        WritableImage writableImage = chart.snapshot(null, null);

        // Convert the WritableImage to a BufferedImage
        BufferedImage bufferedImage = convertToBufferedImage(writableImage);

        // Use FileChooser to prompt the user to select where to save the image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                // Save the BufferedImage to the selected file as PNG
                ImageIO.write(bufferedImage, "PNG", file);
                System.out.println("Chart saved successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("hello world");
            }
        }
    }

    // Convert WritableImage to BufferedImage
    public static BufferedImage convertToBufferedImage(WritableImage writableImage) {
        // Get the width and height of the WritableImage
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        // Create a new BufferedImage
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Get the PixelReader from the WritableImage
        PixelReader pixelReader = writableImage.getPixelReader();

        // Loop through the pixels and transfer the pixel data to the BufferedImage
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                javafx.scene.paint.Color color = pixelReader.getColor(x, y);

                // Convert Color to ARGB (Alpha, Red, Green, Blue)
                int alpha = (int) (color.getOpacity() * 255); // Alpha channel (opacity)
                int red = (int) (color.getRed() * 255);       // Red channel
                int green = (int) (color.getGreen() * 255);   // Green channel
                int blue = (int) (color.getBlue() * 255);     // Blue channel

                // Combine ARGB into a single integer (BufferedImage uses this format)
                int argb = (alpha << 24) | (red << 16) | (green << 8) | blue;

                // Set the pixel in the BufferedImage
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }
}

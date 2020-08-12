/*
 * From Stack Overflow (https://stackoverflow.com/questions/8933893/convert-each-animated-gif-frame-to-a-separate-bufferedimage)
 */
package cml.lib.animation;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;

/**
 * One frame of a GIF animation. Stores data such as the frame's image, duration,
 * disposal method, and size.
 * @author Alex Orzechowski
 */
class GifImageFrame extends AnimationFrame {

    private final String disposal;
    private final int width, height;
    
    GifImageFrame(BufferedImage image, int delay, String disposal, int width, int height) {
        super(SwingFXUtils.toFXImage(image, null), delay * 10);
        this.disposal = disposal;
        this.width = width;
        this.height = height;
    }

    /**
     * 
     * @return The GIF disposal method that this frame uses.
     * @see Disposal methods
     * <a href="http://www.theimage.com/animation/pages/disposal.html">pg. 1</a>,
     * <a href="http://www.theimage.com/animation/pages/disposal2.html">pg. 2</a>
     */
    public String getDisposal() {
        return disposal;
    }

    /**
     * 
     * @return The width of this frame.
     */
    public int getWidth() {
        return width;
    }

    /**
     * 
     * @return The height of this frame.
     */
    public int getHeight() {
        return height;
    }
}

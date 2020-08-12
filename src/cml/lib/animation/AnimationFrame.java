/*
 * Copyright (C) 2020 benne
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cml.lib.animation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
public class AnimationFrame {
    
    private final Image image;
    private final long durationMs;

    public AnimationFrame(Image image, long durationMs) {
        this.image = image;
        this.durationMs = durationMs;
    }
    
    public Image getImage() {
        return image;
    }
    
    public long getDuration() {
        return durationMs;
    }
    
    public void await() throws InterruptedException {
        Thread.sleep(durationMs);
    }
    
    
    public static AnimationFrame[] fromSprites(String name, long frameDuration) {
        List<AnimationFrame> frames = new ArrayList();
        int dotIndex = name.lastIndexOf(".");
        for (int i = 1; ; i++) {
            try {
                frames.add(new AnimationFrame(new Image(AnimationFrame.class.getClassLoader().getResourceAsStream(name.substring(0, dotIndex) + i + name.substring(dotIndex))), frameDuration));
            } catch (NullPointerException ex) {
                break;
            }
        }
        return frames.toArray(new AnimationFrame[0]);
    }
    
    public static AnimationFrame[] fromGif(InputStream gif) throws IOException {
        return GifDecoder.readGif(gif);
    }
    
}

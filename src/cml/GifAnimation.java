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
package cml;

import cml.lib.files.gif.GifDecoder;
import cml.lib.files.gif.ImageFrame;
import cml.lib.threadmanager.ThreadManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author benne
 */
public class GifAnimation {
    
    private static final Logger LOGGER = Logger.getLogger(GifAnimation.class.getName());
    
    private static final boolean DO_LOOP = true;
    
    private final List<Thread> threads = new ArrayList();

    private ImageView imageView;
    private final ImageFrame[] frames;

    private final Runnable animateRunnable = new Runnable() {
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            for (ImageFrame frame : frames) {
                if (!Thread.currentThread().isInterrupted()) {
                    imageView.setImage(SwingFXUtils.toFXImage(frame.getImage(), null));
                    try {
                        Thread.sleep(frame.getDelay() * 10);
                    }catch (InterruptedException ex) {
                        threads.remove(Thread.currentThread());
                        LOGGER.log(Level.FINEST, "Stopped animation on interrupt");
                        return;
                    }
                } else {
                    return;
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                imageView.setImage(getImage());
                threads.remove(Thread.currentThread());
                return;
            }
            if (DO_LOOP) {
                Thread animationThread = new Thread(animateRunnable);
                ThreadManager.removeThread(Thread.currentThread());
                ThreadManager.addThread(animationThread);
                threads.add(animationThread);
                animationThread.start();
            }
        }
    };

    public GifAnimation(InputStream gif) throws IOException {
        this.frames = GifDecoder.readGif(gif);
        LOGGER.log(Level.FINER, "New animation: [{0}, gif]", frames.length);
    }

    public void animate(ImageView imageView) {
        this.imageView = imageView;
        Thread animationThread = new Thread(animateRunnable);
        ThreadManager.addThread(animationThread);
        threads.add(animationThread);
        animationThread.start();
    }

    public boolean isAnimated() {
        return threads.size() > 0;
    }

    private boolean halting = false;

    public void halt() {
        if (!halting) {
            halting = true;
            threads.forEach((thread) -> {
                thread.interrupt();
            });
            halting = false;
        }
    }

    public Image getImage() {
        return SwingFXUtils.toFXImage(frames[0].getImage(), null);
    }
    
}

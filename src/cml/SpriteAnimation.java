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

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 *
 * @author benne
 */
public class SpriteAnimation {

    public static final List<Thread> THREADS = new ArrayList();
    private final List<Thread> threads = new ArrayList();

    private ImageView imageView;
    private final List<Image> frames;
    private final Duration frameDuration;
    private final Runnable onFinish;

    private final Runnable animateRunnable = new Runnable() {
        @Override
        public void run() {
            for (Image frame : frames) {
                if (!Thread.interrupted()) {
                    imageView.setImage(frame);
                    try {
                        Thread.sleep((int) frameDuration.toMillis());
                    } catch (InterruptedException ex) {
                        System.out.println("Stopped animation on interrupt");
                        if (onFinish != null) {
                            onFinish.run();
                        }
                    }
                } else {
                    break;
                }
            }
            threads.remove(Thread.currentThread());
            THREADS.remove(Thread.currentThread());
            if (onFinish != null) {
                onFinish.run();
            }
        }
    };

    public SpriteAnimation(List<Image> frames, Duration frameDuration) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.onFinish = null;
        System.out.println("New animation: [" + frames.size() + ", " + frameDuration.toMillis() + "ms]");
    }
    
    public SpriteAnimation(List<Image> frames, Duration frameDuration, Runnable onFinish) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.onFinish = onFinish;
        System.out.println("New animation: [" + frames.size() + ", " + frameDuration.toMillis() + "ms]");
    }

    public void animate(ImageView imageView) {
        this.imageView = imageView;
        Thread animationThread = new Thread(animateRunnable);
        THREADS.add(animationThread);
        threads.add(animationThread);
        animationThread.start();
    }
    
    public boolean isAnimated() {
        return threads.size() > 0;
    }
    
    public void halt() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    public List<Image> getFrames() {
        return frames;
    }
}

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

import cml.lib.threadmanager.ThreadManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 *
 * @author benne
 */
public class SpriteAnimation {

    private static final Logger LOGGER = Logger.getLogger(SpriteAnimation.class.getName());
    private final List<Thread> threads = new ArrayList();

    private ImageView imageView;
    private final List<Image> frames;
    private final Duration frameDuration;
    private final Runnable onFinish;
    private final boolean repeat;
    private final Image imageOnHalt;

    private final Runnable animateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean interrupted = false;
            for (Image frame : frames) {
                if (!Thread.currentThread().isInterrupted()) {
                    imageView.setImage(frame);
                    try {
                        Thread.sleep((int) frameDuration.toMillis());
                    } catch (InterruptedException ex) {
                        interrupted = true;
                        imageView.setImage(imageOnHalt);
                        LOGGER.log(Level.FINEST, "Stopped animation on interrupt");
                        break;
                    }
                } else {
                    break;
                }
            }
            threads.remove(Thread.currentThread());
            if (onFinish != null) {
                onFinish.run();
            }
            if (interrupted) {
                return;
            }
            if (Thread.currentThread().isInterrupted()) {
                imageView.setImage(imageOnHalt);
                return;
            }
            if (repeat) {
                Thread animationThread = new Thread(animateRunnable);
                ThreadManager.removeThread(Thread.currentThread());
                ThreadManager.addThread(animationThread);
                threads.add(animationThread);
                animationThread.start();
            }
        }
    };

    public SpriteAnimation(List<Image> frames, Duration frameDuration) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.onFinish = null;
        this.repeat = false;
        this.imageOnHalt = null;
        LOGGER.log(Level.FINER, "New animation: [{0}, {1}ms]", new Object[]{frames.size(), frameDuration.toMillis()});
    }

    public SpriteAnimation(List<Image> frames, Duration frameDuration, Runnable onFinish) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.onFinish = onFinish;
        this.repeat = false;
        this.imageOnHalt = null;
        LOGGER.log(Level.FINER, "New animation: [{0}, {1}ms]", new Object[]{frames.size(), frameDuration.toMillis()});
    }

    public SpriteAnimation(List<Image> frames, Duration frameDuration, boolean repeat, Image imageOnHalt) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.onFinish = null;
        this.repeat = repeat;
        this.imageOnHalt = imageOnHalt;
        LOGGER.log(Level.FINER, "New animation: [{0}, {1}ms]", new Object[]{frames.size(), frameDuration.toMillis()});
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
            for (Thread thread : threads) {
                thread.interrupt();
            }
            halting = false;
        }
    }

    public List<Image> getFrames() {
        return frames;
    }
}

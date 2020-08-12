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

import cml.lib.threadmanager.ThreadManager;
import javafx.scene.image.ImageView;

/**
 *
 * @author benne
 */
class AnimationImpl {
    
    private final Runnable animateRunnable = new Runnable() {
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            for (AnimationFrame frame : anim.getFrames()) {
                if (!Thread.currentThread().isInterrupted()) {
                    iv.setImage(frame.getImage());
                    try {
                        frame.await();
                    } catch (InterruptedException ex) {
                        halt = true;
                    }
                } else {
                    halt = true;
                }
            }
            if (!(Thread.currentThread().isInterrupted() || halt) && anim.doLoop()) {
                this.run();
            }
            iv.setImage(anim.getDefaultImage());
            halt = true;
        }
    };
    
    boolean halt = false;
    final Animation anim;
    final ImageView iv;
    
    public AnimationImpl(Animation anim, ImageView iv) {
        this.anim = anim;
        this.iv = iv;
    }
    
    /**
     * Resets the halt state of this animation, then starts the animation on a new thread.
     */
    void animate() {
        halt = false;
        ThreadManager.MANAGER.executor.submit(animateRunnable);
    }

    void halt() {
        this.halt = true;
    }
    
    boolean isHalted() {
        return halt;
    }
    
    ImageView getImageView() {
        return iv;
    }
}

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

import java.util.HashSet;
import java.util.Set;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author benne
 */
public class Animation {
    
    private final Set<AnimationImpl> instances = new HashSet();
    
    private final AnimationFrame[] frames;
    private final boolean doLoop;
    private final Image defaultImage;
    
    public Animation(AnimationFrame[] frames) {
        this(frames, true);
    }
    public Animation(AnimationFrame[] frames, boolean doLoop) {
        this(frames, doLoop, frames[0].getImage());
    }
    public Animation(AnimationFrame[] frames, Image defaultImage) {
        this(frames, true, defaultImage);
    }
    public Animation(AnimationFrame[] frames, boolean doLoop, Image defaultImage) {
        this.frames = frames;
        this.doLoop = doLoop;
        this.defaultImage = defaultImage;
    }
    
    /**
     * 
     * @return This animation's {@link AnimationFrame}<code>s</code>
     */
    public AnimationFrame[] getFrames() {
        return frames;
    }
    /**
     * 
     * @return Whether this animation loops indefinitely.
     */
    public boolean doLoop() {
        return doLoop;
    }
    /**
     * 
     * @return This animation's default image
     */
    public Image getDefaultImage() {
        return defaultImage;
    }
    
    //
    
    /**
     * Animates the GIF into an <code>ImageView</code>.
     *
     * @param imageView The <code>ImageView</code> to animate into.
     */
    public void animate(ImageView imageView) {
        AnimationImpl instance = instances.stream().filter((anim) -> anim.getImageView().equals(imageView)).findAny().orElse(null);
        if (instance == null) {
            AnimationImpl animation = new AnimationImpl(this, imageView);
            instances.add(animation);
            animation.animate();
        } else if (instance.isHalted()) {
            instance.animate();
        } else {
            //ImageView is already being animated.
        }
    }

    /**
     * 
     * @return Whether any instances of this animation exist and are actively animating.
     */
    public boolean isAnimated() {
        if (instances.size() > 0) {
            return instances.stream().anyMatch((anim) -> !anim.isHalted());
        }
        return false;
    }

    /**
     * 
     * @param imageView The {@link ImageView} to query.
     * @return Whether this imageView is being actively animated by this animation.
     */
    public boolean isAnimated(ImageView imageView) {
        return instances.stream().anyMatch((anim) -> !anim.isHalted() && anim.getImageView().equals(imageView));
    }

    /**
     * Halts all instances of this animation.
     */
    public void haltInstances() {
        instances.forEach((instance) -> {
            instance.halt();
        });
    }
    
    /**
     * Halts this animation on the specified {@link ImageView}
     * @param iv The specified {@link ImageView}
     */
    public void halt(ImageView iv) {
        instances.stream().filter((anim) -> !anim.isHalted() && anim.getImageView().equals(iv)).forEach(AnimationImpl::halt);
    }
}

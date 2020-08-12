/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.lib.animation.Animation;
import cml.lib.animation.AnimationFrame;
import javafx.scene.image.Image;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Media {

    public static final javafx.scene.media.Media LAUNCH_SOUND;
    public static final Animation CONVERT_SELECT_ANIM;
    public static final Animation CONVERT_PRESS_ANIM;
    public static final Image CONVERT;
    public static final Image SCROLL;
    public static final Image SCROLL_SELECT;
    public static final Image SCROLL_PRESS;
    public static final Image SCROLL_DISABLE;
    public static final Image SCROLL_B;
    public static final Image SCROLL_B_SELECT;
    public static final Image SCROLL_B_PRESS;
    public static final Image SCROLL_B_DISABLE;
    public static final Image CHECK;
    public static final Image X;
    public static final Image PLUS;
    public static final Image MINUS;
    public static final Image DOT;

    static {
        LAUNCH_SOUND = new javafx.scene.media.Media(Media.class.getClassLoader().getResource("media/Sound/launchCML.mp3").toString());
        CONVERT = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/ConvertMod/Button.png"));
        CONVERT_SELECT_ANIM = new Animation(AnimationFrame.fromSprites("media/MultiStateIcons/ConvertMod/ButtonSelect.png", 250), true, CONVERT);
        CONVERT_PRESS_ANIM = new Animation(AnimationFrame.fromSprites("media/MultiStateIcons/ConvertMod/ButtonPress.png", 500), false);
        SCROLL = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/Slider.png"));
        SCROLL_SELECT = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderSelect.png"));
        SCROLL_PRESS = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderHeld.png"));
        SCROLL_DISABLE = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderDisabled.png"));
        SCROLL_B = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButton.png"));
        SCROLL_B_SELECT = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonSelect.png"));
        SCROLL_B_PRESS = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonPressed.png"));
        SCROLL_B_DISABLE = new Image(Media.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonDisabled.png"));
        CHECK = new Image(Media.class.getClassLoader().getResourceAsStream("media/PopoverTypes/Check.png"));
        X = new Image(Media.class.getClassLoader().getResourceAsStream("media/PopoverTypes/X.png"));
        PLUS = new Image(Media.class.getClassLoader().getResourceAsStream("media/PopoverTypes/Plus.png"));
        MINUS = new Image(Media.class.getClassLoader().getResourceAsStream("media/PopoverTypes/Minus.png"));
        DOT = new Image(Media.class.getClassLoader().getResourceAsStream("media/PopoverTypes/Dot.png"));
    }
}

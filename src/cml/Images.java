/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.media.Media;
import javafx.util.Duration;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Images {
    
    private static final Logger LOGGER = Logger.getLogger(Images.class.getName());

    public static final Background BACKGROUND;
    public static final Media LAUNCH_SOUND;
    public static final ImageView HEADER;
    public static final SpriteAnimation CONVERT_SELECT_ANIM;
    public static final SpriteAnimation CONVERT_PRESS_ANIM;
    public static final Image CONVERT;
    public static final Image SCROLL;
    public static final Image SCROLL_SELECT;
    public static final Image SCROLL_PRESS;
    public static final Image SCROLL_DISABLE;
    public static final Image SCROLL_B;
    public static final Image SCROLL_B_SELECT;
    public static final Image SCROLL_B_PRESS;
    public static final Image SCROLL_B_DISABLE;

    static {
        BACKGROUND = new Background(new BackgroundImage(new Image(Images.class.getClassLoader().getResourceAsStream("media/AdaptableBackground/Background.jpg")), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT));
        LAUNCH_SOUND = new Media(Images.class.getClassLoader().getResource("media/Sound/launchCML.mp3").toString());
        HEADER = new ImageView(new Image(Images.class.getClassLoader().getResourceAsStream("media/AdaptableBackground/Header.jpg")));
        CONVERT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/ConvertMod/Button.png"));
        List<Image> convertSelectFrames = new ArrayList();
        int i = 1;
        while (true) {
            try {
                convertSelectFrames.add(new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/ConvertMod/ButtonSelect" + i + ".png")));
            } catch (NullPointerException ex) {
                break;
            }
            i++;
        }
        CONVERT_SELECT_ANIM = new SpriteAnimation(convertSelectFrames, Duration.millis(250), true, CONVERT);
        List<Image> convertPressFrames = new ArrayList();
        i = 1;
        while (true) {
            try {
                convertPressFrames.add(new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/ConvertMod/ButtonPress" + i + ".png")));
            } catch (NullPointerException ex) {
                break;
            }
            i++;
        }
        CONVERT_PRESS_ANIM = new SpriteAnimation(convertPressFrames, Duration.millis(500));
        SCROLL = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/Slider.png"));
        SCROLL_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderSelect.png"));
        SCROLL_PRESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderHeld.png"));
        SCROLL_DISABLE = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderDisabled.png"));
        SCROLL_B = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButton.png"));
        SCROLL_B_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonSelect.png"));
        SCROLL_B_PRESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonPressed.png"));
        SCROLL_B_DISABLE = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonDisabled.png"));
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.beans.Profile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
    public static final SpriteAnimation DELETE_SELECT_ANIM;
    public static final SpriteAnimation DELETE_PRESS_ANIM;
    public static final Image DELETE_PRESS_START;
    public static final Image DELETE_PRESS_FINAL;
    public static final SpriteAnimation CONVERT_SELECT_ANIM;
    public static final SpriteAnimation CONVERT_PRESS_ANIM;
    public static final Image CONVERT;
    public static final Image ICON;
    public static final Image ICON_ERROR;
    public static final Image ICON_SUCCESS;
    public static final Image BLANK;
    public static final Image LAUNCH;
    public static final Image LAUNCH_SELECT;
    public static final Image LAUNCH_PRESS;
    public static final Image LAUNCH_ERROR;
    public static final Image LAUNCH_SUCCESS;
    public static final Image SETTINGS;
    public static final Image SETTINGS_SELECT;
    public static final Image SETTINGS_PRESS;
    public static final Image OPEN;
    public static final Image OPEN_SELECT;
    public static final Image OPEN_PRESS;
    public static final Image DELETE;
    public static final Image SCROLL;
    public static final Image SCROLL_SELECT;
    public static final Image SCROLL_PRESS;
    public static final Image SCROLL_DISABLE;
    public static final Image SCROLL_B;
    public static final Image SCROLL_B_SELECT;
    public static final Image SCROLL_B_PRESS;
    public static final Image SCROLL_B_DISABLE;
    public static final Image ENABLER_ON;
    public static final Image ENABLER_ON_SELECT;
    public static final Image ENABLER_BOTH;
    public static final Image ENABLER_OFF;
    public static final Image ENABLER_OFF_SELECT;
    public static final Image WORKSHOP;
    public static final Image WORKSHOP_SELECT;
    public static final Image WORKSHOP_PRESS;

    static {
        BACKGROUND = new Background(new BackgroundImage(new Image(Images.class.getClassLoader().getResourceAsStream("media/AdaptableBackground/Background.jpg")), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT));
        LAUNCH_SOUND = new Media(Images.class.getClassLoader().getResource("media/Sound/launchCML.mp3").toString());
        HEADER = new ImageView(new Image(Images.class.getClassLoader().getResourceAsStream("media/AdaptableBackground/Header.jpg")));
        int i = 1;
        List<Image> deleteSelectFrames = new ArrayList();
        while (true) {
            try {
                deleteSelectFrames.add(new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Delete/ButtonSelect" + i + ".png")));
            } catch (NullPointerException ex) {
                break;
            }
            i++;
        }
        DELETE_SELECT_ANIM = new SpriteAnimation(deleteSelectFrames, Duration.millis(100));
        List<Image> deletePressFrames = new ArrayList();
        i = 1;
        while (true) {
            try {
                deletePressFrames.add(new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Delete/ButtonPressed" + i + ".png")));
            } catch (NullPointerException ex) {
                break;
            }
            i++;
        }
        DELETE_PRESS_ANIM = new SpriteAnimation(deletePressFrames, Duration.millis(100), () -> {
            Profile checkMe = Main.activeProfile.get();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, "On Delete Finish interrupted");
            }
            if (!checkMe.getDirectory().exists()) {
                Platform.runLater(() -> Main.activeProfile.setValue(Profile.DELETED));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO, "On Delete Finish interrupted");
                }
                Platform.runLater(() -> Main.updateProfileList());
            }
        });
        DELETE_PRESS_START = DELETE_PRESS_ANIM.getFrames().get(0);
        DELETE_PRESS_FINAL = DELETE_PRESS_ANIM.getFrames().get(DELETE_PRESS_ANIM.getFrames().size() - 1);
        CONVERT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/ConvertMod/Button.png"));
        List<Image> convertSelectFrames = new ArrayList();
        i = 1;
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
        ICON = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Icon/CML.ico.png"));
        ICON_ERROR = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Icon/CML Error.ico.png"));
        ICON_SUCCESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Icon/CML Success.ico.png"));
        BLANK = new Image(Images.class.getClassLoader().getResourceAsStream("media/Blank.png"));
        LAUNCH = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Launch/Launch.jpg"));
        LAUNCH_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Launch/LaunchSelect.jpg"));
        LAUNCH_PRESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Launch/LaunchPressed.jpg"));
        LAUNCH_ERROR = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Launch/LaunchError.jpg"));
        LAUNCH_SUCCESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Launch/LaunchSuccess.jpg"));
        SETTINGS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Properties/Button.png"));
        SETTINGS_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Properties/ButtonSelect.png"));
        SETTINGS_PRESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Properties/ButtonPressed.png"));
        WORKSHOP = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Workshop/Button.png"));
        WORKSHOP_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Workshop/ButtonSelect.png"));
        WORKSHOP_PRESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Workshop/ButtonPressed.png"));
        OPEN = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/OpenFolder/Button.png"));
        OPEN_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/OpenFolder/ButtonSelect.png"));
        OPEN_PRESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/OpenFolder/ButtonPressed.png"));
        DELETE = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Delete/Button.png"));
        SCROLL = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/Slider.png"));
        SCROLL_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderSelect.png"));
        SCROLL_PRESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderHeld.png"));
        SCROLL_DISABLE = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderDisabled.png"));
        SCROLL_B = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButton.png"));
        SCROLL_B_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonSelect.png"));
        SCROLL_B_PRESS = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonPressed.png"));
        SCROLL_B_DISABLE = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonDisabled.png"));
        ENABLER_ON = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/Enabled.png"));
        ENABLER_ON_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/EnabledSelect.png"));
        ENABLER_BOTH = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/Both.png"));
        ENABLER_OFF = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/Disabled.png"));
        ENABLER_OFF_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/DisabledSelect.png"));
    }
}

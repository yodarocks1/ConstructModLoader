/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cml;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.media.Media;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Images {
    public static final Background BACKGROUND;
    public static final Media LAUNCH_SOUND;
    public static final ImageView HEADER;
    public static final Image ICON;
    public static final Image BLANK;
    public static final Image LAUNCH;
    public static final Image LAUNCH_SELECT;
    public static final Image LAUNCH_PRESS;
    public static final Image SETTINGS;
    public static final Image SETTINGS_SELECT;
    public static final Image SETTINGS_PRESS;
    public static final Image PROFILE;
    public static final Image PROFILE_SELECT;
    public static final Image PROFILE_PRESS;
    public static final Image PROFILES;
    public static final Image PROFILES_SELECT;
    public static final Image PROFILES_PRESS;
    public static final Image BACK;
    public static final Image BACK_SELECT;
    public static final Image BACK_PRESS;
    public static final Image OPEN;
    public static final Image OPEN_SELECT;
    public static final Image OPEN_PRESS;
    public static final Image DELETE;
    public static final Image DELETE_SELECT_ANIM;
    public static final Image DELETE_PRESS_ANIM;
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
    
    static {
        BACKGROUND = new Background(new BackgroundImage(new Image(Images.class.getClassLoader().getResourceAsStream("media/AdaptableBackground/Background.jpg")), BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT));
        LAUNCH_SOUND = new Media(Images.class.getClassLoader().getResource("media/Sound/launchCML.mp3").toString());
        HEADER = new ImageView(new Image(Images.class.getClassLoader().getResourceAsStream("media/AdaptableBackground/Header.jpg")));
        ICON =              new Image(Images.class.getClassLoader().getResourceAsStream("media/CML.ico"));
        BLANK =             new Image(Images.class.getClassLoader().getResourceAsStream("media/Blank.png"));
        LAUNCH =            new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Launch/Launch.jpg"));
        LAUNCH_SELECT =     new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Launch/LaunchSelect.jpg"));
        LAUNCH_PRESS =      new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Launch/LaunchPressed.jpg"));
        SETTINGS =          new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Properties/Button.png"));
        SETTINGS_SELECT =   new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Properties/ButtonSelect.png"));
        SETTINGS_PRESS =    new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Properties/ButtonPressed.png"));
        PROFILE =           new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Profile/Button.png"));
        PROFILE_SELECT =    new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Profile/ButtonSelect.png"));
        PROFILE_PRESS =     new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Profile/ButtonPressed.png"));
        PROFILES =          new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Profiles/Button.png"));
        PROFILES_SELECT =   new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Profiles/ButtonSelect.png"));
        PROFILES_PRESS =    new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Profiles/ButtonPressed.png"));
        BACK =              new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/GoBack/Button.png"));
        BACK_SELECT =       new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/GoBack/ButtonSelect.png"));
        BACK_PRESS =        new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/GoBack/ButtonPressed.png"));
        OPEN =              new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/OpenFolder/Button.png"));
        OPEN_SELECT =       new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/OpenFolder/ButtonSelect.png"));
        OPEN_PRESS =        new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/OpenFolder/ButtonPressed.png"));
        DELETE =            new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Delete/Button.png"));
        DELETE_SELECT_ANIM =new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Delete/ButtonSelect.gif"));
        DELETE_PRESS_ANIM = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Delete/ButtonPressed.gif"));
        SCROLL =            new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/Slider.png"));
        SCROLL_SELECT =     new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderSelect.png"));
        SCROLL_PRESS =      new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderHeld.png"));
        SCROLL_DISABLE =    new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderDisabled.png"));
        SCROLL_B =          new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButton.png"));
        SCROLL_B_SELECT =   new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonSelect.png"));
        SCROLL_B_PRESS =    new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonPressed.png"));
        SCROLL_B_DISABLE =  new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Slider/SliderButtonDisabled.png"));
        ENABLER_ON =        new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/Enabled.png"));
        ENABLER_ON_SELECT = new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/EnabledSelect.png"));
        ENABLER_BOTH =      new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/Both.png"));
        ENABLER_OFF =       new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/Disabled.png"));
        ENABLER_OFF_SELECT =new Image(Images.class.getClassLoader().getResourceAsStream("media/MultiStateIcons/Enabled/DisabledSelect.png"));
    }
}

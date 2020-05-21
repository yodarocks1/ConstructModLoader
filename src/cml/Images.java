/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Images {
    public static final String BACKGROUND_IMAGE = Constants.CONSTRUCT_FOLDER + "GUI\\Background.png";
    public static final String BUTTON_IMAGE = Constants.CONSTRUCT_FOLDER + "GUI\\Button.png";
    public static final String BUTTON_SELECT_IMAGE = Constants.CONSTRUCT_FOLDER + "GUI\\ButtonSelect.png";
    public static final String MOD_BACKGROUND_IMAGE = Constants.CONSTRUCT_FOLDER + "GUI\\ModBackground.png";
    public static final String SLIDER_IMAGE = Constants.CONSTRUCT_FOLDER + "GUI\\Slider.png";
    public static final String SLIDER_BACKGROUND_IMAGE = Constants.CONSTRUCT_FOLDER + "GUI\\SliderBackground.png";
    public static Image ButtonBackground;
    public static Image ButtonSelectBackground;
    public static Image ModBackground;
    public static Image Slider;
    public static Image SliderBackground;
    static {
        try {
            ButtonBackground = new Image(new FileInputStream(new File(Images.BUTTON_IMAGE)));
            ButtonSelectBackground = new Image(new FileInputStream(new File(Images.BUTTON_SELECT_IMAGE)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Images.class.getName()).log(Level.SEVERE, "Button Image(s) do not exist", ex);
        }
        try {
            ModBackground = new Image(new FileInputStream(new File(Images.MOD_BACKGROUND_IMAGE)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Images.class.getName()).log(Level.SEVERE, "Mod Background does not exist", ex);
        }
        try {
            Slider = new Image(new FileInputStream(new File(Images.SLIDER_IMAGE)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Images.class.getName()).log(Level.SEVERE, "Slider Image does not exist", ex);
        }
        try {
            SliderBackground = new Image(new FileInputStream(new File(Images.SLIDER_BACKGROUND_IMAGE)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Images.class.getName()).log(Level.SEVERE, "Slider Background Image does not exist", ex);
        }
    }
}

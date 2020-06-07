/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.beans.Profile;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Constants {

    public static final String FOLDERS_LOCATION = "C:\\Program Files (x86)\\Construct\\folders.txt";
    public static final String VERSION = "+A.1.1";
    public static final String LAUNCH_COMMAND = "rundll32 url.dll,FileProtocolHandler steam://rungameid/387990";
    public static final ZoneOffset ZONE = OffsetDateTime.now().getOffset();
    public static final Runnable ON_DELETE_FINISH = () -> {
        Profile checkMe = Main.activeProfile.get();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(Constants.class.getName()).log(Level.INFO, "On Delete Finish interrupted");
        }
        if (!checkMe.getDirectory().exists()) {
            Main.activeProfile.setValue(Profile.DELETED);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Constants.class.getName()).log(Level.INFO, "On Delete Finish interrupted");
            }
            Main.updateProfileList();
        }
    };
    public static final String IGNORE_PREFIX = "_._";
    public static final String IGNORE_SUFFIX = "_._";
    public static final String LOG_FOLDER_NAME = IGNORE_PREFIX + "logs" + IGNORE_SUFFIX;
    public static final String OBJECT_FOLDER_LOCATION = "Data\\CML-Objects";
    public static final String OBJECT_FOLDER_CODE = "$GAME_DATA/CML-Objects";
    public static final String PATCH_FOLDER_NAME = IGNORE_PREFIX + "patches" + IGNORE_SUFFIX;

}

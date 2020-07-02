/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import java.io.File;
import java.io.FilenameFilter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Constants {

    public static final String FOLDERS_LOCATION_RELATIVE = "\\folders.txt";
    public static final String VERSION = "+B.1.0-h2";
    public static final String LAUNCH_COMMAND = "rundll32 url.dll,FileProtocolHandler steam://rungameid/387990";
    public static final String VERIFY_COMMAND = "rundll32 url.dll,FileProtocolHandler steam://validate/387990";
    public static final String KILL_VERIFY_COMMAND = "taskkill <<id>>";
    public static final ZoneOffset ZONE = OffsetDateTime.now().getOffset();
    public static final String IGNORE_PREFIX = "_._";
    public static final String IGNORE_SUFFIX = "_._";
    public static final String LOG_FOLDER_NAME = IGNORE_PREFIX + "logs" + IGNORE_SUFFIX;
    public static final String OBJECT_FOLDER_LOCATION = "Data\\CML-Objects";
    public static final String OBJECT_FOLDER_CODE = "$GAME_DATA/CML-Objects";
    public static final String PATCH_FOLDER_NAME = IGNORE_PREFIX + "patches" + IGNORE_SUFFIX;
    public static final Set<String> VERIFY_SM_FOLDER = new HashSet();
    public static final List<String> IGNORE_PATHS = new ArrayList();
    public static final FilenameFilter IGNORE_PATH_FILTER = (File dir, String name) -> {
        String path = (dir.getAbsolutePath() + "\\" + name).replace(Main.scrapMechanicFolder, "\\");
        for (String ignore : IGNORE_PATHS) {
            if (path.startsWith(ignore)) {
                return false;
            }
        }
        return true;
    };
    public static final Pattern IMG_PATTERN = Pattern.compile("(?<=\\[img\\]).*(?=\\[/img\\])");
    public static final FilenameFilter NO_FILTER = (File dir, String name) -> true;
    public static final FilenameFilter TEXT_FILE = (File dir, String name) -> name.endsWith(".xml") || name.endsWith(".json") || name.endsWith(".vdf") || name.endsWith(".txt") || name.endsWith(".lua") || name.endsWith(".hlsl") || name.endsWith(".layout") || name.endsWith(".glsl") || name.endsWith(".dae");

    static {
        IGNORE_PATHS.add("\\Logs");
        IGNORE_PATHS.add("\\Cache");
        IGNORE_PATHS.add("\\Challenges");
        IGNORE_PATHS.add("\\ChallengeData");
        IGNORE_PATHS.add("\\Data\\ExampleMods");
        IGNORE_PATHS.add("\\Data\\Terrain");
        IGNORE_PATHS.add("\\Survival\\Character\\Char_Male");
        IGNORE_PATHS.add("\\Survival\\Terrain");
        IGNORE_PATHS.add("\\Screenshots");
        IGNORE_PATHS.add("\\" + Constants.OBJECT_FOLDER_LOCATION);
        
        VERIFY_SM_FOLDER.add("Cache");
        VERIFY_SM_FOLDER.add("ChallengeData");
        VERIFY_SM_FOLDER.add("Challenges");
        VERIFY_SM_FOLDER.add("Data");
        VERIFY_SM_FOLDER.add("Logs");
        VERIFY_SM_FOLDER.add("Release");
        VERIFY_SM_FOLDER.add("Survival");
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Constants {

    /**
     * The location of the folder map, relative to the API directory.
     */
    public static final String FOLDERMAP_LOCATION = "folders.txt";
    /**
     * The current version of CML.
     */
    public static final String VERSION = "+B.2.0";
    /**
     * The command used to launch Scrap Mechanic.
     */
    public static final String LAUNCH_COMMAND = "rundll32 url.dll,FileProtocolHandler steam://rungameid/387990";
    /**
     * The command used to reset Scrap Mechanic to vanilla.<br>
     * That is - to validate the files.
     */
    public static final String VERIFY_COMMAND = "rundll32 url.dll,FileProtocolHandler steam://validate/387990";
    /**
     * The command used to kill a task.
     */
    public static final String TASK_KILL_COMMAND = "tskill <<id>>";
    /**
     * Location of the CML Objects folder relative to the Scrap Mechanic folder.
     */
    public static final String OBJECT_FOLDER_LOCATION = "Data" + File.separator + "CML-Objects";
    /**
     * Location of the CML Objects folder relative to the Scrap Mechanic folder,
     * as codified for the Scrap Mechanic Engine to interpret.
     */
    public static final String OBJECT_FOLDER_CODE = "$GAME_DATA/CML-Objects";
    /**
     * The folders that should be within the Scrap Mechanic folder, which can be
     * used to verify that the folder is indeed the Scrap Mechanic folder.
     */
    public static final Set<String> VERIFY_SM_FOLDER = new HashSet();
    /**
     * Relative paths within the Scrap Mechanic folder that should be ignored.
     */
    public static final List<String> IGNORE_PATHS = new ArrayList();
    /**
     * Filters out paths that ought to be ignored as defined in {@link #IGNORE_PATHS},
     * as determined relative to the defined Scrap Mechanic folder.
     */
    public static final FilenameFilter IGNORE_PATH_FILTER = (File dir, String name) -> {
        String path = (dir.getAbsolutePath().replace("/", "\\") + "\\" + name).replace(Main.scrapMechanicFolder, "\\");
        return IGNORE_PATHS.stream().noneMatch((ignore) -> (path.startsWith(ignore)));
    };
    /**
     * If the <code>IGNORE_PREFIX</code> and {@link #IGNORE_SUFFIX} start and end
     * a file name (respectively), then the file will be ignored by the {@link #IGNORE_FILE_FILTER}
     */
    public static final String IGNORE_PREFIX = "_._";
    /**
     * If the {@link #IGNORE_PREFIX} and <code>IGNORE_SUFFIX</code> start and end
     * a file name (respectively), then the file will be ignored by the {@link #IGNORE_FILE_FILTER}
     */
    public static final String IGNORE_SUFFIX = "_._";
    /**
     * Ignores files whose names:
     * <li>Start with the {@link #IGNORE_PREFIX}</li>
     * <li>End with the {@link #IGNORE_SUFFIX}</li>
     * <br>
     * Note: The filename must meet <b>both</b> requirements.
     */
    public static final FilenameFilter IGNORE_FILE_FILTER = (File dir, String name) -> !(name.startsWith(IGNORE_PREFIX) && name.endsWith(IGNORE_SUFFIX));
    /**
     * Patches folder will be removed when the CML Mod Tool plugin has been completed.
     * Begins with the {@link #IGNORE_PREFIX} and ends with the {@link #IGNORE_SUFFIX}.
     */
    public static final String PATCH_FOLDER_NAME = IGNORE_PREFIX + "patches" + IGNORE_SUFFIX;
    /**
     * A filter that accepts all files.
     */
    public static final FilenameFilter NO_FILTER = (File dir, String name) -> true;
    /**
     * A filter that accepts all text files.
     */
    public static final FilenameFilter TEXT_FILE = (File dir, String name) -> name.endsWith(".xml") || name.endsWith(".json") || name.endsWith(".vdf") || name.endsWith(".txt") || name.endsWith(".lua") || name.endsWith(".hlsl") || name.endsWith(".layout") || name.endsWith(".glsl") || name.endsWith(".dae");
    /**
     * The location of the CML.exe file, as determined by the working directory.
     */
    public static final File API_DIRECTORY = new File(System.getProperty("user.dir"));
    /**
     * The location of the downloads folder.
     */
    public static final File DOWNLOADS_FOLDER = new File(API_DIRECTORY, "downloads");

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
        
        VERIFY_SM_FOLDER.add("ChallengeData");
        VERIFY_SM_FOLDER.add("Challenges");
        VERIFY_SM_FOLDER.add("Data");
        VERIFY_SM_FOLDER.add("Logs");
        VERIFY_SM_FOLDER.add("Release");
        VERIFY_SM_FOLDER.add("Survival");
    }
}

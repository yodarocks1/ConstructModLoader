/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.apply.Apply;
import cml.beans.Profile;
import cml.lib.steamverify.SteamVerifier;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Constants {

    public static final String FOLDERS_LOCATION_RELATIVE = "\\folders.txt";
    public static final String VERSION = "+A.1.1";
    public static final String LAUNCH_COMMAND = "rundll32 url.dll,FileProtocolHandler steam://rungameid/387990";
    public static final String VERIFY_COMMAND = "rundll32 url.dll,FileProtocolHandler steam://validate/387990";
    public static final String KILL_VERIFY_COMMAND = "taskkill <<id>>";
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
    public static final Runnable REGEN_VANILLA = new Runnable() {
        @Override
        public void run() {
            ErrorManager.addStateCause("vanillaFolder not created");
            SteamVerifier verifier = new SteamVerifier();
            if (verifier.verify()) {
                System.out.println("Clearing old vanilla folder");
                File vanilla = new File(Main.vanillaFolder);
                if (vanilla.exists()) {
                    try {
                        Apply.deleteDirectory(vanilla.toPath());
                        System.out.println("  Deleted vanilla folder");
                    } catch (IOException ex) {
                        Logger.getLogger(Constants.class.getName()).log(Level.SEVERE, "Could not clear vanilla folder", ex);
                    }
                }
                System.out.println("Creating new vanilla folder");
                copyDirectoryRec(Main.scrapMechanicFolder, "", IGNORE_PATH_FILTER, Main.vanillaFolder);
                System.out.println("Vanilla folder successfully generated");
                ErrorManager.removeStateCause("vanillaFolder not created");
            } else {
                if (new File(Main.vanillaFolder).exists()) {
                    ErrorManager.removeStateCause("vanillaFolder not created");
                    Main.verifyVanillaFolder();
                } else {
                    ErrorManager.addStateCause("vanillaFolder not created");
                }
            }
        }

        private void copyDirectoryRec(String mainDirectory, String addend, FilenameFilter filter, String toPath) {
            File file = new File(mainDirectory + addend);
            if (file.isDirectory()) {
                for (String subFile : file.list(filter)) {
                    System.out.println("  Addend: " + addend);
                    copyDirectoryRec(mainDirectory, addend + "\\" + subFile, filter, toPath);
                }
            } else {
                File toFile = new File(toPath + addend);
                System.out.println("  Create: " + addend);
                try {
                    Files.createDirectories(toFile.getParentFile().toPath());
                    Files.copy(file.toPath(), toFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                } catch (IOException ex) {
                    Logger.getLogger(Constants.class.getName()).log(Level.SEVERE, "Could not copy from Scrap Mechanic to Vanilla", ex);
                }
            }
        }
    };
    public static final String STEAM_REG_PATH_64 = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Valve\\Steam";
    public static final String STEAM_REG_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Valve\\Steam";

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
    }
}

package cml.lib.registry.hardcoded;

import cml.lib.registry.RegQuery;
import cml.lib.registry.apache.WinRegistry;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author HardCoded
 */
public final class Steam {

    private static final Logger LOGGER = Logger.getLogger(Steam.class.getName());

    private static final List<File> LIBRARY_PATHS = new ArrayList<>();
    private static final Map<String, File> GAME_CACHE = new HashMap();
    private static File STEAM_PATH;

    public static void main(String[] args) {
        LOGGER.log(Level.FINE, "Steam: {0}", STEAM_PATH);
        LOGGER.log(Level.FINE, "Libraries: {0}", LIBRARY_PATHS);

        File gamePath = Steam.findGamePath("Scrap Mechanic");
        LOGGER.log(Level.FINE, "Path: \"{0}\"", gamePath);
    }

    static {
        reload();
    }

    /**
     * This function will try find the installPath of steam and then get all the
     * library paths.
     *
     * @return true if it successfully found Steam
     */
    public static boolean reload() {
        LOGGER.log(Level.INFO, "Loading Steam installation path");
        LIBRARY_PATHS.clear();

        String installPath = RegQuery.readRegistryValue(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\WOW6432Node\\Valve\\Steam", "InstallPath");
        if (installPath == null) {
            // 32 bit
            installPath = RegQuery.readRegistryValue(WinRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Valve\\Steam", "InstallPath");
        }

        if (installPath == null) {
            LOGGER.log(Level.WARNING, "Could not find the Steam installation path in the registry");
            return false;
        }

        return reload(new File(installPath));
    }

    /**
     * Load all libraries with the steam installPath
     *
     * @param installPath the path of the steam directory
     * @return always true
     */
    public static boolean reload(File installPath) {
        LOGGER.log(Level.FINE, "Loading all Library locations");
        STEAM_PATH = installPath;
        LIBRARY_PATHS.clear();

        // The default steam path is also a default library location
        LIBRARY_PATHS.add(STEAM_PATH);

        File file = new File(STEAM_PATH, "steamapps/libraryfolders.vdf");

        // The user never changed the library path so the file never got created
        if (!file.exists()) {
            return true;
        }

        ValveData data = new ValveData(file).get("LibraryFolders");
        Set<String> names = data.getValueNames();
        for (String s : names) {
            if (!s.matches("[0-9]+")) {
                continue;
            }

            String path = data.getValue(s);

            LOGGER.log(Level.FINEST, "Adding library: \"{0}\"", path);
            LIBRARY_PATHS.add(new File(path));
        }

        return true;
    }

    public static File findGamePath(String name) {
        if (GAME_CACHE.containsKey(name)) {
            LOGGER.log(Level.FINE, "Cached location for {0}: {1}", new Object[]{name, GAME_CACHE.get(name).getAbsolutePath()});
            return GAME_CACHE.get(name);
        }
        if (LIBRARY_PATHS.isEmpty()) {
            LOGGER.log(Level.WARNING, "No library paths found");
            return null;
        }

        for (File file : LIBRARY_PATHS) {
            File common = new File(file, "steamapps/common");

            if (!common.exists()) {
                continue;
            }
            File[] games = common.listFiles();

            for (File game : games) {
                if (game.getName().equals(name)) {
                    LOGGER.log(Level.INFO, "Found location for {0}: {1}", new Object[]{name, game.getAbsolutePath()});
                    GAME_CACHE.put(name, game);
                    return game;
                }
            }
        }
        LOGGER.log(Level.WARNING, "No location location found for {0}", name);
        return null;
    }

    public static File getInstallDirectory() {
        return STEAM_PATH;
    }

    public static List<File> getLibraryFolders() {
        return Collections.unmodifiableList(LIBRARY_PATHS);
    }
}

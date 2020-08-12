/*
 * Copyright (C) 2020 benne
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cml.lib.lazyupdate;

import cml.Constants;
import cml.lib.threadmanager.ThreadManager;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 *
 * @author benne
 */
public class ChangeDetector {

    /**
     * An observable map of profiles and mods.<br>
     * If valueAdded is null, the profile no longer exists.<br>
     * If valueRemoved is null, the profile was just discovered.<br>
     * New mods can be determined via comparing valueAdded and valueRemoved (equivalent to newValue and oldValue, respectively)<br>
     */
    public final ObservableMap<String, String[]> mods = FXCollections.observableHashMap();
    
    /**
     * An observable map of plugins.<br>
     * Currently not implemented<br>
     */
    public final ObservableMap<String, Boolean> plugins = FXCollections.observableHashMap();
    
    /**
     * An observable map of files in the downloads folder.<br>
     * <br>
     * If valueAdded is null, the downloads folder has been deleted.<br>
     * If valueRemoved is null, the downloads folder was just created or discovered, and all downloads in valueAdded should be handled.<br>
     * New downloads can be determined via comparing valueAdded and valueRemoved (equivalent to newValue and oldValue, respectively).<br>
     * <br>
     * New downloads should always be handled, and the download should be removed if handled successfully.<br>
     * Removed downloads have already been handled.<br>
     * Old downloads have likely failed handling.<br>
     */
    public final ObservableMap<String, String[]> downloads = FXCollections.observableHashMap();

    private static final Logger LOGGER = Logger.getLogger(ChangeDetector.class.getName());

    private final File profileDir;
    private final File downloadsDir;
    /*private final File pluginDir;*/

    /**
     * Detects changes in the given directories, and reports them to the observable maps.
     * @param profileDir The location of the profile directory (typically <code>$INSTDIR/mods</code>)
     * @param downloadsDir The location of the downloads directory (typically <code>$INSTDIR/API/downloads</code>)
     */
    public ChangeDetector(File profileDir, File downloadsDir/*, File pluginDir*/) {
        this.profileDir = profileDir;
        this.downloadsDir = downloadsDir;
        /*this.pluginDir = pluginDir;*/
    }

    /**
     * Run a check of the given directories, and reports them.
     */
    public void check() {
        //Mods
        List<String> profiles = Arrays.asList(profileDir.list(Constants.IGNORE_FILE_FILTER));
        Map<String, String[]> profileToMods = new HashMap();
        for (File profile : profileDir.listFiles(Constants.IGNORE_FILE_FILTER)) {
            String[] newMods = profile.list((File dir, String name) -> {
                return new File(dir, name).isDirectory();
            });
            if (!Arrays.equals(newMods, mods.get(profile.getName()))) {
                profileToMods.put(profile.getName(), newMods);
            }
        }
        mods.replaceAll((key, oldValue) -> {
            return profiles.contains(key) ? oldValue : null;
        });
        mods.putAll(profileToMods);
        
        //Plugins
        
        //Downloads
        if (downloadsDir.exists()) {
            downloads.put(downloadsDir.getName(), downloadsDir.list(Constants.IGNORE_FILE_FILTER));
        }
    }

    /**
     * Recursively checks the given directories and reports them.
     * @param period Recursion period
     * @param unit Time unit for the given period
     */
    public void recursivelyCheck(int period, TimeUnit unit) {
        @SuppressWarnings("SleepWhileInLoop")
        Thread checkerThread = new Thread(() -> {
            for (;;) {
                check();
                try {
                    Thread.sleep(unit.toMillis(period));
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "Change Detector stopped", ex);
                    break;
                }
                if (Thread.interrupted()) {
                    break;
                }
            }
        });
        ThreadManager.addThread(checkerThread);
        checkerThread.start();
    }
}

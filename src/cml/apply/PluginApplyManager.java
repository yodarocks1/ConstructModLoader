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
package cml.apply;

import cml.Main;
import cml.beans.Plugin;
import cml.lib.hash.Hasher;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class PluginApplyManager {

    private static final Logger LOGGER = Logger.getLogger(PluginApplyManager.class.getName());

    public static Set<PluginApplyManager> managers = new HashSet();

    public static File handleMod(File modDir) {
        File mod = modDir;
        for (PluginApplyManager manager : managers) {
            if (manager.plugin.isEnabled()) {
                try {
                    File handled = manager.handle(modDir);
                    if (handled.isDirectory()) {
                        if (!mod.equals(modDir)) {
                            mod.delete();
                        }
                        mod = handled;
                    } else {
                        handled.delete();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "An issue occurred when trying to run mod-handler plugin \"" + manager.plugin.getName() + "\"", ex);
                }
            }
        }
        return mod;
    }

    public static String getHeldSMVersion() {
        return hashToVersion(Hasher.digestFile(new File(Main.vanillaFolder, "Release/ScrapMechanic.exe")));
    }

    public static String hashToVersion(String hash) {
        try (Scanner exeHashes = new Scanner(PluginApplyManager.class.getClassLoader().getResourceAsStream("media/exehashes.dat"))) {
            String out = hashToVersion(exeHashes, hash);
            if (out != null) {
                return out;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to read exehashes.dat", ex);
        }

        //No matches - download file from Github and retry
        try (Scanner exeHashes = new Scanner(new URL("https://raw.githubusercontent.com/yodarocks1/ConstructModLoader/og_master/src/media/exehashes.dat").openStream())) {
            String out = hashToVersion(exeHashes, hash);
            if (out != null) {
                return out;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to read exehashes.dat from GitHub", ex);
        }

        //Still no matches - return "Unknown"
        return "Unknown";
    }

    private static String hashToVersion(Scanner in, String hash) {
        while (in.hasNextLine()) {
            String[] split = in.nextLine().split(" = ");
            if (hash.equalsIgnoreCase(split[1])) {
                return split[0];
            }
        }
        return null;
    }

    @SuppressWarnings("SleepWhileInLoop")
    private static void awaitFile(File await) {
        while (!await.exists()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }

    private final Plugin plugin;

    @SuppressWarnings("LeakingThisInConstructor")
    public PluginApplyManager(Plugin plugin) {
        this.plugin = plugin;
    }

    private File handle(File modDir) throws IOException {
        File outFile = new File(this.plugin.getDirectory(), modDir.getName());
        outFile.delete();
        this.plugin.run(new String[]{modDir.getAbsolutePath(), outFile.getAbsolutePath()});
        awaitFile(outFile);
        return outFile;
    }

}

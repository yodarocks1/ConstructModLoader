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
package cml.lib.steam.verify;

import static cml.Constants.IGNORE_PATH_FILTER;
import cml.ErrorManager;
import cml.Main;
import cml.lib.files.AFileManager;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author benne
 */
public class VanillaFolderManager {
    
    private static final Logger LOGGER = Logger.getLogger(VanillaFolderManager.class.getName());
    
    private static final List<String> VANILLA_FOLDERS_CHECK = new ArrayList();
    
    static {
        VANILLA_FOLDERS_CHECK.add("Data");
        VANILLA_FOLDERS_CHECK.add("Release");
        VANILLA_FOLDERS_CHECK.add("Survival");
    }

    public static void regenVanilla(Optional<Runnable> onComplete) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("(Re)-Generate Vanilla Folder");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Before continuing, make sure that the location for the vanilla folder that you have entered is the desired location.\n\n"
                + "NOTE: This process will attempt to continue in the background, regardless of the closure of Construct Mod Loader. Do not end this process in your task manager or reboot your computer - it may corrupt your data.\n\n"
                + "This process can take between 15 minutes and an hour, depending on your internet connection and drive speed. If you have an exceptionally low-end computer, this may take even longer.");
        alert.showAndWait();
        if (!alert.getResult().getButtonData().isCancelButton()) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ErrorManager.addStateCause("vanillaFolder not created");
                    SteamVerifier verifier = new SteamVerifier();
                    if (verifier.verify()) {
                        LOGGER.log(Level.FINER, "Clearing old vanilla folder");
                        File vanilla = new File(Main.vanillaFolder);
                        if (vanilla.exists()) {
                            AFileManager.FILE_MANAGER.deleteDirectory(vanilla);
                            LOGGER.log(Level.FINER, "  Deleted vanilla folder");
                        }
                        LOGGER.log(Level.FINER, "Creating new vanilla folder");
                        copyDirectoryRec(Main.scrapMechanicFolder, "", IGNORE_PATH_FILTER, Main.vanillaFolder);
                        LOGGER.log(Level.FINER, "Vanilla folder successfully generated");
                        ErrorManager.removeStateCause("vanillaFolder not created");
                    } else {
                        if (new File(Main.vanillaFolder).exists()) {
                            ErrorManager.removeStateCause("vanillaFolder not created");
                            verifyVanillaFolder();
                        } else {
                            ErrorManager.addStateCause("vanillaFolder not created");
                        }
                    }
                    if (onComplete.isPresent()) {
                        onComplete.orElse(null).run();
                    }
                }

                private void copyDirectoryRec(String mainDirectory, String addend, FilenameFilter filter, String toPath) {
                    File file = new File(mainDirectory + addend);
                    if (file.isDirectory()) {
                        for (String subFile : file.list(filter)) {
                            LOGGER.log(Level.FINEST, "  Addend: {0}", addend);
                            copyDirectoryRec(mainDirectory, addend + "\\" + subFile, filter, toPath);
                        }
                    } else {
                        File toFile = new File(toPath + addend);
                        LOGGER.log(Level.FINEST, "  Create: {0}", addend);
                        try {
                            Files.createDirectories(toFile.getParentFile().toPath());
                            Files.copy(file.toPath(), toFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE, "Could not copy from Scrap Mechanic to Vanilla", ex);
                        }
                    }
                }
            });
            
            thread.start(); //This thread will not stop when the application closes because Steam validation would corrupt the game files.
                            //Thus, it is not connected to ThreadManager's Executor
        }
    }

    public static boolean verifyVanillaFolder() {
        File vFolder = new File(Main.vanillaFolder);
        File exe = new File(Main.vanillaFolder, "Release/ScrapMechanic.exe");
        if (!vFolder.exists() || !Arrays.asList(vFolder.list()).containsAll(VANILLA_FOLDERS_CHECK) || !exe.exists()) {
            ErrorManager.addStateCause("VanillaFolder <INVALID>");
            return false;
        } else {
            ErrorManager.removeStateCause("VanillaFolder <INVALID>");
            return true;
        }
    }
    
}

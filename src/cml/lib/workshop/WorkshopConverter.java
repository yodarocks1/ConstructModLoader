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
package cml.lib.workshop;

import cml.Constants;
import cml.Main;
import cml.apply.MergeChanges;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Platform;

/**
 *
 * @author benne
 */
public class WorkshopConverter {

    private static final Logger LOGGER = Logger.getLogger(WorkshopConverter.class.getName());

    static {
        LOGGER.setLevel(Level.ALL);
    }

    private static final FilenameFilter INDICATOR_FILTER = (File dir, String name) -> name.equals("Survival") || name.equals("Release") || name.equals("Data");

    public void copyAndConvert(WorkshopMod workshopMod, File file, boolean fileIsProfile) {
        if (fileIsProfile) {
            copyAndConvert(workshopMod, new File(file, workshopMod.getName()));
        } else {
            copyAndConvert(workshopMod, file);
        }
    }

    public void copyAndConvert(WorkshopMod workshopMod, File mod) {
        LOGGER.log(Level.INFO, "Copying workshop mod \"{0}\" into profile {1}", new Object[]{workshopMod.getName(), mod.getParentFile().getName()});

        boolean handled = false;
        boolean descriptionHandled = false;

        //If this mod is a CML mod, use the CML copy method
        if (workshopMod.isCMLMod()) {
            cmlCopy(workshopMod, mod);
            handled = true;
            descriptionHandled = true;
        }

        //If this mod is normally a manual-install mod, use the direct copy method
        if (!handled) {
            boolean containsIndicator = workshopMod.getDirectory().listFiles(INDICATOR_FILTER).length > 0;
            if (containsIndicator) {
                directCopy(workshopMod, mod);
                handled = true;
            }
        }

        //If this mod is a normal creative mod, convert it then copy it.
        if (!handled) {
            convertAndCopy(workshopMod, mod);
        }

        //Read in the first line of the description if description.txt doesn't already exist
        if (!descriptionHandled) {
            createDescription(workshopMod, mod);
        }

        //Create connection file
        createConnection(workshopMod, mod);

        //Update the profile to visibly show the mod
        Platform.runLater(() -> {
            Main.updateProfileList();
        });
    }

    private void cmlCopy(WorkshopMod workshopMod, File newMod) {
        LOGGER.log(Level.INFO, " - Using CML copy method");
        copyDirectoryRec(
                workshopMod.getDirectory().getAbsolutePath(),
                "",
                (File dir, String name) -> !(name.equals("description.json") && dir.getAbsolutePath().equals(workshopMod.getDirectory().getAbsolutePath())),
                newMod.getAbsolutePath()
        );
    }

    private void directCopy(WorkshopMod workshopMod, File newMod) {
        LOGGER.log(Level.INFO, " - Using direct copy method");
        for (File subFile : workshopMod.getDirectory().listFiles(INDICATOR_FILTER)) {
            copyDirectoryRec(
                    subFile.getAbsolutePath(),
                    "\\" + subFile.getName(),
                    Constants.NO_FILTER,
                    newMod.getAbsolutePath() + "\\" + MergeChanges.MERGE_FOLDER_RELATIVE + "\\" + subFile.getName()
            );
        }
        File shapesetsFolder = new File(newMod, "Objects/Database/ShapeSets");
        if (shapesetsFolder.exists()) {
            File shapesetTxt = new File(newMod, "Objects/Database/shapesets.txt");
            try {
                Files.write(shapesetTxt.toPath(), Arrays.stream(shapesetsFolder.list()).map((shapeset) -> "$THIS_MOD/Objects/Database/ShapeSets/" + shapeset).collect(Collectors.joining("\n")).getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not create a shapesets.txt for mod #" + workshopMod.getWorkshopId(), ex);
            }
        }
        File iconmapsFolder = new File(newMod, "Gui");
        if (iconmapsFolder.exists()) {
            File iconmapsTxt = new File(newMod, "Objects/Database/iconmaps.txt");
            try {
                Files.write(iconmapsTxt.toPath(), Arrays.stream(iconmapsFolder.list(filterExtension(".xml"))).map((iconmap) -> "$THIS_MOD/Gui/" + iconmap + " : $THIS_MOD/Gui/" + iconmap.replace(".xml", ".png")).collect(Collectors.joining("\n")).getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not create a shapesets.txt for mod #" + workshopMod.getWorkshopId(), ex);
            }
        }
        File inventoryDescFolder = new File(newMod, "Gui/Language/English");
        if (inventoryDescFolder.exists()) {
            File inventoryDescTxt = new File(newMod, "Objects/Database/inventorydesc.txt");
            try {
                Files.write(inventoryDescTxt.toPath(), Arrays.stream(iconmapsFolder.list()).map((inventoryDesc) -> "$THIS_MOD/Gui/Language/English/" + inventoryDesc).collect(Collectors.joining("\n")).getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not create a shapesets.txt for mod #" + workshopMod.getWorkshopId(), ex);
            }
        }
    }

    private void convertAndCopy(WorkshopMod workshopMod, File newMod) {
        convertObjects(
                new File(workshopMod.getDirectory(), "Objects"),
                newMod,
                workshopMod.getWorkshopId()
        );
        convertGui(
                new File(workshopMod.getDirectory(), "Gui"),
                newMod,
                workshopMod.getWorkshopId()
        );
        convertScripts(
                new File(workshopMod.getDirectory(), "Scripts"),
                newMod,
                workshopMod.getWorkshopId()
        );
        convertEffects(
                new File(workshopMod.getDirectory(), "Effects"),
                newMod,
                workshopMod.getWorkshopId()
        );
        for (File subFolder : workshopMod.getDirectory().listFiles((File dir) -> dir.isDirectory() && !(dir.getName().equals("Objects") || dir.getName().equals("Gui") || dir.getName().equals("Scripts") /*|| dir.getName().equals("Effects")*/))) {
            copyDirectoryRec(
                    workshopMod.getDirectory().getAbsolutePath(),
                    "\\" + subFolder.getName(),
                    Constants.NO_FILTER,
                    newMod.getAbsolutePath()
            );
        }
    }

    private static void copyDirectoryRec(String mainDirectory, String addend, FilenameFilter filter, String toDirectory) {
        File file = new File(mainDirectory + addend);
        if (file.isDirectory()) {
            for (String subFile : file.list(filter)) {
                LOGGER.log(Level.FINEST, "  Addend: {0}", addend);
                copyDirectoryRec(mainDirectory, addend + "\\" + subFile, filter, toDirectory);
            }
        } else {
            File toFile = new File(toDirectory, addend);
            LOGGER.log(Level.FINER, "  Create: {0}", addend);
            try {
                Files.createDirectories(toFile.getParentFile().toPath());
                Files.copy(file.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not copy from " + file.getAbsolutePath() + " to " + toFile.getAbsolutePath(), ex);
            }
        }
    }

    private FilenameFilter filterExtension(String fileExtension) {
        return (File dir, String name) -> name.endsWith(fileExtension);
    }

    private void convertObjects(File workshopObjectsFolder, File newMod, int workshopId) {

        if (workshopObjectsFolder.exists()) {
            //<editor-fold defaultstate="collapsed" desc="Shapesets">
            File shapesetFolder = new File(workshopObjectsFolder, "Database\\ShapeSets");
            if (shapesetFolder.exists()) {
                String shapesetTxt = "";
                for (File shapeset : shapesetFolder.listFiles(filterExtension(".json"))) {
                    shapesetTxt += "$THIS_MOD/Objects/Database/ShapeSets/" + shapeset.getName();
                    try {
                        File destination = new File(shapesetFolder, shapeset.getName());
                        Files.createDirectories(destination.getParentFile().toPath());
                        Files.write(
                                destination.toPath(),
                                Files.readAllLines(shapeset.toPath()).stream()
                                        .collect(Collectors.joining("\n"))
                                        .replace("$MOD_DATA", "$THIS_MOD").getBytes(),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING
                        );
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Could not copy shapeset " + shapeset.getName() + " from mod #" + workshopId + " into mod " + newMod.getAbsolutePath(), ex);
                    }
                }
                try {
                    File destination = new File(newMod, "Objects\\Database\\shapesets.txt");
                    Files.createDirectories(destination.getParentFile().toPath());
                    Files.write(destination.toPath(), shapesetTxt.getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not write new shapeset file at " + newMod.getAbsolutePath() + "\\Objects\\Database\\shapesets.txt", ex);
                }
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Textures, Mesh, Collisions">
            copyDirectoryRec(
                    workshopObjectsFolder.getAbsolutePath(),
                    "",
                    (File dir, String name) -> !name.equals("Database"),
                    newMod + "\\Objects"
            );
            //</editor-fold>

            //TODO: Manage new rotation sets (CML-side)
        }
    }

    private void convertGui(File workshopGuiFolder, File newMod, int workshopId) {

        if (workshopGuiFolder.exists()) {
            //<editor-fold defaultstate="collapsed" desc="Inventory Descriptions">
            File languageFolder = new File(workshopGuiFolder, "Language\\English");
            if (languageFolder.exists() && languageFolder.isDirectory()) {
                List<String> descriptions = new ArrayList();
                for (File languageFile : languageFolder.listFiles()) {
                    descriptions.add(languageFile.getName());
                    File destination = new File(newMod, "Objects\\Database\\InventoryDescriptions\\" + languageFile.getName());
                    try {
                        Files.createDirectories(destination.getParentFile().toPath());
                        Files.copy(languageFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Could not copy inventory description file from mod #" + workshopId + " into mod " + newMod.getAbsolutePath(), ex);
                    }
                }
                String inventoryDesc = descriptions.stream().map((fileName) -> "$THIS_MOD/Objects/Database/InventoryDescriptions/" + fileName).collect(Collectors.joining("\n"));
                try {
                    File destination = new File(newMod, "Objects\\Database\\inventorydesc.txt");
                    Files.createDirectories(destination.getParentFile().toPath());
                    Files.write(destination.toPath(), inventoryDesc.getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not write inventorydesc.txt in mod folder" + newMod.getAbsolutePath(), ex);
                }
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Icon Maps">
            File[] iconMapsXml = workshopGuiFolder.listFiles(filterExtension(".xml"));
            List<String> iconMaps = new ArrayList();
            for (File iconMapXml : iconMapsXml) {
                String iconMap = "$THIS_MOD/Objects/Database/IconMaps/" + iconMapXml.getName() + " : $THIS_MOD/Objects/Database/IconMaps/";

                String xml = "";
                try {
                    xml = Files.readAllLines(iconMapXml.toPath()).stream().collect(Collectors.joining("\n"));
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not read icon map XML at " + iconMapXml.getAbsolutePath(), ex);
                }

                Pattern texturePattern = Pattern.compile("(?<=texture=\").+.png(?=\")");
                Matcher matcher = texturePattern.matcher(xml);
                String texture;
                if (matcher.find()) {
                    texture = matcher.group();
                } else {
                    texture = iconMapXml.getName().replace(".xml", ".png");
                }
                iconMap += texture;

                iconMaps.add(iconMap);

                File iconMapPng = new File(workshopGuiFolder, texture);
                try {
                    Files.createDirectories(iconMapXml.getParentFile().toPath());
                    Files.copy(iconMapXml.toPath(), new File(newMod, "Objects/Database/IconMaps" + iconMapXml.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(iconMapPng.toPath(), new File(newMod, "Objects/Database/IconMaps" + iconMapPng.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not write icon map " + iconMapXml.getName() + " and its associated texture from mod #" + workshopId, ex);
                }
            }
            if (iconMaps.size() > 0) {
                String iconMapsTxt = iconMaps.stream().collect(Collectors.joining("\n"));
                try {
                    File destination = new File(newMod, "Objects/Database/iconmaps.txt");
                    Files.createDirectories(destination.getParentFile().toPath());
                    Files.write(destination.toPath(), iconMapsTxt.getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not write iconmaps.txt for mod #" + workshopId, ex);
                }
            }
            //</editor-fold>
        }
    }

    private void convertScripts(File workshopScriptsFolder, File newMod, int workshopId) {

        if (workshopScriptsFolder.exists()) {
            //<editor-fold defaultstate="collapsed" desc="Scripts (Compat for ShapeSets)">
            copyDirectoryRec(
                    workshopScriptsFolder.getAbsolutePath(),
                    "",
                    Constants.NO_FILTER,
                    newMod.getAbsolutePath() + "\\Scripts"
            );
            //</editor-fold>
        }
    }

    private void convertEffects(File workshopEffectsFolder, File newMod, int workshopId) {

        if (workshopEffectsFolder.exists()) {
            //TODO: Manage new effects (CML-side)
        }
    }

    private void createConnection(WorkshopMod fromWorkshop, File toMod) {
        File connectionFile = new File(toMod, "workshop.txt");
        try {
            Files.createDirectories(connectionFile.getParentFile().toPath());
            Files.write(connectionFile.toPath(), Integer.toString(fromWorkshop.getWorkshopId()).getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not create workshop connection file in " + toMod.getAbsolutePath(), ex);
        }
    }

    private void createDescription(WorkshopMod workshopMod, File mod) {
        File description = new File(mod, "description.txt");
        File descriptionTxt = new File(workshopMod.getDirectory(), "description.txt");
        if (descriptionTxt.exists()) {
            return;
        }
        String descriptor = workshopMod.getDescription();
        if (descriptor.contains("\n")) {
            descriptor = descriptor.substring(0, descriptor.indexOf("\n"));
        }
        try {
            Files.createDirectories(description.getParentFile().toPath());
            Files.write(description.toPath(), descriptor.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not create description.txt at " + description.getAbsolutePath(), ex);
        }
    }

}

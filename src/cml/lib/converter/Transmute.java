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
package cml.lib.converter;

import cml.Constants;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
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

/**
 *
 * @author benne
 */
public class Transmute {

    public static final byte HANDLES_SPECIAL = (byte) 0b10000000; //Not supported by Transmute

    public static final byte HANDLES_SIMPLE_COPY = 0b00000001;

    public static final byte HANDLES_MANUAL_TO_CML = 0b00000010;
    public static final byte HANDLES_CML_TO_MANUAL = 0b00000100;

    public static final byte HANDLES_CREATIVE_TO_CML = 0b00001000;
    public static final byte HANDLES_CML_TO_CREATIVE = 0b00010000; //Not supported by Transmute

    public static final byte HANDLES_CREATIVE_TO_MANUAL = 0b00100000; //WIP
    public static final byte HANDLES_MANUAL_TO_CREATIVE = 0b01000000; //Not supported by Transmute

    private static final byte HANDLES = 0b00101111 & ~0b00100100; //Second value is what is still WIP

    private static final Logger LOGGER = Logger.getLogger(Transmute.class.getName());

    private static final Method SIMPLE_COPY;
    private static final Method MANUAL_TO_CML;
    private static final Method CML_TO_MANUAL;
    private static final Method CREATIVE_TO_MANUAL;
    private static final Method CREATIVE_TO_CML;

    static {
        Method simpleCopy = null;
        Method manualToCml = null;
        Method cmlToManual = null;
        Method creativeToManual = null;
        Method creativeToCml = null;
        try {
            simpleCopy = Transmute.class.getMethod("simpleCopy", File.class, File.class, FilenameFilter.class);
            manualToCml = Transmute.class.getMethod("manualToCml", File.class, File.class, FilenameFilter.class);
            cmlToManual = Transmute.class.getMethod("cmlToManual", File.class, File.class, FilenameFilter.class);
            creativeToManual = Transmute.class.getMethod("creativeToManual", File.class, File.class, FilenameFilter.class);
            creativeToCml = Transmute.class.getMethod("creativeToCml", File.class, File.class, FilenameFilter.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        SIMPLE_COPY = simpleCopy;
        MANUAL_TO_CML = manualToCml;
        CML_TO_MANUAL = cmlToManual;
        CREATIVE_TO_MANUAL = creativeToManual;
        CREATIVE_TO_CML = creativeToCml;
    }

    public static void simpleCopy(File from, File to, FilenameFilter filter) throws IOException {
        final File in = AFileManager.ZIP_MANAGER.asUnzipped(from);
        AFileManager.FILE_MANAGER.copyDirectory(in, to, AFileManager.FileOptions.DEPTH, new AFileManager.FileOptions.FILTER(filter));
    }

    public static void manualToCml(File manual, File out, FilenameFilter filter) {
        final File in = AFileManager.ZIP_MANAGER.asUnzipped(manual);
        AFileManager.FILE_MANAGER.createDirectory(out, FileOptions.DEPTH);
        AFileManager.FILE_MANAGER.copyDirectory(in, new File(out, "Merge"), FileOptions.DEPTH, new FileOptions.FILTER((File dir, String name)
                -> (!dir.equals(in) || name.equals("Data") || name.equals("Release") || name.equals("Survival")) && (new File(dir, name).isDirectory() || Constants.TEXT_FILE.accept(dir, name)) && filter.accept(dir, name)
        ));
        AFileManager.FILE_MANAGER.copyDirectory(in, new File(out, "Replace"), FileOptions.DEPTH, new FileOptions.FILTER((File dir, String name)
                -> (!dir.equals(in) || name.equals("Data") || name.equals("Release") || name.equals("Survival")) && (new File(dir, name).isDirectory() || !Constants.TEXT_FILE.accept(dir, name)) && filter.accept(dir, name)
        ));
    }

    public static void cmlToManual(File cml, File out, FilenameFilter filter) {
        final File in = AFileManager.ZIP_MANAGER.asUnzipped(cml);
        throw new UnsupportedOperationException("");
    }

    public static void creativeToManual(File creative, File out, FilenameFilter filter) {
        File tempCml;
        try {
            tempCml = Files.createTempDirectory("cmlmod").toFile();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not create temp manual directory. Writing to local", ex);
            tempCml = new File(out.getParentFile(), "tmp-cmlmod-tmp");
        }
        tempCml.deleteOnExit();
        creativeToCml(creative, tempCml, filter);
        manualToCml(tempCml, out, filter);
        tempCml.delete();
    }

    public static void creativeToCml(File creative, File out, FilenameFilter filter) {
        final File in = AFileManager.ZIP_MANAGER.asUnzipped(creative);
        File objectsFolder = new File(in, "Objects");
        if (objectsFolder.exists()) {
            //<editor-fold defaultstate="collapsed" desc="Shapesets">
            File shapesetFolder = new File(objectsFolder, "Database\\ShapeSets");
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
                        LOGGER.log(Level.SEVERE, "Could not copy shapeset " + shapeset.getName() + " from " + in.getAbsolutePath() + " into mod " + out.getAbsolutePath(), ex);
                    }
                }
                try {
                    File destination = new File(out, "Objects\\Database\\shapesets.txt");
                    Files.createDirectories(destination.getParentFile().toPath());
                    Files.write(destination.toPath(), shapesetTxt.getBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not write new shapeset file at " + out.getAbsolutePath() + "\\Objects\\Database\\shapesets.txt", ex);
                }
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Textures, Mesh, Collisions">
            AFileManager.FILE_MANAGER.copyDirectory(objectsFolder, new File(out, "Objects"), FileOptions.DEPTH, new FileOptions.FILTER((dir, name) -> !(name.equals("Database") && new File(dir, name).isDirectory())));
            //</editor-fold>

            //TODO: Manage new rotation sets (CML-side)
        }

        File guiFolder = new File(in, "Gui");
        if (guiFolder.exists()) {
            //<editor-fold defaultstate="collapsed" desc="Inventory Descriptions">
            File languageFolder = new File(in, "Language" + File.pathSeparator + "English");
            if (languageFolder.exists() && languageFolder.isDirectory()) {
                List<String> descriptions = new ArrayList();
                for (File languageFile : languageFolder.listFiles()) {
                    descriptions.add(languageFile.getName());
                    File destination = new File(out, "Objects" + File.pathSeparator + "Database" + File.pathSeparator + "InventoryDescriptions" + File.pathSeparator + languageFile.getName());
                    try {
                        Files.createDirectories(destination.getParentFile().toPath());
                        Files.copy(languageFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Could not copy inventory description file from " + languageFile.getAbsolutePath() + " into " + destination.getAbsolutePath(), ex);
                    }
                }
                String inventoryDesc = descriptions.stream().map((fileName) -> "$THIS_MOD/Objects/Database/InventoryDescriptions/" + fileName).collect(Collectors.joining("\n"));
                AFileManager.FILE_MANAGER.write(new File(out, "Objects" + File.pathSeparator + "Database" + File.pathSeparator + "inventorydesc.txt"), inventoryDesc, FileOptions.REPLACE, FileOptions.DEPTH);
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Icon Maps">
            File[] iconMapsXml = guiFolder.listFiles(filterExtension(".xml"));
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

                File iconMapPng = new File(guiFolder, texture);
                AFileManager.FILE_MANAGER.createParentDirectories(iconMapXml);
                AFileManager.FILE_MANAGER.copy(iconMapXml, new File(out, "Objects/Database/IconMaps" + iconMapXml.getName()), FileOptions.REPLACE);
                AFileManager.FILE_MANAGER.copy(iconMapPng, new File(out, "Objects/Database/IconMaps" + iconMapPng.getName()), FileOptions.REPLACE);
            }
            if (iconMaps.size() > 0) {
                String iconMapsTxt = iconMaps.stream().collect(Collectors.joining("\n"));
                File destination = new File(out, "Objects/Database/iconmaps.txt");
                AFileManager.FILE_MANAGER.write(destination, iconMapsTxt, FileOptions.REPLACE, FileOptions.DEPTH);
            }
            //</editor-fold>
        }
        
        File scriptsFolder = new File(in, "Scripts");
        if (scriptsFolder.exists()) {
            //<editor-fold defaultstate="collapsed" desc="Scripts (Compat for ShapeSets)">
            AFileManager.FILE_MANAGER.copyDirectory(scriptsFolder, new File(out, "Scripts"), FileOptions.DEPTH);
            //</editor-fold>
        }
        
        File effectsFolder = new File(in, "Effects");
        if (effectsFolder.exists()) {
            //TODO: Manage new effects (CML-side)
        }
    }

    private static FilenameFilter filterExtension(String fileExtension) {
        return (File dir, String name) -> name.endsWith(fileExtension);
    }

    public enum ModType {
        CML("cml", SIMPLE_COPY, CML_TO_MANUAL),
        MANUAL("manual", MANUAL_TO_CML, SIMPLE_COPY),
        CREATIVE("creative", CREATIVE_TO_CML, CREATIVE_TO_MANUAL);

        public final String typeName;
        private final Method toCml;
        private final Method toManual;

        private ModType(String typeName, Method toCml, Method toManual) {
            this.typeName = typeName;
            this.toCml = toCml;
            this.toManual = toManual;
        }

        public byte toCml(File original, File out, FilenameFilter filter, byte handled) {
            if (shouldHandleToCml(handled)) {
                toCml(original, out, filter);
                return applyHandle(handled);
            }
            return handled;
        }

        public void toCml(File original, File out, FilenameFilter filter) {
            LOGGER.log(Level.FINE, "{0}: {1} --Construct--> {2}", new Object[]{this.typeName.toUpperCase(), original.getAbsolutePath(), out.getAbsolutePath()});
            try {
                this.toCml.invoke(null, original, out, filter);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Transmute.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public byte toManual(File original, File out, FilenameFilter filter, byte handled) {
            if (shouldHandleToManual(handled)) {
                toManual(original, out, filter);
                return applyHandle(handled);
            }
            return handled;
        }

        public void toManual(File original, File out, FilenameFilter filter) {
            LOGGER.log(Level.FINE, "{0}: {1} ----Manual---> {2}", new Object[]{this.typeName.toUpperCase(), original.getAbsolutePath(), out.getAbsolutePath()});
            try {
                this.toManual.invoke(null, original, out, filter);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Transmute.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public boolean shouldHandleToCml(byte handled) {
            switch (this) {
                case CML:
                    return (~handled & HANDLES & Transmute.HANDLES_SIMPLE_COPY) == 0;
                case MANUAL:
                    return (~handled & HANDLES & Transmute.HANDLES_MANUAL_TO_CML) == 0;
                case CREATIVE:
                    return (~handled & HANDLES & Transmute.HANDLES_CREATIVE_TO_CML) == 0;
            }
            return true;
        }

        public boolean shouldHandleToManual(byte handled) {
            switch (this) {
                case CML:
                    return (~handled & HANDLES & Transmute.HANDLES_CML_TO_MANUAL) == 0;
                case MANUAL:
                    return (~handled & HANDLES & Transmute.HANDLES_SIMPLE_COPY) == 0;
                case CREATIVE:
                    return (~handled & HANDLES & Transmute.HANDLES_CREATIVE_TO_MANUAL) == 0;
            }
            return true;
        }

        public byte applyHandle(byte handled) {
            return (byte) (handled | Transmute.HANDLES);
        }

        public static ModType forFile(File modFile) {
            modFile = AFileManager.ZIP_MANAGER.asUnzipped(modFile);
            File descriptionJson = new File(modFile, "description.json");
            if (descriptionJson.exists()) {
                String json = AFileManager.FILE_MANAGER.readString(descriptionJson);

                for (ModType type : ModType.values()) {
                    if (matchesType(type.typeName, json)) {
                        return type;
                    }
                }

            }

            if (containsAny(modFile, "Data", "Survival", "Release")) {
                return MANUAL;
            }
            if (containsAny(modFile, "Effects", "Gui", "Objects", "Scripts")) {
                return CREATIVE;
            }

            return null;
        }

        public static boolean matchesType(String type, String json) {
            return Pattern.compile("\"" + type + "\"[ ]*:[ ]*[\"]{0,1}true[\"]{0,1}", Pattern.CASE_INSENSITIVE).matcher(json).find() || Pattern.compile("\"type\"[ ]*:[ ]*\"" + type + "\"", Pattern.CASE_INSENSITIVE).matcher(json).find();
        }

        public static boolean containsAny(File directory, String... filenames) {
            List<String> filenamesList = Arrays.asList(filenames);
            return Arrays.stream(directory.list()).anyMatch((item) -> filenamesList.contains(item));
        }
    }

}

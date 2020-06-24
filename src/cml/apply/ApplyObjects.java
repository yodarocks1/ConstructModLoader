/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.Constants;
import cml.Main;
import cml.beans.Item;
import cml.beans.Modification;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import cml.lib.xmliconmap.XMLIconMap;
import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author benne
 */
public class ApplyObjects implements IApplicator {

    private static final Logger LOGGER = Logger.getLogger(ApplyObjects.class.getName());

    //ShapeSets
    public static final String SHAPESETS_RELATIVE = "\\Objects\\Database\\shapesets.txt";
    public static final String SM_SHAPESETS_LOCATION = "Survival\\Objects\\Database\\shapesets.json";

    //SurvivalItems
    public static final String SM_SURVIVALITEMS_LOCATION = "Survival\\Scripts\\game\\survival_items.lua";

    //IconMaps
    public static final String ICONMAPS_RELATIVE = "\\Objects\\Database\\iconmaps.txt";
    public static final String SM_ICONMAP_LOCATION = "Survival\\Gui\\IconMapSurvival.png";
    public static final String SM_ICONMAP_XML_LOCATION = "Survival\\Gui\\IconMapSurvival.xml";

    //InventoryDescriptions
    public static final String INVENTORYDESC_RELATIVE = "\\Objects\\Database\\inventorydesc.txt";
    public static final String SM_INVENTORYDESC_LOCATION = "Survival\\Gui\\Language\\English\\inventoryDescriptions.json";

    //Objects
    public static final String OBJECTS_RELATIVE = "\\Objects";

    private List<Item> items;
    private List<Modification> activeModifications;

    public ApplyObjects(List<Modification> activeModifications) {
        this.items = new ArrayList();
        this.activeModifications = activeModifications;
    }

    public ApplyObjects() {
        this.items = new ArrayList();
        this.activeModifications = new ArrayList();
    }

    @Override
    public void apply() {
        this.cleanObjects();
        this.cloneObjects();
        this.updateSurvivalItems();
        this.updateShapeSets();
        this.updateIcons();
        this.updateInventoryDescriptions();
    }

    @Override
    public void setModifications(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    private void updateSurvivalItems() {
        updateItems();
        LOGGER.log(Level.FINE, "Updating survival items");
        String itemStr = "\n\n----------------------------------------\n-- modded.json\n----------------------------------------";
        itemStr = items.stream().map((item) -> "\n" + item.getName() + " = sm.uuid.new( \"" + item.getUuid() + "\" )").reduce(itemStr, String::concat);
        File SMSurvivalItems = new File(Main.scrapMechanicFolder, SM_SURVIVALITEMS_LOCATION);
        AFileManager.FILE_MANAGER.copy(new File(Main.vanillaFolder, SM_SURVIVALITEMS_LOCATION), SMSurvivalItems, FileOptions.REPLACE);
        AFileManager.FILE_MANAGER.write(SMSurvivalItems, itemStr, FileOptions.APPEND);
    }

    private void updateItems() {
        LOGGER.log(Level.FINE, "Updating items:");
        for (Modification activeModification : this.activeModifications) {
            File shapesets = new File(activeModification.getDirectory().getAbsolutePath() + SHAPESETS_RELATIVE);
            List<String> lines;
            if (shapesets.exists()) {
                lines = AFileManager.FILE_MANAGER.readList(shapesets);
                for (String line : lines) {
                    items.addAll(Item.fromJson(AFileManager.FILE_MANAGER.readString(new File(line.replace("$THIS_MOD", activeModification.getDirectory().getAbsolutePath())))));
                }
            }
        }
    }

    private void updateShapeSets() {
        LOGGER.log(Level.FINE, "Updating shape sets");
        String shapeSetsJson = AFileManager.FILE_MANAGER.readString(new File(Main.scrapMechanicFolder, SM_SHAPESETS_LOCATION)); //Already reset by MergeChanges.java
        String[] shapeSets = shapeSetsJson.substring(shapeSetsJson.indexOf("[") + 1, shapeSetsJson.indexOf("]")).replaceAll("\"", "").replaceAll("\t", "").split(",");
        String result = "{\n\t\"shapeSetList\": [\n";
        for (String shapeSet : shapeSets) {
            if (!shapeSet.startsWith("//")) {
                result += "\t\t\"" + shapeSet.replace("\n", "") + "\",\n";
            }
        }
        for (Modification activeModification : this.activeModifications) {
            File shapesets = new File(activeModification.getDirectory().getAbsolutePath() + SHAPESETS_RELATIVE);
            if (shapesets.exists()) {
                for (String shapeSet : AFileManager.FILE_MANAGER.readList(shapesets).stream().map((line) -> line.replace("$THIS_MOD", Constants.OBJECT_FOLDER_CODE + "/" + activeModification.getName())).collect(Collectors.toList())) {
                    result += "\t\t\"" + shapeSet.replace("\n", "") + "\",\n";
                }
            }
        }
        result = result.substring(0, result.lastIndexOf(",")) + "\n\t]\n}";
        File shapeSetsFile = new File(Main.scrapMechanicFolder, SM_SHAPESETS_LOCATION);
        AFileManager.FILE_MANAGER.delete(shapeSetsFile);
        AFileManager.FILE_MANAGER.write(shapeSetsFile, result, FileOptions.CREATE);
    }

    private void updateIcons() {
        LOGGER.log(Level.FINE, "Updating icon maps");

        XMLIconMap vanillaIconMap = new XMLIconMap(new File(Main.vanillaFolder, SM_ICONMAP_LOCATION), new File(Main.vanillaFolder, SM_ICONMAP_XML_LOCATION));
        List<XMLIconMap> iconMaps = new ArrayList();
        iconMaps.add(vanillaIconMap);

        for (Modification activeModification : this.activeModifications) {
            File mod = new File(activeModification.getDirectory().getAbsolutePath() + ICONMAPS_RELATIVE);
            if (mod.exists()) {
                AFileManager.FILE_MANAGER.readList(mod).stream().map((modStr) -> modStr.replace("$THIS_MOD", Main.endSlash(Main.scrapMechanicFolder) + Constants.OBJECT_FOLDER_LOCATION + "\\" + activeModification.getName())).forEachOrdered((path) -> {
                    iconMaps.add(new XMLIconMap(new File(path.split(" : ")[1]), new File(path.split(" : ")[0])));
                });
            }
        }

        XMLIconMap combined = XMLIconMap.combineIconMaps(42, 96, 96, iconMaps);

        File smIconMap = new File(Main.scrapMechanicFolder, SM_ICONMAP_LOCATION);
        File smIconMapXML = new File(Main.scrapMechanicFolder, SM_ICONMAP_XML_LOCATION);
        AFileManager.FILE_MANAGER.write(smIconMapXML, combined.iconMapXML, FileOptions.CREATE, FileOptions.REPLACE);
        AFileManager.IMAGE_MANAGER.write(smIconMap.toPath(), combined.iconMapImage, "png");
    }

    private void updateInventoryDescriptions() {
        LOGGER.log(Level.FINE, "Updating inventory descriptions");
        String vanilla = AFileManager.FILE_MANAGER.readString(new File(Main.scrapMechanicFolder, SM_INVENTORYDESC_LOCATION)); //Already reset by MergeChanges.java
        List<String> mods = new ArrayList();
        for (Modification activeModification : this.activeModifications) {
            File mod = new File(activeModification.getDirectory().getAbsolutePath() + INVENTORYDESC_RELATIVE);
            if (mod.exists()) {
                mods.addAll(AFileManager.FILE_MANAGER.readList(mod).stream().map((modStr) -> modStr.replace("$THIS_MOD", activeModification.getDirectory().getAbsolutePath())).collect(Collectors.toList()));
            }
        }
        List<String> modsOut = new ArrayList();
        for (String mod : mods) {
            modsOut.add(AFileManager.FILE_MANAGER.readString(new File(mod)));
        }

        String result = vanilla.replace("}\n}", "}" + modsOut.stream().map((mod) -> ",\n" + mod).collect(Collectors.joining()) + "\n}");
        File smInventoryDesc = new File(Main.scrapMechanicFolder, SM_INVENTORYDESC_LOCATION);
        AFileManager.FILE_MANAGER.write(smInventoryDesc, result, FileOptions.CREATE, FileOptions.REPLACE);
    }

    private void cleanObjects() {
        LOGGER.log(Level.FINER, "Clearing Objects folder");
        File objectsFile = new File(Main.scrapMechanicFolder, Constants.OBJECT_FOLDER_LOCATION);
        if (objectsFile.exists()) {
            for (File subFile : objectsFile.listFiles()) {
                AFileManager.FILE_MANAGER.delete(subFile);
            }
        } else {
            AFileManager.FILE_MANAGER.createDirectory(objectsFile, FileOptions.DEPTH);
        }
    }

    private void cloneObjects() {
        LOGGER.log(Level.FINE, "Filling Objects folder");
        for (Modification activeModification : this.activeModifications) {
            File objectsFrom = new File(activeModification.getDirectory().getAbsolutePath() + OBJECTS_RELATIVE);
            if (objectsFrom.exists()) {
                File objectsTo = new File(Main.scrapMechanicFolder, Constants.OBJECT_FOLDER_LOCATION + "\\" + activeModification.getName());
                cloneObjectsRec(objectsFrom.getAbsolutePath(), objectsTo.getAbsolutePath(), "", activeModification.getName());
            }
        }
    }

    private void cloneObjectsRec(String from, String to, String addend, String modFolderName) {
        File thisFile = new File(from + addend);
        File outputFile = new File(to + OBJECTS_RELATIVE + addend);
        LOGGER.log(Level.FINEST, "      -Addend: {0}", addend);
        if (thisFile.isDirectory()) {
            AFileManager.FILE_MANAGER.createDirectory(outputFile, FileOptions.DEPTH);
            for (File file : thisFile.listFiles()) {
                cloneObjectsRec(from, to, addend + "\\" + file.getName(), modFolderName);
            }
        } else {
            LOGGER.log(Level.FINEST, "    Copying object: {0}", addend);
            AFileManager.FILE_MANAGER.write(outputFile, AFileManager.FILE_MANAGER.readString(thisFile).replace("$THIS_MOD", Constants.OBJECT_FOLDER_CODE + "/" + modFolderName), FileOptions.CREATE, FileOptions.REPLACE);
        }
    }
}

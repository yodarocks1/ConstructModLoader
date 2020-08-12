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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    //ItemNames
    public static final String SM_ITEMNAMES_LOCATION = "Survival\\CraftingRecipes\\item_names.json";

    //IconMaps
    public static final String ICONMAPS_RELATIVE = "\\Objects\\Database\\iconmaps.txt";
    public static final String SM_ICONMAP_LOCATION = "Survival\\Gui\\IconMapSurvival.png";
    public static final String SM_ICONMAP_XML_LOCATION = "Survival\\Gui\\IconMapSurvival.xml";

    //InventoryDescriptions
    public static final String INVENTORYDESC_RELATIVE = "\\Objects\\Database\\inventorydesc.txt";
    public static final String SM_INVENTORYDESC_LOCATION = "Survival\\Gui\\Language\\English\\inventoryDescriptions.json";

    //Objects
    public static final String OBJECTS_RELATIVE = "Objects";

    private final List<Item> items;
    private Map<Modification, File> activeModifications;

    public ApplyObjects(Map<Modification, File> activeModifications) {
        this.items = new ArrayList();
        this.activeModifications = activeModifications;
    }

    public ApplyObjects() {
        this.items = new ArrayList();
        this.activeModifications = new HashMap();
    }

    @Override
    public void apply() {
        this.cleanObjects();
        this.cloneObjects();
        this.updateItems();
        this.updateSurvivalItems();
        this.updateItemNames();
        this.updateShapeSets();
        this.updateIcons();
        this.updateInventoryDescriptions();
    }

    @Override
    public void setModifications(Map<Modification, File> activeModifications) {
        this.activeModifications = activeModifications;
    }

    private void updateSurvivalItems() {
        LOGGER.log(Level.FINE, "Updating survival items");
        String itemStr = "\n\n----------------------------------------\n-- modded.json\n----------------------------------------";
        itemStr = items.stream().map((item) -> "\n" + item.getName() + " = sm.uuid.new( \"" + item.getUuid() + "\" )").reduce(itemStr, String::concat);
        File SMSurvivalItems = new File(Main.scrapMechanicFolder, SM_SURVIVALITEMS_LOCATION);
        AFileManager.FILE_MANAGER.copy(new File(Main.vanillaFolder, SM_SURVIVALITEMS_LOCATION), SMSurvivalItems, FileOptions.REPLACE);
        AFileManager.FILE_MANAGER.write(SMSurvivalItems, itemStr, FileOptions.APPEND);
    }
    
    private void updateItemNames() {
        String moddedItems = ",\n\n\t//--------------------------------------\n\t// modded.json\n\t//--------------------------------------";
        moddedItems = items.stream().map((item) -> ",\n\"" + item.getUuid() + "\": \"" + item.getName() + "\"").reduce(moddedItems, String::concat);
        String allItems = AFileManager.FILE_MANAGER.readString(new File(Main.scrapMechanicFolder, SM_ITEMNAMES_LOCATION));
        allItems = allItems.replace("\n}", moddedItems + "\n}");
        AFileManager.FILE_MANAGER.write(new File(Main.scrapMechanicFolder, SM_ITEMNAMES_LOCATION), allItems, FileOptions.CREATE, FileOptions.REPLACE);
    }

    private void updateItems() {
        LOGGER.log(Level.FINE, "Updating items:");
        this.activeModifications.values().forEach((activeModification) -> {
            File shapesets = new File(activeModification.getAbsolutePath() + SHAPESETS_RELATIVE);
            List<String> lines;
            if (shapesets.exists()) {
                lines = AFileManager.FILE_MANAGER.readList(shapesets);
                lines.forEach((line) -> {
                    items.addAll(Item.fromJson(AFileManager.FILE_MANAGER.readString(new File(line.replace("$THIS_MOD", activeModification.getAbsolutePath())))));
                });
            }
        });
    }

    private void updateShapeSets() {
        LOGGER.log(Level.FINE, "Updating shape sets");
        String shapeSetsJson = AFileManager.FILE_MANAGER.readString(new File(Main.scrapMechanicFolder, SM_SHAPESETS_LOCATION)); //Already reset by MergeChanges.java
        String[] shapeSets = shapeSetsJson.substring(shapeSetsJson.indexOf("[") + 1, shapeSetsJson.indexOf("]")).replaceAll("\"", "").replaceAll("\t", "").split(",");
        String result = "{\n\t\"shapeSetList\": [\n";
        for (String shapeSet : shapeSets) {
            shapeSet = shapeSet.replace("\n", "").replace("\r", "").trim();
            if (!shapeSet.startsWith("//") && !shapeSet.isEmpty()) {
                result += "\t\t\"" + shapeSet.replace("\n", "").trim() + "\",\n";
            }
        }
        for (File activeModification : this.activeModifications.values()) {
            File shapesets = new File(activeModification.getAbsolutePath() + SHAPESETS_RELATIVE);
            if (shapesets.exists()) {
                result = AFileManager.FILE_MANAGER.readList(shapesets).stream().map((line) -> line.replace("$THIS_MOD", Constants.OBJECT_FOLDER_CODE + "/" + activeModification.getName())).collect(Collectors.toList()).stream().map((shapeSet) -> "\t\t\"" + shapeSet.replace("\n", "") + "\",\n").reduce(result, String::concat);
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

        this.activeModifications.values().forEach((activeModification) -> {
            File mod = new File(activeModification.getAbsolutePath() + ICONMAPS_RELATIVE);
            if (mod.exists()) {
                AFileManager.FILE_MANAGER.readList(mod).stream().map((modStr) -> modStr.replace("$THIS_MOD", Main.endSlash(Main.scrapMechanicFolder) + Constants.OBJECT_FOLDER_LOCATION + "\\" + activeModification.getName())).forEachOrdered((path) -> {
                    iconMaps.add(new XMLIconMap(new File(path.split(" : ")[1]), new File(path.split(" : ")[0])));
                });
            }
        });

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
        this.activeModifications.values().forEach((activeModification) -> {
            File mod = new File(activeModification.getAbsolutePath() + INVENTORYDESC_RELATIVE);
            if (mod.exists()) {
                mods.addAll(AFileManager.FILE_MANAGER.readList(mod).stream().map((modStr) -> modStr.replace("$THIS_MOD", activeModification.getAbsolutePath())).collect(Collectors.toList()));
            }
        });
        List<String> modsOut = new ArrayList();
        mods.forEach((mod) -> {
            modsOut.add(AFileManager.FILE_MANAGER.readString(new File(mod)));
        });

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
        this.activeModifications.entrySet().forEach((activeModification) -> {
            File objectsFrom = new File(activeModification.getValue(), OBJECTS_RELATIVE);
            if (objectsFrom.exists()) {
                File objectsTo = new File(Main.scrapMechanicFolder, Constants.OBJECT_FOLDER_LOCATION + "/" + activeModification.getKey().getName());
                cloneObjectsRec(objectsFrom, new File (objectsTo, OBJECTS_RELATIVE), activeModification.getKey().getName());
            }
        });
    }

    private void cloneObjectsRec(File from, File to, String modFolderName) {
        if (from.isDirectory()) {
            AFileManager.FILE_MANAGER.createDirectory(to, FileOptions.DEPTH);
            for (File subFile : from.listFiles()) {
                cloneObjectsRec(subFile, new File(to, subFile.getName()), modFolderName);
            }
        } else {
            LOGGER.log(Level.FINEST, "    Copying object file: {0}", from.getName());
            if (Constants.TEXT_FILE.accept(from.getParentFile(), from.getName())) {
                AFileManager.FILE_MANAGER.write(to, AFileManager.FILE_MANAGER.readString(from).replace("$THIS_MOD", Constants.OBJECT_FOLDER_CODE + "/" + modFolderName), FileOptions.CREATE, FileOptions.REPLACE);
            } else {
                AFileManager.FILE_MANAGER.copy(from, to, FileOptions.CREATE, FileOptions.REPLACE);
            }
        }
    }
}

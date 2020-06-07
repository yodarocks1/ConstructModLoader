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
        System.out.println("Updating survival items");
        String itemStr = "\n\n----------------------------------------\n-- modded.json\n----------------------------------------";
        itemStr = items.stream().map((item) -> "\n" + item.getName() + " = sm.uuid.new( \"" + item.getUuid() + "\" )").reduce(itemStr, String::concat);
        try {
            File SMSurvivalItems = new File(Main.scrapMechanicFolder + SM_SURVIVALITEMS_LOCATION);
            Apply.copyFile(new File(Main.vanillaFolder + SM_SURVIVALITEMS_LOCATION).toPath(), SMSurvivalItems.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Apply.writeFile(SMSurvivalItems.toPath(), itemStr, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(Apply.class.getName()).log(Level.SEVERE, "survival_items.lua does not exist", ex);
        }
    }

    private void updateItems() {
        System.out.println("Updating items:");
        for (Modification activeModification : this.activeModifications) {
            File shapesets = new File(activeModification.getDirectory().getAbsolutePath() + SHAPESETS_RELATIVE);
            List<String> lines;
            if (shapesets.exists()) {
                try {
                    lines = Apply.readFileLines(shapesets.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(Apply.class.getName()).log(Level.FINE, SHAPESETS_RELATIVE + " does not exist in modification " + activeModification.getName(), ex);
                    continue;
                }
                for (String line : lines) {
                    try {
                        items.addAll(Item.fromJson(Apply.readFile(new File(line.replace("$THIS_MOD", activeModification.getDirectory().getAbsolutePath())).toPath())));
                    } catch (IOException ex) {
                        Logger.getLogger(Apply.class.getName()).log(Level.FINE, "Shapeset " + line.replace("$THIS_MOD", Constants.OBJECT_FOLDER_LOCATION + "/" + activeModification.getName()) + " could not be read", ex);
                    }
                }
            }
        }
    }

    private void updateShapeSets() {
        System.out.println("Updating shape sets");
        try {
            String shapeSetsJson = Apply.readFile(new File(Main.scrapMechanicFolder + SM_SHAPESETS_LOCATION).toPath()); //Already reset by MergeChanges.java
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
                    for (String shapeSet : Apply.readFileLines(shapesets.toPath()).stream().map((line) -> line.replace("$THIS_MOD", Constants.OBJECT_FOLDER_CODE + "/" + activeModification.getName())).collect(Collectors.toList())) {
                        result += "\t\t\"" + shapeSet.replace("\n", "") + "\",\n";
                    }
                }
            }
            result = result.substring(0, result.lastIndexOf(",")) + "\n\t]\n}";
            File shapeSetsFile = new File(Main.scrapMechanicFolder + SM_SHAPESETS_LOCATION);
            Apply.deleteFile(shapeSetsFile.toPath());
            Apply.writeFile(shapeSetsFile.toPath(), result, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(Apply.class.getName()).log(Level.FINE, "Vanilla shapesets.json does not exist", ex);
        }
    }

    private void updateIcons() {
        System.out.println("Updating icon maps");
        
        XMLIconMap vanillaIconMap = new XMLIconMap(new File(Main.vanillaFolder + SM_ICONMAP_LOCATION), new File(Main.vanillaFolder + SM_ICONMAP_XML_LOCATION));
        List<XMLIconMap> iconMaps = new ArrayList();
        iconMaps.add(vanillaIconMap);
        
        for (Modification activeModification : this.activeModifications) {
            File mod = new File(activeModification.getDirectory().getAbsolutePath() + ICONMAPS_RELATIVE);
            if (mod.exists()) {
                try {
                    Apply.readFileLines(mod.toPath()).stream().map((modStr) -> modStr.replace("$THIS_MOD", Main.scrapMechanicFolder + Constants.OBJECT_FOLDER_LOCATION + "\\" + activeModification.getName())).forEachOrdered((path) ->  {
                        iconMaps.add(new XMLIconMap(new File(path.split(" : ")[1]), new File(path.split(" : ")[0])));
                    });
                } catch (IOException ex) {
                    Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, "Failed to read iconmaps.txt from " + activeModification.getName(), ex);
                }
            }
        }
        
        XMLIconMap combined = XMLIconMap.combineIconMaps(42, 96, 96, iconMaps);
        
        File smIconMap = new File(Main.scrapMechanicFolder + SM_ICONMAP_LOCATION);
        File smIconMapXML = new File(Main.scrapMechanicFolder + SM_ICONMAP_XML_LOCATION);
        try {
            Apply.writeFile(smIconMapXML.toPath(), combined.iconMapXML, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            Apply.writeImage(smIconMap.toPath(), combined.iconMapImage, "png");
        } catch (IOException ex) {
            Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateInventoryDescriptions() {
        System.out.println("Updating inventory descriptions");
        String vanilla = "";
        try {
            vanilla = Apply.readFile(new File(Main.scrapMechanicFolder + SM_INVENTORYDESC_LOCATION).toPath()); //Already reset by MergeChanges.java
        } catch (IOException ex) {
            Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> mods = new ArrayList();
        for (Modification activeModification : this.activeModifications) {
            File mod = new File(activeModification.getDirectory().getAbsolutePath() + INVENTORYDESC_RELATIVE);
            if (mod.exists()) {
                try {
                    mods.addAll(Apply.readFileLines(mod.toPath()).stream().map((modStr) -> modStr.replace("$THIS_MOD", activeModification.getDirectory().getAbsolutePath())).collect(Collectors.toList()));
                } catch (IOException ex) {
                    Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, "Failed to read inventorydesc.txt from " + activeModification.getName(), ex);
                }
            }
        }
        List<String> modsOut = new ArrayList();
        for (String mod : mods) {
            try {
                modsOut.add(Apply.readFile(new File(mod).toPath()));
            } catch (IOException ex) {
                Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, "Could not read inventory description file " + mod, ex);
            }
        }

        String result = vanilla.replace("}\n}", "}" + modsOut.stream().map((mod) -> ",\n" + mod).collect(Collectors.joining()) + "\n}");
        File smInventoryDesc = new File(Main.scrapMechanicFolder + SM_INVENTORYDESC_LOCATION);
        try {
            Apply.writeFile(smInventoryDesc.toPath(), result, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cleanObjects() {
        System.out.println("Clearing Objects folder");
        File objectsFile = new File(Main.scrapMechanicFolder + Constants.OBJECT_FOLDER_LOCATION);
        if (objectsFile.exists()) {
            for (File subFile : objectsFile.listFiles()) {
                try {
                    Apply.delete(subFile.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, "Failed to clean file at " + subFile.getAbsolutePath(), ex);
                }
            }
        } else {
            try {
                Apply.createDirectories(objectsFile.toPath());
            } catch (IOException ex) {
                Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, "Could not create objects folder at " + objectsFile.getAbsolutePath(), ex);
            }
        }
    }

    private void cloneObjects() {
        System.out.println("Filling Objects folder");
        for (Modification activeModification : this.activeModifications) {
            File objectsFrom = new File(activeModification.getDirectory().getAbsolutePath() + OBJECTS_RELATIVE);
            if (objectsFrom.exists()) {
                File objectsTo = new File(Main.scrapMechanicFolder + Constants.OBJECT_FOLDER_LOCATION + "\\" + activeModification.getName());
                cloneObjectsRec(objectsFrom.getAbsolutePath(), objectsTo.getAbsolutePath(), "", activeModification.getName());
            }
        }
    }

    private void cloneObjectsRec(String from, String to, String addend, String modFolderName) {
        File thisFile = new File(from + addend);
        File outputFile = new File(to + OBJECTS_RELATIVE + addend);
        System.out.println("      -Addend: " + addend);
        if (thisFile.isDirectory()) {
            try {
                Apply.createDirectories(outputFile.toPath());
            } catch (IOException ex) {
                Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, "Failed to create object directory at " + outputFile.getAbsolutePath(), ex);
            }
            for (File file : thisFile.listFiles()) {
                cloneObjectsRec(from, to, addend + "\\" + file.getName(), modFolderName);
            }
        } else {
            System.out.println("    Copying object: " + addend);
            try {
                Apply.writeFile(outputFile.toPath(), Apply.readFile(thisFile.toPath()).replace("$THIS_MOD", Constants.OBJECT_FOLDER_CODE + "/" + modFolderName), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (MalformedInputException ex) {
                try {
                    Apply.copyFile(thisFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex1) {
                    Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, "Failed to clone object from " + thisFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath(), ex1);
                }
            } catch (IOException ex) {
                Logger.getLogger(ApplyObjects.class.getName()).log(Level.SEVERE, "Failed to clone object from " + thisFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath(), ex);
            }
        }
    }
}

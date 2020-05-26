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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    public static final String SM_SHAPESETS_LOCATION = Main.scrapMechanicFolder + "Survival\\Objects\\Database\\shapesets.json";
    public static final String VANILLA_SHAPESETS_LOCATION = Main.vanillaFolder + "Survival\\Objects\\Database\\shapesets.json";

    //SurvivalItems
    public static final String SM_SURVIVALITEMS_LOCATION = Main.scrapMechanicFolder + "Survival\\Scripts\\game\\survival_items.lua";
    public static final String VANILLA_SURVIVALITEMS_LOCATION = Main.vanillaFolder + "Survival\\Scripts\\game\\survival_items.lua";

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
        this.updateSurvivalItems();
        this.updateShapeSets();
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
            File SMSurvivalItems = new File(SM_SURVIVALITEMS_LOCATION);
            Files.copy(new File(VANILLA_SURVIVALITEMS_LOCATION).toPath(), SMSurvivalItems.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.write(SMSurvivalItems.toPath(), itemStr.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(Apply.class.getName()).log(Level.SEVERE, "survival_items.lua does not exist", ex);
        }
    }

    private void updateItems() {
        System.out.println("Updating items:");
        for (Modification activeModification : this.activeModifications) {
            String fromTHISMOD = activeModification.getDirectory().getAbsolutePath().replaceAll("\\\\", "/");
            File shapesets = new File(activeModification.getDirectory().getAbsolutePath() + SHAPESETS_RELATIVE);
            List<String> lines;
            if (shapesets.exists()) {
                try {
                    lines = Files.readAllLines(shapesets.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(Apply.class.getName()).log(Level.FINE, SHAPESETS_RELATIVE + " does not exist in modification " + activeModification.getName(), ex);
                    continue;
                }
                for (String line : lines) {
                    try {
                        items.addAll(Item.fromJson(Files.readAllLines(new File(line.replaceAll("\\$THIS_MOD", fromTHISMOD)).toPath()).stream().collect(Collectors.joining("\n"))));
                    } catch (IOException ex) {
                        Logger.getLogger(Apply.class.getName()).log(Level.FINE, "Shapeset " + line.replaceAll("\\$THIS_MOD", fromTHISMOD) + " could not be read", ex);
                    }
                }
            }
        }
    }

    private void updateShapeSets() {
        System.out.println("Updating shape sets");
        try {
            String shapeSetsJson = Files.readAllLines(new File(VANILLA_SHAPESETS_LOCATION).toPath()).stream().collect(Collectors.joining("\n"));
            String[] shapeSets = shapeSetsJson.substring(shapeSetsJson.indexOf("[") + 1, shapeSetsJson.indexOf("]")).replaceAll("\"", "").replaceAll("\t", "").split(",");
            String result = "{\n\t\"shapeSetList\": [\n";
            for (String shapeSet : shapeSets) {
                result += "\t\t\"" + shapeSet.replace("\n", "") + "\",\n";
            }
            for (Modification activeModification : this.activeModifications) {
                String fromTHISMOD = activeModification.getDirectory().getAbsolutePath().replaceAll("\\\\", "/");
                File shapesets = new File(activeModification.getDirectory().getAbsolutePath() + SHAPESETS_RELATIVE);
                if (shapesets.exists()) {
                    for (String shapeSet : Files.readAllLines(shapesets.toPath()).stream().map((line) -> line.replaceFirst("\\$THIS_MOD", fromTHISMOD)).collect(Collectors.toList())) {
                        result += "\t\t\"" + shapeSet.replace("\n", "") + "\",\n";
                    }
                }
            }
            result = result.substring(0, result.lastIndexOf(",")) + "\n\t]\n}";
            File shapeSetsFile = new File(SM_SHAPESETS_LOCATION);
            shapeSetsFile.delete();
            Files.write(shapeSetsFile.toPath(), result.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(Apply.class.getName()).log(Level.FINE, "Vanilla shapesets.json does not exist", ex);
        }
    }
}

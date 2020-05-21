/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.Constants;
import cml.beans.Modification;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
public class ApplyLoot implements IApplicator {

    //SurvivalLoot
    public static final String LOOT_FOLDER_RELATIVE = "\\Loot\\";
    public static final String SM_LOOT_LOCATION = Constants.SCRAP_MECHANIC_FOLDER + "\\Survival\\Scripts\\game\\survival_loot.lua";
    public static final String VANILLA_LOOT_LOCATION = Constants.CONSTRUCT_FOLDER + "\\vanilla\\Scripts\\game\\survival_loot.lua";

    public List<Modification> activeModifications;

    public ApplyLoot(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    public ApplyLoot() {
        this.activeModifications = new ArrayList();
    }

    @Override
    public void apply() {
        this.applyLoot();
    }

    @Override
    public void setModifications(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    private void applyLoot() {
        System.out.println("Applying loot");
        List<String> loots = new ArrayList();
        for (Modification activeModification : this.activeModifications) {
            File lootDirectory = new File(activeModification.getDirectory().getAbsolutePath() + LOOT_FOLDER_RELATIVE);
            if (lootDirectory.exists()) {
                for (File loot : lootDirectory.listFiles()) {
                    System.out.println(" New loot " + loot.getName() + " found");
                    String fromTHISMOD = activeModification.getDirectory().getAbsolutePath().replaceAll("\\\\", "/");
                    try {
                        loots.add(Files.readAllLines(loot.toPath()).stream().collect(Collectors.joining("\n")).replaceAll("\\$THIS_MOD", fromTHISMOD));
                    } catch (IOException ex) {
                        Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Loot file " + loot.getName() + " in modification " + activeModification.getName() + " could not be read", ex);
                    }
                }
            }
        }

        String template = "";
        try {
            template = Files.readAllLines(new File(VANILLA_LOOT_LOCATION).toPath()).stream().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Vanilla survival_loot.lua does not exist", ex);
        }
        try {
            File outputFile = new File(SM_LOOT_LOCATION);
            outputFile.delete();
            Files.write(outputFile.toPath(), Apply.combineLua(template, loots.toArray(new String[0])).getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Could not write vanilla loot", ex);
        }

    }
}

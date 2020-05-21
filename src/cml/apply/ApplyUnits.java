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
public class ApplyUnits implements IApplicator {

    //VanillaUnits
    public static final String VANILLA_UNITS_RELATIVE = "\\Units\\Vanilla\\";
    public static final String SM_UNITS_LOCATION = Constants.SCRAP_MECHANIC_FOLDER + "\\Survival\\Scripts\\game\\units\\";
    public static final String VANILLA_UNITS_LOCATION = Constants.CONSTRUCT_FOLDER + "\\vanilla\\Scripts\\game\\units\\";

    //UnitUtil
    public static final String UNIT_UTIL_RELATIVE = "\\Units\\Vanilla\\unit_util.lua";
    public static final String SM_UNIT_UTIL_LOCATION = Constants.SCRAP_MECHANIC_FOLDER + "Survival\\Scripts\\game\\units\\unit_util.lua";
    public static final String VANILLA_UNIT_UTIL_LOCATION = Constants.CONSTRUCT_FOLDER + "vanilla\\Scripts\\game\\units\\unit_util.lua";

    private List<Modification> activeModifications;

    public ApplyUnits(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    public ApplyUnits() {
        this.activeModifications = new ArrayList();
    }

    @Override
    public void apply() {
        this.applyVanillaUnits();
        this.applyUnitUtil();
    }

    @Override
    public void setModifications(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    private void applyVanillaUnits() {
        System.out.println("Applying vanilla units");
        Map<String, List<String>> unitToScripts = new HashMap();
        for (Modification activeModification : this.activeModifications) {
            String fromTHISMOD = activeModification.getDirectory().getAbsolutePath().replaceAll("\\\\", "/");
            File vanillaUnitsDirectory = new File(activeModification.getDirectory().getAbsolutePath() + VANILLA_UNITS_RELATIVE);
            if (vanillaUnitsDirectory.exists()) {
                for (File vanillaUnit : vanillaUnitsDirectory.listFiles()) {
                    if (vanillaUnit.isDirectory()) {
                        unitToScripts.putIfAbsent(vanillaUnit.getName() + "Unit.lua", new ArrayList());
                        System.out.println(" New " + vanillaUnit.getName() + " code found");
                        try {
                            unitToScripts.get(vanillaUnit.getName() + "Unit.lua").add(Files.readAllLines(new File(vanillaUnit.getAbsolutePath() + "\\UnitScript.lua").toPath()).stream().collect(Collectors.joining("\n")).replaceAll("\\$THIS_MOD", fromTHISMOD));
                        } catch (IOException ex) {
                            Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Unit " + vanillaUnit.getName() + " in mod " + activeModification.getName() + " does not have a UnitScript", ex);
                        }
                    }
                }
            }
        }

        for (String unit : unitToScripts.keySet()) {
            String template = "";
            try {
                template = Files.readAllLines(new File(VANILLA_UNITS_LOCATION + unit).toPath()).stream().collect(Collectors.joining("\n"));
            } catch (IOException ex) {
                Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Vanilla unit " + unit + " does not exist", ex);
            }
            try {
                File outputFile = new File(SM_UNITS_LOCATION + unit);
                outputFile.delete();
                Files.write(outputFile.toPath(), Apply.combineLua(template, unitToScripts.get(unit).toArray(new String[0])).getBytes(), StandardOpenOption.CREATE);
            } catch (IOException ex) {
                Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Could not write vanilla unit " + unit, ex);
            }
        }
    }

    private void applyUnitUtil() {
        System.out.println("Applying vanilla unit util");
        List<String> unitUtilList = new ArrayList();
        for (Modification activeModification : this.activeModifications) {
            File unitUtil = new File(activeModification.getDirectory().getAbsolutePath() + UNIT_UTIL_RELATIVE);
            if (unitUtil.exists()) {
                String fromTHISMOD = activeModification.getDirectory().getAbsolutePath().replaceAll("\\\\", "/");
                try {
                    unitUtilList.add(Files.readAllLines(unitUtil.toPath()).stream().collect(Collectors.joining("\n")).replaceAll("\\$THIS_MOD", fromTHISMOD));
                } catch (IOException ex) {
                    Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Unit util does not exist in mod " + activeModification.getName(), ex);
                }
            }
        }
        String template = "";
        try {
            template = Files.readAllLines(new File(VANILLA_UNIT_UTIL_LOCATION).toPath()).stream().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Vanilla unit_util.lua does not exist", ex);
        }
        try {
            File outputFile = new File(SM_UNIT_UTIL_LOCATION);
            outputFile.delete();
            Files.write(outputFile.toPath(), Apply.combineLua(template, unitUtilList.toArray(new String[0])).getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(ApplyUnits.class.getName()).log(Level.SEVERE, "Could not write vanilla unit_util.lua", ex);
        }
    }

}

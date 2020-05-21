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
public class ApplyScripts implements IApplicator {

    public static final String SCRIPTS_RELATIVE = "Scripts\\";
    public static final String SM_SCRIPTS = Constants.SCRAP_MECHANIC_FOLDER + "Survival\\Scripts\\game\\";
    public static final String VANILLA_SCRIPTS = Constants.CONSTRUCT_FOLDER + "vanilla\\Scripts\\game\\";

    private List<Modification> activeModifications;

    public ApplyScripts(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    public ApplyScripts() {
        this.activeModifications = new ArrayList();
    }

    @Override
    public void apply() {
        this.applyScripts();
    }

    @Override
    public void setModifications(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    private void applyScripts() {
        System.out.println("Applying scripts");
        Map<String, List<String>> scriptToMods = new HashMap();
        for (File script : new File(SM_SCRIPTS).listFiles()) {
            if (!script.isDirectory()) {
                scriptToMods.put(script.getName(), new ArrayList());
            }
        }

        for (Modification activeModification : this.activeModifications) {
            File scripts = new File(activeModification.getDirectory().getAbsolutePath() + SCRIPTS_RELATIVE);
            if (scripts.exists()) {
                for (File script : scripts.listFiles()) {
                    try {
                        scriptToMods.get(script.getName()).add(Files.readAllLines(script.toPath()).stream().collect(Collectors.joining("\n")));
                    } catch (IOException ex) {
                        Logger.getLogger(ApplyScripts.class.getName()).log(Level.SEVERE, "Invalid script " + script.getName() + " in modification " + activeModification.getName(), ex);
                    }
                }
            }
        }

        for (String script : scriptToMods.keySet()) {
            File scriptFile = new File(VANILLA_SCRIPTS + script);
            if (scriptFile.exists()) {
                try {
                    File outputFile = new File(SM_SCRIPTS + script);
                    outputFile.delete();
                    Files.write(outputFile.toPath(), Apply.combineLua(Files.readAllLines(scriptFile.toPath()).stream().collect(Collectors.joining("\n")), scriptToMods.get(script).toArray(new String[0])).getBytes(), StandardOpenOption.CREATE);
                } catch (IOException ex) {
                    Logger.getLogger(ApplyScripts.class.getName()).log(Level.SEVERE, "Script " + script + " could not be written", ex);
                }
            } else if (!scriptToMods.getOrDefault(script, new ArrayList()).isEmpty()) {
                throw new UnsupportedOperationException("Script " + script + " is not yet supported for modification.");
            }
        }
    }

}

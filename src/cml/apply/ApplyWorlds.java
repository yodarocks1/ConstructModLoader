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
public class ApplyWorlds implements IApplicator {

    public static final String WORLDS_FOLDER_RELATIVE = "Worlds\\";
    public static final String SM_WORLDS_FOLDER = Constants.SCRAP_MECHANIC_FOLDER + "Survival\\Scripts\\game\\worlds\\";
    public static final String VANILLA_WORLDS_FOLDER = Constants.CONSTRUCT_FOLDER + "vanilla\\Scripts\\game\\worlds\\";

    private List<Modification> activeModifications;

    public ApplyWorlds(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    public ApplyWorlds() {
        this.activeModifications = new ArrayList();
    }

    @Override
    public void apply() {
        this.applyWorlds();
    }

    @Override
    public void setModifications(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    private void applyWorlds() {
        System.out.println("Applying world modifications");
        Map<String, List<String>> worldToMods = new HashMap();
        for (File world : new File(SM_WORLDS_FOLDER).listFiles()) {
            if (world.isFile()) {
                worldToMods.put(world.getName(), new ArrayList());
            }
        }

        for (Modification activeModification : this.activeModifications) {
            File worlds = new File(activeModification.getDirectory().getAbsolutePath() + WORLDS_FOLDER_RELATIVE);
            if (worlds.exists() && worlds.isDirectory()) {
                for (File world : worlds.listFiles()) {
                    try {
                        worldToMods.get(world.getName()).add(Files.readAllLines(world.toPath()).stream().collect(Collectors.joining("\n")));
                    } catch (IOException ex) {
                        Logger.getLogger(ApplyScripts.class.getName()).log(Level.SEVERE, "Invalid world " + world.getName() + " in modification " + activeModification.getName(), ex);
                    }
                }
            }
        }

        for (String world : worldToMods.keySet()) {
            File worldFile = new File(VANILLA_WORLDS_FOLDER + world);
            if (worldFile.exists()) {
                try {
                    File outputFile = new File(SM_WORLDS_FOLDER + world);
                    outputFile.delete();
                    Files.write(outputFile.toPath(), Apply.combineLua(Files.readAllLines(worldFile.toPath()).stream().collect(Collectors.joining("\n")), worldToMods.get(world).toArray(new String[0])).getBytes(), StandardOpenOption.CREATE);
                } catch (IOException ex) {
                    Logger.getLogger(ApplyScripts.class.getName()).log(Level.SEVERE, "World " + world + " could not be written", ex);
                }
            } else if (!worldToMods.getOrDefault(world, new ArrayList()).isEmpty()) {
                throw new UnsupportedOperationException("New worlds are not yet supported."); //Requires the deletion of worlds that are added by now-disabled mods.
            }
        }
    }

}

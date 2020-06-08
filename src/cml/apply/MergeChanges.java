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
package cml.apply;

import cml.Constants;
import cml.Main;
import cml.beans.ModIncompatibilityException;
import cml.beans.Modification;
import cml.lib.merges.fraser.neil.Patch;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class MergeChanges implements IApplicator {

    public static final String MERGE_FOLDER_RELATIVE = "\\Merge\\";
    public static final String PATCH_FOLDER_RELATIVE = "\\Patch\\";

    public static Map<String, List<Modification>> modifiedBy = new HashMap();

    public static Map<File, String> fileToPatched = new HashMap();

    
    private List<Modification> activeModifications;

    public MergeChanges(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    public MergeChanges() {
        this.activeModifications = new ArrayList();
    }

    @Override
    public void apply() {
        applyMerge();
    }

    @Override
    public void setModifications(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
        prepareMerge();
    }

    public void prepareMerge() {
        System.out.println("Preparing merge {Note: Merge must occur before all other IApplicators}");
        modifiedBy.clear();

        Map<Modification, Map<String, LinkedList<Patch>>> patchMap = new HashMap();
        Set<String> paths = new HashSet();
        for (Modification activeModification : activeModifications) {
            File mergeFolder = new File(activeModification.getDirectory().getAbsolutePath() + MERGE_FOLDER_RELATIVE);
            if (mergeFolder.exists()) {
                System.out.println("  Modification " + activeModification.getName() + " (Merge):");
                patchMap.put(activeModification, new HashMap());
                makePatchRecursive(mergeFolder.getAbsolutePath(), "", patchMap.get(activeModification), paths);
            }
            File patchFolder = new File(activeModification.getDirectory().getAbsolutePath() + PATCH_FOLDER_RELATIVE);
            if (patchFolder.exists()) {
                patchMap.putIfAbsent(activeModification, new HashMap());
                System.out.println("  Modification " + activeModification.getName() + " (Patch):");
                addPatchesRecursive(patchFolder.getAbsolutePath(), "", patchMap.get(activeModification), paths);
            }
        }

        fileToPatched = new HashMap();
        for (String path : paths) {
            String original = "";
            String result;
            File file = new File(Main.scrapMechanicFolder + path);
            File vanillaFile = new File(Main.vanillaFolder + path);
            try {
                if (vanillaFile.exists()) {
                    original = Apply.readFile(vanillaFile.toPath());
                }
                if (!file.exists()) {
                    Apply.createFile(file.toPath());
                }
            } catch (IOException ex) {
                Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Could not read/create file " + file.getAbsolutePath(), ex);
            }
            result = applyPatches(original, path, patchMap);
            fileToPatched.put(file, result);
        }
    }

    public void applyMerge() {
        System.out.println("Merging");
        writePatches(fileToPatched);
    }

    private void makePatchRecursive(String pathStart, String pathAddend, Map<String, LinkedList<Patch>> patchMap, Set<String> paths) {
        File thisFile = new File(pathStart + pathAddend);
        System.out.println("      -Addend: " + pathAddend);
        if (thisFile.isDirectory()) {
            for (File file : thisFile.listFiles()) {
                makePatchRecursive(pathStart, pathAddend + "\\" + file.getName(), patchMap, paths);
            }
        } else {
            if (!paths.contains(pathAddend)) {
                paths.add(pathAddend);
                System.out.print("    New ");
            } else {
                System.out.print("    Repeated ");
            }
            System.out.println("path found: " + pathAddend);
            makePatch(pathAddend, new File(Main.vanillaFolder + pathAddend), thisFile, patchMap);
        }
    }

    private void makePatch(String pathAddend, File vanilla, File modification, Map<String, LinkedList<Patch>> patchMap) {
        String vanillaStr = "";
        String modificationStr = "";
        if (vanilla.exists()) {
            try {
                vanillaStr = Apply.readFile(vanilla.toPath());
            } catch (IOException ex) {
                Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Could not read " + vanilla.getAbsolutePath(), ex);
            }
        }
        if (modification.exists()) {
            try {
                modificationStr = Apply.readFile(modification.toPath());
            } catch (IOException ex) {
                Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Could not read " + modification.getAbsolutePath(), ex);
            }
        }

        patchMap.put(pathAddend, Patch.make(vanillaStr, modificationStr));
    }
    
    private void addPatchesRecursive(String pathStart, String pathAddend, Map<String, LinkedList<Patch>> patchMap, Set<String> paths) {
        File thisFile = new File(pathStart + pathAddend);
        System.out.println("      -Addend: " + pathAddend);
        if (thisFile.isDirectory()) {
            for (File file : thisFile.listFiles()) {
                addPatchesRecursive(pathStart, pathAddend + "\\" + file.getName(), patchMap, paths);
            }
        } else {
            if (!paths.contains(pathAddend)) {
                paths.add(pathAddend);
                System.out.print("    New ");
            } else {
                System.out.print("    Repeated ");
            }
            System.out.println("path found: " + pathAddend);
            addPatch(pathAddend, thisFile, patchMap);
        }
    }
    
    private void addPatch(String pathAddend, File patchFile, Map<String, LinkedList<Patch>> patchMap) {
        try {
            patchMap.put(pathAddend, (LinkedList<Patch>) Patch.fromText(Apply.readFile(patchFile.toPath())));
        } catch (IOException ex) {
            Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Patch at " + patchFile.getAbsolutePath() + " failed to be added to patchMap", ex);
        }
    }

    private String applyPatches(String text1, String pathAddend, Map<Modification, Map<String, LinkedList<Patch>>> patchMap) {
        String finalText = text1;
        for (Modification mod : patchMap.keySet()) {
            if (!modifiedBy.containsKey(pathAddend)) {
                modifiedBy.put(pathAddend, new ArrayList());
            }
            modifiedBy.get(pathAddend).add(mod);
            LinkedList<Patch> patches = patchMap.get(mod).get(pathAddend);
            Main.patchOutputStream.println(Patch.toText(patches));
            Object[] result = Patch.apply(patches, finalText);
            boolean completedPatch = true;
            for (boolean b : ((boolean[]) result[1])) {
                if (!b) {
                    completedPatch = false;
                    break;
                }
            }
            if (!completedPatch) {
                System.out.println("Patch failed. Adding new Incompatibility (" + mod.getName() + ")");
                ModIncompatibilityException.addNewIncompatibility(pathAddend, modifiedBy.get(pathAddend));
            } else {
                finalText = (String) result[0];
            }
        }
        return finalText;
    }

    public void writePatches(Map<File, String> fileToPatched) {
        writePatchRecursive("", fileToPatched);
    }

    private void writePatchRecursive(String path, Map<File, String> fileToPatched) {
        for (String ignorePath : Constants.IGNORE_PATHS) {
            if (path.contains(ignorePath)) {
                System.out.println("  / Ignoring " + path);
                return;
            }
        }
        File smFile = new File(Main.scrapMechanicFolder + path);
        File vanillaFile = new File(Main.vanillaFolder + path);
        if (vanillaFile.exists() || fileToPatched.containsKey(smFile)) {
            if (smFile.isDirectory()) {
                for (String subPath : smFile.list()) {
                    writePatchRecursive(path + "\\" + subPath, fileToPatched);
                }
            } else if (fileToPatched.containsKey(smFile)) {
                try {
                    System.out.println("  + Writing  " + path);
                    Apply.writeFile(smFile.toPath(), fileToPatched.get(smFile), StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Failed to write to file " + smFile.getAbsolutePath(), ex);
                }
            } else if (smFile.length() != vanillaFile.length()) {
                try {
                    System.out.println("  ' Writing  " + path);
                    Apply.copyFile(vanillaFile.toPath(), smFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Failed to copy file from " + vanillaFile.getAbsolutePath() + " to " + smFile.getAbsolutePath(), ex);
                }
            }
        } else {
            System.out.println("  - Deleting old file at " + smFile.getAbsolutePath());
            try {
                Apply.delete(smFile.toPath());
            } catch (IOException ex) {
                Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Could not delete file at " + smFile.getAbsolutePath(), ex);
            }
        }
    }

}

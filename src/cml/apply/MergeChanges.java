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

import cml.Main;
import cml.beans.Modification;
import cml.from.fraser.neil.Patch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author benne
 */
public class MergeChanges implements IApplicator {

    public static final String MERGE_FOLDER_RELATIVE = "\\Merge\\";
    
    public static final List<String> IGNORE_PATHS = new ArrayList();
    
    public static Map<String, Modification> modifiedBy = new HashMap();
    
    public static Map<File, String> fileToPatched = new HashMap();
    
    static {
        IGNORE_PATHS.add("\\Logs");
        IGNORE_PATHS.add("\\Cache");
        IGNORE_PATHS.add("\\Challenges");
        IGNORE_PATHS.add("\\ChallengeData");
        IGNORE_PATHS.add("\\Data\\ExampleMods");
        IGNORE_PATHS.add("\\Data\\Terrain");
        IGNORE_PATHS.add("\\Survival\\Character\\Male");
        IGNORE_PATHS.add("\\Survival\\Terrain");
    }

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
        List<String> paths = new ArrayList();
        for (Modification activeModification : activeModifications) {
            File mergeFolder = new File(activeModification.getDirectory().getAbsolutePath() + MERGE_FOLDER_RELATIVE);
            if (mergeFolder.exists()) {
                System.out.println("  Modification " + activeModification.getName() + ":");
                patchMap.put(activeModification, new HashMap());
                makePatchRecursive(mergeFolder.getAbsolutePath(), "", patchMap.get(activeModification), paths);
            }
        }

        fileToPatched = new HashMap();
        for (String path : paths) {
            String original = "";
            String result;
            File file = new File(Main.scrapMechanicFolder + path);
            File vanillaFile = new File(Main.vanillaFolder + path);
            try {
                if (file.exists()) {
                    original = Files.readAllLines(vanillaFile.toPath()).stream().collect(Collectors.joining("\n"));
                } else {
                    Files.createDirectories(file.getParentFile().toPath());
                    file.createNewFile();
                }
            } catch (IOException ex) {
                Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Could not read/create file " + file.getAbsolutePath(), ex);
            }
            try {
                result = applyPatches(original, path, patchMap);
                fileToPatched.put(file, result);
            } catch (FailedPatchException ex) {
                Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Failed to apply a patch", ex);
            }
        }
    }
    
    public void applyMerge() {
        System.out.println("Merging");
        writePatches(fileToPatched);
    }

    private void makePatchRecursive(String pathStart, String pathAddend, Map<String, LinkedList<Patch>> patchMap, List<String> paths) {
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
            makePatch(new File(Main.vanillaFolder + pathAddend), thisFile, patchMap);
        }
    }

    private void makePatch(File vanilla, File modification, Map<String, LinkedList<Patch>> patchMap) {
        String vanillaStr = "";
        String modificationStr = "";
        if (vanilla.exists()) {
            try {
                vanillaStr = Files.readAllLines(vanilla.toPath()).stream().reduce("", String::concat);
            } catch (IOException ex) {
                Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Could not read " + vanilla.getAbsolutePath(), ex);
            }
        }
        if (modification.exists()) {
            try {
                modificationStr = Files.readAllLines(modification.toPath()).stream().reduce("", String::concat);
            } catch (IOException ex) {
                Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Could not read " + modification.getAbsolutePath(), ex);
            }
        }

        patchMap.put(vanilla.getAbsolutePath(), Patch.make(vanillaStr, modificationStr));
    }

    private String applyPatches(String text1, String pathAddend, Map<Modification, Map<String, LinkedList<Patch>>> patchMap) throws FailedPatchException {
        String finalText = text1;
        for (Modification mod : patchMap.keySet()) {
            modifiedBy.put(pathAddend, mod);
            LinkedList<Patch> patches = patchMap.get(mod).get(new File(Main.vanillaFolder + pathAddend).getAbsolutePath());
            Object[] result = Patch.apply(patches, finalText);
            boolean completedPatch = true;
            for (boolean b : ((boolean[]) result[1])) {
                if (!b) {
                    completedPatch = false;
                    break;
                }
            }
            if (!completedPatch) {
                throw new FailedPatchException(mod);
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
        for (String ignorePath : MergeChanges.IGNORE_PATHS) {
            if (path.startsWith(ignorePath)) {
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
                    System.out.println("    Writing new file to " + smFile.getAbsolutePath());
                    Files.write(smFile.toPath(), fileToPatched.get(smFile).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Failed to write to file " + smFile.getAbsolutePath(), ex);
                }
            } else if (smFile.length() != vanillaFile.length()) {
                try {
                    System.out.println("    Writing vanilla file to " + smFile.getAbsolutePath());
                    Files.write(smFile.toPath(), Files.readAllBytes(vanillaFile.toPath()), StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(MergeChanges.class.getName()).log(Level.SEVERE, "Failed to copy file from " + vanillaFile.getAbsolutePath() + " to " + smFile.getAbsolutePath(), ex);
                }
            }
        } else {
            System.out.println("    Deleting old file at " + smFile.getAbsolutePath());
            smFile.delete();
        }
    }

    public static class FailedPatchException extends Exception {

        public FailedPatchException(Modification mod) {
            super("Modification " + mod.getName() + " failed to patch");
        }
    }

}

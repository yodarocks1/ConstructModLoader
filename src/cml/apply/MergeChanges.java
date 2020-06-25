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
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import cml.lib.merges.fraser.neil.Patch;
import java.io.File;
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

    private static final Logger LOGGER = Logger.getLogger(MergeChanges.class.getName());
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
        LOGGER.log(Level.FINE, "Preparing merge {Note: Merge must occur before all other IApplicators}");
        modifiedBy.clear();

        Map<Modification, Map<String, LinkedList<Patch>>> patchMap = new HashMap();
        Set<String> paths = new HashSet();
        for (Modification activeModification : activeModifications) {
            File mergeFolder = new File(activeModification.getDirectory().getAbsolutePath() + MERGE_FOLDER_RELATIVE);
            if (mergeFolder.exists()) {
                LOGGER.log(Level.FINE, "  Modification {0} (Merge):", activeModification.getName());
                patchMap.put(activeModification, new HashMap());
                makePatchRecursive(mergeFolder.getAbsolutePath(), "", patchMap.get(activeModification), paths);
            }
            File patchFolder = new File(activeModification.getDirectory().getAbsolutePath() + PATCH_FOLDER_RELATIVE);
            if (patchFolder.exists()) {
                patchMap.putIfAbsent(activeModification, new HashMap());
                LOGGER.log(Level.FINE, "  Modification {0} (Patch):", activeModification.getName());
                addPatchesRecursive(patchFolder.getAbsolutePath(), "", patchMap.get(activeModification), paths);
            }
        }

        fileToPatched = new HashMap();
        for (String path : paths) {
            String original = "";
            String result;
            File file = new File(Main.scrapMechanicFolder, path);
            File vanillaFile = new File(Main.vanillaFolder, path);
            if (vanillaFile.exists()) {
                original = AFileManager.FILE_MANAGER.readString(vanillaFile);
            }
            if (!file.exists()) {
                AFileManager.FILE_MANAGER.create(file);
            }
            result = applyPatches(original, path, patchMap);
            fileToPatched.put(file, result);
        }
    }

    public void applyMerge() {
        LOGGER.log(Level.FINER, "Merging");
        writePatches(fileToPatched);
    }

    private void makePatchRecursive(String pathStart, String pathAddend, Map<String, LinkedList<Patch>> patchMap, Set<String> paths) {
        File thisFile = new File(pathStart + pathAddend);
        LOGGER.log(Level.FINEST, "      -Addend: {0}", pathAddend);
        if (thisFile.isDirectory()) {
            for (File file : thisFile.listFiles()) {
                makePatchRecursive(pathStart, pathAddend + "\\" + file.getName(), patchMap, paths);
            }
        } else {
            if (!paths.contains(pathAddend)) {
                paths.add(pathAddend);
            }
            LOGGER.log(Level.FINEST, "Path found: {0}", pathAddend);
            makePatch(pathAddend, new File(Main.vanillaFolder, pathAddend), thisFile, patchMap);
        }
    }

    private void makePatch(String pathAddend, File vanilla, File modification, Map<String, LinkedList<Patch>> patchMap) {
        String vanillaStr = "";
        String modificationStr = "";
        if (vanilla.exists()) {
            vanillaStr = AFileManager.FILE_MANAGER.readString(vanilla);
        }
        if (modification.exists()) {
            modificationStr = AFileManager.FILE_MANAGER.readString(modification);
        }

        patchMap.put(pathAddend, Patch.make(vanillaStr, modificationStr));
    }
    
    private void addPatchesRecursive(String pathStart, String pathAddend, Map<String, LinkedList<Patch>> patchMap, Set<String> paths) {
        File thisFile = new File(pathStart + pathAddend);
        LOGGER.log(Level.FINEST, "      -Addend: {0}", pathAddend);
        if (thisFile.isDirectory()) {
            for (File file : thisFile.listFiles()) {
                addPatchesRecursive(pathStart, pathAddend + "\\" + file.getName(), patchMap, paths);
            }
        } else {
            if (!paths.contains(pathAddend)) {
                paths.add(pathAddend);
            }
            LOGGER.log(Level.FINEST, "Path found: {0}", pathAddend);
            addPatch(pathAddend, thisFile, patchMap);
        }
    }
    
    private void addPatch(String pathAddend, File patchFile, Map<String, LinkedList<Patch>> patchMap) {
        patchMap.put(pathAddend, (LinkedList<Patch>) Patch.fromText(AFileManager.FILE_MANAGER.readString(patchFile)));
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
                LOGGER.log(Level.WARNING, "Patch failed. Adding new Incompatibility ({0}) for path \"{1}\"", new Object[]{mod.getName(), pathAddend});
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
                LOGGER.log(Level.FINER, "  / Ignoring {0}", path);
                return;
            }
        }
        File smFile = new File(Main.scrapMechanicFolder, path);
        File vanillaFile = new File(Main.vanillaFolder, path);
        if (vanillaFile.exists() || fileToPatched.containsKey(smFile)) {
            if (smFile.isDirectory()) {
                for (String subPath : smFile.list()) {
                    writePatchRecursive(path + "\\" + subPath, fileToPatched);
                }
            } else if (fileToPatched.containsKey(smFile)) {
                LOGGER.log(Level.FINER, "  + Writing  {0}", path);
                AFileManager.FILE_MANAGER.write(smFile, fileToPatched.get(smFile), FileOptions.REPLACE);
            } else if (smFile.length() != vanillaFile.length()) {
                LOGGER.log(Level.FINER, "  '' Writing  {0}", path);
                AFileManager.FILE_MANAGER.copy(vanillaFile, smFile, FileOptions.REPLACE);
            }
        } else {
            LOGGER.log(Level.FINER, "  - Deleting old file at {0}", smFile.getAbsolutePath());
            AFileManager.FILE_MANAGER.delete(smFile);
        }
    }

}

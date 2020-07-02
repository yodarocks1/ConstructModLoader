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
import cml.beans.ModIncompatibilityException;
import cml.beans.Modification;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class ReplaceChanges implements IApplicator {

    private static final Logger LOGGER = Logger.getLogger(ReplaceChanges.class.getName());
    public static final String REPLACE_FOLDER_RELATIVE = "\\Replace\\";
    public static Map<String, List<Modification>> replacedBy = new HashMap();

    private List<Modification> activeModifications = new ArrayList();

    private Map<File, File> newToOld = new HashMap();

    @Override
    public void apply() {
        applyReplacement();
    }

    @Override
    public void setModifications(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
        prepareReplacement();
    }

    public void prepareReplacement() {
        LOGGER.log(Level.FINE, "Preparing to replace");
        for (Modification activeModification : activeModifications) {
            File replaceFolder = new File(activeModification.getDirectory().getAbsolutePath() + REPLACE_FOLDER_RELATIVE);
            if (replaceFolder.exists()) {
                LOGGER.log(Level.FINER, "  Modification {0}:", activeModification.getName());
                prepareReplacementRec(replaceFolder.getAbsolutePath(), "", activeModification);
            }
        }
    }

    private void prepareReplacementRec(String directory, String addend, Modification mod) {
        File file = new File(directory + addend);
        File newFile = new File(Main.scrapMechanicFolder, addend);
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                prepareReplacementRec(directory, addend + "\\" + subFile.getName(), mod);
            }
        } else {
            LOGGER.log(Level.SEVERE, "Incompatibility found in file {0}{1}", new Object[]{directory, addend});
            if (!replacedBy.containsKey(addend)) {
                replacedBy.put(addend, new ArrayList());
                replacedBy.get(addend).add(mod);
            } else {
                replacedBy.get(addend).add(mod);
                ModIncompatibilityException.addNewIncompatibility(addend, true, replacedBy.get(addend));
            }
            newToOld.put(newFile, file);
            if (MergeChanges.modifiedBy.containsKey(addend)) {
                ModIncompatibilityException.addNewIncompatibility(addend, true, replacedBy.get(addend));
                ModIncompatibilityException.addNewIncompatibility(addend, true, MergeChanges.modifiedBy.get(addend));
            }
        }
    }

    public void applyReplacement() {
        LOGGER.log(Level.FINE, "Replacing");
        newToOld.keySet().forEach((newFile) -> {
            AFileManager.FILE_MANAGER.copy(newToOld.get(newFile), newFile, FileOptions.REPLACE);
            LOGGER.log(Level.FINEST, "Replace: {0}", newFile.getAbsolutePath());
        });
    }

}

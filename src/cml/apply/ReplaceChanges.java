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
        System.out.println("Preparing to replace");
        for (Modification activeModification : activeModifications) {
            File replaceFolder = new File(activeModification.getDirectory().getAbsolutePath() + REPLACE_FOLDER_RELATIVE);
            if (replaceFolder.exists()) {
                System.out.println("  Modification " + activeModification.getName() + ":");
                prepareReplacementRec(replaceFolder.getAbsolutePath(), "", activeModification);
            }
        }
    }

    private void prepareReplacementRec(String directory, String addend, Modification mod) {
        File file = new File(directory + addend);
        File newFile = new File(Main.scrapMechanicFolder + addend);
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                prepareReplacementRec(directory, addend + "\\" + subFile.getName(), mod);
            }
        } else {
            if (!replacedBy.containsKey(addend)) {
                replacedBy.put(addend, new ArrayList());
            } else {
                ModIncompatibilityException.addNewIncompatibility(addend, true, replacedBy.get(addend));
            }
            replacedBy.get(addend).add(mod);
            newToOld.put(newFile, file);
            if (MergeChanges.modifiedBy.containsKey(addend)) {
                ModIncompatibilityException.addNewIncompatibility(addend, true, replacedBy.get(addend));
                ModIncompatibilityException.addNewIncompatibility(addend, true, MergeChanges.modifiedBy.get(addend));
            }
        }
    }

    public void applyReplacement() {
        System.out.println("Replacing");
        for (File newFile : newToOld.keySet()) {
            try {
                Apply.copyFile(newToOld.get(newFile).toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(ReplaceChanges.class.getName()).log(Level.SEVERE, "Failed to replace file " + newFile.getAbsolutePath(), ex);
            }
        }
    }

}

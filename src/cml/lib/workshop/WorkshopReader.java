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
package cml.lib.workshop;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

/**
 *
 * @author benne
 */
public class WorkshopReader {
    
    private static final Logger LOGGER = Logger.getLogger(WorkshopReader.class.getName());
    
    private final List<WorkshopMod> allWorkshop = new ArrayList();
    private final ListProperty<WorkshopMod> modCollector = new SimpleListProperty(FXCollections.observableArrayList());
    private final File workshopFolder;
    
    public WorkshopReader(File workshopFolder) {
        this.workshopFolder = workshopFolder;
        reloadAllMods();
        
    }
    
    private void reloadAllMods() {
        LOGGER.log(Level.INFO, "Reloading all workshop mods");
        allWorkshop.clear();
        for (File workshopMod : workshopFolder.listFiles()) {
            allWorkshop.add(WorkshopMod.createSafely(workshopMod));
        }
        List<WorkshopMod> modsOnly = allWorkshop.stream().filter((workshopMod) -> workshopMod != null && workshopMod.isApplicable()).collect(Collectors.toList());
        modsOnly.sort(Comparator.comparingInt(WorkshopMod::getWorkshopId));
        modCollector.setAll(modsOnly);
    }

    /**
     * 
     * @return All workshop items
     */
    public List<WorkshopMod> getAllWorkshop() {
        return allWorkshop;
    }

    /**
     * 
     * @return All workshop mods
     */
    public ListProperty<WorkshopMod> getItems() {
        return modCollector;
    }
    
    public void addListener(ListChangeListener<? super WorkshopMod> listener) {
        modCollector.addListener(listener);
    }

    /**
     * 
     * @return The workshop folder that this WorkshopReader reads from
     */
    public File getWorkshopFolder() {
        return workshopFolder;
    }
    
    /**
     * Reloads all mods
     */
    public void reload() {
        reloadAllMods();
    }
    
}

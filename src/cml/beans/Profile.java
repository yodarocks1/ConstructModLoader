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
package cml.beans;

import cml.Constants;
import cml.Main;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import cml.lib.lazyupdate.FlagUpdater;
import cml.lib.lazyupdate.Flags;
import cml.lib.xmliconmap.CMLIcon;
import cml.lib.xmliconmap.CMLIconMap;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javax.management.openmbean.KeyAlreadyExistsException;

/**
 *
 * @author benne
 */
public abstract class Profile {
    
    static {
        FlagUpdater.setListener(Flags.staticFlags(Profile.class), (entry) -> {
            switch (entry.getKey()) {
                case "DO_UPDATE":
                    Platform.runLater(() -> Main.updateProfileList());
                    break;
            }
        });
    }

    /**
     * An <code>InvalidProfile</code> that represents an absence of definition.
     * Typically used for default values.
     * <p>
     * This {@link Profile} is immutable.
     */
    public static final Profile EMPTY = new InvalidProfile(CMLIconMap.ICON_MAP.BLANK.getIcon(0), "<< Please select a profile! >>");
    /**
     * An <code>InvalidProfile</code> that represents an invalid definition.
     * <p>
     * This {@link Profile} is immutable.
     */
    public static final Profile INVALID = new InvalidProfile(CMLIconMap.ICON_MAP.DELETE_C.getIcon(CMLIcon.State.HOVER), "<< Invalid profile! >>");
    public static final String CACHE_HASH_LOC = "cachehash.dat";
    public static final String SELECTED_LOC = "selected.asc";
    
    static final String ICON_RELATIVE = "icon.png";
    static final String DESC_RELATIVE = "description.txt";
    
    final ObjectProperty<Image> icon = new SimpleObjectProperty();
    final StringProperty name;
    final StringProperty description;
    
    Profile(String name) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty();
    }
    
    public abstract void setModifications(List<Modification> modifications);
    public abstract List<Modification> getModifications();
    public abstract void updateModifications();
    public abstract List<Modification> getActiveModifications();

    public Image getIconSafe() {
        return (icon.get() == null) ? CMLIconMap.ICON_MAP.BLANK.getIcon(0) : icon.get();
    }

    public Image getIcon() {
        return icon.get();
    }

    public void setIcon(Image icon) {
        if (icon != null) {
            this.icon.setValue(icon);
        } else {
            this.icon.setValue(CMLIconMap.ICON_MAP.BLANK.getIcon(0));
        }
        Flags.setFlag(Flags.staticFlags(Profile.class), Flags.Flag.DO_UPDATE);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        Profile.PROFILES.remove(name);
        Profile.PROFILES.put(name, this);
        this.name.setValue(name);
        Flags.setFlag(Flags.staticFlags(this), Flags.Flag.DO_UPDATE);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.setValue(description);
        
    }

    public abstract File getDirectory();
    public abstract void delete();

    public void saveAsSelected() {
        saveAsSelected(this);
    }

    @Override
    public String toString() {
        return getName();
    }

    public StringProperty getNameProperty() {
        return name;
    }

    public StringProperty getDescProperty() {
        return description;
    }

    public ObjectProperty<Image> getIconProperty() {
        return icon;
    }

    public abstract String getLeft();
    public abstract String getCenter();
    public abstract String getRight();

    public static void saveAsSelected(Profile profile) {
        if (profile == null || profile.getDirectory() == null) {
            AFileManager.FILE_MANAGER.delete(new File(Constants.API_DIRECTORY, Profile.SELECTED_LOC));
            return;
        }
        AFileManager.FILE_MANAGER.write(new File(Constants.API_DIRECTORY, Profile.SELECTED_LOC), profile.getName(), FileOptions.REPLACE);
    }
    
    
    //
    
    private static final Map<String, Profile> PROFILES = new HashMap();
    
    /**
     * Get a profile by name
     * @param name The profile name to find
     * @return The profile with the given name if it exists; otherwise, returns
     * {@link #INVALID}
     */
    public static Profile get(String name) {
        return PROFILES.getOrDefault(name, INVALID);
    }
    /**
     * Get a profile by name
     * @param directory The profile name to find (uses directory name)
     * @return The profile with the given name if it exists; otherwise, returns
     * {@link #INVALID}
     */
    public static Profile get(File directory) {
        return get(directory.getName());
    }
    
    /**
     * Get a profile by name if it exists; otherwise, create it.
     * @param name The profile name to find
     * @param parentDirectory The directory that the profile should be a child to
     * if it is to be created.
     * @return The profile with the given name if it exists; otherwise, a new profile
     * with the given name.
     */
    public static Profile getOrCreate(String name, File parentDirectory) {
        if (PROFILES.containsKey(name)) {
            return PROFILES.get(name);
        } else {
            PROFILES.put(name, create(new File(parentDirectory, name)));
            return PROFILES.get(name);
        }
    }
    /**
     * Get a profile by name if it exists; otherwise, create it.
     * @param directory The directory that represents the profile; directory name
     * is used for the search.
     * @return The profile with the given name if it exists; otherwise, a new profile
     * at the given directory.
     */
    public static Profile getOrCreate(File directory) {
        return getOrCreate(directory.getName(), directory.getParentFile());
    }
    
    /**
     * Creates a profile at the given location
     * @param directory The location at which the directory should be created
     * @return The new profile
     * @throws KeyAlreadyExistsException If the profile already exists
     */
    public static Profile create(File directory) {
        if (directory != null && PROFILES.containsKey(directory.getName())) {
            throw new KeyAlreadyExistsException();
        }
        if (directory != null) {
            return new ValidProfile(directory);
        }
        return INVALID;
    }
    
    /**
     * Check if a profile is valid
     * @param profile The profile to check
     * @return Whether the profile is valid
     */
    public static boolean isValid(Profile profile) {
        return profile instanceof ValidProfile;
    }

}

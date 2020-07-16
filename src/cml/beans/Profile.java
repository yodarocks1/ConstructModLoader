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
import cml.lib.files.ZipManager;
import cml.lib.workshop.WorkshopConnectionHandler;
import cml.lib.xmliconmap.CMLIcon;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
public class Profile {

    public static final Profile EMPTY = new Profile(false);
    public static final Profile DELETED = new Profile(true);

    private static final Logger LOGGER = Logger.getLogger(Profile.class.getName());
    private static final String ICON_RELATIVE = "icon.png";
    private static final String DESC_RELATIVE = "description.txt";

    private List<Modification> modifications;
    private ObjectProperty<Image> icon = new SimpleObjectProperty();
    private StringProperty name;
    private StringProperty description;
    private File directory;

    public Profile(File directory) {
        this.directory = directory;
        this.modifications = new ArrayList();
        for (File zipFile : directory.listFiles(ZipManager.ZIP_FILTER)) {
            if (zipFile.isFile()) {
                AFileManager.ZIP_MANAGER.unzip(zipFile, new File(directory, zipFile.getName().replace(".zip", "").replace('.', ' ')));
                AFileManager.FILE_MANAGER.delete(zipFile);
            }
        }
        for (File file : directory.listFiles()) {
            if (file.isDirectory() && !(file.getName().startsWith(Constants.IGNORE_PREFIX) && file.getName().endsWith(Constants.IGNORE_SUFFIX))) {
                this.modifications.add(new Modification(file));
            }
        }
        try {
            File iconFile = new File(directory, ICON_RELATIVE);
            if (iconFile.exists()) {
                this.icon.setValue(new Image(iconFile.toURI().toString()));
            } else {
                this.icon.setValue(Main.ICON_MAP.BLANK.getIcon(0));
            }
        } catch (NullPointerException ex) {
            this.icon.setValue(Main.ICON_MAP.BLANK.getIcon(0));
        }
        this.name = new SimpleStringProperty(directory.getName());
        try {
            this.description = new SimpleStringProperty(Files.readAllLines(new File(directory, DESC_RELATIVE).toPath()).get(0));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Profile {0} does not have a description.", this.name);
        } catch (IndexOutOfBoundsException ex) {
            this.description = new SimpleStringProperty("");
        }
    }

    private Profile(boolean isDeleted) {
        this.modifications = new ArrayList();
        if (isDeleted) {
            this.icon.setValue(Main.ICON_MAP.DELETE_C.getIcon(CMLIcon.State.HOVER));
            this.name = new SimpleStringProperty("<< Invalid profile! >>");
        } else {
            this.icon.setValue(Main.ICON_MAP.BLANK.getIcon(0));
            this.name = new SimpleStringProperty("<< Please select a profile! >>");
        }
        this.directory = null;
        this.description = new SimpleStringProperty("");
    }

    public void setModifications(List<Modification> modifications) {
        this.modifications = modifications;
        if (Main.activeProfile.get().equals(this)) {
            Main.activeModifications = getActiveModifications();
        }
    }

    public List<Modification> getModifications() {
        return modifications;
    }

    public List<Modification> getActiveModifications() {
        List<Modification> active = new ArrayList();
        modifications.stream().filter((mod) -> (mod.isEnabled())).forEachOrdered(active::add);
        return active;
    }

    public Image getIcon() {
        return icon.get();
    }

    public void setIcon(Image icon) {
        if (icon != null) {
            this.icon.setValue(icon);
            AFileManager.IMAGE_MANAGER.write(new File(directory, "icon.png"), SwingFXUtils.fromFXImage(icon, null), "png");
        } else {
            this.icon.setValue(Main.ICON_MAP.BLANK.getIcon(0));
            AFileManager.FILE_MANAGER.delete(new File(directory, "icon.png"));
        }
        Main.updateProfileList();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.setValue(name);
        File newDirectory = new File(directory.getParentFile(), name);
        AFileManager.FILE_MANAGER.copyDirectory(directory, newDirectory, FileOptions.DEPTH, FileOptions.CREATE);
        AFileManager.FILE_MANAGER.deleteDirectory(directory, FileOptions.DEPTH);
        this.directory = newDirectory;
        Main.updateProfileList();
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.setValue(description);
        AFileManager.FILE_MANAGER.write(new File(directory, DESC_RELATIVE), description, FileOptions.REPLACE);
        Main.updateProfileList();
    }

    public File getDirectory() {
        return directory;
    }

    public void delete() {
        if (directory != null) {
            this.modifications.forEach(WorkshopConnectionHandler::disconnect);
            AFileManager.FILE_MANAGER.deleteDirectory(directory, FileOptions.DEPTH);
        }
    }

    @Override
    public String toString() {
        return name.get();
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
    
    
}

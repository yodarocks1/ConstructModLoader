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
import cml.Images;
import static cml.Images.BLANK;
import cml.Main;
import cml.apply.Apply;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
public class Profile {

    public static final Profile EMPTY = new Profile(false);
    public static final Profile DELETED = new Profile(true);

    private static final String ICON_RELATIVE = "\\icon.png";
    private static final String DESC_RELATIVE = "\\description.txt";

    private List<Modification> modifications;
    private Image icon;
    private String name;
    private String description;
    private final File directory;

    public Profile(File directory) {
        this.directory = directory;
        this.modifications = new ArrayList();
        for (File file : directory.listFiles()) {
            if (file.isDirectory() && !(file.getName().startsWith(Constants.IGNORE_PREFIX) && file.getName().endsWith(Constants.IGNORE_SUFFIX))) {
                this.modifications.add(new Modification(file));
            }
        }
        try {
            File iconFile = new File(directory.getAbsolutePath() + ICON_RELATIVE);
            if (iconFile.exists()) {
                this.icon = new Image(iconFile.toURI().toString());
            } else {
                this.icon = BLANK;
            }
        } catch (NullPointerException ex) {
            this.icon = BLANK;
        }
        this.name = directory.getName();
        try {
            this.description = Files.readAllLines(new File(directory.getAbsolutePath() + DESC_RELATIVE).toPath()).get(0);
        } catch (IOException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.WARNING, "Profile {0} does not have a description.", this.name);
        }
    }

    private Profile(boolean isDeleted) {
        this.modifications = new ArrayList();
        if (isDeleted) {
            this.icon = Images.DELETE_PRESS_START;
            this.name = "<< Invalid profile! >>";
        } else {
            this.icon = BLANK;
            this.name = "<< Please select a profile! >>";
        }
        this.directory = null;
        this.description = "";
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
        modifications.stream().filter((mod) -> (mod.isEnabled())).forEachOrdered((mod) -> {
            active.add(mod);
        });
        return active;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public File getDirectory() {
        return directory;
    }

    public void delete() {
        if (directory != null) {
            try {
                Apply.deleteDirectory(directory.toPath());
            } catch (IOException ex) {
                Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, "Failed to delete profile " + directory.getName(), ex);
            }
        }
    }
}

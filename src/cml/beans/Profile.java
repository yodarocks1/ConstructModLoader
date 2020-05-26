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

import static cml.Images.BLANK;
import cml.Main;
import cml.beans.Modification;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
public class Profile {

    public static final Profile EMPTY = new Profile();

    private List<Modification> modifications;
    private Image icon;
    private String name;
    private final File directory;

    public Profile(File directory) {
        this.directory = directory;
        this.modifications = new ArrayList();
        for (File file : directory.listFiles()) {
            if (file.isDirectory() && !file.getName().startsWith("IGN") && !file.getName().endsWith("IGN")) {
                this.modifications.add(new Modification(file));
            }
        }
        try {
            this.icon = new Image(new File(directory.getAbsolutePath() + "\\icon.png").toURI().toString());
        } catch (NullPointerException ex) {
            this.icon = BLANK;
        }
        this.name = directory.getName();
    }

    private Profile() {
        this.modifications = new ArrayList();
        this.icon = BLANK;
        this.name = "<< Profile Name >>";
        this.directory = null;
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

    public File getDirectory() {
        return directory;
    }
}

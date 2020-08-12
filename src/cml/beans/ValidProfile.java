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
import static cml.beans.Profile.DESC_RELATIVE;
import cml.lib.files.AFileManager;
import cml.lib.files.ZipManager;
import cml.lib.lazyupdate.Flags;
import cml.lib.workshop.WorkshopConnectionHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
final class ValidProfile extends Profile {
    
    private static final Logger LOGGER = Logger.getLogger(ValidProfile.class.getName());

    private List<Modification> modifications;
    private String left;
    private String center;
    private String right;
    private File directory;
    
    private boolean doModsUpdate = false;

    public ValidProfile(File directory) {
        super(directory.getName());
        
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
                this.icon.setValue(null);
            }
        } catch (NullPointerException ex) {
            this.icon.setValue(null);
        }
        try {
            this.description.setValue(Files.readAllLines(new File(directory, DESC_RELATIVE).toPath()).get(0));
            String desc = this.getDescription();
            int leftIndex = desc.indexOf("Menu Left: ");
            if (leftIndex != -1) {
                this.left = desc.substring(leftIndex + 11, desc.indexOf("\n", leftIndex));
            }
            int centerIndex = desc.indexOf("Menu Center: ");
            if (centerIndex != -1) {
                this.center = desc.substring(centerIndex + 13, desc.indexOf("\n", centerIndex));
            }
            int rightIndex = desc.indexOf("Menu Right: ");
            if (rightIndex != -1) {
                this.right = desc.substring(rightIndex + 12, desc.indexOf("\n", rightIndex));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Profile {0} does not have a description.", this.getName());
        } catch (IndexOutOfBoundsException ex) {
            this.description.setValue("");
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Modifications">
    @Override
    public void setModifications(List<Modification> modifications) {
        this.modifications = modifications;
        Flags.setFlag(Flags.localFlags(this), Flags.Flag.DO_UPDATE);
    }
    
    @Override
    public List<Modification> getModifications() {
        if (Flags.clearFlag(Flags.localFlags(this), Flags.Flag.DO_UPDATE)) {
            updateModifications();
        }
        return modifications;
    }
    
    @Override
    public List<Modification> getActiveModifications() {
        List<Modification> active = new ArrayList();
        modifications.stream().filter((mod) -> (mod.isEnabled())).forEachOrdered(active::add);
        return active;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Icon">
    @Override
    public void setIcon(Image icon) {
        if (icon != null) {
            AFileManager.IMAGE_MANAGER.write(new File(directory, "icon.png"), SwingFXUtils.fromFXImage(icon, null), "png");
        } else {
            AFileManager.FILE_MANAGER.delete(new File(directory, "icon.png"));
        }
        super.setIcon(icon);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Name">
    @Override
    public void setName(String name) {
        File newDirectory = new File(directory.getParentFile(), name);
        AFileManager.FILE_MANAGER.copyDirectory(directory, newDirectory, AFileManager.FileOptions.DEPTH, AFileManager.FileOptions.CREATE);
        AFileManager.FILE_MANAGER.deleteDirectory(directory, AFileManager.FileOptions.DEPTH);
        this.directory = newDirectory;
        super.setName(name);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Description">
    @Override
    public void setDescription(String description) {
        AFileManager.FILE_MANAGER.write(new File(directory, DESC_RELATIVE), description, AFileManager.FileOptions.REPLACE);
        super.setDescription(description);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Directory">
    @Override
    public File getDirectory() {
        return directory;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="SM Menu Subtexts">
    @Override
    public String getLeft() {
        return (left == null) ? "" : left;
    }

    @Override
    public String getCenter() {
        return (center == null) ? "" : center;
    }

    @Override
    public String getRight() {
        return (right == null) ? "" : right;
    }
    //</editor-fold>
    
    
    //<editor-fold defaultstate="collapsed" desc="Operations">
    @Override
    public void delete() {
        if (directory != null) {
            this.modifications.forEach(WorkshopConnectionHandler::disconnect);
            AFileManager.FILE_MANAGER.deleteDirectory(directory, AFileManager.FileOptions.DEPTH);
        }
    }
    
    @Override
    public void updateModifications() {
        this.modifications = new ArrayList();
        for (File file : directory.listFiles()) {
            if (file.isDirectory() && Constants.IGNORE_FILE_FILTER.accept(file.getParentFile(), file.getName())) {
                this.modifications.add(new Modification(file));
            }
        }
        this.doModsUpdate = false;
    }
    //</editor-fold>

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Modification {

    private static final Logger LOGGER = Logger.getLogger(Modification.class.getName());
    
    private File directory;
    private String name;
    private String description;
    private boolean enabled;
    private File enabledFile;
    public BooleanProperty enabledListener;
    private Integer workshopConnection;

    public Modification(File directory) {
        this.directory = directory;
        this.name = directory.getName();
        try (BufferedReader br = Files.newBufferedReader(new File(directory.toPath() + "/description.txt").toPath())) {
            this.description = br.lines().reduce("Description: ", String::concat);
        } catch (IOException | NullPointerException ex) {
            LOGGER.log(Level.WARNING, "Modification {0} does not contain file `description.txt`", this.name);
        }
        enabledFile = new File(directory, "enabled");
        this.enabled = Files.exists(enabledFile.toPath());
        this.enabledListener = new SimpleBooleanProperty(this.enabled);
        try (BufferedReader br = Files.newBufferedReader(new File(directory.toPath() + "/workshop.txt").toPath())) {
            this.workshopConnection = Integer.valueOf(br.readLine());
        } catch (IOException ex) {
            this.workshopConnection = null;
        }
    }

    public void updateFromDirectory() {
        try (BufferedReader br = Files.newBufferedReader(new File(directory.toPath() + "/description.txt").toPath())) {
            this.description = br.lines().reduce("Description: ", String::concat);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Modification {0} does not contain file `description.txt`", this.name);
        }
        enabledFile = new File(directory, "enabled");
        this.enabled = Files.exists(enabledFile.toPath());
        this.enabledListener.setValue(enabled);
        try (BufferedReader br = Files.newBufferedReader(new File(directory.toPath() + "/workshop.txt").toPath())) {
            this.workshopConnection = Integer.valueOf(br.readLine());
        } catch (IOException ex) {
            this.workshopConnection = null;
        }
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
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

    public boolean isEnabled() {
        this.enabled = Files.exists(enabledFile.toPath());
        return this.enabled;
    }

    public void disable() {
        enabledFile.delete();
        this.enabled = false;
        this.enabledListener.setValue(false);
    }

    public void enable() {
        try {
            enabledFile.createNewFile();
            this.enabled = true;
            this.enabledListener.setValue(true);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to enable modification " + this.name, ex);
        }
    }

    public int getWorkshopConnection() {
        if (this.workshopConnection != null) {
            return this.workshopConnection;
        } else {
            return -1;
        }
    }

    public boolean isWorkshopConnected() {
        return this.workshopConnection != null;
    }

    @Override
    public int hashCode() {
        updateFromDirectory();
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.directory);
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + Objects.hashCode(this.description);
        hash = 89 * hash + (this.enabled ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.workshopConnection);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Modification other = (Modification) obj;
        updateFromDirectory();
        other.updateFromDirectory();
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.directory, other.directory)) {
            return false;
        }
        if (!Objects.equals(this.workshopConnection, other.workshopConnection)) {
            return false;
        }
        return true;
    }
    
    

}

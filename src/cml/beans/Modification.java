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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Modification {

    private File directory;
    private String name;
    private String description;
    private boolean enabled;
    private File enabledFile;

    public Modification(File directory) {
        this.directory = directory;
        this.name = directory.getName();
        try ( BufferedReader br = Files.newBufferedReader(new File(directory.toPath() + "/description.txt").toPath())) {
            this.description = br.lines().reduce("Description: ", String::concat);
        } catch (IOException ex) {
            Logger.getLogger(Modification.class.getName()).log(Level.SEVERE, "Modification " + this.name + " does not contain file `description.txt`", ex);
        }
        enabledFile = new File(directory.getAbsolutePath() + "\\enabled");
        this.enabled = Files.exists(enabledFile.toPath());
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
    }

    public void enable() {
        try {
            enabledFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Modification.class.getName()).log(Level.SEVERE, "Failed to enable modification " + this.name, ex);
        }
    }

}

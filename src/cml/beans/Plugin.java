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

import cml.gui.plugins.PluginData;
import cml.lib.files.AFileManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author benne
 */
public class Plugin {
    
    private static final Logger LOGGER = Logger.getLogger(Plugin.class.getName());

    private final File directory;
    private final Map<String, Object> properties = new HashMap();
    public final PluginData dataView;
    private BooleanProperty enabled;

    public Plugin(File directory) {
        this.directory = directory;
        readProperties(new File(directory, "cml.config"));
        this.dataView = new PluginData(this);
        this.enabled = new SimpleBooleanProperty(new File(directory, "enabled").exists());
    }

    private void readProperties(File config) {
        List<String> configStr = AFileManager.FILE_MANAGER.readList(config);
        int readMode = 0;
        for (String line : configStr) {
            switch (readMode) {
                default:
                    if (!line.contains(":")) {
                        if (line.equals("isHook")) {
                            properties.put("isHook", true);
                        } else {
                            properties.putIfAbsent("arguments", new ArrayList<String> ());
                            ((ArrayList<String>) properties.get("arguments")).add(line);
                        }
                    } else {
                        String key = line.split(":")[0].trim();
                        String value = line.split(":")[1].trim();
                        if (key.startsWith("hook")) {
                            properties.putIfAbsent("hooks", new HashMap<String, String>());
                            ((HashMap<String, String>) properties.get("hooks")).put(key.substring(4), value);
                            continue;
                        }
                        switch (key.trim()) {
                            case "executable":
                                properties.put("executable", new File(directory, value));
                                break;
                            case "isHook":
                                properties.put("isHook", Boolean.valueOf(value));
                                break;
                            case "arguments":
                                properties.putIfAbsent("arguments", new ArrayList<String> ());
                                ((ArrayList<String>) properties.get("arguments")).add(value);
                                break;
                            default:
                                properties.put(key, value);
                        }
                    }
            }
        }
    }
    
    public void run() throws IOException {
        File executable = (File) properties.get("executable");
        List<String> arguments = (List<String>) properties.get("arguments");
        String args = executable.getPath() + " " + arguments.stream().collect(Collectors.joining(" "));
        if (executable.getName().toLowerCase().endsWith(".jar")) {
            Runtime.getRuntime().exec("java -jar " + args);
        } else if (properties.containsKey("execution")) {
            Runtime.getRuntime().exec(properties.get("execution") + " " + args);
        } else {
            Runtime.getRuntime().exec(args);
        }
        
    }
    
    public boolean isExecutable() {
        return properties.containsKey("executable") && ((File) properties.get("executable")).exists();
    }
    
    public boolean isHook() {
        return (Boolean) properties.getOrDefault("isHook", false);
    }
    
    public Map<String, String> getHooks() {
        return (HashMap<String, String>) properties.get("hooks");
    }
    
    public String getName() {
        String name = (String) properties.getOrDefault("name", null);
        if (name == null) {
            File executable = (File) properties.get("executable");
            LOGGER.log(Level.WARNING, "Plugin at path \"{0}\" does not have a name. Setting to executable name.", directory.getAbsolutePath());
            name = executable.getName();
        }
        return name;
    }
    
    public String getDescription() {
        String description = (String) properties.getOrDefault("description", null);
        if (description == null) {
            LOGGER.log(Level.WARNING, "Plugin \"{0}\" does not have a description.", getName());
            description = "This plugin did not have a description. Commence subtle shaming.";
        }
        return description;
    }
    
    public String getImageUrl() {
        return (String) properties.getOrDefault("iconUrl", "http://aux.iconspalace.com/uploads/blank-icon-32-1093244638.png");
    }
    
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public File getDirectory() {
        return directory;
    }
    
    public boolean isEnabled() {
        return enabled.getValue();
    }
    
    public BooleanProperty getEnabledProperty() {
        return enabled;
    }
    
    public void enable() {
        try {
            new File(directory, "enabled").createNewFile();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not enable plugin " + getName(), ex);
        }
        enabled.setValue(true);
    }
    
    public void disable() {
        new File(directory, "enabled").delete();
        enabled.setValue(false);
    }
    
    public void delete() {
        directory.delete();
        disable();
    }

}

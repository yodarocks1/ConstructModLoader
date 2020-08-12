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

import cml.Main;
import cml.apply.PluginApplyManager;
import cml.gui.plugins.PluginData;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;

/**
 *
 * @author benne
 */
public class Plugin {

    private static final Logger LOGGER = Logger.getLogger(Plugin.class.getName());

    private final File directory;
    private final File config;
    private final Map<String, Object> properties = new HashMap();
    public final PluginData dataView;
    private final BooleanProperty enabled;

    public Plugin(File directory) {
        this.directory = directory;
        this.config = new File(directory, "cml.config");
        readProperties(config);
        this.enabled = new SimpleBooleanProperty(new File(directory, "enabled").exists());
        this.dataView = new PluginData(this);
    }

    private void readProperties(File config) {
        List<String> configStr = AFileManager.FILE_MANAGER.readList(config);
        int readMode = 0;
        for (String line : configStr) {
            switch (readMode) {
                default:
                    if (line.isEmpty()) {
                        continue;
                    }
                    if (!line.contains(":")) {
                        switch (line.trim().toLowerCase()) {
                            case "ishook":
                                properties.put("ishook", true);
                                break;
                            case "autorun":
                                properties.put("autorun", true);
                                break;
                            case "ishandler":
                                properties.put("ishandler", true);
                                setupAsHandler();
                                break;
                            default:
                                properties.putIfAbsent("arguments", new ArrayList<String>());
                                ((ArrayList<String>) properties.get("arguments")).add(line);
                                break;
                        }
                    } else {
                        String key = line.split(":")[0].trim().toLowerCase();
                        String value = line.substring(line.indexOf(":") + 1).trim();
                        if (key.startsWith("hook")) {
                            properties.putIfAbsent("hooks", new HashMap<String, String>());
                            ((HashMap<String, String>) properties.get("hooks")).put(key.substring(4), value);
                            continue;
                        }
                        switch (key) {
                            case "executable":
                                properties.put("executable", new File(directory, value));
                                break;
                            case "fxml":
                                properties.put("fxml", new File(directory, value));
                                break;
                            case "ishook":
                                properties.put("ishook", Boolean.valueOf(value));
                                break;
                            case "ishandler":
                                properties.put("ishandler", Boolean.valueOf(value));
                                if (Boolean.valueOf(value)) {
                                    setupAsHandler();
                                }
                                break;
                            case "argument":
                            case "arguments":
                                properties.putIfAbsent("arguments", new ArrayList<String>());
                                ((ArrayList<String>) properties.get("arguments")).add(value);
                                break;
                            default:
                                properties.put(key, value);
                                break;
                        }
                    }
            }
        }
    }

    private void setupAsHandler() {
        PluginApplyManager.managers.add(new PluginApplyManager(this));
    }

    private void addProperty(String line) {
        String configStr = AFileManager.FILE_MANAGER.readString(config);
        configStr += "\n" + line;
        AFileManager.FILE_MANAGER.write(config, configStr, FileOptions.REPLACE);
    }

    private void removeProperty(String startsWith) {
        List<String> oldConfig = AFileManager.FILE_MANAGER.readList(config);
        String newConfig = oldConfig.stream().filter((line) -> !line.startsWith(startsWith)).collect(Collectors.joining("\n"));
        AFileManager.FILE_MANAGER.write(config, newConfig, FileOptions.REPLACE);
    }

    public void openConfig() {
        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \"" + config.getAbsolutePath() + "\"");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to open file: url=\"" + config.getAbsolutePath() + "\"", ex);
        }
    }

    public void run() throws IOException {
        run(new String[]{});
    }

    public void run(String[] specialArgs) throws IOException {
        if (!enabled.get()) {
            return;
        }
        if (isExecutable()) {
            String exec;
            List<String> argumentsPointer = (List<String>) properties.get("arguments");
            List<String> arguments;
            if (argumentsPointer != null) {
                arguments = new ArrayList(argumentsPointer);
            } else {
                arguments = new ArrayList();
            }
            arguments.addAll(Arrays.asList(specialArgs));

            if (hasExecutable()) {
                File executable = (File) properties.get("executable");
                String args = '"' + executable.getPath() + '"' + collectArgs(arguments);
                if (properties.containsKey("execution")) {
                    exec = parseExecution((String) properties.get("execution")) + " " + args;
                } else if (executable.getName().toLowerCase().endsWith(".jar")) {
                    exec = "java -jar " + args;
                } else {
                    exec = args;
                }
            } else {
                exec = parseExecution((String) properties.get("execution")) + " " + collectArgs(arguments);
            }

            LOGGER.log(Level.FINE, "Executing \"{0}\" for Plugin \"{1}\"", new Object[]{exec, this.getName()});
            Runtime.getRuntime().exec(exec, null, this.getDirectory());
        } else if (isFXML()) {
            File fxml = (File) properties.get("fxml");
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            loader.setResources(new ListResourceBundle() {
                @Override
                protected Object[][] getContents() {
                    Object[][] contents = new Object[properties.size()][];
                    int i = 0;
                    for (Entry entry : properties.entrySet()) {
                        contents[i++] = new Object[]{entry.getKey(), entry.getValue()};
                    }
                    return contents;
                }
            });
            loader.setClassLoader(this.getClass().getClassLoader());
            LOGGER.log(Level.FINE, "Loading FXML for Plugin {0}", this.getName());
            loader.load();
        } else {
            throw new UnsupportedOperationException("Plugin does not have a defined Executable or an FXML, and cannot be launched.");
        }
    }

    public boolean shouldAutoRun() {
        return Main.AUTORUN_ENABLED_PROPERTY.get() && (boolean) properties.getOrDefault("autorun", false);
    }

    public boolean isAutoRun() {
        return (boolean) properties.getOrDefault("autorun", false);
    }

    public void setAutoRun(boolean autoRun) {
        if (autoRun) {
            properties.put("autorun", true);
            addProperty("autorun");
        } else {
            properties.remove("autorun");
            removeProperty("autorun");
        }
    }

    public boolean hasExecutable() {
        return properties.containsKey("executable") && ((File) properties.get("executable")).exists();
    }

    public boolean isExecutable() {
        return hasExecutable() || properties.containsKey("execution");
    }

    public boolean isFXML() {
        return properties.containsKey("fxml") && ((File) properties.get("fxml")).exists();
    }

    public boolean isHook() {
        return (Boolean) properties.getOrDefault("ishook", false);
    }

    public Map<String, String> getHooks() {
        return (HashMap<String, String>) properties.get("hooks");
    }

    public String getName() {
        String name = (String) properties.getOrDefault("name", null);
        if (name == null) {
            if (isExecutable()) {
                File executable = (File) properties.get("executable");
                LOGGER.log(Level.WARNING, "Plugin at path \"{0}\" does not have a name. Setting to executable name.", directory.getAbsolutePath());
                name = executable.getName();
            } else if (isFXML()) {
                File fxml = (File) properties.get("fxml");
                LOGGER.log(Level.WARNING, "Plugin at path \"{0}\" does not have a name. Setting to fxml name.", directory.getAbsolutePath());
                name = fxml.getName();
            } else {
                LOGGER.log(Level.WARNING, "Plugin at path \"{0}\" does not have a name. Setting to directory name.", directory.getAbsolutePath());
                name = directory.getName();
            }
        }
        return name;
    }

    public String getDescription() {
        String description = (String) properties.getOrDefault("description", null);
        if (description == null) {
            LOGGER.log(Level.WARNING, "Plugin \"{0}\" does not have a description.", getName());
            description = "This plugin did not have a description. Commence subtle shaming.";
        }
        return description.replace("\\n", "\n");
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

    public Object getProperty(String key, Object defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    public Map<String, Object> getProperties() {
        return properties;
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
            enabled.setValue(true);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not enable plugin " + getName(), ex);
        }
    }

    public void disable() {
        new File(directory, "enabled").delete();
        enabled.setValue(false);
    }

    public void delete() {
        disable();
        directory.delete();
    }

    private String collectArgs(List<String> args) {
        return parseArgs(args).stream().collect(Collectors.joining(" ", " ", ""));
    }

    private List<String> parseArgs(List<String> args) {
        return args.stream().map((String arg) -> {
            if (arg != null && arg.trim().length() > 0) {
                switch (arg.trim().toLowerCase()) {
                    case "dir":
                    case "directory":
                        return this.directory.getAbsolutePath();
                    case "\"dir\"":
                    case "\"directory\"":
                        return '\"' + this.directory.getAbsolutePath() + '\"';
                    case "sm_version":
                        return PluginApplyManager.getHeldSMVersion();
                    default:
                        return arg.trim();
                }
            }
            return "";
        }).collect(Collectors.toList());
    }

    private static final Pattern DIRECTORY_PATTERN = Pattern.compile("{$plugindirectory}", Pattern.LITERAL + Pattern.CASE_INSENSITIVE);

    private String parseExecution(String execution) {
        return DIRECTORY_PATTERN.matcher(execution).replaceAll(this.getDirectory().getAbsolutePath().replace("\\", "\\\\"));
    }

}

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
package cml.lib.plugins;

import cml.Constants;
import cml.Main;
import cml.beans.Plugin;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author benne
 */
public class PluginManager {

    public static final AHookManager LUA_HOOK_MANAGER = new LuaHookManager();
    private static final ObservableList<Plugin> ALL_PLUGINS = FXCollections.observableArrayList();
    private static final File DISABLE_FILE = new File(Constants.API_DIRECTORY, "plugins/disabled");
    private static final File AUTORUN_DISABLE_FILE = new File(Constants.API_DIRECTORY, "plugins/autorunDisabled");

    public static void loadPlugins() {
        File pluginFolder = new File(Constants.API_DIRECTORY, "plugins");
        ALL_PLUGINS.setAll(Arrays.stream(pluginFolder.listFiles(Constants.IGNORE_PATH_FILTER)).filter((file) -> file.isDirectory()).map((file) -> new Plugin(file)).collect(Collectors.toList()));
        
        Main.PLUGINS_ENABLED_PROPERTY.set(!DISABLE_FILE.exists());
        Main.AUTORUN_ENABLED_PROPERTY.set(!AUTORUN_DISABLE_FILE.exists() && !DISABLE_FILE.exists());
    }

    public static void processHooks() {
        ALL_PLUGINS.stream().filter(plugin -> plugin.isHook()).forEachOrdered(plugin -> plugin.getHooks().keySet().forEach(key -> {
            if (plugin.isEnabled()) {
                LUA_HOOK_MANAGER.addHook(key, plugin.getHooks().get(key));
            }
            plugin.getEnabledProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue) {
                    LUA_HOOK_MANAGER.addHook(key, plugin.getHooks().get(key));
                } else {
                    LUA_HOOK_MANAGER.removeHook(key);
                }
            });
        }));
    }
    
    public static ObservableList<Plugin> getPlugins() {
        return FXCollections.unmodifiableObservableList(ALL_PLUGINS);
    }

    public static List<Plugin> getLoadedPlugins() {
        return ALL_PLUGINS.stream().filter(plugin -> plugin.isEnabled()).collect(Collectors.toList());
    }
    
    public static Map<String, IHook> getHooks() {
        return LUA_HOOK_MANAGER.getHooks();
    }
    
    static {
        Main.PLUGINS_ENABLED_PROPERTY.addListener((obs, oldValue, newValue) -> {
            setPluginsStatus(newValue);
            if (!newValue) {
                Main.AUTORUN_ENABLED_PROPERTY.setValue(false);
            }
        });
        Main.AUTORUN_ENABLED_PROPERTY.addListener((obs, oldValue, newValue) -> {
            setAutorunStatus(newValue);
        });
    }
    
    private static void setAutorunStatus(boolean enabled) {
        if (enabled) {
            if (AUTORUN_DISABLE_FILE.exists()) {
                AUTORUN_DISABLE_FILE.delete();
            }
        } else {
            if (!AUTORUN_DISABLE_FILE.exists()) {
                try {
                    AUTORUN_DISABLE_FILE.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, "Could not disable autorun", ex);
                }
            }
        }
    }
    
    private static void setPluginsStatus(boolean enabled) {
        if (enabled) {
            if (DISABLE_FILE.exists()) {
                DISABLE_FILE.delete();
            }
        } else {
            if (!DISABLE_FILE.exists()) {
                try {
                    DISABLE_FILE.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, "Could not disable plugin system", ex);
                }
            }
        }
    }

}

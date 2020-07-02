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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author benne
 */
public class PluginManager {

    public static final AHookManager LUA_HOOK_MANAGER = new LuaHookManager();
    private static final List<Plugin> ALL_PLUGINS = new ArrayList();

    public static void loadPlugins() {
        File pluginFolder = new File(Main.API_DIRECTORY, "plugins");
        ALL_PLUGINS.addAll(Arrays.stream(pluginFolder.listFiles(Constants.IGNORE_PATH_FILTER)).map((File file) -> new Plugin(file)).collect(Collectors.toList()));
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

    public static List<Plugin> getLoadedPlugins() {
        return ALL_PLUGINS.stream().filter(plugin -> plugin.isEnabled()).collect(Collectors.toList());
    }
    
    public static Map<String, IHook> getHooks() {
        return LUA_HOOK_MANAGER.getHooks();
    }

}

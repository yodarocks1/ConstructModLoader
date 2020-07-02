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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 * @param <T> Hook type
 * @param <O> Hook output type
 */
public abstract class AHookManager<O, T extends IHook<O>> {
    
    protected abstract T create(String fileName);
    protected abstract void register(T hook);

    private static final Logger LOGGER = Logger.getLogger(LuaHookManager.class.getName());
    private Map<String, T> hooks = new HashMap();
    
    protected AHookManager() {
        
    }
    
    public Map<String, T> getHooks() {
        return hooks;
    }

    public boolean loadHook(String key) {
        boolean success = addHook(key, "plugins/" + key);
        if (success) {
            LOGGER.log(Level.FINE, "Successfully loaded {0} script.", key);
        }
        return success;
    }

    public boolean addHook(String key, String fileName) {
        T hook = create(fileName);
        if (hook.canExecute()) {
            hooks.put(key, hook);
            register(hook);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean removeHook(String key) {
        return hooks.remove(key) != null;
    }

    public boolean reloadHook(String key) {
        if (hooks.containsKey(key)) {
            return addHook(key, hooks.get(key).getFileName());
        } else {
            return false;
        }
    }

    public boolean executeFunction(String key, String functionName, Object... objects) {
        if (hooks.containsKey(key)) {
            return hooks.get(key).executeFunction(functionName, objects);
        } else if (loadHook(key)) {
            return hooks.get(key).executeFunction(functionName, objects);
        }
        return false;
    }

    public boolean executeInit(String key, Object... objects) {
        if (hooks.containsKey(key)) {
            return hooks.get(key).executeInit(objects);
        } else if (loadHook(key)) {
            return hooks.get(key).executeInit(objects);
        }
        return false;
    }

    public void dispose() {
        hooks = new HashMap();
    }

    public O getLastResults(String key) {
        return hooks.get(key).getLatestResults();
    }

}

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

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 *
 * @author benne
 */
class LuaHookManager extends AHookManager<Varargs, LuaHook> {
    
    private final ExecuteLuaScript executor = new ExecuteLuaScript();

    @Override
    protected LuaHook create(String fileName) {
        LuaHook hook = new LuaHook();
        hook.load(fileName);
        return hook;
    }

    @Override
    protected void register(LuaHook hook) {
        hook.registerJavaFunction(executor);
    }

    private final class ExecuteLuaScript extends TwoArgFunction {
        
        @Override
        public LuaValue call(LuaValue modName, LuaValue env) {
            env.set("ExecuteScript", new ExecuteScriptImplementation());
            return env;
        }

        final class ExecuteScriptImplementation extends ThreeArgFunction {

            @Override
            public LuaValue call(LuaValue key, LuaValue functionName, LuaValue objects) {
                if (key.isstring() && functionName.isstring() && objects.istable()) {
                    LuaTable luaTable = objects.checktable();
                    Object[] objectArray = new Object[luaTable.length()];
                    for (int i = 0; i < luaTable.length(); i++) {
                        objectArray[i] = luaTable.get(i + 1);
                    }
                    return LuaValue.valueOf(executeFunction(key.toString(), functionName.toString(), objectArray));
                } else {
                    return LuaValue.valueOf(false);
                }
            }

        }

    }

}

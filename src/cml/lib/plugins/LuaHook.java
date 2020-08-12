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
import cml.gui.console.LogHandler;
import cml.gui.console.StreamTee;
import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author benne
 */
class LuaHook implements IHook<Varargs> {

    private Globals globals = JsePlatform.standardGlobals();
    private LuaValue chunk;

    public File file;
    public Varargs lastResults;

    public LuaHook() {
        globals.STDERR = new PrintStream(new StreamTee(new LogHandler.LogOutputStream(Main.LOG_HANDLER, Level.SEVERE, "LuaJ API"), new LogHandler.LogOutputStream(Main.FILE_HANDLER, Level.SEVERE, "LuaJ API")));
        globals.STDOUT = new PrintStream(new StreamTee(new LogHandler.LogOutputStream(Main.LOG_HANDLER, Level.INFO, "LuaJ API"), new LogHandler.LogOutputStream(Main.FILE_HANDLER, Level.INFO, "LuaJ API")));
    }

    public boolean load(String relativePath) {
        return this.load(new File(Constants.API_DIRECTORY, relativePath));
    }

    public boolean load(File file) {
        this.file = file;

        if (!file.exists()) {
            return false;
        }

        this.chunk = globals.loadfile(file.getAbsolutePath());

        chunk.call();

        return true;
    }

    @Override
    public boolean reload() {
        return this.load(file);
    }

    @Override
    public boolean canExecute() {
        return this.file.exists();
    }

    @Override
    public boolean executeInit(Object... objects) {
        return executeFunction("init", objects);
    }

    @Override
    public boolean executeFunction(String functionName, Object... objects) {
        if (!canExecute()) {
            return false;
        }

        LuaValue luaFunction = globals.get(functionName);

        if (luaFunction.isfunction()) {
            LuaValue[] parameters = new LuaValue[objects.length];

            int i = 0;
            for (Object object : objects) {
                parameters[i] = CoerceJavaToLua.coerce(object);
                i++;
            }

            try {
                lastResults = luaFunction.invoke(parameters);
            } catch (LuaError e) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    public void registerJavaFunction(TwoArgFunction javaFunction) {
        globals.load(javaFunction);
    }
    
    @Override
    public String getFileName() {
        return file.getName();
    }
    
    @Override
    public Varargs getLatestResults() {
        return lastResults;
    }

}

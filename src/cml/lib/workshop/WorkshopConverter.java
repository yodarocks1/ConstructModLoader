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
package cml.lib.workshop;

import cml.beans.Profile;
import cml.lib.converter.Transmute;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import cml.lib.lazyupdate.Flags;
import cml.lib.plugins.PluginManager;
import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;

/**
 *
 * @author benne
 */
public class WorkshopConverter {

    private static final Logger LOGGER = Logger.getLogger(WorkshopConverter.class.getName());

    static {
        LOGGER.setLevel(Level.ALL);
    }

    public static final FilenameFilter MANUAL_INSTALL_INDICATOR_FILTER = (File dir, String name) -> name.equals("Survival") || name.equals("Release") || name.equals("Data");

    public void copyAndConvert(WorkshopMod workshopMod, File file, boolean fileIsProfile) {
        if (fileIsProfile) {
            copyAndConvert(workshopMod, new File(file, workshopMod.getName()));
        } else {
            copyAndConvert(workshopMod, file);
        }
    }

    public void copyAndConvert(WorkshopMod workshopMod, File mod) {
        LOGGER.log(Level.INFO, "Copying workshop mod \"{0}\" into profile {1}", new Object[]{workshopMod.getName(), mod.getParentFile().getName()});

        byte handled = 0; //Using a byte because it maximizes LuaJ performance

        boolean hasHook = PluginManager.LUA_HOOK_MANAGER.executeInit("workshopconvert", workshopMod, mod);

        Transmute.ModType type = Transmute.ModType.forFile(workshopMod.getDirectory());

        //If we have a hook, pass it there first. If not, continue.
        if (hasHook) {
            //Run hook
            PluginManager.LUA_HOOK_MANAGER.executeFunction("workshopconvert", "convert", workshopMod, mod, type);
            Varargs out = (Varargs) PluginManager.LUA_HOOK_MANAGER.getLastResults("workshopconvert");
            //Figure out what's been handled
            if (out.isnumber(1)) { //LuaJ indexes beginning at 1. ¯\_(ツ)_/¯
                try {
                    handled = out.checknumber(1).tobyte();
                } catch (LuaError e) {
                    handled = 0;
                }
            }
        }
        
        if (type == null) {
            LOGGER.log(Level.SEVERE, "Could not determine how to use the specified mod.");
            return;
        }

        //If type has been handled already, don't handle it again.
        //Uses Bitwise-OR on handled (thus saving what hasn't been handled)
        handled = type.toCml(workshopMod.getDirectory(), mod, (File dir, String name) -> !(name.equals("description.json") && dir.equals(workshopMod.getDirectory())), handled);

        //Read in the first line of the description if description.txt doesn't already exist
        if ((handled & Transmute.HANDLES_SPECIAL) == 0) {
            createDescription(workshopMod, mod);
        }

        //Create connection file
        createConnection(workshopMod, mod);

        //Update the profile to visibly show the mod
        Platform.runLater(() -> {
            Flags.setFlag(Flags.staticFlags(Profile.class), Flags.Flag.DO_UPDATE);
        });
    }

    private void createConnection(WorkshopMod fromWorkshop, File toMod) {
        AFileManager.FILE_MANAGER.write(new File(toMod, "workshop.txt"), Integer.toString(fromWorkshop.getWorkshopId()), FileOptions.DEPTH, FileOptions.REPLACE);
    }

    private void createDescription(WorkshopMod workshopMod, File mod) {
        File description = new File(mod, "description.txt");
        File descriptionTxt = new File(workshopMod.getDirectory(), "description.txt");
        if (descriptionTxt.exists()) {
            AFileManager.FILE_MANAGER.copy(descriptionTxt, description, FileOptions.REPLACE);
            return;
        }
        String descriptor = workshopMod.getDescription();
        if (descriptor == null) {
            descriptor = "";
        }
        if (descriptor.contains("\n")) {
            descriptor = descriptor.substring(0, descriptor.indexOf("\n"));
        }
        AFileManager.FILE_MANAGER.write(description, descriptor, FileOptions.REPLACE);
    }

}

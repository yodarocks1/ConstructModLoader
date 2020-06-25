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

import cml.apply.Apply;
import cml.beans.Modification;
import cml.lib.files.AFileManager;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class WorkshopConnectionHandler {

    private static final Logger LOGGER = Logger.getLogger(WorkshopConnectionHandler.class.getName());
    public static Map<Modification, WorkshopMod> connections = new HashMap();
    private static final WorkshopConverter CONVERTER = new WorkshopConverter();

    public static void updateAllConnectedMods() {
        connections.keySet().forEach(WorkshopConnectionHandler::updateFromConnected);
    }
    
    public static void updateFromConnected(Modification mod) {
        if (mod.getDirectory().exists()) {
            AFileManager.FILE_MANAGER.delete(mod.getDirectory());
        }
        CONVERTER.copyAndConvert(connections.get(mod), mod.getDirectory());
        mod.updateFromDirectory();
    }
    
    public static void connectAndConvertInto(WorkshopMod fromWorkshop, File toProfile) {
        CONVERTER.copyAndConvert(fromWorkshop, toProfile, true);
        connections.put(new Modification(new File(toProfile, fromWorkshop.getName())), fromWorkshop);
    }
    
    public static void connectAndConvert(WorkshopMod fromWorkshop, File toMod) {
        CONVERTER.copyAndConvert(fromWorkshop, toMod);
        connections.put(new Modification(toMod), fromWorkshop);
    }
    
    protected static void connect(WorkshopMod fromWorkshop, Modification toMod) {
        connections.put(toMod, fromWorkshop);
    }
    
    private static void connect(Modification mod, String workshopFolder) {
        WorkshopMod workshop = WorkshopMod.createSafely(new File(workshopFolder, Integer.toString(mod.getWorkshopConnection())));
        if (workshop != null) {
            connections.put(mod, workshop);
        }
    }
    
    public static boolean verifyConnectionValidity(Modification mod) {
        return mod.getDirectory().exists() && connections.get(mod).getDirectory().exists();
    }
    
    public static void reconnectIfConnected(String workshopFolder, Modification... mods) {
        for (Modification mod : mods) {
            if (mod.isWorkshopConnected() && !connections.containsKey(mod)) {
                connect(mod, workshopFolder);
            }
        }
    }
    
    public static void disconnect(Modification mod) {
        connections.remove(mod);
    }
    
}

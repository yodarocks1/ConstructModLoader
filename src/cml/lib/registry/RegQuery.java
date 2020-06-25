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
package cml.lib.registry;

import cml.lib.registry.apache.WinRegistry;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author benne
 */
public class RegQuery {
    
    public static String readRegistryValue(int hkey, String path, String key) {
        try {
            return WinRegistry.readString(hkey, path, key);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            return null;
        }
    }
}

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
package cml.lib.registry.hardcoded;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hardcoded
 */
public class RegQuery {

    private static final String QUERY = "REG QUERY ";
    private static final Logger LOGGER = Logger.getLogger(RegQuery.class.getName());

    /**
     * Reads a value from the registry with the selected path and key.
     *
     * @param path
     * @param key
     * @return
     */
    public static String readRegistryValue(String path, String key) {
        try {
            return executeRegistry(path, key);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to execute registry query", ex);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Registry query process was interrupted", ex);
        }
        return null;
    }

    private static String executeRegistry(String path, String key) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(QUERY + path + " /v" + key);
        byte[] bytes = FileUtil.readStreamBytes(process.getInputStream());
        process.waitFor();

        if (bytes.length < 1) {
            return null;
        }

        String value = new String(bytes);
        value = value.substring(2);
        value = value.substring(value.indexOf('\n') + 1).trim();
        value = value.substring(value.indexOf(' ')).trim();
        value = value.substring(value.indexOf(' ')).trim();
        return value;
    }
}

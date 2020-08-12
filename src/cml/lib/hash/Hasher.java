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
package cml.lib.hash;

import cml.lib.files.AFileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class Hasher {
    
    private static final Logger LOGGER = Logger.getLogger(Hasher.class.getName());
    
    public static String digestFile(File file) {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, "Could not find a suitable hasher", ex);
            return null;
        }
        digester.update(AFileManager.FILE_MANAGER.read(file));
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digester.digest()) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    public static String digestString(String str) {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, "Could not find a suitable hasher", ex);
            return null;
        }
        digester.update(str.getBytes());
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digester.digest()) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    public static String digestDir(File directory) {
        String endDigest = directory.getName() + " {\n";
        for (File file : directory.listFiles()) {
            endDigest += file.getName() + " * " + digest(file);
        }
        endDigest += "}\n";
        return digestString(endDigest);
    }
    
    public static String digest(File file) {
        if (file.isDirectory()) {
            return digestDir(file);
        } else {
            return digestFile(file);
        }
    }
}
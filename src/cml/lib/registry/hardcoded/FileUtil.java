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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class FileUtil {
    
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());

    public static byte[] readStreamBytes(InputStream stream) {
        if (stream == null) {
            return new byte[0];
        }

        try (DataInputStream ds = new DataInputStream(stream)) {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int readBytes = 0;

            try {
                while ((readBytes = ds.read(buffer)) != -1) {
                    bs.write(buffer, 0, readBytes);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not read bytes from input stream", ex);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not create new DataInputStream from InputStream", ex);
        }

        return new byte[0];
    }

    public static byte[] readFileBytes(File file) {
        try {
            return readStreamBytes(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Could not find file " + file.getAbsolutePath(), ex);
        }
        
        return new byte[0];
    }
    
    public static byte[] readFileBytes(String path) {
        return readFileBytes(new File(path));
    }
    
    public static String readFile(File file) {
        return new String(readFileBytes(file));
    }
    
    public static String readFile(String path) {
        return new String(readFileBytes(path));
    }
    
    public static String readFile(File path, String name) {
        return new String(readFileBytes(new File(path.getAbsolutePath() + "\\" + name)));
    }
    
    public static String readStream(InputStream stream) {
        return new String(readStreamBytes(stream));
    }

}

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
package cml.lib.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
class FileManager extends AFileManager<File> {

    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

    @Override
    public void createParentDirectories(File file) {
        if (doModify()) {
            file.getParentFile().mkdirs();
        }
    }

    @Override
    public void createDirectory(File directory, FileOptions... options) {
        if (doModify()) {
            if (FileOptions.DEPTH.isIn(options)) {
                directory.mkdirs();
            } else {
                directory.mkdir();
            }
        }
    }

    @Override
    public void deleteDirectory(File directory, FileOptions... options) {
        if (doModify()) {
            if (FileOptions.DEPTH.isIn(options)) {
                deleteRec(directory);
            } else {
                directory.delete();
            }
        }
    }

    private void deleteRec(File file) {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                deleteRec(subFile);
            }
        }
        file.delete();
    }

    @Override
    public void copyDirectory(File directory, File destinationDirectory, FileOptions... options) {
        if (doModify()) {
            if (FileOptions.DEPTH.isIn(options)) {
                copyRec(directory, destinationDirectory, options);
            } else if (directory.exists()) {
                destinationDirectory.mkdir();
            }
        }
    }

    private void copyRec(File file, File destination, FileOptions... options) {
        if (file.isDirectory()) {
            destination.mkdir();
            for (String subFile : file.list()) {
                copyRec(new File(file, subFile), new File(destination, subFile), options);
            }
        } else {
            copy(file, destination, options);
        }
    }

    @Override
    public void create(File file) {
        if (doModify()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to create file " + file.getAbsolutePath(), ex);
            }
        }
    }

    @Override
    public void delete(File file) {
        if (doModify()) {
            file.delete();
        }
    }

    @Override
    public byte[] read(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read file " + file.getAbsolutePath(), ex);
        }
        return null;
    }

    @Override
    public String readString(File file) {
        return new String(read(file));
    }

    @Override
    public List<String> readList(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read file " + file.getAbsolutePath(), ex);
        }
        return null;
    }

    @Override
    public void write(File file, byte[] bytes, FileOptions... options) {
        if (doModify()) {
            if (FileOptions.REPLACE.isIn(options)) {
                delete(file);
                create(file);
            } else if (FileOptions.CREATE.isIn(options) && !file.exists()) {
                create(file);
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(bytes);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not write to file " + file, ex);
            }
        }
    }

    @Override
    public void write(File file, String string, FileOptions... options) {
        if (doModify()) {
            write(file, string.getBytes(), options);
        }
    }

    @Override
    public void copy(File file, File destination, FileOptions... options) {
        if (doModify()) {
            if (FileOptions.REPLACE.isIn(options)) {
                delete(destination);
                create(destination);
            } else if (FileOptions.CREATE.isIn(options)) {
                create(destination);
            }

            try (FileInputStream fis = new FileInputStream(file); FileOutputStream fos = new FileOutputStream(destination)) {
                while (fis.available() != 0) {
                    fos.write(fis.read());
                }
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Could not copy from " + file.getAbsolutePath() + " to " + destination.getAbsolutePath() + " - FileNotFound", ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not copy from " + file.getAbsolutePath() + " to " + destination.getAbsolutePath(), ex);
            }
        }
    }

    @Override
    public void deleteOf(File file, FileOptions... options) {
        if (doModify()) {
            if (file.isDirectory()) {
                deleteDirectory(file, options);
            } else {
                delete(file);
            }
        }
    }

    @Override
    public void createWithParents(File file) {
        if (doModify()) {
            if (file.isDirectory()) {
                createDirectory(file, FileOptions.DEPTH);
            } else {
                createParentDirectories(file);
                create(file);
            }
        }
    }

}

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

import cml.beans.Modification;
import cml.beans.Profile;
import cml.lib.files.AFileManager.FileOptions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javafx.scene.Node;
import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
public class ZipManager {

    private static final Logger LOGGER = Logger.getLogger(ZipManager.class.getName());
    public static final FilenameFilter ZIP_FILTER = (File dir, String name) -> name.toLowerCase().endsWith(".zip");

    public void unzip(File zip, File destination) {
        if (AFileManager.FILE_MANAGER.doModify()) {
            if (!destination.exists()) {
                AFileManager.FILE_MANAGER.createDirectory(zip, FileOptions.DEPTH);
            }
            byte[] buffer = new byte[1024];
            try (FileInputStream zipFileIn = new FileInputStream(zip); ZipInputStream zipIn = new ZipInputStream(zipFileIn)) {
                ZipEntry entry;
                File localDest = destination;
                while ((entry = zipIn.getNextEntry()) != null) {
                    File outputFile = new File(localDest, entry.getName());
                    AFileManager.FILE_MANAGER.createParentDirectories(outputFile);
                    try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            fileOut.write(buffer, 0, len);
                        }
                    }
                    zipIn.closeEntry();
                }
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Could not find zip file", ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to unzip file", ex);
            }
        } else {
            LOGGER.log(Level.WARNING, "File not unzipped - doModify is false.");
        }
    }

    public static boolean isZipped(File zip) {
        return zip.exists() && ZIP_FILTER.accept(zip.getParentFile(), zip.getName());
    }

    public ZippedFile asZipped(File zip) {
        if (!isZipped(zip)) {
            return null;
        }
        try {
            ZipFile zipFile = new ZipFile(zip);
            if (has(zipFile, "Merge") || has(zipFile, "Replace") || has(zipFile, "Objects") || has(zipFile, "Crafting Recipes")) {
                return new ZippedMod(zip);
            } else {
                return new ZippedProfile(zip);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read .zip file at " + zip.getAbsolutePath(), ex);
        }
        return null;
    }

    public boolean has(ZipFile zip, String entryName) {
        return zip.getEntry(entryName) != null;
    }

    public static interface ZippedFile<T> {

        public void unzip(File destination);

        public T convertInto(File parent);
    }

    public static class ZippedProfile implements ZippedFile<Profile> {

        private final File file;
        private final ZipFile zip;

        protected ZippedProfile(File file) throws IOException {
            this.file = file;
            this.zip = new ZipFile(file);
        }

        @Override
        public void unzip(File destination) {
            AFileManager.ZIP_MANAGER.unzip(file, destination);
        }

        public Image getIcon() {
            try {
                return new Image(zip.getInputStream(zip.getEntry("icon.png")));
            } catch (IOException ex) {
                return null;
            }
        }

        public String getDescription() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(zip.getEntry(""))))) {
                String buffer = "";
                while (reader.ready()) {
                    buffer += reader.readLine();
                }
                return buffer;
            } catch (IOException ex) {
                return null;
            }
        }

        public int getEntryNum() {
            return zip.size();
        }

        @Override
        public Profile convertInto(File parent) {
            return null;
        }
    }

    public static class ZippedMod implements ZippedFile<Modification> {

        private final File file;
        private final ZipFile zip;

        protected ZippedMod(File file) throws IOException {
            this.file = file;
            this.zip = new ZipFile(file);
        }

        @Override
        public void unzip(File destination) {
            AFileManager.ZIP_MANAGER.unzip(file, destination);
        }

        public String getDescription() {
            return null;
        }

        public int getEntryNum() {
            return zip.size();
        }

        @Override
        public Modification convertInto(File parent) {
            return null;
        }
    }

}

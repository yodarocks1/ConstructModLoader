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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author benne
 * @param <T> Input type
 */
public abstract class AFileManager<T> {
    
    private static boolean doModify = true;
    public static final AFileManager<File> FILE_MANAGER = new FileManager();
    public static final AFileManager<Path> PATH_MANAGER = new PathManager();
    public static final ImageManager IMAGE_MANAGER = new ImageManager();
    public static final ZipManager ZIP_MANAGER = new ZipManager();
    
    public static void doModify(boolean doModify) {
        AFileManager.doModify = doModify;
    }
    
    public abstract void createParentDirectories(T file);
    public abstract void createDirectory(T directory, FileOptions... options);
    public abstract void deleteDirectory(T directory, FileOptions... options);
    public abstract void copyDirectory(T directory, T destinationDirectory, FileOptions... options);
    
    public abstract void create(T file);
    public abstract void delete(T file);
    public abstract byte[] read(T file);
    public abstract String readString(T file);
    public abstract List<String> readList(T file);
    public abstract void write(T file, byte[] bytes, FileOptions... options);
    public abstract void write(T file, String string, FileOptions... options);
    public abstract void copy(T file, T destination, FileOptions... options);
    
    public abstract void deleteOf(T file, FileOptions... options);
    public abstract void createWithParents(T file);
    
    public enum FileOptions {
        APPEND, CREATE, REPLACE, DEPTH;
        
        public boolean isIn(FileOptions[] list) {
            return Arrays.stream(list).anyMatch((item) -> item.equals(this));
        }
    }
    
    protected boolean doModify() {
        return doModify;
    }
    
}

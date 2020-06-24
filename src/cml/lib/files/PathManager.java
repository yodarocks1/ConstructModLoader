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

import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author benne
 */
class PathManager extends AFileManager<Path> {

    @Override
    public void createParentDirectories(Path file) {
        AFileManager.FILE_MANAGER.createParentDirectories(file.toFile());
    }

    @Override
    public void createDirectory(Path directory, FileOptions... options) {
        AFileManager.FILE_MANAGER.createDirectory(directory.toFile(), options);
    }

    @Override
    public void deleteDirectory(Path directory, FileOptions... options) {
        AFileManager.FILE_MANAGER.deleteDirectory(directory.toFile(), options);
    }

    @Override
    public void copyDirectory(Path directory, Path destinationDirectory, FileOptions... options) {
        AFileManager.FILE_MANAGER.copyDirectory(directory.toFile(), destinationDirectory.toFile(), options);
    }

    @Override
    public void create(Path file) {
        AFileManager.FILE_MANAGER.create(file.toFile());
    }

    @Override
    public void delete(Path file) {
        AFileManager.FILE_MANAGER.delete(file.toFile());
    }

    @Override
    public byte[] read(Path file) {
        return AFileManager.FILE_MANAGER.read(file.toFile());
    }

    @Override
    public String readString(Path file) {
        return AFileManager.FILE_MANAGER.readString(file.toFile());
    }

    @Override
    public List<String> readList(Path file) {
        return AFileManager.FILE_MANAGER.readList(file.toFile());
    }

    @Override
    public void write(Path file, byte[] bytes, FileOptions... options) {
        AFileManager.FILE_MANAGER.write(file.toFile(), bytes, options);
    }

    @Override
    public void write(Path file, String string, FileOptions... options) {
        AFileManager.FILE_MANAGER.write(file.toFile(), string, options);
    }

    @Override
    public void copy(Path file, Path destination, FileOptions... options) {
        AFileManager.FILE_MANAGER.copy(file.toFile(), destination.toFile(), options);
    }

    @Override
    public void deleteOf(Path file, FileOptions... options) {
        AFileManager.FILE_MANAGER.deleteOf(file.toFile(), options);
    }

    @Override
    public void createWithParents(Path file) {
        AFileManager.FILE_MANAGER.createWithParents(file.toFile());
    }
}

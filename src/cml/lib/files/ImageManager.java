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

import cml.lib.files.AFileManager.FileOptions;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author benne
 */
public class ImageManager {

    private static final Logger LOGGER = Logger.getLogger(ImageManager.class.getName());
    
    public void write(File file, RenderedImage image, String formatName) {
        if (AFileManager.FILE_MANAGER.doModify()) {
            try {
                ImageIO.write(image, formatName, file);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to write image to " + file.getAbsolutePath(), ex);
            }
        }
    }

    public void write(Path path, RenderedImage image, String formatName) {
        write(path.toFile(), image, formatName);
    }
    
    public BufferedImage read(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException ex) {
            Logger.getLogger(ImageManager.class.getName()).log(Level.SEVERE, "Could not read image " + file.getAbsolutePath(), ex);
        }
        return null;
    }
    
    public BufferedImage read(Path file) {
        return read(file.toFile());
    }

}

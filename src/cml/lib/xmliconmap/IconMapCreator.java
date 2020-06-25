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
package cml.lib.xmliconmap;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class IconMapCreator {

    private static final Logger LOGGER = Logger.getLogger(IconMapCreator.class.getName());
    
    private final Map<String, BufferedImage> uuidToImage = new HashMap();
    private final int wrapCount;
    private final int iconWidth;
    private final int iconHeight;

    public IconMapCreator(int wrapCount, int iconWidth, int iconHeight) {
        this.wrapCount = wrapCount;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    public void addToCurrent(Map<String, BufferedImage> imageToUuid) {
        this.uuidToImage.putAll(imageToUuid);
    }

    public void addToCurrent(String uuid, BufferedImage image) {
        this.uuidToImage.put(uuid, image);
    }

    public XMLIconMap flush() {
        int width = (uuidToImage.size() >= wrapCount) ? (iconWidth * wrapCount) : (iconWidth * uuidToImage.size());
        int height = iconHeight * (int) Math.ceil(((double) uuidToImage.keySet().size()) / wrapCount);
        LOGGER.log(Level.FINE, "Drawing IconMap\n  Size: [{0},{1}]\n  Icons: {2}", new Object[]{width, height, uuidToImage.size()});
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Map<String, int[]> uuidToLocation = new HashMap();
        int xOffset = 0;
        int yOffset = 0;
        for (String uuid : uuidToImage.keySet()) {
            BufferedImage image = uuidToImage.get(uuid);
            boolean imageDrawn = outputImage.createGraphics().drawImage(image, xOffset, yOffset, null);
            if (!imageDrawn) {
                System.err.println("Icon with UUID " + uuid + " failed to draw");
            }

            uuidToLocation.put(uuid, new int[]{xOffset, yOffset});

            xOffset += iconWidth;
            if (xOffset + iconWidth > width) {
                xOffset = 0;
                yOffset += iconHeight;
            }
        }

        return new XMLIconMap(outputImage, uuidMapToXML(uuidToLocation));
    }

    private String uuidMapToXML(Map<String, int[]> uuidToLocation) {
        String xml = XMLIconMap.getPrefix(iconWidth, iconHeight);
        for (String uuid : uuidToLocation.keySet()) {
            int[] location = uuidToLocation.get(uuid);
            xml += "\n";
            xml += XMLIconMap.elementToString(uuid, location[0], location[1]);
        }
        xml += "\n";
        xml += XMLIconMap.getSuffix();
        return xml;
    }

    

}

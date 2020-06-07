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

/**
 *
 * @author benne
 */
public class IconMapCreator {

    private Map<BufferedImage, String> imageToUuid = new HashMap();
    private final int wrapCount;
    private final int iconWidth;
    private final int iconHeight;

    public IconMapCreator(int wrapCount, int iconWidth, int iconHeight) {
        this.wrapCount = wrapCount;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    public void addToCurrent(Map<BufferedImage, String> imageToUuid) {
        this.imageToUuid.putAll(imageToUuid);
    }

    public void addToCurrent(BufferedImage image, String uuid) {
        this.imageToUuid.put(image, uuid);
    }

    public XMLIconMap flush() {
        int width = (imageToUuid.keySet().size() >= wrapCount) ? (iconWidth * wrapCount) : (iconWidth * imageToUuid.keySet().size());
        int height = iconHeight * (int) Math.ceil(((double) imageToUuid.keySet().size()) / wrapCount);
        System.out.println("  Drawing IconMap");
        System.out.println("    Size: [" + width + "," + height + "]");
        System.out.println("    Icons: " + imageToUuid.keySet().size());
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Map<String, int[]> uuidToLocation = new HashMap();
        int xOffset = 0;
        int yOffset = 0;
        for (BufferedImage image : imageToUuid.keySet()) {
            String uuid = imageToUuid.get(image);
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

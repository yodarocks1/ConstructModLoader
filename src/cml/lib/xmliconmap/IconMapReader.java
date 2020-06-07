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
public class IconMapReader {

    private Map<BufferedImage, String> imageToUuid = new HashMap();
    private final int iconWidth;
    private final int iconHeight;

    public IconMapReader(int iconWidth, int iconHeight) {
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    public void readIn(String xml, BufferedImage iconMap) {
        Map<String, int[]> elements = XMLIconMap.xmlToElements(xml);
        for (String uuid : elements.keySet()) {
            int[] location = elements.get(uuid);
            imageToUuid.put(iconMap.getSubimage(location[0], location[1], iconWidth, iconHeight), uuid);
        }
    }
    
    public void readIn(XMLIconMap map) {
        readIn(map.iconMapXML, map.iconMapImage);
    }

    public Map<BufferedImage, String> flush() {
        return imageToUuid;
    }
}

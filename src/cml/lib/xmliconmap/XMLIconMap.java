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

import cml.apply.Apply;
import cml.lib.files.AFileManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 *
 * @author benne
 */
public class XMLIconMap {
    
    private static final Logger LOGGER = Logger.getLogger(XMLIconMap.class.getName());

    public static String getPrefix(int iconWidth, int iconHeight) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<MyGUI type=\"Resource\" version=\"1.1\">\n"
                + "    <Resource type=\"ResourceImageSet\" name=\"ItemIconsSetSurvival0\">\n"
                + "        <Group name=\"ItemIconsSurvival\" texture=\"IconMapSurvival.png\" size=\"" + iconWidth + " " + iconHeight + "\">";
    }

    public static String getSuffix() {
        return "        </Group>\n"
                + "    </Resource>\n"
                + "</MyGUI>";
    }

    public static String elementToString(String uuid, int x, int y) {
        return "            <Index name=\"" + uuid + "\">\n"
                + "                <Frame point=\"" + x + " " + y + "\"/>\n"
                + "            </Index>";
    }

    public static Map<String, int[]> xmlToElements(String xml) {
        Map<String, int[]> uuidToLocation = new HashMap();
        Pattern uuidPattern = Pattern.compile("(?<= name\\=\\\")(.*)(?=\\\"\\>)");
        Pattern locationPattern = Pattern.compile("(?<=\\<Frame point\\=\\\")(.*)(?=\\\"/\\>)");
        for (String bit : xml.split("\\<Index")) {
            Matcher uuidMatcher = uuidPattern.matcher(bit);
            Matcher locationMatcher = locationPattern.matcher(bit);
            if (uuidMatcher.find() && locationMatcher.find()) {
                String uuid = uuidMatcher.group(1);
                String location = locationMatcher.group(1);
                int x = Integer.valueOf(location.split(" ")[0]);
                int y = Integer.valueOf(location.split(" ")[1]);
                uuidToLocation.put(uuid, new int[]{x, y});
            }
        }
        return uuidToLocation;
    }

    public static XMLIconMap combineIconMaps(int wrapCount, int iconWidth, int iconHeight, List<XMLIconMap> maps) {
        IconMapReader reader = new IconMapReader(iconWidth, iconHeight);
        IconMapCreator creator = new IconMapCreator(wrapCount, iconWidth, iconHeight);
        maps.forEach(reader::readIn);
        creator.addToCurrent(reader.flush());
        return creator.flush();
    }

    public static XMLIconMap combineIconMaps(int wrapCount, int iconWidth, int iconHeight, XMLIconMap... maps) {
        return combineIconMaps(wrapCount, iconWidth, iconHeight, Arrays.asList(maps));
    }

    public BufferedImage iconMapImage;
    public String iconMapXML;

    public XMLIconMap(BufferedImage iconMapImage, String iconMapXML) {
        this.iconMapImage = iconMapImage;
        this.iconMapXML = iconMapXML;
    }

    public XMLIconMap(File iconMapImage, File iconMapXML) {
        this.iconMapImage = AFileManager.IMAGE_MANAGER.read(iconMapImage);
        this.iconMapXML = AFileManager.FILE_MANAGER.readString(iconMapXML);
    }

}

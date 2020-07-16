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

import cml.Main;
import cml.lib.xmliconmap.CMLIcon.State;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

/**
 *
 * @author benne
 */
public class CMLIconMap {
    
    private static final Logger LOGGER = Logger.getLogger(CMLIconMap.class.getName());
    
    public static final CMLIconMap ICON_MAP;
    
    static {
        CMLIconMap map;
        try {
            map = new CMLIconMap(ImageIO.read(new File(Main.API_DIRECTORY, "IconMap.png")), false);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read CML icon map", ex);
            map = null;
        }
        ICON_MAP = map;
    }
    
    private final BufferedImage map;
    private final BooleanProperty isCompact = new SimpleBooleanProperty();
    
    public CMLIconMap(BufferedImage map, boolean isCompact) {
        this.map = map;
        this.isCompact.setValue(isCompact);
    }

    public final CMLIcon EMPTY = new CMLIcon(1000, 488, 24, 24, this);
    public final CMLIcon BLANK = new CMLIcon(894, 1280, 130, 130, this);
    public final CMLIcon ICON = new CMLIcon(768, 512, 256, 256, this, State.NORMAL, State.ERROR, State.SUCCESS);
    public final CMLIcon ENABLER = new CMLIcon(1413, 390, 259, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon DISABLER = new CMLIcon(1802, 780, 259, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon TO_TRAY = new CMLIcon(1157, 0, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon TO_WINDOW = new CMLIcon(1287, 0, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon MAXIMIZE = new CMLIcon(1417, 0, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon CLOSE = new CMLIcon(1547, 0, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    
    public final CMLIcon LAUNCH_C = new CMLIcon(0, 0, 256, 256, this, State.NORMAL, State.HOVER, State.PRESS, State.ERROR, State.SUCCESS);
    public final CMLIcon LAUNCH_W = new CMLIcon(256, 0, 512, 256, this, State.NORMAL, State.HOVER, State.PRESS, State.ERROR, State.SUCCESS);
    public final CMLIcon SETTINGS_C = new CMLIcon(768, 0, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon SETTINGS_W = new CMLIcon(898, 0, 259, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon BACK_C = new CMLIcon(1677, 0, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon BACK_W = new CMLIcon(1807, 0, 259, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon WORKSHOP_C = new CMLIcon(1672, 390, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon WORKSHOP_W = new CMLIcon(1802, 390, 259, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon OPEN_FOLDER_C = new CMLIcon(1024, 780, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon OPEN_FOLDER_W = new CMLIcon(1154, 780, 259, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon PROFILE_SETTINGS_C = new CMLIcon(1413, 780, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon PROFILE_SETTINGS_W = new CMLIcon(1543, 780, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon PROFILE_LIST_C = new CMLIcon(1024, 390, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon PROFILE_LIST_W = new CMLIcon(1154, 390, 259, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon DELETE_C = new CMLIcon(1024, 1170, 130, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    public final CMLIcon DELETE_W = new CMLIcon(1154, 1170, 259, 130, this, State.NORMAL, State.HOVER, State.PRESS);
    
    public final CMLIconConditional LAUNCH = new CMLIconConditional(LAUNCH_C, LAUNCH_W, this, isCompact);
    public final CMLIconConditional SETTINGS = new CMLIconConditional(SETTINGS_C, SETTINGS_W, this, isCompact);
    public final CMLIconConditional BACK = new CMLIconConditional(BACK_C, BACK_W, this, isCompact);
    public final CMLIconConditional WORKSHOP = new CMLIconConditional(WORKSHOP_C, WORKSHOP_W, this, isCompact);
    public final CMLIconConditional OPEN_FOLDER = new CMLIconConditional(OPEN_FOLDER_C, OPEN_FOLDER_W, this, isCompact);
    public final CMLIconConditional PROFILE_SETTINGS = new CMLIconConditional(PROFILE_SETTINGS_C, PROFILE_SETTINGS_W, this, isCompact);
    public final CMLIconConditional PROFILE_LIST = new CMLIconConditional(PROFILE_LIST_C, PROFILE_LIST_W, this, isCompact);
    public final CMLIconConditional DELETE = new CMLIconConditional(DELETE_C, DELETE_W, this, isCompact);
    
    public Image getIcon(int x, int y, int w, int h) {
        return SwingFXUtils.toFXImage(map.getSubimage(x, y, w, h), null);
    }
    
    public BooleanProperty getCompactProperty() {
        return isCompact;
    }
    
    public boolean isCompact() {
        return isCompact.get();
    }
    
    public void setCompact(boolean compact) {
        isCompact.setValue(compact);
    }
    
    public BufferedImage getMap() {
        return map;
    }
    
}

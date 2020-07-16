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

import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
public class CMLIcon {
    
    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final CMLIconMap map;
    private final State[] states;
    
    public CMLIcon(int x, int y, int w, int h, CMLIconMap map, State... states) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.map = map;
        this.states = states;
    }
    
    public CMLIcon(int x, int y, int w, int h, CMLIconMap map) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.map = map;
        this.states = new State[] {State.NORMAL};
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public State[] getStates() {
        return states;
    }
    
    public Image getIcon(int stateIndex, Image empty) {
        if (stateIndex >= states.length) {
            return empty;
        } else {
            return map.getIcon(getX(), getY() + (getH() * stateIndex), getW(), getH());
        }
    }
    
    public Image getIcon(State state, Image empty) {
        int index = 0;
        for (index = 0; index < states.length; index++) {
            if (states[index].equals(state)) {
                break;
            } else if (index == states.length - 1) {
                return empty;
            }
        }
        return getIcon(index, empty);
    }
    
    public Image getIcon(int stateIndex) {
        return getIcon(stateIndex, this.map.BLANK.getIcon());
    }
    
    public Image getIcon(State state) {
        return getIcon(state, this.map.BLANK.getIcon());
    }
    
    private Image getIcon() {
        return map.getIcon(getX(), getY(), getW(), getH());
    }
    
    public enum State {
        NORMAL(0), HOVER(1), PRESS(2), DISABLE(3), SUCCESS(4), ERROR(5);
        
        private final int value;
        
        private State(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
}

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

import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

/**
 *
 * @author benne
 */
public class CMLIconConditional extends CMLIcon {
    
    private final CMLIcon trueState;
    private final CMLIcon falseState;
    private final BooleanProperty property;

    public CMLIconConditional(CMLIcon trueValue, CMLIcon falseValue, CMLIconMap map, BooleanProperty property) {
        super(trueValue.getX(), trueValue.getY(), trueValue.getW(), trueValue.getH(), map, trueValue.getStates());
        this.trueState = trueValue;
        this.falseState = falseValue;
        this.property = property;
    }

    @Override
    public int getH() {
        return property.getValue() ? trueState.getH() : falseState.getH();
    }

    @Override
    public int getW() {
        return property.getValue() ? trueState.getW() : falseState.getW();
    }

    @Override
    public int getY() {
        return property.getValue() ? trueState.getY() : falseState.getY();
    }

    @Override
    public int getX() {
        return property.getValue() ? trueState.getX() : falseState.getX();
    }
    
    public BooleanProperty getProperty() {
        return property;
    }
    
}

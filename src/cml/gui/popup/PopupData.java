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
package cml.gui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 * 
 * @author benne
 * @param <R> Result type
 * @param <D> Internal data type (Should be an immutable type unless you have a 
 * <em>compelling</em> reason otherwise)
 */
public abstract class PopupData<R, D> {

    private final Map<Button, D> buttonData = new HashMap();
    private final Set<Button> buttonCloses = new HashSet();
    private final List<Button> buttons = new ArrayList();
    private D pressed = null;

    /**
     * It is suggested that you only return the root node here.
     * This is because it is better to create and cache it in a background
     * thread rather than doing everything in the FX thread.
     * @return The node to wrap
     */
    protected abstract Node getNode();

    /**
     * Compute the result using the internal data.
     * @param data The internal data as determined by the pressed button
     * @return The result to return.
     */
    protected abstract R getResult(D data);

    /**
     * Use to set title, make draggable, etc.
     * It is suggested that you create and cache the root node here. This will
     * run in a background thread.
     * @param parent CmlPopup that contains this instance.
     */
    protected abstract void setup(CmlPopup parent);

    //Final methods
    
    /**
     * Get the node that this popup should display
     * @return The computed node
     */
    public final Node toNode() {
        return getNode();
    }
    
    /**
     * Get the result of the popup
     * @return The computed result
     */
    public final R getResult() {
        return getResult(pressed);
    }

    /**
     * Adds a button to the popup
     * @param button The button itself
     * @param data The data that it represents
     * @param doClose Whether the button should close the popup on press
     */
    protected final void addButton(Button button, D data, boolean doClose) {
        buttonData.put(button, data);
        buttons.add(button);
        if (doClose) {
            buttonCloses.add(button);
        }
    }
    
    //Package-private methods
    
    final void doSetup(CmlPopup parent) {
        setup(parent);
    }
    
    final void pressed(Button button) {
        D data = buttonData.get(button);
        if (data != null) {
            pressed = data;
        }
    }
    
    final List<Button> getButtons() {
        return buttons;
    }

    final boolean doClose(Button button) {
        return buttonCloses.contains(button);
    }

}

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
 */
public abstract class PopupData<R, D> {

    private final Map<Button, D> buttonData = new HashMap();
    private final Set<Button> buttonCloses = new HashSet();
    private final List<Button> buttons = new ArrayList();
    private Button pressed = null;

    protected abstract Node getNode();

    protected abstract R getResult(D data);

    /**
     * Use to set title, make draggable, etc.
     * @param parent CmlPopup that contains this instance.
     */
    protected abstract void setup(CmlPopup parent);

    //Final methods
    
    public final Node toNode() {
        return getNode();
    }
    
    public final void doSetup(CmlPopup parent) {
        setup(parent);
    }
    
    public final R getResult() {
        return getResult(buttonData.get(pressed));
    }

    protected final void addButton(Button button, D data, boolean doClose) {
        buttonData.put(button, data);
        buttons.add(button);
        if (doClose) {
            buttonCloses.add(button);
        }
    }
    
    public final void pressed(Button button) {
        if (buttonData.get(button) != null) {
            pressed = button;
        }
    }
    
    public final List<Button> getButtons() {
        return buttons;
    }

    public final boolean doClose(Button button) {
        return buttonCloses.contains(button);
    }

}

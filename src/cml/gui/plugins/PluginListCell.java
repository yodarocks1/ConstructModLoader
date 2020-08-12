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
package cml.gui.plugins;

import cml.beans.Plugin;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

/**
 *
 * @author benne
 */
public class PluginListCell extends ListCell<Plugin> {
    
    private final PluginsController controller;

    public PluginListCell(PluginsController controller) {
        this.controller = controller;
    }

    @Override
    protected void updateItem(Plugin item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            PluginData dataView = item.dataView;
            if (controller.isVisible()) {
                if (dataView != null) {
                    dataView.doInit(controller);
                    setGraphic(dataView.toNode());
                } else {
                    setGraphic(new Label(item.getName()));
                }
            } else {
                controller.visibleProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> obs, Boolean oldValue, Boolean newValue) {
                        controller.visibleProperty().removeListener(this);
                        if (newValue) {
                            updateItem(item, empty);
                        }
                    }
                });
            }
        }
    }
    
}

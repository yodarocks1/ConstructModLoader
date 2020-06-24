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
package cml.gui.workshop;

import cml.lib.workshop.WorkshopMod;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 *
 * @author benne
 */
public class WorkshopListCell extends ListCell<WorkshopMod> {
    
    private final ListView listView;
    private final WorkshopController controller;
    
    public WorkshopListCell(ListView listView, WorkshopController controller) {
        this.listView = listView;
        this.controller = controller;
    }
    
    @Override
    protected void updateItem(WorkshopMod item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            WorkshopModData dataView = item.dataView;
            if (dataView != null) {
                dataView.doInit(controller);
                dataView.setInfo(listView.maxWidthProperty());
                setGraphic(dataView.toNode());
            } else {
                setGraphic(new Label(item.getName()));
            }
        }
    }
    
}

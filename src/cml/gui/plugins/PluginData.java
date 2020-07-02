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

import cml.beans.ANodeData;
import cml.beans.Plugin;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

/**
 *
 * @author benne
 */
public class PluginData extends ANodeData<Plugin> {

    public PluginData(Plugin plugin) {
        super("PluginListItem.fxml", plugin);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ButtonBase button;
        if (wrapped.isExecutable()) {
            button = new Button("Launch");
        } else if (wrapped.isHook()) {
            button = new ToggleButton("Enable");
            wrapped.getEnabledProperty().bind(((ToggleButton) button).selectedProperty());
        } else {
            button = new Button("Delete");
            button.getStyleClass().add("cancelButton");
            button.setOnAction((event) -> {
                wrapped.delete();
                root.setVisible(false);
                root.setMouseTransparent(true);
            });
        }
        button.setStyle("margin-right: 10px");

        ImageView icon = new ImageView(wrapped.getImageUrl());
        icon.setFitHeight(60);
        icon.setFitWidth(60);
        icon.setPreserveRatio(true);

        Line separator1 = new Line(0, -60, 0, 60);
        separator1.getStyleClass().add("separator1");

        Text name = new Text(wrapped.getName());
        name.getStyleClass().add("name");

        Line separator2 = new Line(0, -60, 0, 60);
        separator2.getStyleClass().add("separator2");

        Text description = new Text(wrapped.getDescription());
        description.getStyleClass().add("description");

        this.root.getChildren().setAll(button, icon);
    }

}

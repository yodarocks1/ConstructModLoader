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

import cml.Main;
import cml.beans.Plugin;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 *
 * @author benne
 */
public class PluginData implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(PluginData.class.getName());

    private PluginsController controller;
    protected final Plugin wrapped;
    private boolean initialized = false;

    @FXML AnchorPane root;
    @FXML ImageView image;
    @FXML Text name;
    @FXML Text author;
    @FXML TextArea description;
    @FXML Button launch;
    @FXML CheckMenuItem enabled;
    @FXML CheckMenuItem autorun;
    @FXML CheckMenuItem showAdvanced;
    @FXML Button openConfig;
    @FXML Button help;
    @FXML Button info;
    @FXML Rectangle configTooltipBackground;
    @FXML Text configTooltipText;

    public PluginData(Plugin plugin) {
        this.wrapped = plugin;
    }

    public void doInit(PluginsController controller) {
        this.controller = controller;
        if (!initialized) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PluginListItem.fxml"));
            loader.setController(this);
            try {
                loader.load();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not read FXML file", ex);
            }
            initialized = true;
        }
    }

    public Node toNode() {
        return root;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            image.setImage(new Image(wrapped.getImageUrl()));
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.SEVERE, "Could not read image for plugin " + wrapped.getName(), ex);
        }

        name.setText(wrapped.getName());
        author.setText((String) wrapped.getProperty("author", ""));
        description.setText(wrapped.getDescription());

        if (wrapped.isExecutable() || wrapped.isFXML()) {
            launch.setOnAction((event) -> {
                if (wrapped.isEnabled()) {
                    try {
                        wrapped.run();
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Exception when launching plugin \"" + name.getText() + "\"", ex);
                    }
                }
            });
            autorun.selectedProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != oldValue) {
                    wrapped.setAutoRun(newValue);
                }
            });
        } else {
            launch.setDisable(true);
            autorun.setSelected(false);
            autorun.setDisable(true);
        }

        enabled.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != wrapped.isEnabled()) {
                if (newValue) {
                    wrapped.enable();
                    if (Main.PLUGINS_ENABLED_PROPERTY.get()) {
                        launch.setDisable(false);
                        if (Main.AUTORUN_ENABLED_PROPERTY.get()) {
                            autorun.setDisable(false);
                        }
                    }
                } else {
                    wrapped.disable();
                    launch.setDisable(true);
                    autorun.setDisable(true);
                }
            }
        });

        wrapped.getEnabledProperty().addListener((obs, oldValue, newValue) -> {
            if (enabled.isSelected() != newValue) {
                enabled.setSelected(newValue);
            }
        });

        if (wrapped.hasProperty("info")) {
            String link = (String) wrapped.getProperty("info");
            info.setOnAction((event) -> {
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + link);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to open link: url=\"" + link + "\"", ex);
                }
            });
        } else {
            info.setDisable(true);
        }

        if (wrapped.hasProperty("help")) {
            String link = (String) wrapped.getProperty("help");
            help.setOnAction((event) -> {
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + link);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to open link: url=\"" + link + "\"", ex);
                }
            });
        } else {
            help.setDisable(true);
        }

        openConfig.setOnAction((event) -> {
            wrapped.openConfig();
        });
        openConfig.visibleProperty().bind(showAdvanced.selectedProperty());
        openConfig.setOnMouseEntered((event) -> {
            configTooltipBackground.setVisible(true);
            configTooltipText.setVisible(true);
        });
        openConfig.setOnMouseExited((event) -> {
            configTooltipBackground.setVisible(false);
            configTooltipText.setVisible(false);
        });

        Main.PLUGINS_ENABLED_PROPERTY.addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                enabled.setDisable(false);
                if (enabled.isSelected()) {
                    launch.setDisable(false);
                    if (Main.AUTORUN_ENABLED_PROPERTY.get()) {
                        autorun.setDisable(false);
                    }
                }
                showAdvanced.setDisable(false);
            } else {
                enabled.setDisable(true);
                launch.setDisable(true);
                showAdvanced.setDisable(true);
                showAdvanced.setSelected(false);
            }
        });

        Main.AUTORUN_ENABLED_PROPERTY.addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                if (enabled.isSelected() && Main.PLUGINS_ENABLED_PROPERTY.get()) {
                    autorun.setDisable(false);
                }
            } else {
                autorun.setDisable(true);
            }
        });

        //Default values
        enabled.setSelected(wrapped.isEnabled());
        autorun.setSelected(wrapped.isAutoRun());

        if (Main.PLUGINS_ENABLED_PROPERTY.get()) {
            enabled.setDisable(false);
            if (enabled.isSelected()) {
                launch.setDisable(false);
            }
            showAdvanced.setDisable(false);
        } else {
            enabled.setDisable(true);
            launch.setDisable(true);
            showAdvanced.setDisable(true);
            showAdvanced.setSelected(false);
        }

        if (Main.AUTORUN_ENABLED_PROPERTY.get()) {
            if (enabled.isSelected()) {
                autorun.setDisable(false);
            }
        } else {
            autorun.setDisable(true);
        }
    }

}

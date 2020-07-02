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

import cml.ErrorManager;
import cml.Images;
import cml.beans.Profile;
import cml.gui.main.MainController;
import cml.lib.threadmanager.ThreadManager;
import cml.lib.workshop.WorkshopConnectionHandler;
import cml.lib.workshop.WorkshopMod;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

/**
 *
 * @author benne
 */
public class WorkshopModData implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(WorkshopModData.class.getName());

    private WorkshopController controller;
    protected final WorkshopMod mod;
    private boolean initialized = false;

    @FXML private AnchorPane root;
    @FXML private ImageView image;
    @FXML private Text header;
    @FXML private Text description;
    @FXML private Text properties;
    @FXML private ImageView showInWorkshop;
    @FXML private ImageView convert;

    public WorkshopModData(WorkshopMod item) {
        this.mod = item;
    }

    public void doInit(WorkshopController controller) {
        this.controller = controller;
        if (!initialized) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("WorkshopListItem.fxml"));
            loader.setController(this);
            try {
                loader.load();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not read FXML file", ex);
            }
            initialized = true;
        }
    }

    public void setInfo(DoubleProperty listWidth) {
        root.maxWidthProperty().bind(listWidth);
    }

    public Node toNode() {
        return root;
    }

    public void showInWorkshop() {
        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler steam://url/SteamWorkshopPage/" + mod.getWorkshopId());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open mod #" + mod.getWorkshopId() + " in the Steam Workshop", ex);
        }
    }

    public void convert() {
        if (mod.isApplicable()) {
            Profile destination = controller.getDestination();
            if (destination != null) {
                Images.CONVERT_PRESS_ANIM.animate(convert);
                Thread convertThread = new Thread(() -> {
                    WorkshopConnectionHandler.connectAndConvertInto(mod, destination.getDirectory());
                });
                ThreadManager.addThread(convertThread);
                convertThread.start();
            } else {
                LOGGER.log(Level.SEVERE, "Could not convert mod #{0} because the workshop destination profile has not been set", mod.getWorkshopId());
                ErrorManager.addStateCause("WorkshopDestination == null");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MainController.setImageMouseHandlers(showInWorkshop, Images.WORKSHOP, Images.WORKSHOP_SELECT, Images.WORKSHOP_PRESS);
        if (mod.isApplicable()) {
            MainController.setImageMouseHandlers(convert, Images.CONVERT, Images.CONVERT_SELECT_ANIM, Images.CONVERT_PRESS_ANIM, false);
        }
        if (mod.getPreview().exists()) {
            try {
                this.image.setImage(new javafx.scene.image.Image(new FileInputStream(mod.getPreview())));
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.FINE, "Mod #{0} does not have a preview.", mod.getWorkshopId());
            }
        }
        this.header.setText(mod.getName());
        this.description.setText(mod.getDescription());
        if (mod.isCMLMod()) {
            this.properties.setText(
                    "\"CML\" : \"true\"\n"
                    + "\"Workshop ID" + mod.getWorkshopId()
            );
        } else {
            this.properties.setText(
                    "\"CML\" : \"false\"\n"
                    + "\"Convertable\" : \"" + mod.isApplicable() + "\"\n"
                    + "\"ID\" : " + mod.getWorkshopId()
            );
        }
    }

}

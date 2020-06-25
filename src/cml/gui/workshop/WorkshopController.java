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
import cml.Main;
import cml.beans.Profile;
import cml.gui.main.MainController;
import cml.gui.main.SubController;
import cml.lib.threadmanager.ThreadManager;
import cml.lib.workshop.WorkshopMod;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author benne
 */
public class WorkshopController extends SubController {

    private static final Logger LOGGER = Logger.getLogger(WorkshopController.class.getName());

    @FXML private AnchorPane workshopPane;
    @FXML private ListView<WorkshopMod> workshopList;
    @FXML private ListView<WorkshopMod> searchList;
    @FXML private HBox workshopHeader;
    @FXML private ChoiceBox<Profile> workshopDestinationProfile;
    @FXML private TextField searchName;
    @FXML private TextField searchID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        workshopPane.widthProperty().addListener((obs, oldValue, newValue) -> {
            workshopHeader.setTranslateX((newValue.doubleValue() - workshopHeader.getPrefWidth()) / 2);
        });
        Main.profileList.addListener((obs, oldValue, newValue) -> {
            Profile selected = workshopDestinationProfile.getValue();
            workshopDestinationProfile.getItems().setAll(newValue);
            if (selected != null && selected.getDirectory() != null && selected.getDirectory().exists()) {
                boolean hasOldSelection = newValue.stream().filter((newVal) -> selected.getName().equals(newVal.getName())).findFirst().isPresent();
                if (hasOldSelection) {
                    workshopDestinationProfile.setValue(selected);
                }
            }
        });
        workshopDestinationProfile.setConverter(new StringConverter<Profile>() {
            @Override
            public String toString(Profile object) {
                return object.getName();
            }

            @Override
            public Profile fromString(String string) {
                return workshopDestinationProfile.getItems().stream().filter((Profile profile) -> profile.getName().equals(string)).findFirst().orElse(null);
            }
        });
        Main.workshopReader.addListener((obs, oldValue, newValue) -> {
            workshopList.itemsProperty().bind(newValue.getItems());
        });
        if (Main.profileList.getValue() != null) {
            workshopDestinationProfile.getItems().setAll(Main.profileList.getValue());
        }
        if (Main.workshopReader.getValue() != null) {
            workshopList.itemsProperty().bind(Main.workshopReader.getValue().getItems());
        } else {
            ErrorManager.addStateCause("WorkshopFolder <INVALID>");
        }
        workshopDestinationProfile.valueProperty().addListener((event) -> {
            ErrorManager.removeStateCause("WorkshopDestination == null");
        });

        ErrorManager.addCauseResolver("WorkshopDestination == null", () -> {
            mainController.switchToMenu(MainController.WORKSHOP);
            workshopDestinationProfile.requestFocus();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("WorkshopDestination == null");
            alert.setContentText("Please choose a destination profile for converted workshop mods");
            alert.setTitle("User Error Resolver");
            alert.initModality(Modality.NONE);
            alert.show();
        });

        searchID.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("^[0-9]*$")) {
                searchID.setText(oldValue);
            }
        });

        Callback<ListView<WorkshopMod>, ListCell<WorkshopMod>> cellFactory = (ListView<WorkshopMod> listView) -> new WorkshopListCell(listView, this);
        workshopList.setCellFactory(cellFactory);
        searchList.setCellFactory(cellFactory);

        searchList.setVisible(false);
        searchList.setMouseTransparent(true);
    }

    @Override
    public void setVisible(boolean visible) {
        workshopPane.setVisible(visible);
    }

    protected Profile getDestination() {
        return (Profile) workshopDestinationProfile.getValue();
    }

    public void doSearch() {
        String search = searchName.getText().trim().toLowerCase();
        String idSearch = searchID.getText().trim().toLowerCase();
        searchName.setStyle("");
        searchID.setStyle("");

        if (search.length() > 0 || idSearch.length() > 0) {
            LOGGER.log(Level.INFO, "Initiating search '{'Str=\"{0}\",ID={1}'}'", new Object[]{search, idSearch});
            Map<WorkshopMod, Integer> relevance = new HashMap();
            List<WorkshopMod> lowRelevance = new ArrayList();
            for (WorkshopMod mod : workshopList.getItems().toArray(new WorkshopMod[0])) {
                String modName = mod.getName().toLowerCase();

                int relation = 0;
                if (modName.startsWith(search)) {
                    relation = 2;
                } else if (modName.contains(search)) {
                    relation = 1;
                }

                String idString = Integer.toString(mod.getWorkshopId());

                if (idString.equals(search)) {
                    relation += 3;
                } else if (idString.startsWith(search)) {
                    relation += 2;
                } else if (idString.endsWith(search)) {
                    relation += 1;
                }

                if (idSearch.length() > 0) {
                    if (!idString.contains(idSearch)) {
                        relation = 0;
                    } else if (idString.equals(idSearch)) {
                        relation += 3;
                    } else if (idString.startsWith(idSearch)) {
                        relation += 2;
                    } else if (idString.endsWith(idSearch)) {
                        relation += 1;
                    }
                }

                if (relation >= 2) {
                    relevance.put(mod, relation);
                }
                if (relation == 1) {
                    lowRelevance.add(mod);
                }
            }
            if (relevance.keySet().size() <= 2) {
                lowRelevance.forEach((mod) -> relevance.put(mod, 1));
            }
            if (!relevance.keySet().isEmpty()) {
                List<WorkshopMod> searchData = new ArrayList(relevance.keySet());
                searchData.sort(Comparator.comparingInt(relevance::get).reversed());
                LOGGER.log(Level.INFO, "Search found {0} items", searchData.size());
                searchList.getItems().setAll(searchData);
                searchList.setVisible(true);
                searchList.setMouseTransparent(false);
                return;
            } else {
                searchName.setStyle("-fx-text-fill: #ff0000");
                searchID.setStyle("-fx-text-fill: #ff0000");
            }
        }
        searchList.setVisible(false);
        searchList.setMouseTransparent(true);
    }

    private final Runnable lazySearch = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (!Thread.interrupted()) {
                doSearch();
                lazySearchThread = null;
                ThreadManager.removeThread(Thread.currentThread());
            }
        }
    };

    private Thread lazySearchThread = null;

    public void doSearchLazy() {
        if (lazySearchThread != null) {
            lazySearchThread.interrupt();
        }
        lazySearchThread = new Thread(lazySearch);
        ThreadManager.addThread(lazySearchThread);
        lazySearchThread.start();
    }

}

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
import cml.gui.main.SubController;
import cml.lib.threadmanager.ThreadManager;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author benne
 */
public class PluginsController extends SubController {

    private static final Logger LOGGER = Logger.getLogger(PluginsController.class.getName());

    @FXML private AnchorPane root;
    @FXML private TextField searchName;
    @FXML private TextField searchAuthor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @Override
    public void setVisible(boolean visible) {
        root.setVisible(visible);
    }

    public void doSearch() {
//        String nameSearch = searchName.getText().trim().toLowerCase();
//        String authorSearch = searchAuthor.getText().trim().toLowerCase();
//        searchName.setStyle("");
//        searchAuthor.setStyle("");
//
//        if (nameSearch.length() > 0 || authorSearch.length() > 0) {
//            LOGGER.log(Level.INFO, "Initiating search '{'Str=\"{0}\",ID={1}'}'", new Object[]{nameSearch, authorSearch});
//            Map<Plugin, Integer> relevance = new HashMap();
//            List<Plugin> lowRelevance = new ArrayList();
//            for (Plugin plugin : pluginList.getItems().toArray(new Plugin[0])) {
//                String pluginName = plugin.getName().trim().toLowerCase();
//
//                int relation = 0;
//                if (pluginName.startsWith(nameSearch)) {
//                    relation = 2;
//                } else if (pluginName.contains(nameSearch)) {
//                    relation = 1;
//                }
//
//                if (plugin.hasProperty("author")) {
//                    String authorString = ((String) plugin.getProperty("author")).trim().toLowerCase();
//
//                    if (authorString.equals(nameSearch)) {
//                        relation += 3;
//                    } else if (authorString.contains(nameSearch)) {
//                        relation += 2;
//                    }
//
//                    if (authorSearch.length() > 0) {
//                        if (authorString.equals(authorSearch)) {
//                            relation += 3;
//                        } else if (authorString.contains(authorSearch)) {
//                            relation += 2;
//                        }
//                    }
//                } else {
//                    relation++;
//                }
//
//                if (relation >= 2) {
//                    relevance.put(plugin, relation);
//                }
//                if (relation == 1) {
//                    lowRelevance.add(plugin);
//                }
//            }
//            if (relevance.keySet().size() <= 2) {
//                lowRelevance.forEach((mod) -> relevance.put(mod, 1));
//            }
//            if (!relevance.keySet().isEmpty()) {
//                List<Plugin> searchData = new ArrayList(relevance.keySet());
//                searchData.sort(Comparator.comparingInt(relevance::get).reversed());
//                LOGGER.log(Level.INFO, "Search found {0} items", searchData.size());
//                searchList.getItems().setAll(searchData);
//                searchList.setVisible(true);
//                searchList.setMouseTransparent(false);
//                return;
//            } else {
//                searchName.setStyle("-fx-text-fill: #ff0000");
//                searchAuthor.setStyle("-fx-text-fill: #ff0000");
//            }
//        }
//        searchList.setVisible(false);
//        searchList.setMouseTransparent(true);
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

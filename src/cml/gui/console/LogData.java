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
package cml.gui.console;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 *
 * @author benne
 */
public class LogData implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LogData.class.getName());
    private static final Map<Level, Color> LEVEL_TO_COLOR = new HashMap();

    static {
        LEVEL_TO_COLOR.put(Level.ALL, Color.PURPLE);
        LEVEL_TO_COLOR.put(Level.OFF, Color.MAGENTA);
        LEVEL_TO_COLOR.put(Level.CONFIG, Color.CADETBLUE);
        LEVEL_TO_COLOR.put(Level.FINE, Color.AQUAMARINE);
        LEVEL_TO_COLOR.put(Level.FINER, Color.AQUAMARINE);
        LEVEL_TO_COLOR.put(Level.FINEST, Color.AQUAMARINE);
        LEVEL_TO_COLOR.put(Level.INFO, Color.WHITESMOKE);
        LEVEL_TO_COLOR.put(Level.WARNING, Color.YELLOW);
        LEVEL_TO_COLOR.put(Level.SEVERE, Color.RED);
    }

    private final LogRecord log;

    @FXML private AnchorPane logPane;
    @FXML private Text level;
    @FXML private Text dateTime;
    @FXML private TextArea message;
    @FXML private Text source;

    @SuppressWarnings("LeakingThisInConstructor")
    public LogData(LogRecord log) {
        this.log = log;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("listCellItem.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not read FXML file", ex);
        }
    }

    public void setInfo(ListView list) {
        list.widthProperty().addListener((obs, oldValue, newValue) -> {
            double newWidth = Math.max(logPane.getMinWidth(), newValue.doubleValue() - 20);
            logPane.setPrefWidth(newWidth);
            logPane.setMaxWidth(newWidth);
        });
    }

    public Node getNode() {
        return logPane;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Context Menu
        final ContextMenu contextMenu = new ContextMenu();
        final Clipboard clipboard = Clipboard.getSystemClipboard();

        MenuItem copyMessage = new MenuItem("Copy message");
        copyMessage.setOnAction((event) -> {
            ClipboardContent messageContent = new ClipboardContent();
            messageContent.putString(log.getMessage());
            messageContent.put(ConsoleController.RECORD_CLIPBOARD_FORMAT, log);
            clipboard.setContent(messageContent);
        });

        MenuItem copyAll = new MenuItem("Copy all data");
        copyAll.setOnAction((event) -> {
            ClipboardContent allContent = new ClipboardContent();
            allContent.putString(ConsoleController.CLIPBOARD_FORMATTER.format(log));
            allContent.put(ConsoleController.RECORD_CLIPBOARD_FORMAT, log);
            clipboard.setContent(allContent);
        });

        contextMenu.getItems().addAll(copyMessage, copyAll);
        logPane.setOnContextMenuRequested((event) -> {
            contextMenu.show(logPane, event.getScreenX(), event.getScreenY());
        });

        logPane.widthProperty().addListener((obs, oldValue, newValue) -> {
            message.setPrefWidth(newValue.doubleValue() - 15);
            message.setMaxWidth(newValue.doubleValue() - 15);
            logPane.setMaxHeight((message.prefHeight(newValue.doubleValue()) - 26) + logPane.getMinHeight());
        });

        level.setText("[" + log.getLevel().getName() + "]");
        level.setFill(LEVEL_TO_COLOR.get(log.getLevel()));

        dateTime.setText(String.format("[%1$tF %1$tT]", log.getMillis()));

        Throwable thrown = log.getThrown();
        if (thrown != null) {
            message.setText(log.getMessage() + "\n");
        } else {
            message.setText(log.getMessage());
        }

        source.setText(log.getLoggerName() + " - " + log.getSourceClassName() + "::" + log.getSourceMethodName());
    }

}

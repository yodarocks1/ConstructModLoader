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

import cml.Constants;
import cml.GUI;
import cml.Media;
import cml.Main;
import cml.lib.threadmanager.ThreadManager;
import cml.lib.xmliconmap.CMLIconConditional;
import cml.lib.xmliconmap.CMLIconMap;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.SystemTray;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author benne
 */
public class ConsoleController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ConsoleController.class.getName());
    public static final DataFormat RECORD_CLIPBOARD_FORMAT = new DataFormat("LogRecord");
    public static final DataFormat CONSOLE_CLIPBOARD_FORMAT = new DataFormat("List<LogRecord>");
    public static final Formatter CLIPBOARD_FORMATTER = new XMLFormatter();

    private Set<Level> doLogPointer;
    private LogHandler handler;
    private Stage stage;
    private GUI gui;
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    @FXML private ListView console;
    @FXML private MenuButton fineness;
    @FXML private ToggleButton config;
    @FXML private ToggleButton info;
    @FXML private ToggleButton warning;
    @FXML private ToggleButton severe;
    @FXML private Rectangle header;

    public ObservableList<LogData> getItems() {
        return console.getItems();
    }

    public void initHandler(Set<Level> doLogs, LogHandler handler) {
        this.doLogPointer = doLogs;
        updateToggles();
        if (doLogPointer.contains(Level.FINEST)) {
            setFinest();
        } else if (doLogPointer.contains(Level.FINER)) {
            setFiner();
        } else if (doLogPointer.contains(Level.FINE)) {
            setFine();
        } else {
            setUnfine();
        }
        this.handler = handler;
    }

    private volatile boolean isConsoleManuallyClosed = !Main.showConsole;

    public void initStage(Stage stage, GUI gui) {
        Platform.setImplicitExit(false);
        this.stage = stage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getIcons().add(CMLIconMap.ICON_MAP.ICON.getIcon(0));
        stage.setTitle("Console - Construct Mod Loader");

        PopupMenu menu = new PopupMenu();
        MenuItem hideLoader = new MenuItem("Hide Loader");
        MenuItem showLoader = new MenuItem("Show Loader");
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();

            MenuItem maximizeItem = new MenuItem("Show Console");
            maximizeItem.addActionListener(e -> Platform.runLater(() -> maximizeFromTray()));
            hideLoader.addActionListener(e -> Platform.runLater(() -> {
                gui.hide();
                menu.remove(1);
                menu.insert(showLoader, 1);
            }));
            showLoader.addActionListener(e -> Platform.runLater(() -> {
                gui.show();
                menu.remove(1);
                menu.insert(hideLoader, 1);
            }));
            MenuItem closeItem = new MenuItem("Close Construct Mod Loader");
            closeItem.addActionListener(e -> Platform.exit());
            menu.add(maximizeItem);
            menu.add(hideLoader);
            menu.add(closeItem);

            trayIcon = new TrayIcon(SwingFXUtils.fromFXImage(CMLIconMap.ICON_MAP.ICON.getIcon(0), null), "Console - Construct Mod Loader", menu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(() -> maximizeFromTray()));

            try {
                systemTray.add(trayIcon);
                systemTray.remove(trayIcon);
            } catch (AWTException e) {
                LOGGER.log(Level.SEVERE, "Failed to test Console in System Tray.", e);
                trayIcon = null;
            }
        }
        if (trayIcon != null) {
            EventHandler<WindowEvent> consoleCloseHandler = (event) -> {
                Platform.runLater(() -> {
                    if (gui != null && gui.isShowing()) {
                        this.isConsoleManuallyClosed = true;
                        minimizeToTray();
                    } else {
                        Platform.exit();
                    }
                });
                event.consume();
            };

            stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, consoleCloseHandler);
            stage.setOnCloseRequest(consoleCloseHandler);

            stage.iconifiedProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue) {
                    Platform.runLater(() -> minimizeToTray());
                } else {
                    Platform.runLater(() -> maximizeFromTray());
                }
            });

            if (gui != null) {
                EventHandler<WindowEvent> stageCloseHandler = (event) -> {
                    if (this.isConsoleManuallyClosed) {
                        systemTray.remove(trayIcon);
                        Platform.exit();
                    } else {
                        Platform.runLater(() -> {
                            gui.hide();
                            menu.remove(1);
                            menu.insert(showLoader, 1);
                        });
                    }
                    event.consume();
                };

                gui.addWindowCloseHandler(stageCloseHandler);
                gui.setOnCloseRequest(stageCloseHandler);
            }

            ThreadManager.onStop(() -> {
                systemTray.remove(trayIcon);
            });
        }
        header.widthProperty().bind(stage.widthProperty());
        makeDraggable(header, stage);
    }

    public void minimizeToTray() {
        if (trayIcon != null) {
            boolean minimize = false;
            try {
                systemTray.add(trayIcon);
            } catch (AWTException ex) {
                LOGGER.log(Level.SEVERE, "Failed to add Console to System Tray. Minimizing instead.", ex);
                minimize = true;
            }
            if (minimize) {
                stage.setIconified(true);
            } else {
                stage.hide();
                if (this.isConsoleManuallyClosed) {
                    trayIcon.displayMessage("CML Console", "The Console has been closed to the system tray. Closing the Loader at this point will exit the program.", TrayIcon.MessageType.INFO);
                } else {
                    trayIcon.displayMessage("CML Console", "The Console has been minimized to the system tray.", TrayIcon.MessageType.INFO);
                }
            }
        }
    }

    private void maximizeFromTray() {
        if (trayIcon != null) {
            stage.show();
            systemTray.remove(trayIcon);
            stage.setIconified(false);
            stage.requestFocus();
            gui.show();
        }
    }

    public void maximizeSize() {
        stage.setMaximized(!stage.isMaximized());
    }

    public void tryClose() {
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private final EventHandler<ActionEvent> writeToFile = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            File outputFile = makeDialog();
            while (!outputFile.getParentFile().exists() || !outputFile.getParentFile().isDirectory()) {
                warnFailure();
                makeDialog();
            }
            write(outputFile);
        }

        private File makeDialog() {
            TextInputDialog writeToFileDialog = new TextInputDialog(new File(Constants.API_DIRECTORY, "log.xml").getAbsolutePath());
            writeToFileDialog.setHeaderText("Write the current log to file");
            writeToFileDialog.setContentText("Please give a destination file");
            writeToFileDialog.initOwner(stage);
            writeToFileDialog.initModality(Modality.WINDOW_MODAL);
            writeToFileDialog.getDialogPane().getStylesheets().add(ConsoleController.class.getClassLoader().getResource("../popup/popup.css").toExternalForm());
            writeToFileDialog.showAndWait();
            return new File(writeToFileDialog.getResult());
        }

        private void warnFailure() {
            Alert failureAlert = new Alert(AlertType.ERROR);
            failureAlert.setContentText("The file path given must be in an already-existing folder.");
            failureAlert.initModality(Modality.WINDOW_MODAL);
            failureAlert.initOwner(stage);
            failureAlert.showAndWait();
        }

        private void write(File file) {
            String xml = "<records>\n" + handler.getCache().stream().map((record) -> CLIPBOARD_FORMATTER.format(record)).collect(Collectors.joining("", "  ", "")) + "</records>";
            try {
                Files.write(file.toPath(), xml.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not write log to file.", ex);
            }
        }
    };

    private double dragXOffset = 0;
    private double dragYOffset = 0;

    public void makeDraggable(Node byNode, Stage stage) {
        byNode.setOnMousePressed((event) -> {
            dragXOffset = stage.getX() - event.getScreenX();
            dragYOffset = stage.getY() - event.getScreenY();
            byNode.setCursor(Cursor.MOVE);
        });
        byNode.setOnMouseReleased((event) -> {
            byNode.setCursor(Cursor.DEFAULT);
        });
        byNode.setOnMouseDragged((event) -> {
            stage.setX(event.getScreenX() + dragXOffset);
            stage.setY(event.getScreenY() + dragYOffset);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        console.setCellFactory((param) -> new LogListCell(console));
        console.requestFocus();

        //Context Menu
        final ContextMenu contextMenu = new ContextMenu();
        final Clipboard clipboard = Clipboard.getSystemClipboard();

        javafx.scene.control.MenuItem copyAsXml = new javafx.scene.control.MenuItem("Copy to Clipboard");
        copyAsXml.setOnAction((event) -> {
            String xml = handler.getCache().stream().map((record) -> CLIPBOARD_FORMATTER.format(record)).collect(Collectors.joining("\n"));
            ClipboardContent copyContent = new ClipboardContent();
            copyContent.put(ConsoleController.CONSOLE_CLIPBOARD_FORMAT, handler.getCache());
            copyContent.putString(xml);
            clipboard.setContent(copyContent);
        });

        javafx.scene.control.MenuItem writeToFileItem = new javafx.scene.control.MenuItem("Write to File");
        writeToFileItem.setOnAction(this.writeToFile);

        contextMenu.getItems().addAll(copyAsXml, writeToFileItem);
        header.setOnContextMenuRequested((event) -> {
            contextMenu.show(console, event.getScreenX(), event.getScreenY());
        });
    }

    public void updateToggles() {
        config.setSelected(doLogPointer.contains(Level.CONFIG));
        info.setSelected(doLogPointer.contains(Level.INFO));
        warning.setSelected(doLogPointer.contains(Level.WARNING));
        severe.setSelected(doLogPointer.contains(Level.SEVERE));
    }

    public void toggleConfig() {
        if (config.isSelected()) {
            doLogPointer.add(Level.CONFIG);
        } else {
            doLogPointer.remove(Level.CONFIG);
        }
    }

    public void toggleInfo() {
        if (config.isSelected()) {
            doLogPointer.add(Level.INFO);
        } else {
            doLogPointer.remove(Level.INFO);
        }
    }

    public void toggleWarning() {
        if (config.isSelected()) {
            doLogPointer.add(Level.WARNING);
        } else {
            doLogPointer.remove(Level.WARNING);
        }
    }

    public void toggleSevere() {
        if (config.isSelected()) {
            doLogPointer.add(Level.SEVERE);
        } else {
            doLogPointer.remove(Level.SEVERE);
        }
    }

    public void allOff() {
        doLogPointer.removeAll(LogHandler.LEVELS);
        setUnfine();
        updateToggles();
    }

    public void allOn() {
        doLogPointer.addAll(LogHandler.LEVELS);
        setFinest();
        updateToggles();
    }

    public void setUnfine() {
        fineness.setText("NONE");
        doLogPointer.remove(Level.FINE);
        doLogPointer.remove(Level.FINER);
        doLogPointer.remove(Level.FINEST);
    }

    public void setFine() {
        fineness.setText("FINE");
        doLogPointer.add(Level.FINE);
        doLogPointer.remove(Level.FINER);
        doLogPointer.remove(Level.FINEST);
    }

    public void setFiner() {
        fineness.setText("FINER");
        doLogPointer.add(Level.FINE);
        doLogPointer.add(Level.FINER);
        doLogPointer.remove(Level.FINEST);
    }

    public void setFinest() {
        fineness.setText("FINEST");
        doLogPointer.add(Level.FINE);
        doLogPointer.add(Level.FINER);
        doLogPointer.add(Level.FINEST);
    }
}

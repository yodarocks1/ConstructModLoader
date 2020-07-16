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
package cml;

import cml.beans.SceneEvent;
import cml.beans.SceneEvent.IconChange;
import cml.gui.console.ConsoleController;
import cml.gui.console.LogHandler;
import cml.gui.console.StreamTee;
import cml.lib.threadmanager.ThreadManager;
import cml.lib.xmliconmap.CMLIcon;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.scenicview.ScenicView;

/**
 *
 * @author benne
 */
public class GUI extends Application {

    private static final Logger LOGGER = Logger.getLogger(GUI.class.getName());

    private Stage stage;

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void start(Stage stage) throws Exception {
        try {
            this.stage = stage;
            if (Main.showConsole) {
                try {
                    FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("gui/console/Console.fxml"));
                    ConsoleController console = new ConsoleController();
                    consoleLoader.setController(console);
                    Stage consoleStage = new Stage(StageStyle.DECORATED);
                    consoleStage.setScene(new Scene(consoleLoader.load()));
                    console.initStage(consoleStage, this);
                    consoleStage.show();
                    Main.LOG_HANDLER.initController(console);
                    System.setErr(new PrintStream(new StreamTee(new LogHandler.LogOutputStream(Main.LOG_HANDLER, Level.SEVERE, "System.err", true), new LogHandler.LogOutputStream(Main.FILE_HANDLER, Level.SEVERE, "System.err", true))));
                    System.setOut(new PrintStream(new StreamTee(new LogHandler.LogOutputStream(Main.LOG_HANDLER, Level.INFO, "System.out", true), System.out, new LogHandler.LogOutputStream(Main.FILE_HANDLER, Level.INFO, "System.out", true))));
                } catch (IOException ex) {
                    System.err.println("Could not open console");
                    ex.printStackTrace(System.err);
                    ex.printStackTrace(System.out);
                }
            }
            Main.mainSetup();
            Parent root = FXMLLoader.load(getClass().getResource("gui/main/Main.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("media/listViewStyles.css").toExternalForm());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("media/buttonStyles.css").toExternalForm());

            if (Boolean.valueOf(System.getProperty("evPanel", "false"))) {
                ScenicView.show(scene);
            }

            scene.addEventHandler(SceneEvent.ICON_CHANGE, (event) -> {
                if (event.getData() instanceof IconChange) {
                    IconChange icons = (IconChange) event.getData();
                    switch (icons.changeType.id) {
                        case 0:
                            stage.getIcons().addAll(icons.icons);
                            break;
                        case 1:
                            stage.getIcons().removeAll(icons.icons);
                            break;
                        case 2:
                            stage.getIcons().setAll(icons.icons);
                    }
                }
            });

            stage.setTitle("Construct Mod Loader - V" + Constants.VERSION);
            stage.getIcons().add(Main.ICON_MAP.ICON.getIcon(CMLIcon.State.NORMAL));
            stage.setMinWidth(500);
            stage.setScene(scene);
            SplashScreenLoader.close();
            stage.show();
            stage.requestFocus();
        } catch (Throwable ex) {
            Main.LOGGER.log(Level.SEVERE, "Uncaught exception", ex);
            ex.printStackTrace(System.err);
            ex.printStackTrace(System.out);
            SplashScreenLoader.close();
        }
    }

    @Override
    public void stop() throws IOException {
        LOGGER.log(Level.INFO, "Stopping...");
        ThreadManager.stop();
        try {
            String lines = Main.scrapMechanicFolder + "\n" + Main.vanillaFolder + "\n" + Main.modsFolder + "\n" + Main.workshopFolder;
            Files.write(new File(Main.API_DIRECTORY.getAbsolutePath() + Constants.FOLDERS_LOCATION_RELATIVE).toPath(), lines.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (AccessDeniedException ex) {
            LOGGER.log(Level.SEVERE, "Folders - AccessDeniedException: Make sure you have given the program Administrator Privileges! ({0}/" + Constants.FOLDERS_LOCATION_RELATIVE + ")", Main.API_DIRECTORY.getAbsolutePath());
        }
    }

    //Because NetBeans won't recognize my Main class as the main class...
    public static void main(String[] args) {
        Main.main(args);
    }

    public void show() {
        stage.show();
    }

    public void hide() {
        stage.hide();
    }

    public boolean isShowing() {
        return stage.isShowing();
    }

    public void setOnCloseRequest(EventHandler<WindowEvent> eventHandler) {
        stage.setOnCloseRequest(eventHandler);
    }

    public void addWindowCloseHandler(EventHandler<WindowEvent> eventHandler) {
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, eventHandler);
    }

}

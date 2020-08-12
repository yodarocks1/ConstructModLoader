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

import cml.beans.Profile;
import cml.beans.SceneEvent;
import cml.beans.SceneEvent.IconChange;
import cml.gui.console.ConsoleController;
import cml.gui.console.LogHandler;
import cml.gui.console.StreamTee;
import cml.gui.popup.CmlPopover;
import cml.gui.popup.CmlPopup;
import cml.gui.popup.PopupData;
import cml.gui.splash.SplashScreenLoader;
import cml.lib.converter.Transmute;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import cml.lib.lazyupdate.ChangeDetector;
import cml.lib.lazyupdate.FlagUpdater;
import cml.lib.lazyupdate.Flags;
import cml.lib.threadmanager.ThreadManager;
import cml.lib.url.UrlHandler;
import cml.lib.xmliconmap.CMLIcon;
import cml.lib.xmliconmap.CMLIconMap;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
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
                    System.setErr(new PrintStream(new StreamTee(new LogHandler.LogOutputStream(Main.LOG_HANDLER, Level.SEVERE, "System.err", true), new LogHandler.LogOutputStream(Main.FILE_HANDLER, Level.SEVERE, "System.err", true), new LogHandler.LogOutputStream(Main.FILE_HANDLER2, Level.SEVERE, "System.err", true))));
                    System.setOut(new PrintStream(new StreamTee(new LogHandler.LogOutputStream(Main.LOG_HANDLER, Level.INFO, "System.out", true), System.out, new LogHandler.LogOutputStream(Main.FILE_HANDLER, Level.INFO, "System.out", true), new LogHandler.LogOutputStream(Main.FILE_HANDLER2, Level.SEVERE, "System.out", true))));
                } catch (IOException ex) {
                    System.err.println("Could not open console");
                    ex.printStackTrace(System.err);
                    ex.printStackTrace(System.out);
                }
            }
            Main.mainSetup();
            newDownloadPopup = new CmlPopup(Modality.NONE, stage, newDownloadData);
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
            stage.getIcons().add(CMLIconMap.ICON_MAP.ICON.getIcon(CMLIcon.State.NORMAL));
            stage.setMinWidth(500);
            stage.setScene(scene);
            SplashScreenLoader.close();
            stage.show();
            stage.requestFocus();

            ChangeDetector detector = new ChangeDetector(new File(Main.modsFolder), Constants.DOWNLOADS_FOLDER);
            detector.mods.addListener(new MapChangeListener<String, String[]>() {
                @Override
                public void onChanged(MapChangeListener.Change<? extends String, ? extends String[]> change) {
                    if (change.getValueRemoved() == null && change.getValueAdded() == null) {
                        //No change
                    } else if (change.getValueRemoved() == null) { //Added profile
                        addedProfile(change.getKey(), change.getValueAdded().length);
                    } else if (change.getValueAdded() == null) { //Removed profile
                        removedProfile(change.getKey());
                    } else { //Mods were changed
                        List<String> oldValues = Arrays.asList(change.getValueRemoved());
                        List<String> newValues = Arrays.asList(change.getValueAdded());

                        List<String> addedValues = new ArrayList();
                        addedValues.addAll(newValues);
                        addedValues.removeAll(oldValues);
                        addedValues.forEach((addedMod) -> {
                            added(addedMod);
                        });

                        List<String> removedValues = new ArrayList();
                        removedValues.addAll(oldValues);
                        removedValues.removeAll(newValues);
                        removedValues.forEach((removedMod) -> {
                            removed(removedMod);
                        });
                        
                        Flags.setFlag(Flags.localFlags(Profile.get(change.getKey())), Flags.Flag.DO_UPDATE);
                    }
                }

                private void added(String name) {
                    CmlPopover.popover(stage, CmlPopover.PopoverType.PLUS, CmlPopover.PopoverColor.GREEN, "Found mod \"", name, "\"", 6, TimeUnit.SECONDS);
                }

                private void removed(String name) {
                    CmlPopover.popover(stage, CmlPopover.PopoverType.MINUS, CmlPopover.PopoverColor.RED, "Removed mod \"", name, "\"", 6, TimeUnit.SECONDS);
                }

                private void addedProfile(String name, int count) {
                    CmlPopover.popover(stage, CmlPopover.PopoverType.PLUS, CmlPopover.PopoverColor.GREEN, "Found profile \"", name, "\" (" + count + " mods)", 6, TimeUnit.SECONDS);
                }

                private void removedProfile(String name) {
                    CmlPopover.popover(stage, CmlPopover.PopoverType.MINUS, CmlPopover.PopoverColor.RED, "Removed profile \"", name, "\"", 6, TimeUnit.SECONDS);
                }
            });
            detector.downloads.addListener(new MapChangeListener<String, String[]>() {
                @Override
                public void onChanged(MapChangeListener.Change<? extends String, ? extends String[]> change) {
                    List<String> addedValues = new ArrayList(Arrays.asList(change.getValueAdded()));
                    if (change.getValueRemoved() != null) {
                        addedValues.removeAll(Arrays.asList(change.getValueRemoved()));
                    }
                    addedValues.forEach((mod) -> {
                        added(mod);
                    });
                }

                private void added(String name) {
                    File newMod = new File(Constants.DOWNLOADS_FOLDER, name);
                    ThreadManager.MANAGER.executor.submit(() -> {
                        CountDownLatch latch = new CountDownLatch(1);
                        popupLock.tryLock();
                        Platform.runLater(() -> {
                            newDownloadData.setMod(newMod);
                            newDownloadPopup.showThenRun((profile, ex) -> handleNewDownload(newMod, profile, ex, latch));
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException ex) {
                            Platform.runLater(() -> {
                                newDownloadPopup.close();
                            });
                        }
                        popupLock.unlock();
                    });
                }

                private void handleNewDownload(File mod, Profile profile, Throwable ex, CountDownLatch latch) {
                    latch.countDown();
                    if (profile != null && profile.getDirectory() != null) {
                        Transmute.ModType.forFile(mod).toCml(mod, new File(profile.getDirectory(), mod.getName()), Constants.NO_FILTER);
                        AFileManager.FILE_MANAGER.deleteOf(mod, FileOptions.DEPTH);
                    }
                }
            });
            detector.recursivelyCheck(15, TimeUnit.SECONDS);
            
            FlagUpdater.recursivelyCheck(15, TimeUnit.SECONDS);

            Main.SIS.argsProperty.addListener((obs, oldValue, newValue) -> {
                if (obs.getValue() != null) {
                    if (UrlHandler.handle(obs.getValue(), stage)) {
                        Main.SIS.argsProperty.set(null);
                    } else {
                        CmlPopover.popover(stage, CmlPopover.PopoverType.DOT, CmlPopover.PopoverColor.GRAY, "Cmd Input: \"", obs.getValue(), "\"", 5, 2, TimeUnit.SECONDS);
                    }
                }
            });

        } catch (Throwable ex) {
            LOGGER.log(Level.SEVERE, "Uncaught exception", ex);
            ex.printStackTrace(System.err);
            ex.printStackTrace(System.out);
            SplashScreenLoader.close();
        }
    }

    @Override
    public void stop() throws IOException {
        LOGGER.log(Level.INFO, "Stopping...");
        Main.activeProfile.get().saveAsSelected();
        ThreadManager.stop();
        try {
            String lines = Main.scrapMechanicFolder + "\n" + Main.vanillaFolder + "\n" + Main.modsFolder + "\n" + Main.workshopFolder;
            Files.write(new File(Constants.API_DIRECTORY.getAbsolutePath(), Constants.FOLDERMAP_LOCATION).toPath(), lines.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (AccessDeniedException ex) {
            LOGGER.log(Level.SEVERE, "Folders - AccessDeniedException: Make sure you have given the program Administrator Privileges! ({0}/" + Constants.FOLDERMAP_LOCATION + ")", Constants.API_DIRECTORY.getAbsolutePath());
        }
    }

    //Because NetBeans won't recognize my Main class as the main class...
    public static void main(String[] args) {
        Main.main(args);
    }

    /**
     * Shows the loader stage
     */
    public void show() {
        stage.show();
    }

    /**
     * Hides the loader stage
     */
    public void hide() {
        stage.hide();
    }

    /**
     * 
     * @return Whether the loader stage is shown
     */
    public boolean isShowing() {
        return stage.isShowing();
    }

    /**
     * When the user requests to close the loader stage, run the given handler.
     * @param eventHandler The handler to notify on close request
     */
    public void setOnCloseRequest(EventHandler<WindowEvent> eventHandler) {
        stage.setOnCloseRequest(eventHandler);
    }

    /**
     * When the loader stage closes, run the given handler.
     * @param eventHandler The handler to notify on close
     */
    public void addWindowCloseHandler(EventHandler<WindowEvent> eventHandler) {
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, eventHandler);
    }

    private final Lock popupLock = new ReentrantLock();
    private final NewDownloadData newDownloadData = new NewDownloadData();
    private CmlPopup<Profile, Boolean> newDownloadPopup;

    private final class NewDownloadData extends PopupData<Profile, Boolean> {

        private AnchorPane root;
        private final Text modName = new Text();
        private final ChoiceBox<Profile> profileSelector = new ChoiceBox();

        public void setMod(File newMod) {
            modName.setText("New mod: " + newMod.getName());
        }

        @Override
        protected Node getNode() {
            return root;
        }

        @Override
        protected Profile getResult(Boolean data) {
            if (data) {
                return profileSelector.getValue();
            } else {
                return null;
            }
        }

        @Override
        protected void setup(CmlPopup parent) {
            parent.setTitle("New Download");
            root = new AnchorPane();
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

            modName.setFill(Color.WHITE);
            AnchorPane.setTopAnchor(modName, 5.0);
            AnchorPane.setLeftAnchor(modName, 5.0);

            Text userPrompt = new Text("Choose a destination profile");
            userPrompt.setFill(Color.WHITE);
            userPrompt.setFont(Font.font("LIBRARY 3 AM", 12));
            AnchorPane.setTopAnchor(userPrompt, 20.0);
            AnchorPane.setLeftAnchor(userPrompt, 5.0);

            profileSelector.getStyleClass().add("button");
            Main.profileList.addListener((obs, oldValue, newValue) -> {
                Profile selected = profileSelector.getValue();
                profileSelector.getItems().setAll(newValue);
                if (selected != null && selected.getDirectory() != null && selected.getDirectory().exists()) {
                    boolean hasOldSelection = newValue.stream().filter((newVal) -> selected.getName().equals(newVal.getName())).findFirst().isPresent();
                    if (hasOldSelection) {
                        profileSelector.setValue(selected);
                    }
                }
            });
            profileSelector.setConverter(new StringConverter<Profile>() {
                @Override
                public String toString(Profile object) {
                    return object.getName();
                }

                @Override
                public Profile fromString(String string) {
                    return profileSelector.getItems().stream().filter((profile) -> profile.getName().equals(string)).findFirst().orElse(null);
                }
            });
            if (Main.profileList.getValue() != null) {
                profileSelector.getItems().setAll(Main.profileList.getValue());
            }
            AnchorPane.setTopAnchor(profileSelector, 40.0);
            AnchorPane.setLeftAnchor(profileSelector, 5.0);
            AnchorPane.setRightAnchor(profileSelector, 20.0);

            root.getChildren().setAll(modName, userPrompt, profileSelector);

            addButton(new Button("Cancel"), false, true);
            addButton(new Button("OK"), true, true);

            parent.makeDraggable();
        }
    }

}

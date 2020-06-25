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
package cml.gui.settings;

import cml.Constants;
import cml.ErrorManager;
import cml.Main;
import cml.gui.main.MainController;
import cml.gui.main.SubController;
import cml.lib.files.AFileManager;
import cml.lib.git.UpdateManager;
import cml.lib.registry.hardcoded.Steam;
import cml.lib.steam.shortcut.ShortcutManager;
import cml.lib.steam.verify.VanillaFolderManager;
import cml.lib.threadmanager.ThreadManager;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;

/**
 *
 * @author benne
 */
public class SettingsController extends SubController {

    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());

    @FXML private AnchorPane root;
    @FXML private Button updateButton;
    @FXML private Text updateAvailableText;
    @FXML private TextField smFolder;
    @FXML private TextField modsFolder;
    @FXML private TextField vanillaFolder;
    @FXML private TextField workshopFolder;
    @FXML private ButtonBar errorBar;
    @FXML private Text shortcutStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        smFolder.setText(Main.scrapMechanicFolder);
        modsFolder.setText(Main.modsFolder);
        vanillaFolder.setText(Main.vanillaFolder);
        workshopFolder.setText(Main.workshopFolder);
        smFolder.setOnAction((event) -> {
            String text = smFolder.getText();
            Main.endSlash(Main.scrapMechanicFolder = text);
            Main.verifySMFolder();
            mainController.showSuccess(3000);
        });
        modsFolder.setOnAction((event) -> {
            String text = modsFolder.getText();
            Main.modsFolder = Main.endSlash(text);
            Main.updateProfileList();
            mainController.showSuccess(3000);
        });
        vanillaFolder.setOnAction((event) -> {
            String text = vanillaFolder.getText();
            Main.vanillaFolder = Main.endSlash(text);
            VanillaFolderManager.verifyVanillaFolder();
            mainController.showSuccess(3000);
        });
        workshopFolder.setOnAction((event) -> {
            String text = workshopFolder.getText();
            Main.setWorkshopFolder(Main.endSlash(text));
            Main.verifyWorkshopFolder();
            mainController.showSuccess(3000);
        });
        ShortcutManager.STATE.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                shortcutStatus.setText(newValue);
            } else {
                shortcutStatus.setText("Create Steam shortcut");
            }
        });

        ErrorManager.State.addListener((obs, oldValue, newValue) -> {
            if (newValue.isError()) {
                errorBar.setVisible(true);
            } else {
                errorBar.setVisible(false);
            }
        });
        ErrorManager.addCauseResolver("vanillaFolder not created", () -> {
            VanillaFolderManager.regenVanilla();
        });
        ErrorManager.addCauseResolver("ModsFolder <INVALID>", () -> {
            mainController.switchToMenu(MainController.SETTINGS);
            modsFolder.requestFocus();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("ModsFolder <INVALID>");
            alert.setContentText("Please choose a valid mods folder");
            alert.setTitle("User Error Resolver");
            alert.initModality(Modality.NONE);
            alert.show();
        });
        ErrorManager.addCauseResolver("SMFolder <INVALID>", () -> {
            mainController.switchToMenu(MainController.SETTINGS);
            smFolder.requestFocus();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("SMFolder <INVALID>");
            alert.setContentText("Please choose a valid Scrap Mechanic folder");
            alert.setTitle("User Error Resolver");
            alert.initModality(Modality.NONE);
            alert.show();
        });
        ErrorManager.addCauseResolver("VanillaFolder <INVALID>", () -> {
            mainController.switchToMenu(MainController.SETTINGS);
            vanillaFolder.requestFocus();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("VanillaFolder <INVALID>");
            alert.setContentText("Please choose a valid vanilla folder");
            alert.setTitle("User Error Resolver");
            alert.initModality(Modality.NONE);
            alert.show();
        });
        ErrorManager.addCauseResolver("WorkshopFolder <INVALID>", () -> {
            mainController.switchToMenu(MainController.SETTINGS);
            vanillaFolder.requestFocus();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("WorkshopFolder <INVALID>");
            alert.setContentText("Please choose a valid workshop folder");
            alert.setTitle("User Error Resolver");
            alert.initModality(Modality.NONE);
            alert.show();
        });
    }

    public void checkForUpdate() {
        String update = UpdateManager.checkForUpdate()[0];
        if (update.length() > 0) {
            updateButton.setDisable(false);
            updateAvailableText.setText("Update Available: V" + Constants.VERSION + " -> " + update);
            updateAvailableText.setVisible(true);
        } else {
            updateButton.setDisable(true);
            updateAvailableText.setVisible(false);
        }
    }

    private boolean previousFailure = false;

    public void update() {
        updateButton.setDisable(true);
        if (previousFailure) {
            UpdateManager.openUpdate();
        } else {
            boolean success = UpdateManager.update(new File(Main.API_DIRECTORY, "UpdateAssets.zip"));
            if (success) {
                updateAvailableText.setText("Update applied! Please restart to avoid conflicts and apply the update.");
            } else {
                updateButton.setDisable(false);
                updateAvailableText.setText("Update failed. Press update again to manually install.");
                previousFailure = true;
            }
        }
    }

    public void openLogs() {
        Main.openLogs();
    }

    public void regenVanilla() {
        VanillaFolderManager.regenVanilla();
    }

    public void errorAutoResolve() {
        if (!ErrorManager.autoResolve()) {
            showErrorCauses();
        }
    }

    public void showErrorCauses() {
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + now.format(DateTimeFormatter.ISO_LOCAL_TIME);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Error Causes (" + time + ")");
        alert.setContentText(ErrorManager.State.getValue().toString());
        alert.initModality(Modality.NONE);
        alert.show();
    }

    public void createShortcut() {
        File path = Steam.getInstallDirectory();
        double percent = ShortcutManager.alreadyExists(path);
        LOGGER.log(Level.FINER, "{0}% of users have the shortcut.", Math.round(percent * 1000) / 10.0);
        if (percent < 1.0) {
            ShortcutManager.createShortcut(path);
        } else {
            shortcutStatus.setText("Shortcut already exists");
            shortcutStatus.setFill(Color.RED);
            Thread statusThread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "Status thread interrupted", ex);
                }
                shortcutStatus.setText("Create Steam shortcut");
                shortcutStatus.setFill(Color.WHITE);
            });
            ThreadManager.addThread(statusThread);
            statusThread.start();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        root.setVisible(visible);
    }
}

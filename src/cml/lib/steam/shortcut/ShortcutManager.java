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
package cml.lib.steam.shortcut;

import cml.gui.popup.CmlPopup;
import cml.gui.popup.PopupData;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import cml.lib.threadmanager.ThreadManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;

/**
 *
 * @author benne
 */
public class ShortcutManager {

    private static final Logger LOGGER = Logger.getLogger(ShortcutManager.class.getName());

    private static final Image VERTICAL = new Image(ShortcutManager.class.getClassLoader().getResourceAsStream("media/Grid/vert.png"));
    private static final Image HORIZONTAL = new Image(ShortcutManager.class.getClassLoader().getResourceAsStream("media/Grid/horz.png"));
    private static final Map<File, String> OLD_FAVORITES = new HashMap();

    private static final String VDF_FORMAT
            = "\0{0}\0" // {0} = Index
            + "\1AppName\0{1}\0" // {1} = App name
            + "\1Exe\0{2}\0" // {2} = Path to EXE
            + "\1StartDir\0{3}" // {3} = Path to Start Directory
            + "\1icon\0{4}\0" // {4} = Path to Icon (can be .exe or .ico)
            + "\1ShortcutPath\0{5}\0" // {5} = Path to Desktop Shortcut
            + "\1LaunchOptions\0{6}\0" // {6} = Launch options
            + "\2IsHidden\0\0\0\0\0"
            + "\2AllowDekstopConfig\0\1\0\0\0"
            + "\2AllowOverlay\0\1\0\0\0"
            + "\2OpenVR\0\0\0\0\0"
            + "\2Devkit\0\0\0\0\0"
            + "\1DevkitGameID\0\0"
            + "\2LastPlayTime\0\0\0\0\0"
            + "\0tags\0"
            + "\1" + "0\0favorite\0"
            + "\b"
            + "\b"
            + "\b"
            + "\b";

    private static final CmlPopup<String, Integer> POPUP = new CmlPopup(Modality.NONE, new PopupData<String, Integer>() {

        StringProperty textFieldValue;

        @Override
        protected Node getNode() {
            AnchorPane pane = new AnchorPane();
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);

            TextField idField = new TextField();
            idField.setPromptText("If you have the ID on hand, you can also put it in here");
            AnchorPane.setTopAnchor(idField, 100.0);
            AnchorPane.setLeftAnchor(idField, 5.0);
            AnchorPane.setRightAnchor(idField, 5.0);
            idField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (!newValue.matches("[0-9]*")) {
                    idField.setText(oldValue);
                }
            });
            textFieldValue = idField.textProperty();
            idField.getStyleClass().add("button");

            Text content = new Text("Please open Steam and put Construct Mod Loader into the Favorites collection.\n"
                    + "This allows CML to identify the shortcut and apply images to it.\n"
                    + "Once the images have been applied, you can feel free to remove it again.");
            content.setFill(Color.WHITE);
            content.setFont(Font.font("LIBRARY 3 AM", 11));
            AnchorPane.setTopAnchor(content, 5.0);
            AnchorPane.setLeftAnchor(content, 5.0);
            AnchorPane.setRightAnchor(content, 5.0);

            Button idButton = new Button("Apply ID (Skips identification)");
            idButton.visibleProperty().bind(idField.textProperty().isNotEmpty());
            this.addButton(idButton, 0, true);
            this.addButton(new Button("I've done it. (Continue)"), 1, true);
            this.addButton(new Button("Don't care (Cancel)"), 2, true);

            pane.getChildren().addAll(content, idField);

            return pane;
        }

        @Override
        protected String getResult(Integer data) {
            switch (data) {
                case 0:
                    return textFieldValue.getValueSafe();
                case 1:
                    return "";
                default:
                    return null;
            }
        }

        @Override
        public void setup(CmlPopup parent) {
            parent.setTitle("Shortcut Creator");
            parent.makeDraggable();
        }
    });

    private static String favoritePromptResult = null;
    private static String shortcutId = null;

    public static final StringProperty STATE = new SimpleStringProperty("Create Steam Shortcut");

    static {
        STATE.addListener((obs, oldValue, newValue) -> {
            LOGGER.log(Level.INFO, "ShortcutManager State: {0}", newValue);
        });
    }

    /**
     *
     * @param appName
     * @param exe
     * @param startDirectory
     * @param icon Icon to show in Steam (Must be .exe or .ico)
     * @param desktopShortcut Optional - Null will leave the value blank
     * @param launchOptions Optional - Null or an empty string will leave the
     * value blank
     * @return
     */
    private static byte[] fillFormat(int index, String appName, File exe, File startDirectory, File icon, File desktopShortcut, String launchOptions) { //Doesn't work...
        String filledFormat = VDF_FORMAT
                .replace("{0}", Integer.toString(index))
                .replace("{1}", appName)
                .replace("{2}", "\"" + exe.getAbsolutePath() + "\"")
                .replace("{3}", "\"" + startDirectory.getAbsolutePath() + "\\\"");
        if (icon != null) {
            filledFormat = filledFormat.replace("{4}", "\"" + icon.getAbsolutePath() + "\"");
        } else {
            filledFormat = filledFormat.replace("{4}", "");
        }
        if (desktopShortcut != null) {
            filledFormat = filledFormat.replace("{5}", "\"" + desktopShortcut.getAbsolutePath() + "\"");
        } else {
            filledFormat = filledFormat.replace("{5}", "");
        }
        if (launchOptions != null) {
            filledFormat = filledFormat.replace("{6}", launchOptions);
        } else {
            filledFormat = filledFormat.replace("{6}", "");
        }
        return filledFormat.getBytes(StandardCharsets.US_ASCII);
    }

    public static void createShortcut(File steamLocation) {
        File userData = new File(steamLocation, "userdata");
        Thread shortcutCreationThread = new Thread(() -> {
            closeSteam();
            getOldFavorites(userData);
            writeShortcut(userData);
            startSteam();
            promptFavorite(userData);
        }, "Shortcut Creation Thread");
        ThreadManager.addThread(shortcutCreationThread);
        shortcutCreationThread.start();
    }

    public static void identifyAndGridImages(File userData) {
        if (favoritePromptResult != null) {
            closeSteam();
            identifyShortcut(userData);
            createGridImages(userData);
            startSteam();
        } else {
            LOGGER.log(Level.INFO, "The player decided to forgo grid images");
        }
        STATE.setValue("Create Steam Shortcut");
    }

    public static double alreadyExists(File steamLocation) {
        File userData = new File(steamLocation, "userdata");
        int hasCount = 0;
        int totalCount = 0;
        for (File user : userData.listFiles()) {
            File shortcuts = new File(user, "config/shortcuts.vdf");
            if (shortcuts.exists() && AFileManager.FILE_MANAGER.readString(shortcuts).contains("Construct Mod Loader")) {
                hasCount++;
            }
            totalCount++;
        }
        return hasCount / totalCount;
    }

    private static void getOldFavorites(File userData) {
        STATE.setValue("Preparing...");
        for (File user : userData.listFiles()) {
            OLD_FAVORITES.put(user, getFavorites(new File(user, "config/localconfig.vdf")));
        }
    }

    private static final Pattern FAVORITE_PATTERN = Pattern.compile("(?<=favorite\\\\\",\\\\\"added\\\\\":\\[).*(?=\\],)");

    private static String getFavorites(File localConfig) {
        String config = AFileManager.FILE_MANAGER.readString(localConfig);
        Matcher matcher = FAVORITE_PATTERN.matcher(config);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "";
        }
    }

    private static void closeSteam() {
        STATE.setValue("Closing Steam...");
        try {
            Process p = Runtime.getRuntime().exec("taskkill /F /T /IM steam.exe");
            p.waitFor();
            p = Runtime.getRuntime().exec("taskkill /F /T /IM steamwebhelper.exe");
            p.waitFor();
            p = Runtime.getRuntime().exec("taskkill /F /T /IM SteamService.exe");
            p.waitFor();
            Thread.sleep(6000);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Interrupted when closing Steam", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't close Steam", ex);
        }
    }

    private static void startSteam() {
        STATE.setValue("Restarting steam");
        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler steam://nav/games");
            Thread.sleep(16000);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Interrupted when opening Steam", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Couldn't open Steam", ex);
        }
    }

    private static void writeShortcut(File userData) {
        File apiDirectory = new File("C:/Program Files (x86)/Construct/API");
        STATE.setValue("Registering with Steam API");
        for (File user : userData.listFiles()) {
            File shortcuts = new File(user, "config/shortcuts.vdf");
            if (shortcuts.exists()) {
                byte[] shortcutsVdf = AFileManager.FILE_MANAGER.read(shortcuts);
                String shortcutsString = new String(shortcutsVdf);
                if (shortcutsString.contains("Construct Mod Loader")) {
                    continue;
                }
                Matcher match = Pattern.compile("\0[0-9]+\0").matcher(shortcutsString);
                int count = 0;
                while (match.find()) {
                    count++;
                }
                if (count > 0) {
                    byte[] formatBytes = fillFormat(count, "Construct Mod Loader", new File(apiDirectory, "CML.exe"), apiDirectory, new File(apiDirectory, "CML.exe"), null, null);
                    byte[] outBytes = new byte[shortcutsVdf.length - 2 + formatBytes.length];
                    System.arraycopy(shortcutsVdf, 0, outBytes, 0, shortcutsVdf.length - 2);
                    System.arraycopy(formatBytes, 0, outBytes, shortcutsVdf.length - 2, formatBytes.length);
                    try {
                        Files.write(shortcuts.toPath(), outBytes, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Failed to write new shortcuts.vdf", ex);
                    }
                    continue;
                }
            }
            AFileManager.FILE_MANAGER.write(shortcuts, ("\0shortcuts\0").getBytes(), FileOptions.CREATE, FileOptions.REPLACE);
            try {
                Files.write(shortcuts.toPath(), fillFormat(0, "Construct Mod Loader", new File(apiDirectory, "CML.exe"), apiDirectory, new File(apiDirectory, "CML.exe"), null, null), StandardOpenOption.APPEND);
            } catch (IOException ex) {
                Logger.getLogger(ShortcutManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void promptFavorite(File userData) {
        STATE.setValue("Waiting for user...");
        POPUP.showThenRun((String result, Throwable ex) -> {
            favoritePromptResult = result;
            ShortcutManager.identifyAndGridImages(userData);
        });
    }

    private static void identifyShortcut(File userData) {
        STATE.setValue("Finding identifiers...");
        for (File user : userData.listFiles()) {
            LOGGER.log(Level.FINEST, "User {0}", user.getAbsolutePath());
            String id = favoritePromptResult;
            if (id.isEmpty()) {
                File localConfig = new File(user, "config/localconfig.vdf");
                String newFavorites = getFavorites(localConfig);
                id = newFavorites.replace(OLD_FAVORITES.get(user), "").replace(",", "");
            }
            if (id.length() > 0) {
                File cache = new File(user, "config/librarycache/" + id + ".json");
                AFileManager.FILE_MANAGER.write(cache,
                        "[[\"achievements\",{\"version\":2,\"data\":{\"vecHighlight\":[],\"vecUnachieved\":[],\"vecAchievedHidden\":[],\"nTotal\":0,\"nAchieved\":0}}],[\"customimage\",{\"version\":1}]]",
                        FileOptions.CREATE, FileOptions.REPLACE
                );
                shortcutId = id;
                LOGGER.log(Level.FINEST, "Shortcut ID: {0}", shortcutId);
            }
        }
    }

    private static void createGridImages(File userData) {
        STATE.setValue("Creating images...");
        for (File user : userData.listFiles()) {
            File grid = new File(user, "config/grid");
            File gridV = new File(grid, shortcutId + "p.png");
            File gridH = new File(grid, shortcutId + ".png");
            AFileManager.IMAGE_MANAGER.write(gridV, SwingFXUtils.fromFXImage(VERTICAL, null), "png");
            AFileManager.IMAGE_MANAGER.write(gridH, SwingFXUtils.fromFXImage(HORIZONTAL, null), "png");
        }
    }

}

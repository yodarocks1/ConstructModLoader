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
import cml.beans.Modification;
import cml.beans.SceneEvent;
import cml.beans.SceneEvent.IconChange;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author benne
 */
public class NewGUI extends Application {

    private final List<Thread> THREADS = new ArrayList();
    

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainGUI.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("media/listViewStyles.css").toExternalForm());

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
        stage.getIcons().add(Images.ICON);
        stage.setMinWidth(500);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        System.out.println("Stopping...");
        THREADS.forEach((thread) -> {
            thread.interrupt();
        });
        SpriteAnimation.THREADS.forEach((thread) -> {
            thread.interrupt();
        });
        try {
            String lines = Main.scrapMechanicFolder + "\n" + Main.vanillaFolder + "\n" + Main.modsFolder;
            Files.write(new File(Constants.FOLDERS_LOCATION).toPath(), lines.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (AccessDeniedException ex) {
            ErrorManager.printNonUserError("Folders - AccessDeniedException: Make sure you have given the program Administrator Privileges! (" + Constants.FOLDERS_LOCATION + ")");
        }
        System.err.close();
        if (Main.errFile.length() < 10) {
            Main.errFile.deleteOnExit();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Main Overlay">
    @FXML
    private AnchorPane pane;
    @FXML
    private Text headerText;
    @FXML
    private ImageView header;
    @FXML
    private Rectangle footer;
    @FXML
    private Rectangle footerCenter;
    @FXML
    private Rectangle footerCenterAngleLeft;
    @FXML
    private Rectangle footerCenterAngleRight;
    @FXML
    private ImageView background;
    @FXML
    private ImageView launchButton;
    @FXML
    private ImageView cmlSettings;
    @FXML
    private ImageView cmlProfile;
    @FXML
    private ImageView cmlProfiles;
    private IntegerProperty menuView = new SimpleIntegerProperty(0);

    public void initialize() {
        pane.setMinWidth(50 + 440);
        pane.setMinHeight(600);
        pane.widthProperty().addListener((obs, oldValue, newValue) -> {
            double width = newValue.doubleValue();
            double height = pane.getHeight();
            double headerTextScale = ((width - 80) < 520) ? (width - 80) / 520 : 1;
            headerText.setScaleX(headerTextScale);
            headerText.setScaleY(headerTextScale);
            headerText.setTranslateX(-(1 - headerTextScale) * 260);
            header.setFitWidth(width);
            footer.setWidth(width);
            background.setFitWidth(width);
            double deltaFooterWidth = Math.max(width - 440 - (height / 2), 50) - (width - 440 - (height / 2));
            footerCenter.setWidth(width - 440 - (height / 2) + deltaFooterWidth);
            footerCenter.setTranslateX((height / 2 - deltaFooterWidth) / 2);
            footerCenterAngleLeft.setTranslateX((height / 2 - deltaFooterWidth) / 2);
            footerCenterAngleRight.setTranslateX(-(height / 2 - deltaFooterWidth) / 2);
            launchButton.setTranslateX(((width - 440) / 2) - 80);
        });
        pane.heightProperty().addListener((obs, oldValue, newValue) -> {
            double width = pane.getWidth();
            double height = newValue.doubleValue();
            background.setFitHeight(height);
            double deltaFooterWidth = Math.max(width - 440 - (height / 2), 50) - (width - 440 - (height / 2));
            footerCenter.setWidth(width - 440 - (height / 2) + deltaFooterWidth);
            footerCenter.setTranslateX((height / 2 - deltaFooterWidth) / 2);
            footerCenterAngleLeft.setTranslateX((height / 2 - deltaFooterWidth) / 2);
            footerCenterAngleRight.setTranslateX(-(height / 2 - deltaFooterWidth) / 2);
        });

        launchButton.setOnMouseEntered((event) -> {
            if (!launching && !ErrorManager.isStateError()) {
                launchButton.setImage(Images.LAUNCH_SELECT);
            }
        });
        launchButton.setOnMouseExited((event) -> {
            if (!launching && !ErrorManager.isStateError()) {
                launchButton.setImage(Images.LAUNCH);
            }
        });
        launchButton.setOnMousePressed((event) -> {
            if (Main.activeProfile.get().getDirectory() != null) {
                launchButton.setImage(Images.LAUNCH_PRESS);
                launch();
            } else {
                ErrorManager.addStateCause("ActiveProfile == null");
                menuView.setValue(3);
            }
        });
        ErrorManager.State.addListener((obs, oldValue, newValue) -> {
            if (newValue.isError()) {
                launchButton.setImage(Images.LAUNCH_ERROR);
                launchButton.fireEvent(new SceneEvent(SceneEvent.ICON_CHANGE, new IconChange(IconChange.IconChangeType.SET, Images.ICON_ERROR)));
            } else if (oldValue.isError()) {
                launchButton.setImage(Images.LAUNCH);
                launchButton.fireEvent(new SceneEvent(SceneEvent.ICON_CHANGE, new IconChange(IconChange.IconChangeType.SET, Images.ICON)));
            }
        });

        initializeMenu();

        initializeSettings();
        initializeProfiles();
        initializeProfile();
    }

    private boolean launching = false;
    private final Runnable launchRunnable = new Runnable() {
        @Override
        public void run() {
            launching = true;
            launchButton.setImage(Images.LAUNCH_PRESS);
            launchButton.setMouseTransparent(true);
            MediaPlayer mediaPlayer = new MediaPlayer(Images.LAUNCH_SOUND);
            mediaPlayer.setOnEndOfMedia(mediaPlayer::dispose);
            mediaPlayer.play();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(NewGUI.class.getName()).log(Level.INFO, "Launch thread interrupted", ex);
            }
            Main.launchGame();
            launching = false;
            new SceneEvent(SceneEvent.ICON_CHANGE, new IconChange(IconChange.IconChangeType.SET, Images.ICON_SUCCESS)).fire(launchButton);
            launchButton.setImage(Images.LAUNCH);
            launchButton.setMouseTransparent(false);
        }
    };

    public void launch() {
        if (!launching) {
            Thread launchThread = new Thread(launchRunnable);
            launchThread.start();
            THREADS.add(launchThread);
        }
    }

    long successTime = 0;
    int runningSuccess = 0;
    private final Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            if (runningSuccess > 0 && !ErrorManager.isStateError()) {
                launchButton.setImage(Images.LAUNCH);
                new SceneEvent(SceneEvent.ICON_CHANGE, new IconChange(IconChange.IconChangeType.SET, Images.ICON)).fire(launchButton);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(NewGUI.class.getName()).log(Level.INFO, "Pre-success thread interrupted");
                }
            }
            if (!ErrorManager.isStateError()) {
                new SceneEvent(SceneEvent.ICON_CHANGE, new IconChange(IconChange.IconChangeType.SET, Images.ICON_SUCCESS)).fire(launchButton);
                launchButton.setImage(Images.LAUNCH_SUCCESS);
            }
            long t = successTime;
            runningSuccess++;
            try {
                Thread.sleep(t);
            } catch (InterruptedException ex) {
                Logger.getLogger(NewGUI.class.getName()).log(Level.INFO, "Success thread interrupted");
            }
            if (runningSuccess == 1 && !ErrorManager.isStateError()) {
                new SceneEvent(SceneEvent.ICON_CHANGE, new IconChange(IconChange.IconChangeType.SET, Images.ICON)).fire(launchButton);
                launchButton.setImage(Images.LAUNCH);
            }
            runningSuccess--;
        }
    };

    public void showSuccess(long time) {
        if (!ErrorManager.isStateError()) {
            successTime = time;
            Thread successThread = new Thread(successRunnable);
            successThread.start();
            THREADS.add(successThread);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Main Menu Pane">
    @FXML
    private AnchorPane mainMenuPane;
    @FXML
    private ImageView mainMenuBackground;
    @FXML
    private Rectangle mainMenuOverlay;
    @FXML
    private Text launchText;

    private void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {

            double ratioX = imageView.getFitWidth() / img.getWidth();
            double ratioY = imageView.getFitHeight() / img.getHeight();

            double reducCoeff = 0;
            if (ratioX >= ratioY) {
                reducCoeff = ratioY;
            } else {
                reducCoeff = ratioX;
            }

            double w = img.getWidth() * reducCoeff;
            double h = img.getHeight() * reducCoeff;

            imageView.setTranslateX((imageView.getFitWidth() - w) / 2);
            imageView.setTranslateY((imageView.getFitHeight() - h) / 2);

        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Menu Items">
    public void toggleSettings() {
        menuView.setValue(menuView.getValue() == 1 ? 0 : 1);
    }

    public void toggleProfile() {
        menuView.setValue(menuView.getValue() == 2 ? 0 : 2);
    }

    public void toggleProfiles() {
        menuView.setValue(menuView.getValue() == 3 ? 0 : 3);
    }

    private boolean settingsIsSelected = false;
    private boolean profileIsSelected = false;
    private boolean profilesIsSelected = false;

    private void initializeMenu() {
        mainMenuBackground.setFitWidth(mainMenuOverlay.getWidth());
        mainMenuBackground.setFitHeight(mainMenuOverlay.getHeight());
        centerImage(mainMenuBackground);
        mainMenuOverlay.setWidth(mainMenuPane.getWidth());
        mainMenuPane.widthProperty().addListener((obs, oldValue, newValue) -> {
            mainMenuBackground.setFitWidth(newValue.doubleValue());
            centerImage(mainMenuBackground);
            mainMenuOverlay.setWidth(newValue.doubleValue());
            launchText.setLayoutX((newValue.doubleValue() / 2) - 19);
        });
        mainMenuPane.heightProperty().addListener((obs, oldValue, newValue) -> {
            mainMenuBackground.setFitHeight(newValue.doubleValue());
            centerImage(mainMenuBackground);
        });
        cmlSettings.setOnMouseEntered((event) -> {
            cmlSettings.setImage(menuView.getValue() == 1 ? Images.BACK_SELECT : Images.SETTINGS_SELECT);
            settingsIsSelected = true;
        });
        cmlSettings.setOnMouseExited((event) -> {
            cmlSettings.setImage(menuView.getValue() == 1 ? Images.BACK : Images.SETTINGS);
            settingsIsSelected = false;
        });
        cmlSettings.setOnMousePressed((event) -> {
            cmlSettings.setImage(menuView.getValue() == 1 ? Images.BACK_PRESS : Images.SETTINGS_PRESS);
        });
        cmlSettings.setOnMouseReleased((event) -> {
            cmlSettings.setImage(menuView.getValue() == 1 ? Images.BACK : Images.SETTINGS);
        });

        cmlProfile.setOnMouseEntered((event) -> {
            cmlProfile.setImage(menuView.getValue() == 2 ? Images.BACK_SELECT : Images.PROFILE_SELECT);
            profileIsSelected = true;
        });
        cmlProfile.setOnMouseExited((event) -> {
            cmlProfile.setImage(menuView.getValue() == 2 ? Images.BACK : Images.PROFILE);
            profileIsSelected = false;
        });
        cmlProfile.setOnMousePressed((event) -> {
            cmlProfile.setImage(menuView.getValue() == 2 ? Images.BACK_PRESS : Images.PROFILE_PRESS);
        });
        cmlProfile.setOnMouseReleased((event) -> {
            cmlProfile.setImage(menuView.getValue() == 2 ? Images.BACK : Images.PROFILE);
        });

        cmlProfiles.setOnMouseEntered((event) -> {
            cmlProfiles.setImage(menuView.getValue() == 3 ? Images.BACK_SELECT : Images.PROFILES_SELECT);
            profilesIsSelected = true;
        });
        cmlProfiles.setOnMouseExited((event) -> {
            cmlProfiles.setImage(menuView.getValue() == 3 ? Images.BACK : Images.PROFILES);
            profilesIsSelected = false;
        });
        cmlProfiles.setOnMousePressed((event) -> {
            cmlProfiles.setImage(menuView.getValue() == 3 ? Images.BACK_PRESS : Images.PROFILES_PRESS);
        });
        cmlProfiles.setOnMouseReleased((event) -> {
            cmlProfiles.setImage(menuView.getValue() == 3 ? Images.BACK : Images.PROFILES);
        });
        menuView.addListener((obs, oldValue, newValue) -> {
            cmlSettings.setImage(newValue.intValue() == 1 ? (settingsIsSelected ? Images.BACK_SELECT : Images.BACK) : (settingsIsSelected ? Images.SETTINGS_SELECT : Images.SETTINGS));
            cmlProfile.setImage(newValue.intValue() == 2 ? (profileIsSelected ? Images.BACK_SELECT : Images.BACK) : (profileIsSelected ? Images.PROFILE_SELECT : Images.PROFILE));
            cmlProfiles.setImage(newValue.intValue() == 3 ? (profilesIsSelected ? Images.BACK_SELECT : Images.BACK) : (profilesIsSelected ? Images.PROFILES_SELECT : Images.PROFILES));
            mainMenuPane.setVisible(newValue.intValue() == 0);
            settingsPane.setVisible(newValue.intValue() == 1);
            profilePane.setVisible(newValue.intValue() == 2);
            profileListPane.setVisible(newValue.intValue() == 3);
        });
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Settings Pane">
    @FXML
    private FlowPane settingsPane;
    @FXML
    private Button updateButton;
    @FXML
    private Text updateAvailableText;
    private StringProperty updateAvailable = new SimpleStringProperty("");
    @FXML
    private TextField smFolder;
    @FXML
    private TextField modsFolder;
    @FXML
    private TextField vanillaFolder;

    private void initializeSettings() {
        updateAvailable.addListener((obs, oldValue, newValue) -> {
            if (newValue.length() > 0) {
                updateButton.setDisable(false);
                updateAvailableText.setText("Update Available: V" + Constants.VERSION + " -> " + newValue);
                updateAvailableText.setVisible(true);
            } else {
                updateButton.setDisable(true);
                updateAvailableText.setVisible(false);
            }
        });
        smFolder.setText(Main.scrapMechanicFolder);
        modsFolder.setText(Main.modsFolder);
        vanillaFolder.setText(Main.vanillaFolder);
        smFolder.setOnAction((event) -> {
            String text = smFolder.getText();
            Main.scrapMechanicFolder = (text.endsWith("/") || text.endsWith("\\") ? text : (text.contains("\\") ? text + "\\" : text + "/"));
            Main.verifySMFolder();
            this.showSuccess(3000);
        });
        modsFolder.setOnAction((event) -> {
            String text = modsFolder.getText();
            Main.modsFolder = (text.endsWith("/") || text.endsWith("\\") ? text : (text.contains("\\") ? text + "\\" : text + "/"));
            Main.updateProfileList();
            this.showSuccess(3000);
        });
        vanillaFolder.setOnAction((event) -> {
            String text = vanillaFolder.getText();
            Main.vanillaFolder = (text.endsWith("/") || text.endsWith("\\") ? text : (text.contains("\\") ? text + "\\" : text + "/"));
            Main.verifyVanillaFolder();
            this.showSuccess(3000);
        });
    }

    public void checkForUpdate() {
        updateAvailable.setValue(Main.checkForUpdate()[0]);
    }

    public void update() {
        Main.update();
    }
    
    public void openLogs() {
        Main.openLogs();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Profile List Pane">
    @FXML
    private AnchorPane profileListPane;
    @FXML
    public ListView profileListView;

    private void initializeProfiles() {
        profileListView.getItems().addAll(fromProfileList(Main.profileList.getValue()));
        Main.profileList.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                profileListView.getItems().setAll(fromProfileList(newValue));
            } else {
                Label errorLabel = new Label("Invalid mods folder");
                errorLabel.setTextFill(Color.RED);
                profileListView.getItems().setAll(errorLabel);
            }
        });
    }

    private List<Node> fromProfileList(List<Profile> profiles) {
        List<Node> nodes = new ArrayList();
        for (Profile profile : profiles) {
            HBox hBox = new HBox(8);
            Line separator0 = new Line(0, -10, 0, 10);
            separator0.setStroke(Color.WHITE);
            ImageView icon = new ImageView(profile.getIcon());
            icon.setFitHeight(24);
            icon.setPreserveRatio(true);
            Line separator1 = new Line(0, -10, 0, 10);
            separator1.setStroke(Color.WHITE);
            Line separator2 = new Line(0, -10, 0, 10);
            separator2.setStroke(Color.WHITE);
            Label name = new Label(profile.getName());
            name.setTextFill(Color.WHITE);
            Label description = new Label(profile.getDescription());
            description.setTextFill(Color.WHITE);

            hBox.getChildren().addAll(selectProfileNode(profile), separator0, icon, separator1, name, separator2, description);
            nodes.add(hBox);
            System.out.println(" Detected Profile: " + profile.getName());
        }
        return nodes;
    }

    private Node selectProfileNode(Profile profile) {
        ImageView iv = new ImageView();
        iv.setPreserveRatio(true);
        BooleanBinding isSelectedProfile = Main.activeProfile.isEqualTo(profile);
        if (isSelectedProfile.getValue()) {
            iv.setImage(Images.ENABLER_ON);
        } else {
            iv.setImage(Images.ENABLER_OFF);
        }
        iv.setOnMouseEntered((event) -> {
            iv.setImage(isSelectedProfile.getValue() ? Images.ENABLER_ON : Images.ENABLER_OFF_SELECT);
        });
        iv.setOnMouseExited((event) -> {
            iv.setImage(isSelectedProfile.getValue() ? Images.ENABLER_ON : Images.ENABLER_OFF);
        });
        iv.setOnMousePressed((event) -> {
            if (!isSelectedProfile.getValue()) {
                iv.setImage(Images.ENABLER_BOTH);
                Main.activeProfile.setValue(profile);
            }
        });
        iv.setOnMouseReleased((event) -> {
            iv.setImage(isSelectedProfile.getValue() ? Images.ENABLER_ON : Images.ENABLER_OFF);
        });
        iv.setPreserveRatio(true);
        iv.setFitHeight(24);
        Main.activeProfile.addListener((obs, oldValue, newValue) -> {
            if (oldValue.equals(profile)) {
                iv.setImage(Images.ENABLER_OFF);
            } else if (newValue.equals(profile)) {
                System.out.println("Profile switched from " + oldValue.getName() + " to " + newValue.getName());
                iv.setImage(Images.ENABLER_ON);
            }
        });
        return iv;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Profile Pane">
    @FXML
    private AnchorPane profilePane;
    @FXML
    private Rectangle profileTopBorder;
    @FXML
    private Text profileNameText;
    @FXML
    private ImageView profileImage;
    @FXML
    private ImageView openProfileFolder;
    @FXML
    private ImageView deleteProfile;
    @FXML
    private ImageView profileScrollBar;
    @FXML
    private AnchorPane profileScrollPane;
    @FXML
    private Rectangle modsListBackground;
    @FXML
    private Rectangle profileSliderBackground;
    @FXML
    private ListView modsListVBox;
    @FXML
    private ImageView profileScrollButtonTop;
    @FXML
    private ImageView profileScrollButtonBottom;

    private BooleanProperty scrollEnabled = new SimpleBooleanProperty(true);
    private boolean scrollSelected = false;
    private DoubleProperty scroll = new SimpleDoubleProperty(0.0);
    private double scrollCenter = 0.0;
    private double scrollTranslate = 0.0;

    private void initializeProfile() {
        profileNameText.setText(Main.activeProfile.getValue().getName());
        profileImage.setImage(Main.activeProfile.getValue().getIcon());
        openProfileFolder.setMouseTransparent(true);
        openProfileFolder.setImage(Images.BLANK);
        deleteProfile.setMouseTransparent(true);
        deleteProfile.setImage(Images.BLANK);
        profilePane.widthProperty().addListener((obs, oldValue, newValue) -> {
            profileTopBorder.setWidth(newValue.doubleValue());
            modsListBackground.setWidth(newValue.doubleValue() - 32);
        });
        profilePane.heightProperty().addListener((obs, oldValue, newValue) -> {
            modsListBackground.setHeight(newValue.doubleValue() - 96);
        });
        profileScrollPane.heightProperty().addListener((obs, oldValue, newValue) -> {
            profileSliderBackground.setHeight(newValue.doubleValue());
        });
        Main.activeProfile.addListener((obs, oldValue, newValue) -> {
            profileNameText.setText(newValue.getName());
            profileImage.setImage(newValue.getIcon());
            modsListVBox.getItems().setAll(fromModList(newValue.getModifications()));
            Main.activeModifications = Main.activeProfile.get().getActiveModifications();
            if (newValue.getDirectory() != null) {
                openProfileFolder.setMouseTransparent(false);
                openProfileFolder.setImage(Images.OPEN);
                deleteProfile.setMouseTransparent(false);
                deleteProfile.setImage(Images.DELETE);
                ErrorManager.removeStateCause("ActiveProfile == null");
            } else {
                openProfileFolder.setMouseTransparent(true);
                openProfileFolder.setImage(Images.BLANK);
                deleteProfile.setMouseTransparent(true);
                deleteProfile.setImage(Images.BLANK);
            }
        });
        openProfileFolder.setOnMouseEntered((event) -> {
            openProfileFolder.setImage(Images.OPEN_SELECT);
        });
        openProfileFolder.setOnMouseExited((event) -> {
            openProfileFolder.setImage(Images.OPEN);
        });
        openProfileFolder.setOnMousePressed((event) -> {
            openProfileFolder.setImage(Images.OPEN_PRESS);
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + Main.activeProfile.getValue().getDirectory().getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(NewGUI.class.getName()).log(Level.SEVERE, "Failed to open profile " + Main.activeProfile.getValue().getName(), ex);
            }
        });
        openProfileFolder.setOnMouseReleased((event) -> {
            openProfileFolder.setImage(Images.OPEN);
        });
        deleteProfile.setOnMouseEntered((event) -> {
            if (!Images.DELETE_SELECT_ANIM.isAnimated() && !Images.DELETE_PRESS_ANIM.isAnimated() && !deleteProfile.getImage().equals(Images.DELETE_PRESS_FINAL)) {
                Images.DELETE_SELECT_ANIM.animate(deleteProfile);
            }
        });
        deleteProfile.setOnMouseExited((event) -> {
            if (!Images.DELETE_PRESS_ANIM.isAnimated() && !deleteProfile.getImage().equals(Images.DELETE_PRESS_FINAL)) {
                deleteProfile.setImage(Images.DELETE);
            }
        });

        profileScrollBar.setOnMouseEntered((event) -> {
            if (scrollEnabled.get()) {
                profileScrollBar.setImage(Images.SCROLL_SELECT);
            }
            scrollSelected = true;
        });
        profileScrollBar.setOnMouseExited((event) -> {
            if (scrollEnabled.get()) {
                profileScrollBar.setImage(Images.SCROLL);
            }
            scrollSelected = false;
        });
        profileScrollBar.setOnMousePressed((event) -> {
            if (scrollEnabled.get()) {
                profileScrollBar.setImage(Images.SCROLL_PRESS);
                scrollCenter = event.getSceneY();
                scrollTranslate = profileScrollBar.getTranslateY();
            }
        });
        profileScrollBar.setOnMouseReleased((event) -> {
            if (scrollEnabled.get()) {
                profileScrollBar.setImage(scrollSelected ? Images.SCROLL_SELECT : Images.SCROLL);
            }
        });
        profileScrollBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, (event) -> {
            if (scrollEnabled.get()) {
                event.consume();
                double yMax = profileSliderBackground.getHeight() - 140;
                double y = scrollTranslate + (event.getSceneY() - scrollCenter);
                y = y > yMax ? yMax : (y < 0 ? 0 : y);
                scroll.setValue(y / yMax);
                profileScrollBar.setImage(Images.SCROLL_PRESS);
            }
        });
        scrollEnabled.addListener((obs, oldValue, newValue) -> {
            if (newValue && !oldValue) {
                profileScrollBar.setImage(scrollSelected ? Images.SCROLL_SELECT : Images.SCROLL);
                profileScrollButtonTop.setImage(Images.SCROLL_B);
                profileScrollButtonBottom.setImage(Images.SCROLL_B);
            } else {
                profileScrollBar.setImage(Images.SCROLL_DISABLE);
                profileScrollButtonTop.setImage(Images.SCROLL_B_DISABLE);
                profileScrollButtonBottom.setImage(Images.SCROLL_B_DISABLE);
            }
        });
        profileScrollButtonTop.setOnMouseEntered((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonTop.setImage(Images.SCROLL_B_SELECT);
            }
        });
        profileScrollButtonTop.setOnMouseExited((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonTop.setImage(Images.SCROLL_B);
            }
        });
        profileScrollButtonTop.setOnMousePressed((event) -> {
            if (scrollEnabled.get()) {
                scroll.set(Math.max(0, scroll.get() - 0.05));
                profileScrollButtonTop.setImage(Images.SCROLL_B_PRESS);
            }
        });
        profileScrollButtonTop.setOnMouseReleased((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonTop.setImage(Images.SCROLL_B);
            }
        });
        profileScrollButtonBottom.setOnMouseEntered((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonBottom.setImage(Images.SCROLL_B_SELECT);
            }
        });
        profileScrollButtonBottom.setOnMouseExited((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonBottom.setImage(Images.SCROLL_B);
            }
        });
        profileScrollButtonBottom.setOnMousePressed((event) -> {
            if (scrollEnabled.get()) {
                scroll.set(Math.min(1, scroll.get() + 0.05));
                profileScrollButtonBottom.setImage(Images.SCROLL_B_PRESS);
            }
        });
        profileScrollButtonBottom.setOnMouseReleased((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonBottom.setImage(Images.SCROLL_B);
            }
        });

        scroll.addListener((obs, oldValue, newValue) -> {
            double maxY = profileSliderBackground.getHeight() - 140;
            profileScrollBar.setTranslateY(newValue.doubleValue() * maxY);
            ScrollBar modsListScroll = (ScrollBar) modsListVBox.lookup(".scroll-bar:vertical");
            modsListScroll.setValue(newValue.doubleValue() * modsListScroll.getMax());
        });

        EventHandler<MouseEvent> oneTimeEvent = new EventHandler() {
            @Override
            public void handle(Event event) {
                ScrollBar modsListScroll = (ScrollBar) modsListVBox.lookup(".scroll-bar:vertical");
                modsListScroll.valueProperty().addListener((obs, oldValue, newValue) -> {
                    scroll.setValue(newValue.doubleValue() / modsListScroll.getMax());
                });
                modsListScroll.visibleProperty().addListener((obs, oldValue, newValue) -> {
                    if (!Objects.equals(newValue, oldValue)) {
                        scrollEnabled.setValue(newValue);
                    }
                });
                scrollEnabled.setValue(modsListScroll.isVisible());
                modsListVBox.removeEventHandler(MouseEvent.MOUSE_ENTERED, this);
            }
        };
        modsListVBox.addEventHandler(MouseEvent.MOUSE_ENTERED, oneTimeEvent);
        Main.activeProfile.setValue(Profile.EMPTY);
    }

    private List<Node> fromModList(List<Modification> activeMods) {
        List<Node> nodes = new ArrayList();
        for (Modification mod : activeMods) {
            HBox hBox = new HBox(8);
            Line separator1 = new Line(0, -10, 0, 10);
            separator1.setStroke(Color.WHITE);
            Line separator2 = new Line(0, -10, 0, 10);
            separator2.setStroke(Color.WHITE);
            Label name = new Label(mod.getName());
            name.setTextFill(Color.WHITE);
            Label description = new Label(mod.getDescription());
            description.setTextFill(Color.WHITE);

            hBox.getChildren().addAll(enablerNode(mod), separator1, name, separator2, description);
            nodes.add(hBox);
            System.out.println(" Detected mod: " + mod.getName());
        }
        return nodes;
    }

    private Node enablerNode(Modification mod) {
        ImageView iv = new ImageView();
        boolean[] selected = new boolean[] {false};
        if (mod.isEnabled()) {
            iv.setImage(Images.ENABLER_ON);
        } else {
            iv.setImage(Images.ENABLER_OFF);
        }
        iv.setOnMouseEntered((event) -> {
            iv.setImage(mod.isEnabled() ? Images.ENABLER_ON_SELECT : Images.ENABLER_OFF_SELECT);
            selected[0] = true;
        });
        iv.setOnMouseExited((event) -> {
            iv.setImage(mod.isEnabled() ? Images.ENABLER_ON : Images.ENABLER_OFF);
            selected[0] = false;
        });
        iv.setOnMousePressed((event) -> {
            iv.setImage(Images.ENABLER_BOTH);
            boolean isEnabled = mod.isEnabled();
            if (isEnabled) {
                mod.disable();
            } else {
                mod.enable();
            }
            if (isEnabled == mod.isEnabled()) {
                ErrorManager.printNonUserError("Enabler - AccessDeniedException: Make sure you have given the program Administrator Privileges!");
            }
        });
        iv.setOnMouseReleased((event) -> {
            iv.setImage(mod.isEnabled() ? Images.ENABLER_ON : Images.ENABLER_OFF);
        });
        mod.enabledListener.addListener((obs, oldValue, newValue) -> {
            if (selected[0]) {
                iv.setImage(mod.isEnabled() ? Images.ENABLER_ON_SELECT : Images.ENABLER_OFF_SELECT);
            } else {
                iv.setImage(mod.isEnabled() ? Images.ENABLER_ON : Images.ENABLER_OFF);
            }
        });
        iv.setPreserveRatio(true);
        iv.setFitHeight(24);
        return iv;
    }

    public void deleteActiveProfile() {
        deleteProfile.setMouseTransparent(true);
        deleteProfile.setImage(Images.DELETE_PRESS_START);
        Alert alert = new Alert(AlertType.CONFIRMATION, "Delete " + Main.activeProfile.getValue().getName() + " profile?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
                Main.activeProfile.getValue().delete();
                Images.DELETE_SELECT_ANIM.halt();
                Images.DELETE_PRESS_ANIM.animate(deleteProfile);
            } else {
                System.out.println("[WARNING] Deletion is disabled when -Domodify=false");
            }
        } else {
            deleteProfile.setImage(Images.DELETE);
            System.out.println("[INFO] Deletion cancelled");
            deleteProfile.setMouseTransparent(false);
        }
    }

    public void openProfileProperties() {
        System.out.println("[ERROR] Editing of profile properties is currently not supported.");
    }
    //</editor-fold>

    //Because NetBeans won't recognize my Main class as the main class...
    public static void main(String[] args) throws FileNotFoundException {
        Main.main(args);
    }

}

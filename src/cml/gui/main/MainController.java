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
package cml.gui.main;

import cml.Constants;
import cml.ErrorManager;
import cml.Images;
import cml.Main;
import cml.SpriteAnimation;
import cml.beans.Modification;
import cml.beans.Profile;
import cml.beans.SceneEvent;
import cml.gui.popup.CmlPopup;
import cml.gui.popup.PopupData;
import cml.gui.settings.SettingsController;
import cml.gui.workshop.WorkshopController;
import cml.lib.threadmanager.ThreadManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

/**
 * FXML Controller class
 *
 * @author benne
 */
public class MainController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML private SettingsController settingsController;
    @FXML private WorkshopController workshopController;

    //<editor-fold defaultstate="collapsed" desc="Main Overlay">
    @FXML private AnchorPane pane;
    @FXML private Text headerText;
    @FXML private ImageView header;
    @FXML private Rectangle footer;
    @FXML private Rectangle footerCenter;
    @FXML private Rectangle footerCenterAngleLeft;
    @FXML private Rectangle footerCenterAngleRight;
    @FXML private ImageView background;
    @FXML private ImageView launchButton;
    @FXML private ImageView cmlSettings;
    @FXML private ImageView cmlProfile;
    @FXML private ImageView cmlProfiles;
    @FXML private ImageView cmlWorkshop;
    @FXML private Text workshopTooltip;
    @FXML private Text settingsTooltip;
    @FXML private Text profileTooltip;
    @FXML private Text profileListTooltip;
    private IntegerProperty menuView = new SimpleIntegerProperty(0);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        settingsController.init(this);
        workshopController.init(this);
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
            }
        });
        ErrorManager.State.addListener((obs, oldValue, newValue) -> {
            if (newValue.isError()) {
                launchButton.setImage(Images.LAUNCH_ERROR);
                launchButton.fireEvent(new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Images.ICON_ERROR)));
            } else if (oldValue.isError()) {
                launchButton.setImage(Images.LAUNCH);
                launchButton.fireEvent(new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Images.ICON)));
            }
        });
        ErrorManager.addCauseResolver("ActiveProfile == null", () -> {
            switchToMenu(PROFILE_LIST);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("ActiveProfile == null");
            alert.setContentText("Please select an active profile");
            alert.setTitle("User Error Resolver");
            alert.initModality(Modality.NONE);
            alert.show();
        });

        initializeMenu();

        initializeProfiles();
        initializeProfile();
    }

    public static final int MAIN_MENU = 0;
    public static final int SETTINGS = 1;
    public static final int PROFILE = 2;
    public static final int PROFILE_LIST = 3;
    public static final int WORKSHOP = 4;

    public void switchToMenu(int menuValue) {
        menuView.set(menuValue);
    }

    public void toggleMenu(int menuValue) {
        menuView.set(menuView.get() == menuValue ? MAIN_MENU : menuValue);
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
                LOGGER.log(Level.INFO, "Launch thread interrupted", ex);
            }
            Main.launchGame();
            launching = false;
            new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Images.ICON_SUCCESS)).fire(launchButton);
            launchButton.setImage(Images.LAUNCH);
            launchButton.setMouseTransparent(false);
        }
    };

    public void launch() {
        if (!new File(Main.vanillaFolder).exists()) {
            ErrorManager.addStateCause("vanillaFolder not created");
        } else if (!launching) {
            Thread launchThread = new Thread(launchRunnable);
            launchThread.start();
            ThreadManager.addThread(launchThread);
        }
    }

    long successTime = 0;
    int runningSuccess = 0;
    private final Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            if (runningSuccess > 0 && !ErrorManager.isStateError()) {
                launchButton.setImage(Images.LAUNCH);
                new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Images.ICON)).fire(launchButton);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO, "Pre-success thread interrupted");
                }
            }
            if (!ErrorManager.isStateError()) {
                new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Images.ICON_SUCCESS)).fire(launchButton);
                launchButton.setImage(Images.LAUNCH_SUCCESS);
            }
            long t = successTime;
            runningSuccess++;
            try {
                Thread.sleep(t);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, "Success thread interrupted");
            }
            if (runningSuccess == 1 && !ErrorManager.isStateError()) {
                new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Images.ICON)).fire(launchButton);
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
            ThreadManager.addThread(successThread);
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
        toggleMenu(1);
    }

    public void toggleProfile() {
        toggleMenu(2);
    }

    public void toggleProfiles() {
        toggleMenu(3);
    }

    public void toggleWorkshop() {
        toggleMenu(4);
    }

    private void initializeMenu() {
        mainMenuBackground.setFitWidth(mainMenuOverlay.getWidth());
        mainMenuBackground.setFitHeight(mainMenuOverlay.getHeight());
        centerImage(mainMenuBackground);
        mainMenuOverlay.setWidth(mainMenuPane.getWidth());
        mainMenuPane.widthProperty().addListener((obs, oldValue, newValue) -> {
            mainMenuBackground.setFitWidth(newValue.doubleValue());
            centerImage(mainMenuBackground);
            mainMenuOverlay.setWidth(newValue.doubleValue());
            launchText.setLayoutX((newValue.doubleValue() / 2) - 25);
        });
        mainMenuPane.heightProperty().addListener((obs, oldValue, newValue) -> {
            mainMenuBackground.setFitHeight(newValue.doubleValue());
            centerImage(mainMenuBackground);
        });

        cmlWorkshop.setOnMouseEntered((event) -> {
            workshopTooltip.setVisible(true);
        });
        cmlWorkshop.setOnMouseExited((event) -> {
            workshopTooltip.setVisible(false);
        });

        cmlSettings.setOnMouseEntered((event) -> {
            settingsTooltip.setVisible(true);
        });
        cmlSettings.setOnMouseExited((event) -> {
            settingsTooltip.setVisible(false);
        });

        cmlProfile.setOnMouseEntered((event) -> {
            profileTooltip.setVisible(true);
        });
        cmlProfile.setOnMouseExited((event) -> {
            profileTooltip.setVisible(false);
        });

        cmlProfiles.setOnMouseEntered((event) -> {
            profileListTooltip.setVisible(true);
        });
        cmlProfiles.setOnMouseExited((event) -> {
            profileListTooltip.setVisible(false);
        });

        menuView.addListener((obs, oldValue, newValue) -> {
            cmlSettings.setId(newValue.intValue() == 1 ? "goBack" : "");
            cmlProfile.setId(newValue.intValue() == 2 ? "goBack" : "");
            cmlProfiles.setId(newValue.intValue() == 3 ? "goBack" : "");
            cmlWorkshop.setId(newValue.intValue() == 4 ? "goBack" : "");

            settingsTooltip.setText(newValue.intValue() == 1 ? "Go Back" : "CML Settings");
            profileTooltip.setId(newValue.intValue() == 2 ? "Go Back" : "Profile Settings");
            profileListTooltip.setId(newValue.intValue() == 3 ? "Go Back" : "Profile List");
            workshopTooltip.setId(newValue.intValue() == 4 ? "Go Back" : "Workshop Mods");

            mainMenuPane.setVisible(newValue.intValue() == 0);
            settingsController.setVisible(newValue.intValue() == 1);
            profilePane.setVisible(newValue.intValue() == 2);
            profileListPane.setVisible(newValue.intValue() == 3);
            workshopController.setVisible(newValue.intValue() == 4);
        });
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Profile List Pane">
    @FXML
    private AnchorPane profileListPane;
    @FXML
    public ListView profileListView;
    @FXML
    public TextField newProfileField;

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
        profiles.forEach((profile) -> {
            HBox hBox = new HBox(8);
            Line separator0 = new Line(0, -10, 0, 10);
            separator0.setStroke(Color.WHITE);
            ImageView icon = new ImageView(profile.getIcon());
            icon.setFitHeight(24);
            icon.setPreserveRatio(true);
            profile.getIconProperty().addListener((obs, oldValue, newValue) -> icon.setImage(newValue));
            Line separator1 = new Line(0, -10, 0, 10);
            separator1.setStroke(Color.WHITE);
            Line separator2 = new Line(0, -10, 0, 10);
            separator2.setStroke(Color.WHITE);
            Text name = new Text(profile.getName());
            profile.getNameProperty().addListener((obs, oldValue, newValue) -> name.setText(newValue));
            name.setFill(Color.WHITE);
            Text description = new Text(profile.getDescription());
            profile.getDescProperty().addListener((obs, oldValue, newValue) -> description.setText(newValue));
            description.setFill(Color.WHITE);

            hBox.getChildren().addAll(selectProfileNode(profile), separator0, icon, separator1, name, separator2, description);
            nodes.add(hBox);
            LOGGER.log(Level.INFO, " Detected Profile: {0}", profile.getName());
        });
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
                LOGGER.log(Level.INFO, "Profile switched from {0} to {1}", new Object[]{oldValue.getName(), newValue.getName()});
                iv.setImage(Images.ENABLER_ON);
            }
        });
        return iv;
    }

    public void createNewProfile() {
        Main.createProfile(newProfileField.getText());
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
    @FXML
    private ImageView profileProperties;

    private final BooleanProperty scrollEnabled = new SimpleBooleanProperty(true);
    private boolean scrollSelected = false;
    private final DoubleProperty scroll = new SimpleDoubleProperty(0.0);
    private double scrollCenter = 0.0;
    private double scrollTranslate = 0.0;

    private final class ProfilePropertiesData extends PopupData<Object[], Integer> {

        private Profile editingProfile = null;
        private final ImageView iconField = new ImageView();
        private final TextField descriptionField = new TextField();
        private final TextField nameField = new TextField();
        private final FileChooser fileChooser = new FileChooser();
        private CmlPopup parent = null;

        public void setProfile(Profile profile) {
            this.editingProfile = profile;
            iconField.setImage(profile.getIcon());
            descriptionField.setText(profile.getDescription());
            nameField.setText(profile.getName());
            if (parent != null) {
                setTitle(parent);
            }
        }

        @Override
        public Node getNode() {
            AnchorPane pane = new AnchorPane();
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);
            
            iconField.setOnDragOver((DragEvent event) -> {
                Dragboard board = event.getDragboard();
                if (event.getGestureSource() != iconField && (board.hasImage() || (board.hasFiles() && board.getFiles().get(0).getName().toLowerCase().endsWith(".png")))) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            });
            iconField.setOnDragDropped((DragEvent event) -> {
                Dragboard board = event.getDragboard();
                boolean success = false;
                if (board.hasImage()) {
                    iconField.setImage(board.getImage());
                    success = true;
                } else if (board.hasFiles()) {
                    try {
                        iconField.setImage(new Image(new FileInputStream(board.getFiles().get(0))));
                        success = true;
                    } catch (FileNotFoundException ex) {
                        LOGGER.log(Level.SEVERE, "Could not read file from dragboard", ex);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });
            iconField.setFitHeight(48);
            iconField.setPreserveRatio(true);
            AnchorPane.setTopAnchor(iconField, 5.0);
            AnchorPane.setLeftAnchor(iconField, 5.0);
            
            Button chooseImage = new Button("Choose a new icon");
            chooseImage.setFont(Font.font("LIBRARY 3 AM", 10));
            chooseImage.setTextFill(Color.WHITE);
            AnchorPane.setTopAnchor(chooseImage, 58.0);
            AnchorPane.setLeftAnchor(chooseImage, 5.0);
            chooseImage.setOnAction((event) -> {
                try {
                    iconField.setImage(new Image(new FileInputStream(fileChooser.showOpenDialog(parent.asWindow()))));
                } catch (FileNotFoundException ex) {
                    LOGGER.log(Level.SEVERE, "Could not read file from dragboard", ex);
                }
                event.consume();
            });

            descriptionField.setPromptText("Description");
            descriptionField.setFont(Font.font("LIBRARY 3 AM", 10));
            AnchorPane.setTopAnchor(descriptionField, 110.0);
            AnchorPane.setLeftAnchor(descriptionField, 5.0);
            AnchorPane.setRightAnchor(descriptionField, 5.0);

            nameField.setPromptText("Name");
            nameField.setFont(Font.font("LIBRARY 3 AM", 10));
            nameField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.startsWith(Constants.IGNORE_PREFIX) || newValue.endsWith(Constants.IGNORE_SUFFIX)) {
                    nameField.setText(oldValue);
                }
            });
            AnchorPane.setTopAnchor(nameField, 83.0);
            AnchorPane.setLeftAnchor(nameField, 5.0);
            AnchorPane.setRightAnchor(nameField, 5.0);

            addButton(new Button("Apply"), 0, false);
            addButton(new Button("Cancel"), 1, true);
            addButton(new Button("OK"), 0, true);

            pane.getChildren().setAll(iconField, chooseImage, nameField, descriptionField);

            return pane;
        }

        @Override
        public Object[] getResult(Integer data) {
            switch (data) {
                case 0:
                    return new Object[]{
                        editingProfile,
                        iconField.getImage(),
                        nameField.getText(),
                        descriptionField.getText()
                    };
                default:
                    return new Object[]{
                        editingProfile,
                        null,
                        null,
                        null
                    };
            }
        }

        @Override
        public void setup(CmlPopup parent) {
            parent.makeDraggable();
            setTitle(parent);
            this.parent = parent;
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(".PNG Icon", "*.png")
            );
        }

        private void setTitle(CmlPopup parent) {
            String title = "Edit Profile";
            if (editingProfile != null) {
                title += " - " + editingProfile.getName();
            }
            parent.setTitle(title);
        }
    }
    private final ProfilePropertiesData PROFILE_PROPERTIES_DATA = new ProfilePropertiesData();
    private final CmlPopup<Object[], Integer> PROFILE_PROPERTIES = new CmlPopup(Modality.WINDOW_MODAL, PROFILE_PROPERTIES_DATA);

    private void initializeProfile() {
        profileNameText.textProperty().bind(Main.activeProfile.getValue().getNameProperty());
        profileImage.imageProperty().bind(Main.activeProfile.getValue().getIconProperty());
        openProfileFolder.setMouseTransparent(true);
        openProfileFolder.setImage(Images.BLANK);
        deleteProfile.setMouseTransparent(true);
        deleteProfile.setImage(Images.BLANK);
        profileProperties.setMouseTransparent(true);
        profileProperties.setImage(Images.BLANK);
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
            profileNameText.textProperty().bind(newValue.getNameProperty());
            profileImage.imageProperty().bind(newValue.getIconProperty());
            modsListVBox.getItems().setAll(fromModList(newValue.getModifications()));
            Main.activeModifications = Main.activeProfile.get().getActiveModifications();
            if (newValue.getDirectory() != null) {
                openProfileFolder.setMouseTransparent(false);
                openProfileFolder.setImage(Images.OPEN);
                deleteProfile.setMouseTransparent(false);
                deleteProfile.setImage(Images.DELETE);
                profileProperties.setMouseTransparent(false);
                profileProperties.setImage(Images.SETTINGS);
                ErrorManager.removeStateCause("ActiveProfile == null");
            } else {
                openProfileFolder.setMouseTransparent(true);
                openProfileFolder.setImage(Images.BLANK);
                deleteProfile.setMouseTransparent(true);
                deleteProfile.setImage(Images.BLANK);
                profileProperties.setMouseTransparent(true);
                profileProperties.setImage(Images.BLANK);
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
                LOGGER.log(Level.SEVERE, "Failed to open profile " + Main.activeProfile.getValue().getName(), ex);
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
        profileProperties.setOnMouseEntered((event) -> {
            profileProperties.setImage(Images.SETTINGS_SELECT);
        });
        profileProperties.setOnMouseExited((event) -> {
            profileProperties.setImage(Images.SETTINGS);
        });
        profileProperties.setOnMousePressed((event) -> {
            profileProperties.setImage(Images.SETTINGS_PRESS);
        });
        profileProperties.setOnMouseReleased((event) -> {
            profileProperties.setImage(Images.SETTINGS);
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
        activeMods.forEach((mod) -> {
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
            LOGGER.log(Level.INFO, " Detected mod: {0}", mod.getName());
        });
        return nodes;
    }

    private Node enablerNode(Modification mod) {
        ImageView iv = new ImageView();
        boolean[] selected = new boolean[]{false};
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
                LOGGER.log(Level.SEVERE, "Enabler - AccessDeniedException: Make sure you have given the program Administrator Privileges!");
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + Main.activeProfile.getValue().getName() + " profile?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
                Main.activeProfile.getValue().delete();
                Images.DELETE_SELECT_ANIM.halt();
                Images.DELETE_PRESS_ANIM.animate(deleteProfile);
            } else {
                LOGGER.log(Level.WARNING, "Deletion is disabled when -Domodify=false");
            }
        } else {
            deleteProfile.setImage(Images.DELETE);
            LOGGER.log(Level.INFO, "Deletion cancelled");
            deleteProfile.setMouseTransparent(false);
        }
    }

    public void openProfileProperties() {
        PROFILE_PROPERTIES_DATA.setProfile(Main.activeProfile.get());
        PROFILE_PROPERTIES.showThenRun((Object[] result, Throwable ex) -> handleProfileProperties(result, ex));
    }

    private void handleProfileProperties(Object[] result, Throwable ex) {
        Profile editedProfile = (Profile) result[0];
        Image newIcon = (Image) result[1];
        String newName = (String) result[2];
        String newDesc = (String) result[3];

        if (newIcon != null && !newIcon.equals(editedProfile.getIcon())) {
            Platform.runLater(() -> editedProfile.setIcon(newIcon));
        }
        
        if (newName != null && !newName.equals(editedProfile.getName())) {
            Platform.runLater(() -> editedProfile.setName(newName));
        }

        if (newDesc != null && !newDesc.equals(editedProfile.getDescription())) {
            Platform.runLater(() -> editedProfile.setDescription(newDesc));
        }
    }
    //</editor-fold>

    public static void setImageMouseHandlers(ImageView imageView, Image normal, Image onSelect, Image onPress) {
        imageView.setOnMouseEntered((event) -> {
            imageView.setImage(onSelect);
        });
        imageView.setOnMouseExited((event) -> {
            imageView.setImage(normal);
        });
        if (onPress != null) {
            imageView.setOnMousePressed((event) -> {
                imageView.setImage(onPress);
            });
            imageView.setOnMouseReleased((event) -> {
                imageView.setImage(normal);
            });
        }
    }

    public static void setImageMouseHandlers(ImageView imageView, Image normal, SpriteAnimation onSelect, SpriteAnimation onPress, boolean handleOnPress) {
        if (onPress != null) {
            imageView.setOnMouseEntered((event) -> {
                if (!onPress.isAnimated()) {
                    onSelect.animate(imageView);
                }
            });
            imageView.setOnMouseExited((event) -> {
                if (!onPress.isAnimated()) {
                    onSelect.halt();
                    imageView.setImage(normal);
                }
            });
            if (handleOnPress) {
                imageView.setOnMousePressed((event) -> {
                    onSelect.halt();
                    onPress.animate(imageView);
                });
                imageView.setOnMouseReleased((event) -> {
                    onPress.halt();
                    imageView.setImage(normal);
                });
            }
        } else {
            imageView.setOnMouseEntered((event) -> {
                onSelect.animate(imageView);
            });
            imageView.setOnMouseExited((event) -> {
                onSelect.halt();
                imageView.setImage(normal);
            });
        }
    }

}

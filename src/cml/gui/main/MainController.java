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

import cml.ErrorManager;
import cml.Images;
import cml.Main;
import cml.SpriteAnimation;
import cml.beans.Profile;
import cml.beans.SceneEvent;
import cml.gui.profilesettings.ProfileSettingsController;
import cml.gui.settings.SettingsController;
import cml.gui.workshop.WorkshopController;
import cml.lib.threadmanager.ThreadManager;
import cml.lib.xmliconmap.CMLIcon;
import cml.lib.xmliconmap.CMLIconConditional;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
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
    @FXML private ProfileSettingsController profilesettingsController;

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
        profilesettingsController.init(this);
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
                launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.HOVER));
            }
        });
        launchButton.setOnMouseExited((event) -> {
            if (!launching && !ErrorManager.isStateError()) {
                launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.NORMAL));
            }
        });
        launchButton.setOnMousePressed((event) -> {
            if (Main.activeProfile.get().getDirectory() != null) {
                launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.PRESS));
                launch();
            } else {
                ErrorManager.addStateCause("ActiveProfile == null");
            }
        });
        ErrorManager.State.addListener((obs, oldValue, newValue) -> {
            if (newValue.isError()) {
                launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.ERROR));
                launchButton.fireEvent(new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Main.ICON_MAP.ICON.getIcon(CMLIcon.State.ERROR))));
            } else if (oldValue.isError()) {
                launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.NORMAL));
                launchButton.fireEvent(new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Main.ICON_MAP.ICON.getIcon(CMLIcon.State.NORMAL))));
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
            launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.PRESS));
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
            new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Main.ICON_MAP.ICON.getIcon(CMLIcon.State.SUCCESS))).fire(launchButton);
            launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.NORMAL));
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
                launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.NORMAL));
                new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Main.ICON_MAP.ICON.getIcon(CMLIcon.State.NORMAL))).fire(launchButton);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO, "Pre-success thread interrupted");
                }
            }
            if (!ErrorManager.isStateError()) {
                new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Main.ICON_MAP.ICON.getIcon(CMLIcon.State.SUCCESS))).fire(launchButton);
                launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.SUCCESS));
            }
            long t = successTime;
            runningSuccess++;
            try {
                Thread.sleep(t);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, "Success thread interrupted");
            }
            if (runningSuccess == 1 && !ErrorManager.isStateError()) {
                new SceneEvent(SceneEvent.ICON_CHANGE, new SceneEvent.IconChange(SceneEvent.IconChange.IconChangeType.SET, Main.ICON_MAP.ICON.getIcon(CMLIcon.State.NORMAL))).fire(launchButton);
                launchButton.setImage(Main.ICON_MAP.LAUNCH.getIcon(CMLIcon.State.NORMAL));
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
            profilesettingsController.setVisible(newValue.intValue() == 2);
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
        ToggleGroup profileSelectorGroup = new ToggleGroup();
        profileSelectorGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
            Main.activeProfile.setValue((Profile) profileSelectorGroup.getUserData());
        });

        profileListView.getItems().addAll(fromProfileList(Main.profileList.getValue(), profileSelectorGroup));
        Main.profileList.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                profileListView.getItems().setAll(fromProfileList(newValue, profileSelectorGroup));
            } else {
                Label errorLabel = new Label("Invalid mods folder");
                errorLabel.setTextFill(Color.RED);
                profileListView.getItems().setAll(errorLabel);
            }
        });

    }

    private List<Node> fromProfileList(List<Profile> profiles, ToggleGroup toggleGroup) {
        List<Node> nodes = new ArrayList();
        Profile selected = (Profile) toggleGroup.getUserData();
        toggleGroup.getToggles().clear();
        profiles.forEach((profile) -> {
            RadioButton selector = new RadioButton();
            selector.setMouseTransparent(true);
            selector.setToggleGroup(toggleGroup);
            selector.setStyle("-fx-translate-x: 4px; -fx-translate-y: 2px;");
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

            hBox.setOnMouseEntered((event) -> {
                if (hBox.getBackground() == null || hBox.getBackground().isEmpty()) {
                    hBox.setBackground(new Background(
                            new BackgroundFill(Color.color(1.0, 0.9, 0.7, 0.25), CornerRadii.EMPTY, Insets.EMPTY),
                            new BackgroundFill(Color.color(0.0, 0.0, 0.0, 0.2), CornerRadii.EMPTY, new Insets(2.0))));
                }
            });
            hBox.setOnMouseExited((event) -> {
                if (!Main.activeProfile.getValue().equals(profile)) {
                    hBox.setBackground(Background.EMPTY);
                }
            });
            hBox.setOnMousePressed((event) -> {
                hBox.setBackground(new Background(
                        new BackgroundFill(Color.color(1.0, 0.9, 0.7, 0.5), CornerRadii.EMPTY, Insets.EMPTY),
                        new BackgroundFill(Color.color(0.0, 0.0, 0.0, 0.2), CornerRadii.EMPTY, new Insets(1.0)))
                );
                toggleGroup.setUserData(profile);
                toggleGroup.selectToggle(selector);
            });
            Main.activeProfile.addListener((obs, oldValue, newValue) -> {
                if (oldValue == profile) {
                    hBox.setBackground(Background.EMPTY);
                }
            });

            hBox.getChildren().addAll(selector, separator0, icon, separator1, name, separator2, description);
            nodes.add(hBox);
            if (selected != null && selected.equals(profile)) {
                selector.setSelected(true);
                toggleGroup.selectToggle(selector);
            }
            LOGGER.log(Level.INFO, " Detected Profile: {0}", profile.getName());
        });
        return nodes;
    }

    public void createNewProfile() {
        Main.createProfile(newProfileField.getText());
    }
    //</editor-fold>

    public static void setImageMouseHandlers(ImageView imageView, CMLIcon icon) {
        if (icon instanceof CMLIconConditional) {
            setUpdatingImageMouseHandlers(imageView, (CMLIconConditional) icon);
        } else {
            setImageMouseHandlers(imageView, icon.getIcon(CMLIcon.State.NORMAL), icon.getIcon(CMLIcon.State.HOVER, null), icon.getIcon(CMLIcon.State.PRESS, null));
        }
    }

    private static void setUpdatingImageMouseHandlers(ImageView imageView, CMLIconConditional icon) {
        icon.getProperty().addListener((obs, oldValue, newValue) -> {
            setImageMouseHandlers(imageView, icon.getIcon(CMLIcon.State.NORMAL), icon.getIcon(CMLIcon.State.HOVER, null), icon.getIcon(CMLIcon.State.PRESS, null));
        });
    }

    public static void setImageMouseHandlers(ImageView imageView, Image normal, Image onSelect, Image onPress) {
        imageView.setOnMouseEntered((event) -> {
            imageView.getOnMouseEntered().handle(event);
            imageView.setImage(onSelect);
        });
        imageView.setOnMouseExited((event) -> {
            imageView.getOnMouseExited().handle(event);
            imageView.setImage(normal);
        });
        if (onPress != null) {
            imageView.setOnMousePressed((event) -> {
                imageView.getOnMousePressed().handle(event);
                imageView.setImage(onPress);
            });
            imageView.setOnMouseReleased((event) -> {
                imageView.getOnMouseReleased().handle(event);
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

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
package cml.gui.profilesettings;

import cml.Constants;
import cml.ErrorManager;
import cml.Main;
import cml.Media;
import cml.beans.Modification;
import cml.beans.Profile;
import cml.gui.main.MainController;
import cml.gui.main.SubController;
import cml.gui.popup.CmlPopup;
import cml.gui.popup.PopupData;
import cml.lib.xmliconmap.CMLIcon;
import cml.lib.xmliconmap.CMLIconConditional;
import cml.lib.xmliconmap.CMLIconMap;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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
public class ProfileSettingsController extends SubController {

    private static final Logger LOGGER = Logger.getLogger(ProfileSettingsController.class.getName());

    @FXML private AnchorPane profilePane;
    @FXML private Rectangle profileTopBorder;
    @FXML private Text profileNameText;
    @FXML private ImageView profileImage;
    @FXML private ImageView openProfileFolder;
    @FXML private ImageView deleteProfile;
    @FXML private ImageView profileScrollBar;
    @FXML private AnchorPane profileScrollPane;
    @FXML private Rectangle modsListBackground;
    @FXML private Rectangle profileSliderBackground;
    @FXML private ListView modsListVBox;
    @FXML private ImageView profileScrollButtonTop;
    @FXML private ImageView profileScrollButtonBottom;
    @FXML private ImageView profileProperties;

    private final BooleanProperty scrollEnabled = new SimpleBooleanProperty(true);
    private final DoubleProperty scroll = new SimpleDoubleProperty(0.0);
    private double scrollCenter = 0.0;
    private double scrollTranslate = 0.0;

    @Override
    public void setVisible(boolean visible) {
        profilePane.setVisible(visible);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeProfile();
    }

    private final class ProfilePropertiesData extends PopupData<Object[], Integer> {

        private Profile editingProfile = null;
        private final ImageView iconField = new ImageView();
        private final TextField descriptionField = new TextField();
        private final TextField nameField = new TextField();
        private final FileChooser fileChooser = new FileChooser();
        private CmlPopup parent = null;

        private AnchorPane root;

        public void setProfile(Profile profile) {
            this.editingProfile = profile;
            iconField.setImage(profile.getIconSafe());
            descriptionField.setText(profile.getDescription());
            nameField.setText(profile.getName());
            if (parent != null) {
                setTitle(parent);
            }
        }

        @Override
        public Node getNode() {
            return root;
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

            root = new AnchorPane();
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

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

            root.getChildren().setAll(iconField, chooseImage, nameField, descriptionField);

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
        
        openProfileFolder.setVisible(false);
        deleteProfile.setVisible(false);
        profileProperties.setVisible(false);
        
        modsListBackground.widthProperty().bind(profilePane.widthProperty().subtract(32));
        modsListBackground.heightProperty().bind(profilePane.heightProperty().subtract(96));
        profileTopBorder.widthProperty().bind(profilePane.widthProperty());
        profileSliderBackground.heightProperty().bind(profileScrollPane.heightProperty());
        
        Main.activeProfile.addListener((obs, oldValue, newValue) -> {
            profileNameText.textProperty().bind(newValue.getNameProperty());
            profileImage.imageProperty().bind(newValue.getIconProperty());
            modsListVBox.getItems().setAll(fromModList(newValue.getModifications()));
            if (newValue.getDirectory() != null) {
                openProfileFolder.setVisible(true);
                deleteProfile.setVisible(true);
                profileProperties.setVisible(true);
                ErrorManager.removeStateCause("ActiveProfile == null");
            } else {
                openProfileFolder.setVisible(false);
                deleteProfile.setVisible(false);
                profileProperties.setVisible(false);
            }
        });
        openProfileFolder.setOnMousePressed((event) -> {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + Main.activeProfile.getValue().getDirectory().getAbsolutePath());
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to open profile " + Main.activeProfile.getValue().getName(), ex);
            }
        });
        MainController.setImageMouseHandlers(openProfileFolder, CMLIconMap.ICON_MAP.OPEN_FOLDER);
        MainController.setImageMouseHandlers(deleteProfile, CMLIconMap.ICON_MAP.DELETE);
        MainController.setImageMouseHandlers(profileProperties, CMLIconMap.ICON_MAP.PROFILE_SETTINGS);

        profileScrollBar.setOnMousePressed((event) -> {
            scrollCenter = event.getSceneY();
            scrollTranslate = profileScrollBar.getTranslateY();
        });
        MainController.setImageMouseHandlers(profileScrollBar, Media.SCROLL, Media.SCROLL_SELECT, Media.SCROLL_PRESS);
        profileScrollBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, (event) -> {
            if (scrollEnabled.get()) {
                event.consume();
                double yMax = profileSliderBackground.getHeight() - 140;
                double y = scrollTranslate + (event.getSceneY() - scrollCenter);
                y = y > yMax ? yMax : (y < 0 ? 0 : y);
                scroll.setValue(y / yMax);
                profileScrollBar.setImage(Media.SCROLL_PRESS);
            }
        });
        scrollEnabled.addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                profileScrollPane.setMouseTransparent(false);
                profileScrollBar.setImage(Media.SCROLL);
                profileScrollButtonTop.setImage(Media.SCROLL_B);
                profileScrollButtonBottom.setImage(Media.SCROLL_B);
            } else {
                profileScrollPane.setMouseTransparent(true);
                profileScrollBar.setImage(Media.SCROLL_DISABLE);
                profileScrollButtonTop.setImage(Media.SCROLL_B_DISABLE);
                profileScrollButtonBottom.setImage(Media.SCROLL_B_DISABLE);
            }
        });
        
        profileScrollButtonTop.setOnMousePressed((event) -> {
            scroll.set(Math.max(0, scroll.get() - 0.05));
        });
        MainController.setImageMouseHandlers(profileScrollButtonTop, Media.SCROLL_B, Media.SCROLL_B_SELECT, Media.SCROLL_B_PRESS);
        
        profileScrollButtonBottom.setOnMousePressed((event) -> {
            scroll.set(Math.min(1, scroll.get() + 0.05));
        });
        MainController.setImageMouseHandlers(profileScrollButtonBottom, Media.SCROLL_B, Media.SCROLL_B_SELECT, Media.SCROLL_B_PRESS);

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
                modsListScroll.getStylesheets().add(getClass().getClassLoader().getResource("media/listViewHideVerticalScroll.css").toExternalForm());
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

    public void deleteActiveProfile() {
        deleteProfile.setMouseTransparent(true);
        deleteProfile.setImage(CMLIconMap.ICON_MAP.DELETE.getIcon(CMLIcon.State.HOVER));
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete profile: " + Main.activeProfile.getValue().getName(), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
                Main.activeProfile.getValue().delete();
            } else {
                LOGGER.log(Level.WARNING, "Deletion is disabled when -Domodify=false");
            }
        } else {
            deleteProfile.setImage(CMLIconMap.ICON_MAP.DELETE.getIcon(CMLIcon.State.NORMAL));
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

        if (newIcon != null && !newIcon.equals(editedProfile.getIconSafe())) {
            Platform.runLater(() -> editedProfile.setIcon(newIcon));
        }

        if (newName != null && !newName.equals(editedProfile.getName())) {
            Platform.runLater(() -> editedProfile.setName(newName));
        }

        if (newDesc != null && !newDesc.equals(editedProfile.getDescription())) {
            Platform.runLater(() -> editedProfile.setDescription(newDesc));
        }
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
        iv.setOnMousePressed((event) -> {
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
        CMLIcon enableIcon = new CMLIconConditional(CMLIconMap.ICON_MAP.ENABLER, CMLIconMap.ICON_MAP.DISABLER, CMLIconMap.ICON_MAP, mod.enabledListener);
        MainController.setImageMouseHandlers(iv, enableIcon);
        iv.setPreserveRatio(true);
        iv.setFitHeight(24);
        return iv;
    }
}

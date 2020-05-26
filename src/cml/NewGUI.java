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
import static cml.Images.BACK;
import static cml.Images.BACK_PRESS;
import static cml.Images.BACK_SELECT;
import static cml.Images.BLANK;
import static cml.Images.DELETE;
import static cml.Images.DELETE_PRESS_ANIM;
import static cml.Images.DELETE_SELECT_ANIM;
import static cml.Images.ENABLER_BOTH;
import static cml.Images.ENABLER_OFF;
import static cml.Images.ENABLER_OFF_SELECT;
import static cml.Images.ENABLER_ON;
import static cml.Images.ENABLER_ON_SELECT;
import static cml.Images.ICON;
import static cml.Images.LAUNCH;
import static cml.Images.LAUNCH_PRESS;
import static cml.Images.LAUNCH_SELECT;
import static cml.Images.LAUNCH_SOUND;
import static cml.Images.OPEN;
import static cml.Images.OPEN_PRESS;
import static cml.Images.OPEN_SELECT;
import static cml.Images.PROFILE;
import static cml.Images.PROFILES;
import static cml.Images.PROFILES_PRESS;
import static cml.Images.PROFILES_SELECT;
import static cml.Images.PROFILE_PRESS;
import static cml.Images.PROFILE_SELECT;
import static cml.Images.SCROLL;
import static cml.Images.SCROLL_B;
import static cml.Images.SCROLL_B_DISABLE;
import static cml.Images.SCROLL_B_PRESS;
import static cml.Images.SCROLL_B_SELECT;
import static cml.Images.SCROLL_DISABLE;
import static cml.Images.SCROLL_PRESS;
import static cml.Images.SCROLL_SELECT;
import static cml.Images.SETTINGS;
import static cml.Images.SETTINGS_PRESS;
import static cml.Images.SETTINGS_SELECT;
import cml.beans.Modification;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
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
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //stage.setResizable(true);
        //stage.setTitle("Construct Mod Loader - V+" + Constants.VERSION);
        /*stage.heightProperty().addListener((obs, oldValue, newValue) -> {
            makeStage(stage);
        });
        stage.widthProperty().addListener((obs, oldValue, newValue) -> {
            makeStage(stage);
        });*/
        //makeStage(stage);
        Parent root = FXMLLoader.load(getClass().getResource("MainGUI.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("media/listViewStyles.css").toExternalForm());

        stage.getIcons().add(ICON);
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
        try {
            String lines = Main.scrapMechanicFolder + "\n" + Main.vanillaFolder + "\n" + Main.modsFolder;
            Files.write(new File(Constants.FOLDERS_LOCATION).toPath(), lines.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (AccessDeniedException ex) {
            Logger.getLogger(NewGUI.class.getName()).log(Level.SEVERE, "Could not access " + Constants.FOLDERS_LOCATION + " - make sure you have granted Administrator Privileges", ex);
        }
    }
//
//    public void makeStage(Stage stage) {
//        /*AnchorPane pane = new AnchorPane();
//        
//        pane.getChildren().add(HEADER);
//        pane.getChildren().add(FOOTER);
//        pane.getChildren().add(FOOTER_CENTER);
//        
//        //BACKGROUND
//        pane.setBackground(BACKGROUND);
//        pane.setPadding(new Insets(0));
//        
//        //HEADER
//        AnchorPane.setLeftAnchor(HEADER, 0.1);
//        AnchorPane.setRightAnchor(HEADER, 0.1);
//        AnchorPane.setTopAnchor(HEADER, 0.0);
//        
//        //FOOTER
//        AnchorPane.setLeftAnchor(FOOTER, 0.1);
//        AnchorPane.setRightAnchor(FOOTER, 0.1);
//        AnchorPane.setBottomAnchor(FOOTER, 0.0);
//        
//        //FOOTER_CENTER
//        AnchorPane.setBottomAnchor(FOOTER_CENTER, 0.0);
//        
//        
//        Scene scene = new Scene(pane);
//        stage.setScene(scene);*/
//        //stage.show();
//    }

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
            if (!launching) {
                launchButton.setImage(LAUNCH_SELECT);
            }
        });
        launchButton.setOnMouseExited((event) -> {
            if (!launching) {
                launchButton.setImage(LAUNCH);
            }
        });
        launchButton.setOnMousePressed((event) -> {
            launchButton.setImage(LAUNCH_PRESS);
            launch();
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
            launchButton.setImage(LAUNCH_PRESS);
            launchButton.setMouseTransparent(true);
            MediaPlayer mediaPlayer = new MediaPlayer(LAUNCH_SOUND);
            mediaPlayer.setOnEndOfMedia(mediaPlayer::dispose);
            mediaPlayer.play();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(NewGUI.class.getName()).log(Level.INFO, "Launch thread interrupted", ex);
            }
            Main.launchGame();
            launching = false;
            launchButton.setImage(LAUNCH);
            launchButton.setMouseTransparent(false);
        }
    };

    public void launch() {
        if (!launching) {
            new Thread(launchRunnable).start();
        }
    }

    @FXML
    private AnchorPane mainMenuPane;
    int oldMenuView = 0;

    public void toggleSettings() {
        menuView.setValue(menuView.getValue() == 1 ? oldMenuView : 1);
    }

    public void toggleProfile() {
        menuView.setValue(menuView.getValue() == 2 ? oldMenuView : 2);
    }

    public void toggleProfiles() {
        menuView.setValue(menuView.getValue() == 3 ? oldMenuView : 3);
    }

    private boolean settingsIsSelected = false;
    private boolean profileIsSelected = false;
    private boolean profilesIsSelected = false;

    private void initializeMenu() {
        cmlSettings.setOnMouseEntered((event) -> {
            cmlSettings.setImage(menuView.getValue() == 1 ? BACK_SELECT : SETTINGS_SELECT);
            settingsIsSelected = true;
        });
        cmlSettings.setOnMouseExited((event) -> {
            cmlSettings.setImage(menuView.getValue() == 1 ? BACK : SETTINGS);
            settingsIsSelected = false;
        });
        cmlSettings.setOnMousePressed((event) -> {
            cmlSettings.setImage(menuView.getValue() == 1 ? BACK_PRESS : SETTINGS_PRESS);
        });
        cmlSettings.setOnMouseReleased((event) -> {
            cmlSettings.setImage(menuView.getValue() == 1 ? BACK : SETTINGS);
        });

        cmlProfile.setOnMouseEntered((event) -> {
            cmlProfile.setImage(menuView.getValue() == 2 ? BACK_SELECT : PROFILE_SELECT);
            profileIsSelected = true;
        });
        cmlProfile.setOnMouseExited((event) -> {
            cmlProfile.setImage(menuView.getValue() == 2 ? BACK : PROFILE);
            profileIsSelected = false;
        });
        cmlProfile.setOnMousePressed((event) -> {
            cmlProfile.setImage(menuView.getValue() == 2 ? BACK_PRESS : PROFILE_PRESS);
        });
        cmlProfile.setOnMouseReleased((event) -> {
            cmlProfile.setImage(menuView.getValue() == 2 ? BACK : PROFILE);
        });

        cmlProfiles.setOnMouseEntered((event) -> {
            cmlProfiles.setImage(menuView.getValue() == 3 ? BACK_SELECT : PROFILES_SELECT);
            profilesIsSelected = true;
        });
        cmlProfiles.setOnMouseExited((event) -> {
            cmlProfiles.setImage(menuView.getValue() == 3 ? BACK : PROFILES);
            profilesIsSelected = false;
        });
        cmlProfiles.setOnMousePressed((event) -> {
            cmlProfiles.setImage(menuView.getValue() == 3 ? BACK_PRESS : PROFILES_PRESS);
        });
        cmlProfiles.setOnMouseReleased((event) -> {
            cmlProfiles.setImage(menuView.getValue() == 3 ? BACK : PROFILES);
        });
        menuView.addListener((obs, oldValue, newValue) -> {
            oldMenuView = oldValue.intValue();
            cmlSettings.setImage(newValue.intValue() == 1 ? (settingsIsSelected ? BACK_SELECT : BACK) : (settingsIsSelected ? SETTINGS_SELECT : SETTINGS));
            cmlProfile.setImage(newValue.intValue() == 2 ? (profileIsSelected ? BACK_SELECT : BACK) : (profileIsSelected ? PROFILE_SELECT : PROFILE));
            cmlProfiles.setImage(newValue.intValue() == 3 ? (profilesIsSelected ? BACK_SELECT : BACK) : (profilesIsSelected ? PROFILES_SELECT : PROFILES));
            mainMenuPane.setVisible(newValue.intValue() == 0);
            settingsPane.setVisible(newValue.intValue() == 1);
            profilePane.setVisible(newValue.intValue() == 2);
        });
    }

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
                updateAvailableText.setText("Update Available: " + Constants.VERSION + " -> " + newValue);
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
        });
        modsFolder.setOnAction((event) -> {
            String text = modsFolder.getText();
            Main.modsFolder = (text.endsWith("/") || text.endsWith("\\") ? text : (text.contains("\\") ? text + "\\" : text + "/"));
            Main.activeProfile.setValue(new Profile(new File(text).listFiles()[0]));
        });
        vanillaFolder.setOnAction((event) -> {
            String text = vanillaFolder.getText();
            Main.vanillaFolder = (text.endsWith("/") || text.endsWith("\\") ? text : (text.contains("\\") ? text + "\\" : text + "/"));
        });
    }

    public void checkForUpdate() {
        updateAvailable.setValue(Main.checkForUpdate()[0]);
    }

    public void update() {
        Main.update();
    }

    private void initializeProfiles() {

    }

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
    private AnchorPane modsListPane;
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
        openProfileFolder.setImage(BLANK);
        deleteProfile.setMouseTransparent(true);
        deleteProfile.setImage(BLANK);
        profilePane.widthProperty().addListener((obs, oldValue, newValue) -> {
            profileTopBorder.setWidth(newValue.doubleValue());
        });
        profilePane.heightProperty().addListener((obs, oldValue, newValue) -> {
            modsListVBox.setMaxHeight(newValue.doubleValue() - 235);
            modsListVBox.setPrefHeight(newValue.doubleValue() - 235);
        });
        Main.activeProfile.addListener((obs, oldValue, newValue) -> {
            profileNameText.setText(newValue.getName());
            profileImage.setImage(newValue.getIcon());
            openProfileFolder.setMouseTransparent(false);
            openProfileFolder.setImage(OPEN);
            deleteProfile.setMouseTransparent(false);
            deleteProfile.setImage(DELETE);
            modsListVBox.getItems().clear();
            modsListVBox.getItems().addAll(fromModList(newValue.getModifications()));
            Main.activeModifications = Main.activeProfile.get().getActiveModifications();
        });
        openProfileFolder.setOnMouseEntered((event) -> {
            openProfileFolder.setImage(OPEN_SELECT);
        });
        openProfileFolder.setOnMouseExited((event) -> {
            openProfileFolder.setImage(OPEN);
        });
        openProfileFolder.setOnMousePressed((event) -> {
            openProfileFolder.setImage(OPEN_PRESS);
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + Main.activeProfile.getValue().getDirectory().getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(NewGUI.class.getName()).log(Level.SEVERE, "Failed to open profile " + Main.activeProfile.getValue().getName(), ex);
            }
        });
        openProfileFolder.setOnMouseReleased((event) -> {
            openProfileFolder.setImage(OPEN);
        });
        deleteProfile.setOnMouseEntered((event) -> {
            deleteProfile.setImage(DELETE_SELECT_ANIM);
        });
        deleteProfile.setOnMouseExited((event) -> {
            deleteProfile.setImage(DELETE);
        });
        deleteProfile.setOnMousePressed((event) -> {
            deleteProfile.setImage(DELETE_PRESS_ANIM);
        });

        profileScrollBar.setOnMouseEntered((event) -> {
            if (scrollEnabled.get()) {
                profileScrollBar.setImage(SCROLL_SELECT);
            }
            scrollSelected = true;
        });
        profileScrollBar.setOnMouseExited((event) -> {
            if (scrollEnabled.get()) {
                profileScrollBar.setImage(SCROLL);
            }
            scrollSelected = false;
        });
        profileScrollBar.setOnMousePressed((event) -> {
            if (scrollEnabled.get()) {
                profileScrollBar.setImage(SCROLL_PRESS);
                scrollCenter = event.getSceneY();
                scrollTranslate = profileScrollBar.getTranslateY();
            }
        });
        profileScrollBar.setOnMouseReleased((event) -> {
            if (scrollEnabled.get()) {
                profileScrollBar.setImage(scrollSelected ? SCROLL_SELECT : SCROLL);
            }
        });
        profileScrollBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, (event) -> {
            if (scrollEnabled.get()) {
                event.consume();
                double yMax = profileSliderBackground.getHeight() - 140;
                double y = scrollTranslate + (event.getSceneY() - scrollCenter);
                y = y > yMax ? yMax : (y < 0 ? 0 : y);
                scroll.setValue(y / yMax);
                profileScrollBar.setImage(SCROLL_PRESS);
            }
        });
        scrollEnabled.addListener((obs, oldValue, newValue) -> {
            if (newValue && !oldValue) {
                profileScrollBar.setImage(scrollSelected ? SCROLL_SELECT : SCROLL);
                profileScrollButtonTop.setImage(SCROLL_B);
                profileScrollButtonBottom.setImage(SCROLL_B);
            } else {
                profileScrollBar.setImage(SCROLL_DISABLE);
                profileScrollButtonTop.setImage(SCROLL_B_DISABLE);
                profileScrollButtonBottom.setImage(SCROLL_B_DISABLE);
            }
        });
        profileScrollButtonTop.setOnMouseEntered((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonTop.setImage(SCROLL_B_SELECT);
            }
        });
        profileScrollButtonTop.setOnMouseExited((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonTop.setImage(SCROLL_B);
            }
        });
        profileScrollButtonTop.setOnMousePressed((event) -> {
            if (scrollEnabled.get()) {
                scroll.set(Math.max(0, scroll.get() - 0.05));
                profileScrollButtonTop.setImage(SCROLL_B_PRESS);
            }
        });
        profileScrollButtonTop.setOnMouseReleased((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonTop.setImage(SCROLL_B);
            }
        });
        profileScrollButtonBottom.setOnMouseEntered((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonBottom.setImage(SCROLL_B_SELECT);
            }
        });
        profileScrollButtonBottom.setOnMouseExited((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonBottom.setImage(SCROLL_B);
            }
        });
        profileScrollButtonBottom.setOnMousePressed((event) -> {
            if (scrollEnabled.get()) {
                scroll.set(Math.min(1, scroll.get() + 0.05));
                profileScrollButtonBottom.setImage(SCROLL_B_PRESS);
            }
        });
        profileScrollButtonBottom.setOnMouseReleased((event) -> {
            if (scrollEnabled.get()) {
                profileScrollButtonBottom.setImage(SCROLL_B);
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
                    if (newValue != oldValue) {
                        scrollEnabled.setValue(newValue);
                    }
                });
                scrollEnabled.setValue(modsListScroll.isVisible());
                modsListVBox.removeEventHandler(MouseEvent.ANY, this);
            }
        };
        modsListPane.setOnMouseEntered(oneTimeEvent);

        modsListPane.widthProperty().addListener((obs, oldValue, newValue) -> {
            modsListBackground.setWidth(newValue.doubleValue());
        });
        modsListPane.heightProperty().addListener((obs, oldValue, newValue) -> {
            modsListBackground.setHeight(newValue.doubleValue());
            profileSliderBackground.setHeight(newValue.doubleValue());
        });
        Main.activeProfile.setValue(new Profile(new File(Main.modsFolder).listFiles()[0]));
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
            System.out.println(mod.getName());
        }
        return nodes;
    }

    private Node enablerNode(Modification mod) {
        ImageView iv = new ImageView();
        if (mod.isEnabled()) {
            iv.setImage(ENABLER_ON);
        } else {
            iv.setImage(ENABLER_OFF);
        }
        iv.setOnMouseEntered((event) -> {
            iv.setImage(mod.isEnabled() ? ENABLER_ON_SELECT : ENABLER_OFF_SELECT);
        });
        iv.setOnMouseExited((event) -> {
            iv.setImage(mod.isEnabled() ? ENABLER_ON : ENABLER_OFF);
        });
        iv.setOnMousePressed((event) -> {
            iv.setImage(ENABLER_BOTH);
            if (mod.isEnabled()) {
                mod.disable();
            } else {
                mod.enable();
            }
        });
        iv.setOnMouseReleased((event) -> {
            iv.setImage(mod.isEnabled() ? ENABLER_ON : ENABLER_OFF);
        });
        iv.setPreserveRatio(true);
        iv.setFitHeight(24);
        return iv;
    }

    public void deleteActiveProfile() {
        System.out.println("[ERROR] Deletion of profiles is currently not supported.");
    }

    public void openProfileProperties() {
        System.out.println("[ERROR] Editing of profile properties is currently not supported.");
    }

    //Because NetBeans won't recognize my Main class as the main class...
    public static void main(String[] args) {
        Main.main(args);
    }

}

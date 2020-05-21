/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.apply.Apply;
import cml.beans.Modification;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 *
 * @author bennett.spiphi
 */
public class GUI extends Application {

    public static List<Modification> activeModifications;
    public static List<Modification> allModifications;
    public static LocalDateTime lastApplied = LocalDateTime.MIN;
    public static LocalDateTime lastChange = LocalDateTime.MIN;
    private double scroll = 0;
    private double scrollMin = 0;
    private double scrollMax = 0;
    private List<Node> activeModsGroup = getActiveModsGroup(this.scroll, 300, -100);
    private final StackPane root = new StackPane();
    private Thread lastAppliedThread;
    private Thread lastChangeThread;
    private Thread lastChangeTextThread;

    @Override
    public void start(Stage primaryStage) {

        //<editor-fold defaultstate="collapsed" desc="showModsFolder">
        ImageView showModsFolder = new ImageView(Images.ButtonBackground);
        showModsFolder.setOnMousePressed((MouseEvent event) -> {
            try {
                System.out.println("Showing mods folder");
                Runtime.getRuntime().exec("explorer.exe /select," + Constants.MODS_FOLDER);
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Mods folder could not be opened");
            }
        });
        showModsFolder.setOnMouseEntered((MouseEvent event) -> {
            showModsFolder.setImage(Images.ButtonSelectBackground);
        });
        showModsFolder.setOnMouseExited((MouseEvent event) -> {
            showModsFolder.setImage(Images.ButtonBackground);
        });
        showModsFolder.setScaleX(0.8);
        showModsFolder.setScaleY(0.8);
        Text showModsFolderText = new Text();
        showModsFolderText.setText("Show mods folder");
        showModsFolderText.setStroke(Color.color(0.21875, 0.22265625, 0.23828125));
        showModsFolderText.setScaleX(1.4);
        showModsFolderText.setScaleY(1.4);
        showModsFolderText.setMouseTransparent(true);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="applyModifications">
        ImageView applyModifications = new ImageView(Images.ButtonBackground);
        applyModifications.setOnMousePressed((MouseEvent event) -> {
            Apply.apply(activeModifications);
        });
        applyModifications.setOnMouseEntered((MouseEvent event) -> {
            applyModifications.setImage(Images.ButtonSelectBackground);
        });
        applyModifications.setOnMouseExited((MouseEvent event) -> {
            applyModifications.setImage(Images.ButtonBackground);
        });
        applyModifications.setScaleX(0.8);
        applyModifications.setScaleY(0.8);
        Text applyModificationsText = new Text();
        applyModificationsText.setText("Apply modifications");
        applyModificationsText.setStroke(Color.color(0.21875, 0.22265625, 0.23828125));
        applyModificationsText.setScaleX(1.32);
        applyModificationsText.setScaleY(1.32);
        applyModificationsText.setMouseTransparent(true);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="activeMods">
        Text activeMods = new Text();
        activeMods.setText("Active Modifications:");
        activeMods.setStroke(Color.WHITE);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="header">
        Label header = new Label();
        header.setText("Construct Mod Loader");
        header.setTextFill(Color.WHITE);
        header.setTextAlignment(TextAlignment.CENTER);
        header.setScaleX(4);
        header.setScaleY(4);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="launchGame">
        ImageView launchGame = new ImageView(Images.ButtonBackground);
        launchGame.setOnMousePressed((MouseEvent event) -> {
            try {
                Runtime.getRuntime().exec(Constants.LAUNCH_COMMAND);
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Failed to launch Scrap Mechanic", ex);
            }
        });
        launchGame.setOnMouseEntered((MouseEvent event) -> {
            launchGame.setImage(Images.ButtonSelectBackground);
        });
        launchGame.setOnMouseExited((MouseEvent event) -> {
            launchGame.setImage(Images.ButtonBackground);
        });
        launchGame.setScaleX(1.1);
        launchGame.setScaleY(1.1);
        Text launchGameText = new Text();
        launchGameText.setText("Launch");
        launchGameText.setStroke(Color.color(0.21875, 0.22265625, 0.23828125));
        launchGameText.setScaleX(3.0);
        launchGameText.setScaleY(3.0);
        launchGameText.setMouseTransparent(true);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="nextChangeUpdateTime">
        Text nextChangeUpdateTime = new Text();
        nextChangeUpdateTime.setText(getNextChangeUpdateTimeBar(30, 30));
        nextChangeUpdateTime.setStroke(Color.DARKGOLDENROD);
        nextChangeUpdateTime.setScaleX(2.0);
        nextChangeUpdateTime.setScaleY(0.25);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="sliderBackground">
        ImageView sliderBackground = new ImageView(Images.SliderBackground);
        sliderBackground.setScaleX(0.5);
        sliderBackground.setScaleY(1.95);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="lastAppliedText">
        Text lastAppliedText = new Text();
        Task<Void> lastAppliedTask = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                boolean a = false;
                while (!Thread.currentThread().isInterrupted()) {
                    updateMessage("" + a);
                    a = !a;
                    Thread.sleep(1000);
                }
                return null;
            }
        };
        lastAppliedTask.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            if (lastApplied.isEqual(LocalDateTime.MIN)) {
                lastAppliedText.setText("Last Applied: Never in this instance");
            } else {
                lastAppliedText.setText("Last Applied: " + lastApplied.format(DateTimeFormatter.ISO_DATE) + " at " + lastApplied.format(DateTimeFormatter.ISO_LOCAL_TIME));
            }

            if (lastApplied.isEqual(LocalDateTime.MIN)) {
                lastAppliedText.setStroke(Boolean.valueOf(newMessage) ? Color.RED : Color.ALICEBLUE);
            } else if (GUI.lastChange.isAfter(lastApplied)) {
                lastAppliedText.setStroke(Boolean.valueOf(newMessage) ? Color.RED : Color.DARKRED);
            } else if (lastApplied.isAfter(LocalDateTime.now().minusSeconds(5))) {
                lastAppliedText.setStroke(Color.AQUA);
            } else if (lastApplied.isAfter(LocalDateTime.now().minusMinutes(1))) {
                double value = (lastApplied.toEpochSecond(Constants.ZONE) - LocalDateTime.now().minusMinutes(1).toEpochSecond(Constants.ZONE) + 30.0) / 90.0;
                System.out.println(value);
                lastAppliedText.setStroke(Color.color(0, value, value));
            } else if (lastApplied.isAfter(LocalDateTime.now().minusMinutes(2))) {
                double valueA = (lastApplied.toEpochSecond(Constants.ZONE) - LocalDateTime.now().minusMinutes(1).toEpochSecond(Constants.ZONE) - 325.3) / (-985.8);
                double valueB = 1 - (lastApplied.toEpochSecond(Constants.ZONE) - LocalDateTime.now().minusMinutes(1).toEpochSecond(Constants.ZONE) - 120.0) / (-180.0);
                System.out.println(valueA + "; " + valueB);
                lastAppliedText.setStroke(Color.color(0, valueA, valueB));
            } else {
                lastAppliedText.setStroke(Color.DARKGREEN);
            }
        });
        Task<Void> lastChangeTask = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                while (!Thread.currentThread().isInterrupted()) {
                    updateMessage("" + getLastModified());
                    doChangeTimeDelay(nextChangeUpdateTime);
                }
                return null;
            }
        };
        lastChangeTask.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            GUI.lastChange = LocalDateTime.ofEpochSecond(Long.valueOf(newMessage), 0, Constants.ZONE);
        });

        this.lastAppliedThread = new Thread(lastAppliedTask);
        this.lastAppliedThread.start();

        this.lastChangeThread = new Thread(lastChangeTask);
        this.lastChangeThread.start();
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="lastChangedText">
        Text lastChangedText = new Text();
        Task<Void> lastChangedTextTask = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                while (!Thread.currentThread().isInterrupted()) {
                    updateMessage("" + GUI.lastChange.toEpochSecond(Constants.ZONE));
                    Thread.sleep(1000);
                }
                return null;
            }
        };
        lastChangedTextTask.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            LocalDateTime time = LocalDateTime.ofEpochSecond(Long.valueOf(newMessage), 0, Constants.ZONE);
            if (time.isEqual(LocalDateTime.MIN)) {
                lastChangedText.setText("Last Changed: < Calculating >");
            } else {
                lastChangedText.setText("Last Changed: " + time.format(DateTimeFormatter.ISO_DATE) + " at " + time.format(DateTimeFormatter.ISO_LOCAL_TIME));
            }
        });
        lastChangedText.setStroke(Color.DARKGOLDENROD);

        this.lastChangeTextThread = new Thread(lastChangedTextTask);
        this.lastChangeTextThread.start();
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="translation">
        header.setTranslateY(-300);
        showModsFolder.setTranslateX(-200);
        showModsFolder.setTranslateY(275);
        showModsFolderText.setTranslateX(-200);
        showModsFolderText.setTranslateY(275);
        applyModifications.setTranslateX(200);
        applyModifications.setTranslateY(275);
        applyModificationsText.setTranslateX(200);
        applyModificationsText.setTranslateY(275);
        activeMods.setTranslateX(-220);
        activeMods.setTranslateY(-190);
        launchGame.setTranslateY(275);
        launchGameText.setTranslateY(275);
        nextChangeUpdateTime.setTranslateY(344);
        sliderBackground.setTranslateX(295);
        sliderBackground.setTranslateY(8);
        lastAppliedText.setTranslateY(225);
        lastChangedText.setTranslateY(325);
        //</editor-fold>

        root.getChildren().add(header);
        root.getChildren().add(showModsFolder);
        root.getChildren().add(showModsFolderText);
        root.getChildren().add(applyModifications);
        root.getChildren().add(applyModificationsText);
        root.getChildren().add(activeMods);
        root.getChildren().add(launchGame);
        root.getChildren().add(launchGameText);
        root.getChildren().add(nextChangeUpdateTime);
        root.getChildren().add(sliderBackground);
        root.getChildren().add(lastAppliedText);
        root.getChildren().add(lastChangedText);
        root.getChildren().addAll(this.activeModsGroup);
        try {
            root.setBackground(new Background(new BackgroundImage(new Image(new FileInputStream(new File(Images.BACKGROUND_IMAGE))), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Background image not found", ex);
        }
        root.setPadding(new Insets(10));
        root.setOnScroll((ScrollEvent event) -> {
            this.scroll += event.getDeltaY();
            this.scroll = Math.max(Math.min(this.scroll, this.scrollMin), this.scrollMax);
            root.getChildren().removeAll(this.activeModsGroup);
            this.activeModsGroup = getActiveModsGroup(this.scroll, 250, 0);
            root.getChildren().addAll(this.activeModsGroup);
        });
        root.setOnMouseClicked((MouseEvent event) -> {
            sliderOffset += 0.1;
            System.out.println(sliderOffset);
        });

        Scene scene = new Scene(root, 601, 679);

        primaryStage.setTitle("Construct Mod Loader - V+" + Constants.VERSION);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        this.lastAppliedThread.interrupt();
        this.lastChangeThread.interrupt();
        this.lastChangeTextThread.interrupt();
    }

    public static void reloadMods() {
        System.out.println("Getting active modifications\n\n------- Modifications -------");
        File directory = new File(Constants.MODS_FOLDER);
        String[] mods = directory.list();
        GUI.activeModifications = new ArrayList();
        GUI.allModifications = new ArrayList();
        for (String mod : mods) {
            File modFile = new File(directory.getAbsolutePath() + "/" + mod);
            if (modFile.isDirectory()) {
                System.out.print("  • [Mod] ");
                Modification modObj = new Modification(modFile);
                if (modObj.isEnabled()) {
                    GUI.activeModifications.add(new Modification(modFile));
                    System.out.println(mod + " - Active");
                } else {
                    System.out.println(mod + " - Disabled");
                }
                GUI.allModifications.add(new Modification(modFile));
            } else {
                System.out.println("  • [File] " + mod);
            }
        }
    }

    double sliderOffset = 6;

    public List<Node> getActiveModsGroup(double scroll, double height, double offset) {

        List<Node> nodes = new ArrayList();

        double heightOffset = 90;
        double padding = 10;
        int i = 0;
        for (Modification mod : allModifications) {
            if ((heightOffset + padding) * (i + 1) + scroll <= height) {
                if ((heightOffset + padding) * (i + 1) + scroll >= 0) {
                    double modOffset = (heightOffset + padding) * i + scroll + offset;

                    //<editor-fold defaultstate="collapsed" desc="background">
                    ImageView background = new ImageView(Images.ModBackground);
                    background.setScaleX(0.9);
                    background.setScaleY(0.45);
                    background.setTranslateY(modOffset);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="name">
                    Text name = new Text();
                    name.setText(mod.getName());
                    name.setStroke(Color.WHITE);
                    name.setTranslateY(modOffset - 35);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="description">
                    Text description = new Text();
                    description.setText(mod.getDescription());
                    description.setStroke(Color.WHITE);
                    description.setTranslateY(modOffset - 15);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="ableButton">
                    ImageView ableButton = new ImageView(Images.ButtonBackground);
                    ableButton.setOnMousePressed((MouseEvent event) -> {
                        if (mod.isEnabled()) {
                            System.out.println("Disabling " + mod.getName());
                            mod.disable();
                        } else {
                            System.out.println("Enabling " + mod.getName());
                            mod.enable();
                        }

                        root.getChildren().removeAll(this.activeModsGroup);
                        this.activeModsGroup = getActiveModsGroup(this.scroll, 250, 0);
                        root.getChildren().addAll(this.activeModsGroup);

                        reloadMods();
                    });
                    ableButton.setOnMouseEntered((MouseEvent event) -> {
                        ableButton.setImage(Images.ButtonSelectBackground);
                    });
                    ableButton.setOnMouseExited((MouseEvent event) -> {
                        ableButton.setImage(Images.ButtonBackground);
                    });
                    ableButton.setScaleX(0.4);
                    ableButton.setScaleY(0.4);
                    Text ableButtonText = new Text();
                    if (mod.isEnabled()) {
                        ableButtonText.setText("Disable");
                    } else {
                        ableButtonText.setText("Enable");
                    }
                    ableButtonText.setStroke(Color.color(0.21875, 0.22265625, 0.23828125));
                    ableButtonText.setScaleX(1.4);
                    ableButtonText.setScaleY(1.4);
                    ableButtonText.setMouseTransparent(true);
                    ableButton.setTranslateX(200);
                    ableButton.setTranslateY(modOffset + 20);
                    ableButtonText.setTranslateX(200);
                    ableButtonText.setTranslateY(modOffset + 20);
                    //</editor-fold>

                    nodes.add(background);
                    nodes.add(name);
                    nodes.add(description);
                    nodes.add(ableButton);
                    nodes.add(ableButtonText);
                }
            } else {
                break;
            }
            i++;
        }
        this.scrollMin = -(heightOffset + padding);
        this.scrollMax = Math.min(0, -(allModifications.size() - 1) * (heightOffset + padding));

        //<editor-fold defaultstate="collapsed" desc="slider">
        ImageView slider = new ImageView(Images.Slider);
        slider.setScaleX(0.5);
        slider.setScaleY(0.5);
        slider.setTranslateX(295);
        double sliderY = Math.max(-107, offset - determineSliderOffset(height, scrollMin, scrollMax) + 18 - (height / 2) + (((scroll + scrollMin) / (scrollMax - scrollMin)) * (height - 20)));
        System.out.println(sliderY);
        slider.setTranslateY(sliderY);
        if (height >= scrollMax - scrollMin) {
            slider.disableProperty().set(true);
        } else {
            slider.disableProperty().set(false);
        }
        //</editor-fold>

        nodes.add(slider);

        return nodes;
    }

    private double determineSliderOffset(double height, double scrollMin, double scrollMax) {
        return ((2 * scrollMin) / (scrollMax - scrollMin)) * (height - 20);
    }

    public static long getLastModified() {
        return Math.max(getLastModifiedRecursive(new File(Constants.CONSTRUCT_FOLDER)) / 1000, GUI.lastChange.toEpochSecond(Constants.ZONE));
    }

    public static long getLastModifiedRecursive(File file) {
        if (file.isDirectory()) {
            long lastModified = file.lastModified();
            for (File subFile : file.listFiles()) {
                lastModified = Math.max(getLastModifiedRecursive(subFile), lastModified);
            }
            return lastModified;
        } else {
            return file.lastModified();
        }
    }

    public static void resetChangeTime() {
        GUI.lastChange = LocalDateTime.now();
    }

    private static void doChangeTimeDelay(Text nextChangeUpdateTime) throws InterruptedException {
        int iterations = 30;
        int totalDelay = 15000;

        int delay = totalDelay / (iterations + 1);
        int delayEnd = delay / 2;

        nextChangeUpdateTime.setText(getNextChangeUpdateTimeBar(0, iterations));
        Thread.sleep(delayEnd);
        for (int i = 0; i < iterations; i++) {
            nextChangeUpdateTime.setText(getNextChangeUpdateTimeBar(i, iterations));
            Thread.sleep(delay);
        }
        nextChangeUpdateTime.setText(getNextChangeUpdateTimeBar(iterations, iterations));
        Thread.sleep(delayEnd);
    }

    private static String getNextChangeUpdateTimeBar(int iteration, int iterations) {
        String result = "";
        for (int i = 0; i < iteration; i++) {
            result += "▓";
        }
        for (int i = 0; i < iterations - iteration; i++) {
            result += "░";
        }
        return result;
    }

}

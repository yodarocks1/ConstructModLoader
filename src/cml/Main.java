/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.beans.Profile;
import cml.apply.Apply;
import cml.apply.MergeChanges;
import cml.apply.ReplaceChanges;
import cml.beans.ModIncompatibilityException;
import cml.beans.Modification;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static javafx.application.Application.launch;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author benne
 */
public class Main {
    
    public static final File API_DIRECTORY = new File(".");
    public static String scrapMechanicFolder;
    public static String vanillaFolder;
    public static String modsFolder;

    public static List<Modification> activeModifications = new ArrayList();
    public static LocalDateTime lastChangeTime = LocalDateTime.MIN;

    public static ObjectProperty<List<Profile>> profileList = new SimpleObjectProperty(new ArrayList());
    public static ObjectProperty<Profile> activeProfile = new SimpleObjectProperty(Profile.EMPTY);

    public static File logFile;
    public static File errFile;
    public static File patFile;

    public static PrintStream patchOutputStream;

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Args: " + args.length);
        for (String arg : args) {
            System.out.println("Arg: " + arg);
        }
        getFolders();
        setLog();
        System.setOut(new PrintStream(new FileOutputStream(logFile)));
        System.setErr(new PrintStream(new FileOutputStream(errFile)));
        patchOutputStream = new PrintStream(new FileOutputStream(patFile));
        updateProfileList();
        System.out.println("Start2");
        launch(NewGUI.class, args);
    }

    private static void getFolders() {
        try {
            List<String> folders = Files.readAllLines(new File(Main.API_DIRECTORY.getAbsolutePath() + Constants.FOLDERS_LOCATION_RELATIVE).toPath());
            scrapMechanicFolder = folders.get(0);
            vanillaFolder = folders.get(1);
            modsFolder = folders.get(2);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Failed to read folder setting file. Setting to default");
            scrapMechanicFolder = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Scrap Mechanic\\";
            vanillaFolder = "C:\\Program Files (x86)\\Construct\\vanilla\\";
            modsFolder = "C:\\Program Files (x86)\\Construct\\mods\\";
        }
    }

    private static void setLog() {
        File logFolder = new File(modsFolder + Constants.LOG_FOLDER_NAME);
        File patchFolder = new File(modsFolder + Constants.PATCH_FOLDER_NAME);
        if (!logFolder.exists()) {
            try {
                Files.createDirectory(logFolder.toPath());
                Files.setAttribute(logFolder.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Could not create log folder", ex);
            }
        }
        if (!patchFolder.exists()) {
            try {
                Files.createDirectory(patchFolder.toPath());
                Files.setAttribute(patchFolder.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Could not create patch folder", ex);
            }
        }
        LocalDateTime time = LocalDateTime.now();
        logFile = new File(String.format("%s%s\\%04d-%02d-%02d+%02d;%02d;%02d-log.txt", modsFolder, Constants.LOG_FOLDER_NAME, time.getYear(), time.getMonthValue(), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond()));
        System.out.println("Log: " + logFile.getAbsolutePath());
        errFile = new File(String.format("%s%s\\%04d-%02d-%02d+%02d;%02d;%02d-err.txt", modsFolder, Constants.LOG_FOLDER_NAME, time.getYear(), time.getMonthValue(), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond()));
        System.out.println("Err: " + errFile.getAbsolutePath());
        patFile = new File(String.format("%s%s\\%04d-%02d-%02d+%02d;%02d;%02d.txt", modsFolder, Constants.PATCH_FOLDER_NAME, time.getYear(), time.getMonthValue(), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond()));
        System.out.println("Pat: " + patFile.getAbsolutePath());
    }

    public static void updateProfileList() {
        List<Profile> profiles = new ArrayList();
        File modsFile = new File(Main.modsFolder);
        File modsVerificationFile = new File(Main.modsFolder + Constants.LOG_FOLDER_NAME);
        if (modsFile.exists() && modsVerificationFile.exists()) {
            for (File proFile : modsFile.listFiles()) {
                if (proFile.isDirectory() && !(proFile.getName().startsWith(Constants.IGNORE_PREFIX) && proFile.getName().endsWith(Constants.IGNORE_SUFFIX))) {
                    profiles.add(new Profile(proFile));
                }
            }
            ErrorManager.removeStateCause("ModsFolder <INVALID>");
        } else {
            profiles = null;
            ErrorManager.addStateCause("ModsFolder <INVALID>");
        }
        activeProfile.setValue(Profile.EMPTY);
        profileList.setValue(profiles);
    }

    private static final List<String> SM_FOLDERS_CHECK = new ArrayList();
    private static final List<String> VANILLA_FOLDERS_CHECK = new ArrayList();

    static {
        SM_FOLDERS_CHECK.add("Cache");
        SM_FOLDERS_CHECK.add("ChallengeData");
        SM_FOLDERS_CHECK.add("Challenges");
        SM_FOLDERS_CHECK.add("Data");
        SM_FOLDERS_CHECK.add("Logs");
        SM_FOLDERS_CHECK.add("Release");
        SM_FOLDERS_CHECK.add("Survival");
        VANILLA_FOLDERS_CHECK.add("Data");
        VANILLA_FOLDERS_CHECK.add("Release");
        VANILLA_FOLDERS_CHECK.add("Survival");
    }

    public static void verifySMFolder() {
        File smFolder = new File(Main.scrapMechanicFolder);
        File exe = new File(Main.scrapMechanicFolder + "Release\\ScrapMechanic.exe");
        if (!smFolder.exists() || !Arrays.asList(smFolder.list()).containsAll(SM_FOLDERS_CHECK) || !exe.exists()) {
            ErrorManager.addStateCause("SMFolder <INVALID>");
        } else {
            ErrorManager.removeStateCause("SMFolder <INVALID>");
        }
    }

    public static void verifyVanillaFolder() {
        File smFolder = new File(Main.vanillaFolder);
        File exe = new File(Main.vanillaFolder + "Release\\ScrapMechanic.exe");
        if (!smFolder.exists() || !Arrays.asList(smFolder.list()).containsAll(VANILLA_FOLDERS_CHECK) || !exe.exists()) {
            ErrorManager.addStateCause("VanillaFolder <INVALID>");
        } else {
            ErrorManager.removeStateCause("VanillaFolder <INVALID>");
        }
    }

    public static void applyModifications() {
        Main.activeModifications = Main.activeProfile.get().getActiveModifications();
        Apply.apply(Main.activeModifications);
    }

    public static String[] checkForUpdate() {
        String releaseName = "";
        String releaseURL = "";
        try {
            URL url = new URL("https://api.github.com/repos/yodarocks1/ConstructModLoader/releases/latest");

            HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
            httpClient.setRequestMethod("GET");
            httpClient.setRequestProperty("name", "");
            System.out.println("Fetching data from Github (Code " + httpClient.getResponseCode() + ")");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {
                for (Object line : in.lines().toArray()) {
                    for (String sectionLong : line.toString().split(",")) {
                        String section = sectionLong.trim();
                        if (section.startsWith("\"html_url\":\"https://github.com/yodarocks1/ConstructModLoader/releases/tag")) {
                            releaseURL = section.substring(section.indexOf(":") + 1).replace("\"", "");
                            System.out.println(section);
                            System.out.println("  URL: " + releaseURL);
                        } else if (section.startsWith("\"tag_name\"")) {
                            releaseName = section.substring(section.indexOf(":") + 1).replace("\"", "");
                            System.out.println(section);
                            System.out.println("  Name: " + releaseName);
                        }
                    }
                }
            }
            httpClient.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Github URL given is invalid", ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "GET protocol is invalid", ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Could not connect to Github URL", ex);
        }

        if (releaseName.length() > 0) {
            if (releaseName.equalsIgnoreCase("V" + Constants.VERSION)) {
                return new String[]{"", ""};
            }
        }
        return new String[]{releaseName, releaseURL};
    }

    public static void update() {
        String url = checkForUpdate()[1];
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (URISyntaxException | IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Failed to open update with Desktop. Trying a workaround", ex);
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                } catch (IOException ex1) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Workaround failed", ex1);
                }
            }
        } else {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Desktop isn't supported. Trying a workaround");
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Workaround failed", ex);
            }
        }
    }
    
    public static void regenVanilla() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("(Re)-Generate Vanilla Folder");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Before continuing, make sure that the location for the vanilla folder that you have entered is the desired location.\n\n"
                + "NOTE: This process will continue in the background, regardless of the closure of Construct Mod Loader. Do not end this process or reboot your computer - it may corrupt your data.\n\n"
                + "This process can take between 15 minutes and an hour, depending on your internet connection and drive speed. If you have an exceptionally low-end computer, this may take even longer.");
        alert.showAndWait();
        if (!alert.getResult().getButtonData().isCancelButton()) {
            Thread thread = new Thread(Constants.REGEN_VANILLA);
            thread.start(); //This thread will not stop when the application closes because Steam validation would corrupt the game files.
        }
    }

    public static void downloadLatestRelease() {

    }

    public static void handleModIncompatibility(ModIncompatibilityException ex) {
        System.err.println(ex.getMessage() + "\n  Conflicts:");
        for (Modification offender : ex.getOffenders()) {
            System.err.println("    " + offender.getName());
        }
        Background background = new Background(new BackgroundFill(Color.color(0, 0, 0, 0), new CornerRadii(1.0), Insets.EMPTY));
        if (ex.isCertain()) {
            List<String> choices = ex.getOffenders().stream().map((offender) -> offender.getName()).collect(Collectors.toList());
            ChoiceDialog<String> choiceDialog = new ChoiceDialog("< None >", choices);
            choiceDialog.setTitle("Certain Incompatibility Handler");
            choiceDialog.initModality(Modality.APPLICATION_MODAL);
            choiceDialog.setHeaderText("Incompatibility Handler (Certainty: True)");
            choiceDialog.setContentText("Choose only one mod to leave enabled: ");
            choiceDialog.getDialogPane().getButtonTypes().removeAll(ButtonType.CANCEL);
            ((Stage) choiceDialog.getDialogPane().getScene().getWindow()).getIcons().add(Images.ICON_ERROR);
            choiceDialog.showAndWait();
            for (Modification offender : ex.getOffenders()) {
                if (offender.getName().equals(choiceDialog.getSelectedItem())) {
                    if (!offender.isEnabled()) {
                        offender.enable();
                    }
                } else if (offender.isEnabled()) {
                    offender.disable();
                }
            }
        } else {
            Dialog<List<CheckBox>> selectDialog = new Dialog();
            selectDialog.setTitle("Possible Incompatibility Handler");
            selectDialog.initModality(Modality.APPLICATION_MODAL);
            selectDialog.setHeaderText("Incompatibility Handler (Certainty: False)");
            selectDialog.getDialogPane().setBackground(background);
            selectDialog.getDialogPane().getScene().setFill(null);
            ((Stage) selectDialog.getDialogPane().getScene().getWindow()).getIcons().add(Images.ICON_ERROR);
            ListView checkList = new ListView();
            List<CheckBox> checkBoxes = new ArrayList();
            for (Modification offender : ex.getOffenders()) {
                CheckBox checkBox = new CheckBox();
                checkBox.setText(offender.getName());
                checkBox.setTextFill(Color.BLACK);
                checkBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue) {
                        offender.disable();
                        checkBox.setText(offender.getName() + "  <-- Disable");
                    } else {
                        offender.enable();
                        checkBox.setText(offender.getName());
                    }
                });
                checkBoxes.add(checkBox);
            }
            checkList.getItems().addAll(checkBoxes);
            ButtonType cont = new ButtonType("Continue", ButtonData.OK_DONE);
            selectDialog.getDialogPane().getButtonTypes().setAll(cont);
            Text contentText = new Text("These mods all modify a file that failed to compile. Please disable one or more until compilation succeeds.");
            GridPane group = new GridPane();
            group.add(contentText, 0, 0);
            group.add(checkList, 0, 1);
            group.setVgap(10);
            selectDialog.getDialogPane().setContent(group);
            selectDialog.showAndWait();
        }
        Main.activeModifications = Main.activeProfile.get().getActiveModifications();
        MergeChanges.modifiedBy.clear();
        ReplaceChanges.replacedBy.clear();
    }

    public static void launchGame() {
        Main.applyModifications();
        if (Boolean.valueOf(System.getProperty("olaunch", "true"))) {
            System.out.println("Launching Scrap Mechanic");
            try {
                Runtime.getRuntime().exec(Constants.LAUNCH_COMMAND);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to launch Scrap Mechanic", ex);
            }
        } else {
            System.out.println("Not launching Scrap Mechanic - Dolaunch is set to false");
        }
    }

    public static void openLogs() {
        try {
            Runtime.getRuntime().exec("explorer.exe /select," + Main.logFile.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to open Log folder at " + Main.logFile.getAbsolutePath(), ex);
        }
    }

}

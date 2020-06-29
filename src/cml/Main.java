/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.apply.Apply;
import cml.apply.MergeChanges;
import cml.apply.ReplaceChanges;
import cml.beans.Profile;
import cml.beans.ModIncompatibilityException;
import cml.beans.Modification;
import cml.gui.console.LogHandler;
import cml.lib.files.AFileManager;
import cml.lib.files.ZipManager;
import cml.lib.registry.hardcoded.Steam;
import cml.lib.workshop.WorkshopConnectionHandler;
import cml.lib.workshop.WorkshopReader;
import com.sun.javafx.application.LauncherImpl;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
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

    protected static final Logger LOGGER;

    public static final File API_DIRECTORY = new File(".");
    public static String scrapMechanicFolder = null;
    public static String vanillaFolder = null;
    public static String modsFolder = null;
    public static String workshopFolder = null;
    public static ObjectProperty<WorkshopReader> workshopReader = new SimpleObjectProperty(null);

    public static List<Modification> activeModifications = new ArrayList();
    public static LocalDateTime lastChangeTime = LocalDateTime.MIN;

    public static ObjectProperty<List<Profile>> profileList = new SimpleObjectProperty(new ArrayList());
    public static ObjectProperty<Profile> activeProfile = new SimpleObjectProperty(Profile.EMPTY);

    public static boolean showConsole = true;
    public static File patFile;
    public static final LogHandler LOG_HANDLER = new LogHandler(Level.ALL);
    public static final FileHandler FILE_HANDLER;

    public static PrintStream patchOutputStream;

    private static final List<String> SM_FOLDERS_CHECK = new ArrayList();

    static {
        SM_FOLDERS_CHECK.add("Cache");
        SM_FOLDERS_CHECK.add("ChallengeData");
        SM_FOLDERS_CHECK.add("Challenges");
        SM_FOLDERS_CHECK.add("Data");
        SM_FOLDERS_CHECK.add("Logs");
        SM_FOLDERS_CHECK.add("Release");
        SM_FOLDERS_CHECK.add("Survival");
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s%6$s - %2$s%n");
        LOGGER = Logger.getLogger(Main.class.getName());
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.CONFIG);
        rootLogger.addHandler(LOG_HANDLER);

        Logger cmlLogger = Logger.getLogger("cml");
        cmlLogger.setUseParentHandlers(false);
        cmlLogger.setLevel(Level.ALL);
        cmlLogger.addHandler(LOG_HANDLER);

        FileHandler handler = null;
        try {
            handler = new FileHandler("heavy_log.xml");
            handler.setFormatter(new XMLFormatter());
            rootLogger.addHandler(handler);
            cmlLogger.addHandler(handler);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Could not create File Handler", ex);
        }
        FILE_HANDLER = handler;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LauncherImpl.launchApplication(GUI.class, SplashScreenLoader.class, args);
    }

    public static void mainSetup() {
        LOGGER.log(Level.INFO, "--    Main  setup    --");
        LOGGER.log(Level.CONFIG, "CML Version: V{0}", Constants.VERSION);
        LOGGER.log(Level.CONFIG, "Operating System: {0} [{1}]", new Object[]{System.getProperty("os.name"), System.getProperty("os.version")});
        LOGGER.log(Level.CONFIG, "Architecture: {0}", System.getProperty("os.arch"));
        LOGGER.log(Level.CONFIG, "Available Processors: {0}", Runtime.getRuntime().availableProcessors());
        LOGGER.log(Level.CONFIG, "Free Heap Memory (bytes): {0}", Runtime.getRuntime().freeMemory());
        LOGGER.log(Level.CONFIG, "Max Heap Memory (bytes): {0}", Runtime.getRuntime().maxMemory());
        LOGGER.log(Level.CONFIG, "Total Heap Memory (bytes): {0}", Runtime.getRuntime().totalMemory());
        LOGGER.log(Level.CONFIG, "Logger Format: {0}", System.getProperty("java.util.logging.SimpleFormatter.format", "<ABSENT>"));
        LOGGER.log(Level.CONFIG, "API Directory: {0}", Main.API_DIRECTORY.getAbsolutePath());
        System.out.println("System.out test");
        System.err.println("System.err test");
        LOGGER.log(Level.CONFIG, "Getting folders");
        getFolders();
        LOGGER.log(Level.INFO, "Setting logs");
        setLog();
        LOGGER.log(Level.INFO, "Updating profile list");
        updateProfileList();
        LOGGER.log(Level.INFO, "-- GUI initalization --");
    }

    /**
     * Reads through folders.txt to determine the default/preset values of the
     * various folders.
     */
    private static void getFolders() {
        File foldersTxt = new File(Main.API_DIRECTORY.getAbsolutePath() + Constants.FOLDERS_LOCATION_RELATIVE);
        try {
            //Read the folders
            List<String> folders = Files.readAllLines(foldersTxt.toPath());
            if (folders.stream().filter((folder) -> folder.matches("^null/{0,1}$")).findAny().isPresent()) {
                throw new IOException();
            }
            scrapMechanicFolder = endSlash(folders.get(0));
            vanillaFolder = endSlash(folders.get(1));
            modsFolder = endSlash(folders.get(2));
            setWorkshopFolder(folders.get(3));
        } catch (IOException ex) {
            //On failed read (File doesn't exist), set to default
            LOGGER.log(Level.WARNING, "Failed to read folder setting file. Setting to default");
            String[] defaults = getDefaults();
            scrapMechanicFolder = defaults[0];
            vanillaFolder = defaults[1];
            modsFolder = defaults[2];
            setWorkshopFolder(defaults[3]);
        } catch (IndexOutOfBoundsException ex) {
            //Since there were too few folders given in the folders setting file,
            //we need to go through and set the nulls to their defaults.
            LOGGER.log(Level.WARNING, "One or more folders was/were not defined in the folder setting file. Setting nulls to default");
            String[] defaults = getDefaults();
            scrapMechanicFolder = (scrapMechanicFolder == null ? defaults[0] : scrapMechanicFolder);
            vanillaFolder = (vanillaFolder == null ? defaults[1] : vanillaFolder);
            modsFolder = (modsFolder == null ? defaults[2] : modsFolder);
            setWorkshopFolder(workshopFolder == null ? workshopFromGamePath(new File(scrapMechanicFolder)) : workshopFolder);
        }
    }

    public static void setWorkshopFolder(String workshopFolder) {
        Main.workshopFolder = endSlash(workshopFolder);
        if (Main.verifyWorkshopFolder()) {
            Main.workshopReader.setValue(new WorkshopReader(new File(workshopFolder)));
        }
    }

    public static void openLink(String url) {
        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open URL " + url, ex);
        }
    }

    /**
     * Determines and returns default folder locations
     *
     * @return Default folder locations
     */
    private static String[] getDefaults() {
        String[] defaults = new String[4];
        File smFolder = Steam.findGamePath("Scrap Mechanic");
        defaults[0] = endSlash(smFolder.getAbsolutePath());
        defaults[1] = "C:\\Program Files (x86)\\Construct\\vanilla\\";
        defaults[2] = "C:\\Program Files (x86)\\Construct\\mods\\";
        defaults[3] = workshopFromGamePath(smFolder);
        return defaults;
    }

    /**
     * Determines the workshop location based upon the location of the Scrap
     * Mechanic folder
     *
     * @param smFolder Path to the Scrap Mechanic folder
     * @return Path to the Scrap Mechanic Workshop directory
     */
    private static String workshopFromGamePath(File smFolder) {
        return endSlash(smFolder.getParentFile().getParentFile().getAbsolutePath()) + "workshop\\content\\387990\\";
    }

    /**
     * Makes sure a folder path ends with a slash (respects forwards vs
     * backwards slashes)
     *
     * @param in The folder path to end with a slash
     * @return The folder path, with assuredness that it ends in a slash
     */
    public static String endSlash(String in) {
        return ((in.endsWith("\\") || in.endsWith("/"))
                ? in
                : (in.contains("/")
                ? in + "/"
                : in + "\\"));
    }

    /**
     * Sets up the patch write file<br>
     * Pat: {@code patchFolder/YYYY-MM-DD+HH;MM;SS.txt}<br>
     */
    private static void setLog() {
        File patchFolder = new File(modsFolder, Constants.PATCH_FOLDER_NAME);
        if (!patchFolder.exists()) {
            try {
                Files.createDirectory(patchFolder.toPath());
                Files.setAttribute(patchFolder.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not create patch folder", ex);
            }
        }
        LocalDateTime time = LocalDateTime.now();
        patFile = new File(String.format("%s%s\\%04d-%02d-%02d+%02d;%02d;%02d.txt", endSlash(modsFolder), Constants.PATCH_FOLDER_NAME, time.getYear(), time.getMonthValue(), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond()));
        LOGGER.log(Level.INFO, "Pat: {0}", patFile.getAbsolutePath());
        patFile.createNewFile();
        patchOutputStream = new PrintStream(new FileOutputStream(patFile));
    }

    public static void createProfile(String profileName) {
        LOGGER.log(Level.INFO, "Creating new profile \"{0}\"", profileName);
        File newProfile = new File(Main.modsFolder + profileName);
        if (newProfile.exists()) {
            LOGGER.log(Level.SEVERE, "Could not create new profile \"{0}\" - a profile by that name already exists.", profileName);
            return;
        }

        File newDescriptionFile = new File(newProfile, "description.txt");
        try {
            Files.createDirectory(newProfile.toPath());
            Files.write(newDescriptionFile.toPath(), "<<Default Description>> - Please edit description.txt".getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not create new profile \"" + profileName + "\"", ex);
        }
        Main.updateProfileList();
    }

    public static void updateProfileList() {
        List<Profile> profiles = new ArrayList();
        File modsFile = new File(Main.modsFolder);
        File modsVerificationFile = new File(Main.modsFolder + Constants.LOG_FOLDER_NAME);
        if (modsFile.exists() && modsVerificationFile.exists()) {
            for (File zipFile : modsFile.listFiles(ZipManager.ZIP_FILTER)) {
                if (zipFile.isFile()) {
                    AFileManager.ZIP_MANAGER.unzip(zipFile, new File(modsFile, zipFile.getName().replace(".zip", "").replace('.', ' ')));
                    AFileManager.FILE_MANAGER.delete(zipFile);
                }
            }
            for (File proFile : modsFile.listFiles()) {
                if (proFile.isDirectory() && !(proFile.getName().startsWith(Constants.IGNORE_PREFIX) && proFile.getName().endsWith(Constants.IGNORE_SUFFIX))) {
                    Profile profile = new Profile(proFile);
                    WorkshopConnectionHandler.reconnectIfConnected(workshopFolder, profile.getModifications().toArray(new Modification[0]));
                    profiles.add(profile);
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

    public static boolean verifySMFolder() {
        File smFolder = new File(Main.scrapMechanicFolder);
        File exe = new File(Main.scrapMechanicFolder, "Release/ScrapMechanic.exe");
        if (!smFolder.exists() || !Arrays.asList(smFolder.list()).containsAll(SM_FOLDERS_CHECK) || !exe.exists()) {
            ErrorManager.addStateCause("SMFolder <INVALID>");
            return false;
        } else {
            ErrorManager.removeStateCause("SMFolder <INVALID>");
            return true;
        }
    }

    public static boolean verifyWorkshopFolder() {
        File wsFolder = new File(Main.workshopFolder);
        boolean verified = false;
        if (wsFolder.exists()) {
            verified = true;
            for (String subFile : wsFolder.list()) {
                if (!subFile.matches("^[0-9]*$")) {
                    verified = false;
                    break;
                }
            }
        }
        if (!verified) {
            ErrorManager.addStateCause("WorkshopFolder <INVALID>");
            return false;
        } else {
            ErrorManager.removeStateCause("WorkshopFolder <INVALID>");
            return true;
        }
    }

    public static void applyModifications() {
        Main.activeModifications = Main.activeProfile.get().getActiveModifications();
        Apply.apply(Main.activeModifications);
    }

    public static void downloadLatestRelease() {

    }

    public static void handleModIncompatibility(ModIncompatibilityException ex) {
        LOGGER.log(Level.SEVERE, "{0}\n  Conflicts:", ex.getMessage());
        ex.getOffenders().forEach((offender) -> {
            LOGGER.log(Level.SEVERE, "    {0}", offender.getName());
        });
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
            LOGGER.log(Level.INFO, "Launching Scrap Mechanic");
            try {
                Runtime.getRuntime().exec(Constants.LAUNCH_COMMAND);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to launch Scrap Mechanic", ex);
            }
        } else {
            LOGGER.log(Level.INFO, "Not launching Scrap Mechanic - Dolaunch is set to false");
        }
    }

    public static void openLogs() {
        File logFile = new File(Main.API_DIRECTORY, "heavy_log.xml");
        try {
            Runtime.getRuntime().exec("explorer.exe /select," + logFile.getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to open Log folder at " + logFile.getAbsolutePath(), ex);
        }
    }

}

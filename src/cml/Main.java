/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.beans.Profile;
import cml.apply.Apply;
import cml.beans.Modification;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.Application.launch;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author benne
 */
public class Main {

    public static String scrapMechanicFolder;
    public static String vanillaFolder;
    public static String modsFolder;

    public static List<Modification> activeModifications = new ArrayList();
    public static LocalDateTime lastChangeTime = LocalDateTime.MIN;

    public static ObjectProperty<Profile> activeProfile = new SimpleObjectProperty(Profile.EMPTY);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //GUI.reloadMods();
        getFolders();
        System.out.println("Start2");
        //lastChangeTime = LocalDateTime.ofEpochSecond(getLastModified(), 0, Constants.ZONE);
        launch(NewGUI.class, args);
    }

    private static void getFolders() {
        try {
            List<String> folders = Files.readAllLines(new File(Constants.FOLDERS_LOCATION).toPath());
            scrapMechanicFolder = folders.get(0);
            vanillaFolder = folders.get(1);
            modsFolder = folders.get(2);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Failed to read folder file. Setting to default");
            scrapMechanicFolder = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Scrap Mechanic\\";
            vanillaFolder = "C:\\Program Files (x86)\\Construct\\vanilla\\";
            modsFolder = "C:\\Program Files (x86)\\Construct\\mods\\";
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
                    for (String section : line.toString().split(",")) {
                        String[] split = section.split(":");
                        System.out.print(split[0] + " - ");
                        switch (split[0]) {
                            case "\"html_url\"":
                                if (releaseURL.length() == 0) {
                                    releaseURL = split[1].replace("\"", "");
                                }
                                System.out.println("html_url");
                                break;
                            case "\"tag_name\"":
                                if (releaseName.length() == 0) {
                                    releaseName = split[1].replace("\"", "");
                                }
                                System.out.println("name");
                                break;
                            default:
                                System.out.println("X");
                                break;
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
            System.out.println("Latest release: " + releaseName);
            if (releaseName.equalsIgnoreCase("V+" + Constants.VERSION)) {
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
                return;
            } catch (URISyntaxException | IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Failed to open update with Dektop. Trying a workaround", ex);
            }
        } else {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Desktop isn't supported. Trying a workaround");
        }
        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Workaround failed", ex);
        }
    }

    public static void triggerModIncompatibility() {

    }

    public static void launchGame() {
        Main.applyModifications();
        if (Boolean.valueOf(System.getProperty("olaunch", "true"))) {
            try {
                Runtime.getRuntime().exec(Constants.LAUNCH_COMMAND);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to launch Scrap Mechanic", ex);
            }
        }
    }

}

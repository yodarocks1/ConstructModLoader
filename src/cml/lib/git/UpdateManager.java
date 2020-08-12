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
package cml.lib.git;

import cml.Constants;
import static cml.Main.openLink;
import cml.lib.files.AFileManager;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class UpdateManager {
    
    private static final Logger LOGGER = Logger.getLogger(UpdateManager.class.getName());
    
    private static final String UPDATE_ASSET_URL = "https://github.com/yodarocks1/ConstructModLoader/releases/latest/download/UpdateAssets.zip";

    private static String[] update = null;

    public static String[] checkForUpdate() {
        String releaseName = "";
        String releaseURL = "";
        try {
            URL url = new URL("https://api.github.com/repos/yodarocks1/ConstructModLoader/releases/latest");

            HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
            httpClient.setRequestMethod("GET");
            httpClient.setRequestProperty("name", "");
            LOGGER.log(Level.INFO, "Fetching data from Github (Code {0})", httpClient.getResponseCode());
            try (BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {
                for (Object line : in.lines().toArray()) {
                    for (String sectionLong : line.toString().split(",")) {
                        String section = sectionLong.trim();
                        if (section.startsWith("\"html_url\":\"https://github.com/yodarocks1/ConstructModLoader/releases/tag")) {
                            releaseURL = section.substring(section.indexOf(":") + 1).replace("\"", "");
                            LOGGER.log(Level.INFO, "  URL: {0}", releaseURL);
                        } else if (section.startsWith("\"tag_name\"")) {
                            releaseName = section.substring(section.indexOf(":") + 1).replace("\"", "");
                            LOGGER.log(Level.INFO, "  Name: {0}", releaseName);
                        }
                    }
                }
            }
            httpClient.disconnect();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Github URL given is invalid", ex);
        } catch (ProtocolException ex) {
            LOGGER.log(Level.SEVERE, "GET protocol is invalid", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not connect to Github URL", ex);
        }

        if (releaseName.length() > 0) {
            if (releaseName.equalsIgnoreCase("V" + Constants.VERSION)) {
                return new String[]{"", ""};
            }
        }
        update = new String[]{releaseName, releaseURL};
        return update;
    }

    /**
     * 
     * @param outputLocation Output .zip file
     * @return Whether the update was successfully downloaded
     */
    public static boolean update(File outputLocation) {
        try {
            URL url = new URL(UPDATE_ASSET_URL);
            HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
            httpClient.setRequestMethod("GET");
            httpClient.setInstanceFollowRedirects(true);
            httpClient.addRequestProperty("Accept", "application/octet-stream");
            LOGGER.log(Level.INFO, "Fetching Update Assets from Github (Code {0})", httpClient.getResponseCode());
            try (BufferedInputStream in = new BufferedInputStream(httpClient.getInputStream()); FileOutputStream out = new FileOutputStream(outputLocation)) {
                byte[] buffer = new byte[1024];
                while (in.read(buffer) > 0) {
                    out.write(buffer);
                }
            }
            AFileManager.ZIP_MANAGER.unzip(outputLocation, outputLocation.getParentFile());
            LOGGER.log(Level.INFO, "Update has been successfully downloaded.");
            return true;
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Update Asset URL is invalid: " + UPDATE_ASSET_URL, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to download Update Asset from Github", ex);
        }
        return false;
    }
    
    public static void openUpdate() {
        openLink(update[1]);
    }
}

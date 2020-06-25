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
package cml.lib.steam.verify;

import cml.Constants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
public class SteamVerifier {

    private static final Logger LOGGER = Logger.getLogger(SteamVerifier.class.getName());
    
    private static boolean verificationInProgress = false;

    public boolean verify() {
        if (!verificationInProgress) {
            verificationInProgress = true;
            LOGGER.log(Level.FINE, "Beginning Scrap Mechanic file verification");
            try {
                Runtime.getRuntime().exec(Constants.VERIFY_COMMAND);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to verify Scrap Mechanic files", ex);
                verificationInProgress = false;
                return false;
            }
            LocalDateTime startTime = LocalDateTime.now();
            while (verificationInProgress) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.WARNING, "Steam file verification interruption attempted");
                }
                if (validatorIsDone()) {
                    LOGGER.log(Level.FINE, "Scrap Mechanic took {0} seconds to verify.", startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS) / 1000.0);
                    verificationInProgress = false;
                }
                if (Thread.interrupted()) {
                    verificationInProgress = false;
                    LOGGER.log(Level.WARNING, "Could not complete verification - The thread was interrupted");
                    return false;
                }
            }
            return true;
        } else {
            LOGGER.log(Level.WARNING, "Could not verify - verification is already in progress.");
            return false;
        }
    }

    private boolean validatorIsDone() {
        String output = "";
        try {
            String line;
            Process p = Runtime.getRuntime().exec("tasklist.exe /fi \"WINDOWTITLE eq Validating Steam Files - 100% complete\" /fo csv /nh");
            p.waitFor();
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                while ((line = input.readLine()) != null) {
                    output += line + "\n";
                }
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        if (output.startsWith("\"steam.exe\"")) {
            try {
                Runtime.getRuntime().exec(Constants.KILL_VERIFY_COMMAND.replace("<<id>>", output.split(",")[1].replace("\"", "")));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not kill validator process with data: " + output, ex);
            }
            return true;
        }
        return false;
    }

}

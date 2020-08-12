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
package cml.lib.url;

import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import cml.lib.threadmanager.ThreadManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author benne
 */
public class SingleInstanceService {

    private static final Logger LOGGER = Logger.getLogger(SingleInstanceService.class.getName());

    private final File lckFile;
    private Scanner lckInput;

    public final StringProperty argsProperty = new SimpleStringProperty(null);

    public SingleInstanceService(File lckFile) {
        this.lckFile = lckFile;
    }

    public boolean start(String[] args) {
        if (lckFile.exists()) {
            AFileManager.FILE_MANAGER.write(lckFile, Arrays.stream(args).collect(Collectors.joining(" ")), FileOptions.REPLACE);
            LOGGER.log(Level.INFO, "Application already running - passing args to running instance");
            System.out.println("Application already running - passing args to running instance");
            System.exit(0);
        } else {
            try {
                lckFile.createNewFile();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> lckFile.delete()));
                lckFile.deleteOnExit();
                argsProperty.set(Arrays.stream(args).collect(Collectors.joining(" ")));
                startThread();
                return true;
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to obtain instance lock", ex);
                System.out.println("Failed to obtain instance lock");
            }
        }
        return false;
    }

    private void startThread() {
        try {
            lckInput = new Scanner(Files.newInputStream(lckFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open input stream for instance lock", ex);
            return;
        }
        @SuppressWarnings("SleepWhileInLoop")
        Thread argCheckThread = new Thread(() -> {
            boolean interrupted = false;
            while (!Thread.interrupted() && !interrupted) {
                checkForArgs();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
            lckInput.close();
        });
        ThreadManager.addThread(argCheckThread);
        argCheckThread.start();
    }

    private void checkForArgs() {
        if (lckFile.length() > 0) {
            while (lckInput.hasNextLine()) {
                argsProperty.set(lckInput.nextLine());
            }
            AFileManager.FILE_MANAGER.write(lckFile, new byte[0], FileOptions.REPLACE);
        }
    }
}

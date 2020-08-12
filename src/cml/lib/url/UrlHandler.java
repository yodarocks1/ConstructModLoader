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

import cml.gui.popup.CmlPopover;
import cml.lib.threadmanager.ThreadManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.stage.Window;

/**
 *
 * @author benne
 */
public class UrlHandler {

    private static final Logger LOGGER = Logger.getLogger(UrlHandler.class.getName());

    public static boolean handle(String url, Window popupOwner) {
        System.out.println("New URL: " + url);
        if (!url.toLowerCase().startsWith("cml://")) {
            return false;
        }
        String[] args = url.substring(6).split("/");
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "download":
                    try {
                        Runtime.getRuntime().exec("cmd /c cd steamcmd & CMLDownloadTool.exe" + Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).map(String::toLowerCase).collect(Collectors.joining(" ", " ", "")));
                        switch (args.length) {
                            case 1:
                                CmlPopover.popover(popupOwner, CmlPopover.PopoverType.CHECK, CmlPopover.PopoverColor.GRAY, "Opened download tool", 4, TimeUnit.SECONDS);
                                break;
                            case 2:
                                if (args[1].equalsIgnoreCase("smm") || args[1].equalsIgnoreCase("steam")) {
                                    CmlPopover.popover(popupOwner, CmlPopover.PopoverType.CHECK, CmlPopover.PopoverColor.GRAY, "Opened " + args[1] + " download tool", 4, TimeUnit.SECONDS);
                                } else {
                                    CmlPopover.popover(popupOwner, CmlPopover.PopoverType.CHECK, CmlPopover.PopoverColor.GRAY, "Opened download tool for ID ", args[1], "", 4, TimeUnit.SECONDS);
                                }
                                break;
                            case 3:
                                if (args[1].equalsIgnoreCase("smm") || args[1].equalsIgnoreCase("steam")) {
                                    CmlPopover.popover(popupOwner, CmlPopover.PopoverType.CHECK, CmlPopover.PopoverColor.GRAY, "Downloading ID ", args[2], " from " + args[1], 4, TimeUnit.SECONDS);
                                } else {
                                    CmlPopover.popover(popupOwner, CmlPopover.PopoverType.CHECK, CmlPopover.PopoverColor.GRAY, "Opened download tool for IDs [", args[1] + " " + args[2], "]", 4, TimeUnit.SECONDS);
                                }
                                break;
                            default:
                                if (args[1].equalsIgnoreCase("smm")) {
                                    CmlPopover.popover(popupOwner, CmlPopover.PopoverType.CHECK, CmlPopover.PopoverColor.GRAY, "Downloading ID ", args[2], " from SMM", 4, TimeUnit.SECONDS);
                                } else if (args[1].equalsIgnoreCase("steam")) {
                                    CmlPopover.popover(popupOwner, CmlPopover.PopoverType.CHECK, CmlPopover.PopoverColor.GRAY, "Downloading ID ", args[2], " from Steam App" + args[3], 4, TimeUnit.SECONDS);
                                }
                                break;
                        }
                        return true;
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Could not open download tool", ex);
                    }
                    break;
            }
        }
        return false;
    }

}

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
package cml.lib.workshop;

import cml.gui.workshop.WorkshopModData;
import cml.lib.files.AFileManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author benne
 */
public class WorkshopMod {

    private static final Logger LOGGER = Logger.getLogger(WorkshopMod.class.getName());
    public static final String CML_MOD_TYPE = "cml";

    private File directory;
    private final int workshopId;
    private String description;
    private String name;
    private String type;
    private File preview;
    private boolean CMLMod = false;
    private boolean applicable = true;
    public final WorkshopModData dataView;

    public WorkshopMod(File directory) {
        this.directory = directory;
        this.workshopId = Integer.valueOf(directory.getName());
        AFileManager.FILE_MANAGER.readList(new File(directory.getAbsolutePath(), "description.json")).stream().map((jsonLine) -> jsonLine.replace(",", "").trim()).forEachOrdered((jsonLine) -> {
            String[] jsonLineSplit = jsonLine.split(":");
            if (jsonLine.startsWith("\"description\"")) {
                this.description = jsonLineSplit[1].trim().replace("\"", "").replace("\\n", "\n").replace("#FFFFFF", "");
            } else if (jsonLine.startsWith("\"name\"")) {
                this.name = jsonLineSplit[1].trim().replace("\"", "") + " (W#" + workshopId + ")";
            } else if (jsonLine.startsWith("\"type\"")) {
                this.type = jsonLineSplit[1].replace("\"", "").trim();
                if (type.equalsIgnoreCase(CML_MOD_TYPE)) {
                    this.CMLMod = true;
                }
            } else if (jsonLine.startsWith("\"cml\"")) {
                this.CMLMod = Boolean.valueOf(jsonLineSplit[1].replace("\"", ""));
            }
        });
        this.preview = new File(directory, "preview.png");
        if (!preview.exists()) {
            this.preview = new File(directory, "preview.jpg");
            if (!preview.exists()) {
                File[] files = directory.listFiles((File dir, String fileName) -> fileName.endsWith(".png") || fileName.endsWith(".jpg"));
                if (files.length > 0) {
                    this.preview = files[0];
                }
            }
        }
        this.dataView = new WorkshopModData(this);
    }

    private WorkshopMod(File directory, int id) {
        this.directory = directory;
        this.workshopId = id;
        this.applicable = false;
        this.dataView = null;
    }
    
    private WorkshopMod(File directory, int id, String type) {
        this.directory = directory;
        this.type = type;
        this.workshopId = id;
        this.applicable = false;
        this.dataView = null;
    }

    public static WorkshopMod createSafely(File directory) {
        if (!directory.exists()) {
            return null;
        }
        try {
            List<String> fileList = Arrays.asList(directory.list());
            if (fileList.contains("blueprint.json")) {
                return new WorkshopMod(directory, Integer.valueOf(directory.getName()));
            }
            if (fileList.contains("description.json")) {
                try {
                    String descriptionType = Files.readAllLines(new File(directory, "description.json").toPath())
                            .stream().filter((line) -> line.replace(" ", "").replace("\t", "").startsWith("\"type\":\""))
                            .map((typeLine) -> typeLine.replace("\"type\":", "").replace("\"", "")).collect(Collectors.joining());
                    switch (descriptionType) {
                        case "Tile":
                        case "TerrainAssets":
                        case "ChallengePack":
                            return new WorkshopMod(directory, Integer.valueOf(directory.getName()), descriptionType);
                        default:
                            return new WorkshopMod(directory);
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not read description.json for mod #" + directory.getName(), ex);
                }
            }
            return new WorkshopMod(directory, Integer.valueOf(directory.getName()));
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, "Supplied directory is not a workshop mod", ex);
            return null;
        }
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public int getWorkshopId() {
        return workshopId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public File getPreview() {
        return preview;
    }

    public void setPreview(File preview) {
        this.preview = preview;
    }

    public boolean isCMLMod() {
        return CMLMod;
    }

    public void setCMLMod(boolean CMLMod) {
        this.CMLMod = CMLMod;
    }

    public boolean isApplicable() {
        return applicable;
    }
    
    public String getApplicability() {
        if (!applicable) {
            return "Unsupported";
        }
        if (CMLMod) {
            return "Fully Supported";
        }
        if (directory.listFiles(WorkshopConverter.MANUAL_INSTALL_INDICATOR_FILTER).length > 0) {
            return "Almost Certainly";
        }
        return "Unlikely, but Unknown";
    }

    public void setApplicable(boolean applicable) {
        this.applicable = applicable;
    }

}

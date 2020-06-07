/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.Main;
import cml.beans.ModIncompatibilityException;
import cml.beans.Modification;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javax.imageio.ImageIO;

/**
 *
 * @author benne
 */
public class Apply {

    private static final List<IApplicator> APPLICATORS = new ArrayList();
    private static int handlingIncompatibilities = 0;

    static {
        APPLICATORS.add(new MergeChanges());
        APPLICATORS.add(new ReplaceChanges());
        APPLICATORS.add(new ApplyCraftingRecipes());
        APPLICATORS.add(new ApplyObjects());
    }

    public static void apply(List<Modification> activeModifications) {
        System.out.println("\n--Apply.apply()--\nChecking for incompatibilities");
        for (IApplicator applicator : APPLICATORS) {
            applicator.setModifications(activeModifications);
        }
        ModIncompatibilityException[] exceptions = ModIncompatibilityException.compileIncompatibilities();
        if (exceptions.length == 0) {
            System.out.println("No incompatibilities detected.\nApplying modifications");
            for (IApplicator applicator : APPLICATORS) {
                applicator.apply();
            }
            System.out.println("Modifications applied");
        } else {
            System.out.println("Incompatibilities were detected.\nHandling incompatibilities");
            for (ModIncompatibilityException ex : exceptions) {
                handlingIncompatibilities++;
                Platform.runLater(() -> {
                    Main.handleModIncompatibility(ex);
                    handlingIncompatibilities--;
                });
            }
            while (handlingIncompatibilities > 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Apply.class.getName()).log(Level.INFO, "Interrupted while waiting for FX thread to handle incompatibilities.");
                }
            }
            System.out.println("Incompatibilities handled.\nRe-starting application process");
            apply(Main.activeModifications);
        }
    }

    public static String readFile(Path path, OpenOption... options) throws IOException {
        return Apply.readFileLines(path, options).stream().collect(Collectors.joining("\n"));
    }

    public static List<String> readFileLines(Path path, OpenOption... options) throws IOException {
        return Files.readAllLines(path);
    }

    public static Path writeFile(Path path, String toWrite, OpenOption... options) throws IOException {
        if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
            return Files.write(path, toWrite.getBytes(), options);
        } else {
            System.out.println("\t\t-DoModify is false; No modification made in Apply.writeFile()");
            return path;
        }
    }
    
    public static boolean writeImage(Path path, RenderedImage image, String formatName) throws IOException {
        if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
            return ImageIO.write(image, formatName, path.toFile());
        } else {
            System.out.println("\t\t-DoModify is false; No modification made in Apply.writeImage()");
            return true;
        }
    }

    public static Path createFile(Path path) throws IOException {
        if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
            createDirectories(path.getParent());
            return Files.createFile(path);
        } else {
            System.out.println("\t\t-DoModify is false; No modification made in Apply.createFile()");
            return path;
        }
    }

    public static Path createDirectories(Path path) throws IOException {
        if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
            return Files.createDirectories(path);
        } else {
            System.out.println("\t\t-DoModify is false; No modification made in Apply.createDirectories()");
            return path;
        }
    }

    public static void deleteFile(Path path) throws IOException {
        if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
            Files.delete(path);
        } else {
            System.out.println("\t\t-DoModify is false; No modification made in Apply.deleteFile()");
        }
    }

    public static void deleteDirectory(Path path) throws IOException {
        if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
            for (File subFile : path.toFile().listFiles()) {
                if (subFile.isDirectory()) {
                    deleteDirectory(subFile.toPath());
                } else {
                    deleteFile(subFile.toPath());
                }
            }
            deleteFile(path);
        } else {
            System.out.println("\t\t-DoModify is false; No modification made in Apply.deleteDirectory()");
        }
    }

    public static void delete(Path path) throws IOException {
        if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
            if (path.toFile().isDirectory()) {
                deleteDirectory(path);
            } else {
                deleteFile(path);
            }
        } else {
            System.out.println("\t\t-DoModify is false; No modification made in Apply.delete()");
        }
    }

    public static Path copyFile(Path from, Path to, CopyOption... options) throws IOException {
        if (Boolean.valueOf(System.getProperty("omodify", "true"))) {
            return Files.copy(from, to, options);
        } else {
            System.out.println("\t\t-DoModify is false; No modification made in Apply.copyFile()");
            return to;
        }
    }

}

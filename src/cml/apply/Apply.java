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

    private static final Logger LOGGER = Logger.getLogger(Apply.class.getName());
    private static final List<IApplicator> APPLICATORS = new ArrayList();
    private static int handlingIncompatibilities = 0;

    static {
        APPLICATORS.add(new MergeChanges());
        APPLICATORS.add(new ReplaceChanges());
        APPLICATORS.add(new ApplyCraftingRecipes());
        APPLICATORS.add(new ApplyObjects());
    }

    public static void apply(List<Modification> activeModifications) {
        LOGGER.log(Level.INFO, "\n--Apply.apply()--\nChecking for incompatibilities");
        for (IApplicator applicator : APPLICATORS) {
            applicator.setModifications(activeModifications);
        }
        ModIncompatibilityException[] exceptions = ModIncompatibilityException.compileIncompatibilities();
        if (exceptions.length == 0) {
            LOGGER.log(Level.INFO, "No incompatibilities detected.\nApplying modifications");
            for (IApplicator applicator : APPLICATORS) {
                applicator.apply();
            }
            LOGGER.log(Level.INFO, "Modifications applied");
        } else {
            LOGGER.log(Level.WARNING, "Incompatibilities were detected.\nHandling incompatibilities");
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
                    LOGGER.log(Level.INFO, "Interrupted while waiting for FX thread to handle incompatibilities.");
                }
            }
            LOGGER.log(Level.INFO, "Incompatibilities handled.\nRe-starting application process");
            apply(Main.activeModifications);
        }
    }

}

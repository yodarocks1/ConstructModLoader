/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.Constants;
import cml.Main;
import cml.beans.ModIncompatibilityException;
import cml.beans.Modification;
import cml.beans.Profile;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import cml.lib.hash.Hasher;
import cml.lib.xmliconmap.CMLIconMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 *
 * @author benne
 */
public class Apply {

    private static final Logger LOGGER = Logger.getLogger(Apply.class.getName());
    private static final List<IApplicator> APPLICATORS = new ArrayList();
    private static int handlingIncompatibilities = 0;
    private static final String MAIN_MENU_MOD = "Main Menu Mod";

    static {
        APPLICATORS.add(new MergeChanges()); //MergeChanges must occur first (it resets everything to Vanilla)
        APPLICATORS.add(new ReplaceChanges());
        APPLICATORS.add(new ApplyCraftingRecipes());
        APPLICATORS.add(new ApplyObjects());
    }

    /**
     * Applies the supplied modifications
     * @param activeModifications Modifications to apply
     */
    @SuppressWarnings("SleepWhileInLoop")
    public static void apply(List<Modification> activeModifications) {
        LOGGER.log(Level.INFO, "--Apply.apply()--");
        
        File mainMenuModFile = new File(Constants.API_DIRECTORY, MAIN_MENU_MOD);
        Profile activeProfile = Main.activeProfile.get();
        String read = AFileManager.FILE_MANAGER.readString(new File(mainMenuModFile, "DoMerge/MainMenuBottom.layout"))
                .replace("<VERSION>", "V" + Constants.VERSION)
                .replace("<MOD_COUNT>", String.valueOf(activeModifications.size()))
                .replace("<PROFILE_NAME>", activeProfile.getName())
                .replace("<PROFILE_LEFT>", activeProfile.getLeft())
                .replace("<PROFILE_CENTER>", activeProfile.getCenter())
                .replace("<PROFILE_RIGHT>", activeProfile.getRight());
        AFileManager.FILE_MANAGER.write(new File(mainMenuModFile, "Merge/Data/Gui/Layouts/MainMenuBottom.layout"), read, AFileManager.FileOptions.REPLACE);
        Image profileIcon = activeProfile.getIcon();
        if (profileIcon == null) {
            profileIcon = CMLIconMap.ICON_MAP.EMPTY.getIcon(0);
        }
        AFileManager.IMAGE_MANAGER.write(new File(Main.scrapMechanicFolder, "Data/Gui/cml_profile_image.png"), SwingFXUtils.fromFXImage(profileIcon, null), "png");
        Modification mainMenuMod = new Modification(mainMenuModFile);
        activeModifications.add(mainMenuMod);
        
        LOGGER.log(Level.INFO, "Checking for incompatibilities");
        APPLICATORS.forEach((applicator) -> {
            applicator.setModifications(activeModifications.stream().collect(Collectors.toMap((mod) -> mod, (mod) -> mod.getDirectory())));
        });
        ModIncompatibilityException[] exceptions = ModIncompatibilityException.compileIncompatibilities();
        if (exceptions.length == 0) {
            LOGGER.log(Level.INFO, "No incompatibilities detected.\nApplying modifications");
            APPLICATORS.forEach((applicator) -> {
                applicator.apply();
            });
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
            activeModifications.remove(mainMenuMod);
            apply(Main.activeProfile.get().getActiveModifications());
        }
        
        activeModifications.remove(mainMenuMod);
        
        AFileManager.FILE_MANAGER.write(new File(Constants.API_DIRECTORY, Profile.CACHE_HASH_LOC), Hasher.digest(activeProfile.getDirectory()), FileOptions.REPLACE);
    }

}

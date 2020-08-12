/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.Main;
import cml.beans.Modification;
import cml.lib.files.AFileManager;
import cml.lib.files.AFileManager.FileOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author benne
 */
class ApplyCraftingRecipes implements IApplicator {

    private static final Logger LOGGER = Logger.getLogger(ApplyCraftingRecipes.class.getName());
    public static final String CRAFTING_RECIPES_FOLDER_RELATIVE = "Crafting Recipes";
    public static final String SM_CRAFTING_RECIPES_FOLDER = "Survival/CraftingRecipes/";

    private List<File> activeModifications;

    public ApplyCraftingRecipes(List<File> activeModifications) {
        this.activeModifications = activeModifications;
    }

    public ApplyCraftingRecipes() {
        this.activeModifications = new ArrayList();
    }

    @Override
    public void apply() {
        this.applyCraftingRecipes();
    }

    @Override
    public void setModifications(Map<Modification, File> activeModifications) {
        this.activeModifications = new ArrayList(activeModifications.values());
    }

    private void applyCraftingRecipes() {
        LOGGER.log(Level.FINE, "Applying crafting recipes");
        Map<String, List<String>> recipeListToMods = new HashMap();
        for (File recipeList : new File(Main.scrapMechanicFolder, SM_CRAFTING_RECIPES_FOLDER).listFiles()) {
            if (recipeList.isFile()) {
                recipeListToMods.put(recipeList.getName(), new ArrayList());
            }
        }

        this.activeModifications.stream().map((File activeModification) -> new File(activeModification.getAbsolutePath(), CRAFTING_RECIPES_FOLDER_RELATIVE)).filter((File recipeLists) -> (recipeLists.exists() && recipeLists.isDirectory())).forEachOrdered((File recipeLists) -> {
            for (File recipeList : recipeLists.listFiles()) {
                LOGGER.log(Level.FINEST, " Crafting list found: {0}", recipeList.getName());
                recipeListToMods.get(recipeList.getName()).add(AFileManager.FILE_MANAGER.readString(recipeList));
            }
        });

        recipeListToMods.keySet().forEach((recipeList) -> {
            File recipeListFile = new File(Main.scrapMechanicFolder, SM_CRAFTING_RECIPES_FOLDER + recipeList);
            LOGGER.log(Level.FINEST, " Crafting list search: {0}", recipeList);
            if (!recipeListToMods.getOrDefault(recipeList, new ArrayList()).isEmpty()) {
                if (recipeListFile.exists()) {
                    File outputFile = new File(Main.scrapMechanicFolder, SM_CRAFTING_RECIPES_FOLDER + recipeList);
                    AFileManager.FILE_MANAGER.write(outputFile, addRecipes(AFileManager.FILE_MANAGER.readString(recipeListFile), recipeListToMods.get(recipeList).toArray(new String[0])), FileOptions.REPLACE, FileOptions.CREATE);
                } else {
                    throw new UnsupportedOperationException("This recipe list (" + recipeList + ") or new recipe lists are not yet supported by ApplyCraftingRecipes.java. Please use MergeChanges.java or ReplaceChanges.java."); //Requires the deletion of recipe lists that are added by now-disabled mods.
                }
            }
        });
    }

    private String addRecipes(String original, String[] toAdd) {
        String result = original;
        for (String add : toAdd) {
            result = addRecipe(result, add);
        }
        return result;
    }

    private String addRecipe(String original, String toAdd) {
        String result = original;
        int location = result.lastIndexOf("}");
        result = result.substring(0, location + 1) + "," + toAdd + result.substring(location + 1);
        return result;
    }

}

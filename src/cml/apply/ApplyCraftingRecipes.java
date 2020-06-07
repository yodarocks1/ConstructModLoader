/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.Main;
import cml.beans.Modification;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
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
public class ApplyCraftingRecipes implements IApplicator {

    public static final String CRAFTING_RECIPES_FOLDER_RELATIVE = "\\Crafting Recipes\\";
    public static final String SM_CRAFTING_RECIPES_FOLDER = "Survival\\CraftingRecipes\\";

    private List<Modification> activeModifications;

    public ApplyCraftingRecipes(List<Modification> activeModifications) {
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
    public void setModifications(List<Modification> activeModifications) {
        this.activeModifications = activeModifications;
    }

    private void applyCraftingRecipes() {
        System.out.println("Applying crafting recipes");
        Map<String, List<String>> recipeListToMods = new HashMap();
        for (File recipeList : new File(Main.scrapMechanicFolder + SM_CRAFTING_RECIPES_FOLDER).listFiles()) {
            if (recipeList.isFile()) {
                recipeListToMods.put(recipeList.getName(), new ArrayList());
            }
        }

        for (Modification activeModification : this.activeModifications) {
            File recipeLists = new File(activeModification.getDirectory().getAbsolutePath() + CRAFTING_RECIPES_FOLDER_RELATIVE);
            if (recipeLists.exists() && recipeLists.isDirectory()) {
                for (File recipeList : recipeLists.listFiles()) {
                    System.out.println(" Crafting list found: " + recipeList.getName());
                    try {
                        recipeListToMods.get(recipeList.getName()).add(Apply.readFile(recipeList.toPath()));
                    } catch (IOException ex) {
                        Logger.getLogger(ApplyCraftingRecipes.class.getName()).log(Level.SEVERE, "Invalid recipe list " + recipeList.getName() + " in modification " + activeModification.getName(), ex);
                    }
                }
            }
        }

        for (String recipeList : recipeListToMods.keySet()) {
            File recipeListFile = new File(Main.scrapMechanicFolder + SM_CRAFTING_RECIPES_FOLDER + recipeList);
            System.out.println(" Crafting list search: " + recipeList);
            if (!recipeListToMods.getOrDefault(recipeList, new ArrayList()).isEmpty()) {
                if (recipeListFile.exists()) {
                    try {
                        File outputFile = new File(Main.scrapMechanicFolder + SM_CRAFTING_RECIPES_FOLDER + recipeList);
                        Apply.writeFile(outputFile.toPath(), addRecipes(Apply.readFile(recipeListFile.toPath()), recipeListToMods.get(recipeList).toArray(new String[0])), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException ex) {
                        Logger.getLogger(ApplyCraftingRecipes.class.getName()).log(Level.SEVERE, "Recipe list " + recipeList + " could not be written", ex);
                    }
                } else {
                    throw new UnsupportedOperationException("This recipe list (" + recipeList + ") or new recipe lists are not yet supported by ApplyCraftingRecipes.java. Please use MergeChanges.java or ReplaceChanges.java."); //Requires the deletion of recipe lists that are added by now-disabled mods.
                }
            }
        }
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

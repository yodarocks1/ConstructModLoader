/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.beans.Modification;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author benne
 */
public class Apply {

    private static final List<IApplicator> APPLICATORS = new ArrayList();

    static {
        APPLICATORS.add(new MergeChanges());
        APPLICATORS.add(new ReplaceChanges());
        APPLICATORS.add(new ApplyCraftingRecipes());
        //applicators.add(new ApplyLoot());
        APPLICATORS.add(new ApplyObjects());
    }

    public static void apply(List<Modification> activeModifications) {
        System.out.println("Applying modifications");
        for (IApplicator applicator : APPLICATORS) {
            applicator.setModifications(activeModifications);
        }
        for (IApplicator applicator : APPLICATORS) {
            applicator.apply();
        }
        System.out.println("Modifications applied");
    }

}

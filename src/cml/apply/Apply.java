/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.GUI;
import cml.beans.Modification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author benne
 */
public class Apply {

    private static List<IApplicator> applicators;

    static {
        applicators = new ArrayList();
        applicators.add(new ApplyCraftingRecipes());
        applicators.add(new ApplyLoot());
        applicators.add(new ApplyObjects());
        applicators.add(new ApplyScripts());
        applicators.add(new ApplyUnits());
        applicators.add(new ApplyWorlds());
    }

    public static void apply(List<Modification> activeModifications) {
        System.out.println("Applying modifications");
        for (IApplicator applicator : applicators) {
            applicator.setModifications(activeModifications);
            applicator.apply();
        }
        GUI.lastApplied = LocalDateTime.now();
    }

    public static String combineLua(String template, String[] inputs) {
        System.out.println("   Combining LUA: ");

        //Get sections
        Map<String, List<String>> templateSections = new HashMap();
        String templateCurrent = template;
        boolean reachedEnd = false;
        while (!reachedEnd) {
            int end = templateCurrent.indexOf(" ~}~");
            if (end != -1) {
                String section = templateCurrent.substring(templateCurrent.indexOf("~{~ ") + 4, end);
                templateSections.put(section, new ArrayList());
                templateCurrent = templateCurrent.substring(end + 4);
                System.out.println("     New section: " + section);
            } else {
                reachedEnd = true;
            }
        }
        System.out.println("     Sections: " + templateSections.size());

        //Sort inputs
        for (String input : inputs) {
            String in = input;
            reachedEnd = false;
            while (!reachedEnd) {
                int end = in.indexOf("~}~");
                if (end != -1) {
                    String sectionName = in.substring(in.indexOf("~{~ ") + 4, in.indexOf(" ~|~"));
                    String section = in.substring(in.indexOf(" ~|~") + 4, end);
                    templateSections.get(sectionName).add(section);
                    System.out.println("     Adding string of length " + end + " to section " + sectionName);
                    in = in.substring(end + 3);
                } else {
                    reachedEnd = true;
                }
            }
        }

        //Apply inputs to sections
        String result = template;
        for (String sectionName : templateSections.keySet()) {
            result = result.replaceAll("~\\{~ " + sectionName + " ~}~", "\t\t\t-- Modded Section\n" + templateSections.get(sectionName).stream().collect(Collectors.joining("\t\t\t\t-- New Modded Section\n")) + "\t\t\t-- End Modded Section\n");
        }

        return result;
    }

}

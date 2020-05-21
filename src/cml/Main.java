/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import cml.apply.Apply;
import static javafx.application.Application.launch;

/**
 *
 * @author benne
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GUI.reloadMods();
        launch(GUI.class, args);
    }

    public static void applyModifications() {
        Apply.apply(GUI.activeModifications);
    }

}

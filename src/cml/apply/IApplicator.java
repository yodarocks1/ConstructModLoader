/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.apply;

import cml.beans.Modification;
import java.io.File;
import java.util.Map;

/**
 *
 * @author benne
 */
public interface IApplicator {

    public void apply();

    public void setModifications(Map<Modification, File> activeModifications);
}

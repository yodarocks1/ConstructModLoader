/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 *
 * @author Bennett_DenBleyker
 */
public class Constants {

    public static final String SCRAP_MECHANIC_FOLDER = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Scrap Mechanic\\";
    public static final String CONSTRUCT_FOLDER = "C:\\Program Files (x86)\\Construct\\";
    public static final String MODS_FOLDER = Constants.CONSTRUCT_FOLDER + "mods";
    public static final String VERSION = "A.0.0";
    public static final String LAUNCH_COMMAND = "steam://rungameid/387990";
    public static final ZoneOffset ZONE = OffsetDateTime.now().getOffset();
}

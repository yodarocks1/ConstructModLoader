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

    public static final String FOLDERS_LOCATION = "C:\\Program Files (x86)\\Construct\\folders.txt";
    public static final String VERSION = "A.1.0";
    public static final String LAUNCH_COMMAND = "rundll32 url.dll,FileProtocolHandler steam://rungameid/387990";
    public static final ZoneOffset ZONE = OffsetDateTime.now().getOffset();
}

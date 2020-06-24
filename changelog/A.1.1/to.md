# Construct Mod Loader (CML) Version Alpha A.1.1
## Changes in this release:
* Modification API
  * Patch modifications - more reliable after Scrap Mechanic updates, and less likely to conflict with other mods.
    * Check the mods/_._patches_._ to see all patches made to the game in the current instance of CML
  * FULL incompatibility detection
    * On a detected incompatibility:
      * If it is a "certain" incompatibility, the user will be forced to choose only one modification to leave active.
      * If it is a "possible" incompatibility, the user can pick and choose which modifications to disable.
    * Certain incompatibility detection criteria:
      * One modification is Replacing a file that another modification is attempting to Merge or Replace.
    * Possible incompatibility detection criteria:
      * A patch failed, thus marking all modifications that affect the file as possibly incompatible.
  * IconMaps and InventoryDescriptions can now be easily modified
* Finished GUI [Except for the profile preferences button]
  * Selection between multiple profiles
  * Update checker (automatic updating coming soon)
* Example mods are fully functional
  * Examples show how to use the following modification systems:
    * Patch
    * Merge
    * Crafting recipes
    * Icon maps
    * Inventory descriptions
    * Shape sets
    * New blocks and parts
* Log files
  * Log file: `Construct/mods/_._logs_._/YYYY-MM-DD+HH;MM;SS-log.txt`
  * Errors file: `Construct/mods/_._logs_._/YYYY-MM-DD+HH;MM;SS-err.txt`
  * Patch file: `Construct/mods/_._patches_._/YYYY-MM-DD+HH;MM;SS.txt`

## Installation instructions:
* Method 1 (Automatic installation)
  * Download and run the .msi file
  * Make sure to run CML as an Administrator
    * To make it easier on yourself, navigate to `C:\Program Files (x86)\Construct\API`, right click `CML.exe`, click `Properties`, go to the `Compatibility` tab, and tick the `Run this program as an administrator` checkbox.
* Method 2 (Manual installation)
  * Unzip the .zip file to an easy-to-access location
  * Move the Construct folder into `C:\Program Files (x86)`
    * This should result in a file structure that looks like: `C:\Program Files (x86)\Construct\API\CML.exe`
  * Make sure to run CML.exe as an Administrator
    * To make it easier on yourself, navigate to `C:\Program Files (x86)\Construct\API`, right click `CML.exe`, click `Properties`, go to the `Compatibility` tab, and tick the `Run this program as an administrator` checkbox.

## To apply a Scrap Mechanic update:
1. Make sure CML **is not running**
1. Go to Scrap Mechanic in your Steam library
1. Open the game settings
   1. Click the gear located to the right of the play button
   1. Click properties
1. Go to the Local Files tab
1. Click `Verify Integrity of Game Files`
   1. This may take a while. Be aware.
1. Copy files from within the Scrap Mechanic folder into the Construct/vanilla folder
   * The following folders are not necessary to copy:
     * \Logs
     * \Cache
     * \Challenges
     * \ChallengeData
     * \Data\ExampleMods
     * \Data\Terrain
     * \Data\CML-Objects
     * \Survival\Character\Char_Male
     * \Survival\Terrain
     * \Screenshots

## Known issues:
* The preferences button has not yet been implemented, and thus does nothing.
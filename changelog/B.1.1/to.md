# Construct Mod Loader (CML) Version Beta B.1.0
## Changes in this release:
* Fixed Splash Screen
* HUGE optimizations
* New console
  * When closed, instead minimizes to tray until the main window is closed.
* Workshop integration!
  * Convert mods from the Steam Workshop to work with CML!
* Steam shortcut integration
  * Add CML to your Steam Library with the click of a button!
* Implemented Profile Properties
  * Change the icon, name, or description of a profile with ease.
* New font! ([Library 3am by Igor Kosinsky](https://www.fontspace.com/library-3-am-font-f30355))
* Automatically unzip profiles and mods!
* New icon! Let me know what you think!
* Update assets - Check out the [README.md](https://github.com/yodarocks1/ConstructModLoader/blob/master/README.md#update-instructions) for more information
* Choose between bundled or unbundled JRE
  * Most versions of Java include JavaFX, but some don't.
* A custom-scripted installer using the Nullsoft Scriptable Installation System!
  * Reduce the installation time (and size) dramatically if your default JRE natively supports JavaFX.

* Log file locations
  * Patch file: `Construct/mods/_._patches_._/YYYY-MM-DD+HH;MM;SS.txt`
  * Log XML: `Construct/API/heavy_log.xml`

## Update to Beta!
I am happy to announce that CML is moving into Beta!
Coming up in the Beta phase:
* Plugin support! (Maybe a website, too?)
  * Crafting recipe manager
  * Shapeset manager
  * Kariaro/HardCoded's world viewer
  * etc.
* Further optimization
  * Immutable objects
  * Subdividing controllers
  * Java Concurrency
* If you have any suggestions, feel free to create an ['issue'](https://github.com/yodarocks1/ConstructModLoader/issues) (and mark it as a suggestion), or let me know on [discord](https://discord.gg/ZcWwHeZ).
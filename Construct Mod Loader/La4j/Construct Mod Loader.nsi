
#!define VERSION_SIMPLE "B.2.0"
#!define FILE_VERSION "01.01.01.00"
!define INSTALL_DIR "$PROGRAMFILES\Construct"
!define WEB_SITE "https://github.com/yodarocks1/ConstructModLoader/"
!define BRANDING_IMAGE "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\src\media\CML App.png"

!define THIS_DIR "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Construct Mod Loader\La4j"
!define INSTALL_TYPE "SetShellVarContext all"

!define VERSION "V+${VERSION_SIMPLE}"
!define LIBRARY_3AM "${THIS_DIR}\Library3am.otf"
!define LICENSE "${THIS_DIR}\License.rtf"
!define INSTALLER_OUT "${THIS_DIR}\CML-${VERSION_SIMPLE}-setup.exe"
!define ICON "${THIS_DIR}\CML Installer.ico"

##################################################

!include LogicLib.nsh
!include Sections.nsh
!include zipdll.nsh

LangString name 1033 "Construct Mod Loader"
LangString name 2070 "Construto Carregador de Modificação"

VIProductVersion "${FILE_VERSION}"
VIFileVersion "${FILE_VERSION}"
VIAddVersionKey /LANG=1033 "ProductName" "Construct Mod Loader"
VIAddVersionKey /LANG=2070 "ProductName" "Construto Carregador de Modificação"
VIAddVersionKey "CompanyName" "CML - Bennett DenBleyker"
VIAddVersionKey "LegalCopyright" "Bennett DenBleyker  © 2020"
VIAddVersionKey /LANG=1033 "FileDescription" "Construct Mod Loader"
VIAddVersionKey /LANG=2070 "FileDescription" "Construto Carregador de Modificação"

SetCompressor /SOLID ZLIB
SetDateSave off
Name "Construct Mod Loader ${VERSION}"
Caption "Construct Mod Loader"
OutFile "${INSTALLER_OUT}"
Icon "${ICON}"
BrandingText /TRIMCENTER "$(name)"
InstallDirRegKey "HKLM" "Software\Microsoft\Windows\CurrentVersion\App Paths\CML.exe" ""
InstallDir "${INSTALL_DIR}"
ChangeUI all "${NSISDIR}\Contrib\UIs\modern.exe"
SetFont "Georgia" 12

##################################################

Section "JRE" JRE_SECTION
  ${INSTALL_TYPE}
  AddSize 197653
  CreateDirectory "$INSTDIR\API"
  inetc::get "https://www.dropbox.com/s/fymhwrxsqv209ae/jre.zip?dl=1" "$INSTDIR\API\jre.zip"
  Pop $0
  DetailPrint $0
  !insertmacro ZIPDLL_EXTRACT "$INSTDIR\API\jre.zip" "$INSTDIR\API" "<ALL>"
  Delete "$INSTDIR\API\jre.zip"
SectionEnd

Section "CML" REQUIRED_BOLD_SECTION_1
  ${INSTALL_TYPE}
  SetOutPath "$FONTS"
    File "${LIBRARY_3AM}"
  SetOutPath "$INSTDIR\API"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Construct Mod Loader\La4j\CML.exe"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Construct Mod Loader\La4j\ConstructModLoader.jar"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Construct Mod Loader\La4j\logo.ico"
	File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\CleanupLocks.bat"
  SetOutPath "$INSTDIR\API\lib"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\lib\luaj-jse-3.0.1.jar"
	File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\lib\scenicView.jar"
  SetOutPath "$INSTDIR\docs"
	File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\CONTRIBUTING.md"
	File /oname=LICENSE.md "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\LICENSE"
  SetOutPath "$INSTDIR\mods\Vanilla"
    File "C:\Program Files (x86)\Construct\mods\Vanilla\description.txt"
  CreateDirectory "$INSTDIR\mods\_._patches_._"
  FileOpen $1 "$INSTDIR\mods\_._patches_._\Future Deprecation.txt" w
  FileWrite $1 "The Patches folder will have no use, and will thus be removed, once the Construct Mod Tool plugin has been completed. If you use this feature often, make sure to download the Construct Mod Tool plugin upon its release."
  FileClose $1
  
  CreateDirectory "$SMPROGRAMS\CML"
  CreateShortcut "$SMPROGRAMS\CML\Construct Mod Loader.lnk" "$INSTDIR\API\CML.exe" "" "$INSTDIR\API\logo.ico"
  CreateShortcut "$DESKTOP\Construct Mod Loader.lnk" "$INSTDIR\API\CML.exe" "" "$INSTDIR\API\logo.ico"
SectionEnd

Section "Example Mods"
  ${INSTALL_TYPE}
  SetOutPath "$INSTDIR\mods\Developer Profile"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\description.txt"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\icon.png"

  SetOutPath "$INSTDIR\mods\Developer Profile\Dev Mode Mod - Merge Example"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Dev Mode Mod - Merge Example\description.txt"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Dev Mode Mod - Merge Example\version.txt"
  SetOutPath "$INSTDIR\mods\Developer Profile\Dev Mode Mod - Merge Example\Merge\Survival\Scripts\game"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Dev Mode Mod - Merge Example\Merge\Survival\Scripts\game\SurvivalGame.lua"

  SetOutPath "$INSTDIR\mods\Developer Profile\Dev Mode Mod - Patch Example"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Dev Mode Mod - Patch Example\description.txt"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Dev Mode Mod - Patch Example\version.txt"
  SetOutPath "$INSTDIR\mods\Developer Profile\Dev Mode Mod - Patch Example\Patch\Survival\Scripts\game"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Dev Mode Mod - Patch Example\Patch\Survival\Scripts\game\SurvivalGame.lua"

  SetOutPath "$INSTDIR\mods\Developer Profile\Example Mod"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\description.txt"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\version.txt"
  SetOutPath "$INSTDIR\mods\Developer Profile\Example Mod\Crafting Recipes"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Crafting Recipes\craftbot.json"
  SetOutPath "$INSTDIR\mods\Developer Profile\Example Mod\Objects\Database"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\Database\iconmaps.txt"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\Database\inventorydesc.txt"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\Database\shapesets.txt"
  SetOutPath "$INSTDIR\mods\Developer Profile\Example Mod\Objects\Database\IconMaps"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\Database\IconMaps\examplemod.png"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\Database\IconMaps\examplemod.xml"
  SetOutPath "$INSTDIR\mods\Developer Profile\Example Mod\Objects\Database\InventoryDescriptions"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\Database\InventoryDescriptions\examplemod.json"
  SetOutPath "$INSTDIR\mods\Developer Profile\Example Mod\Objects\Database\ShapeSets"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\Database\ShapeSets\examplemod.json"
  SetOutPath "$INSTDIR\mods\Developer Profile\Example Mod\Objects\blk_example"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\blk_example\texture_asg.tga"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\blk_example\texture_dif.tga"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\blk_example\texture_nor.tga"
  SetOutPath "$INSTDIR\mods\Developer Profile\Example Mod\Objects\obj_example"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\obj_example\texture_asg.tga"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\obj_example\texture_dif.tga"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\obj_example\texture_nor.tga"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\obj_example\collision.obj"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\obj_example\mesh.fbx"
    File "C:\Program Files (x86)\Construct\mods\Developer Profile\Example Mod\Objects\obj_example\script.lua"
    
SectionEnd

SectionGroup "Plugins"
  
  Section "Plugin Manager" REQUIRED_SECTION_1
    CreateDirectory "$INSTDIR\API\plugins"
  SectionEnd

  Section "Mod Tool / Uploader" DISABLED_SECTION_1
  SectionEnd

  Section "Crafting Recipe Manager" DISABLED_SECTION_2
  SectionEnd

  Section "Shapeset Manager" DISABLED_SECTION_3
  SectionEnd

  Section "Backup Manager" DISABLED_SECTION_4
  SectionEnd
  
  Section "Scrap Mechanic Modded Main Menu" DEFAULT_SECTION_1
  SectionEnd

  Section "URL and Download Handler" REQUIRED_SECTION_2
    ${INSTALL_TYPE}
	SetOutPath "$INSTDIR\API"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\CMLProtocol.exe"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\CMLProtocol.jar"
	SetOutPath "$INSTDIR\API\steamcmd"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\steamcmd\steamcmd.exe"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\steamcmd\CMLDownloadTool.exe"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\steamcmd\DownloadComplete.bat"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\steamcmd\compatible.txt"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\steamcmd\incompatible.txt"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\steamcmd\PastebinGet.exe"
	  File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\steamcmd\PastebinShare.exe"
	CreateDirectory "$INSTDIR\API\steamcmd\smm"
	CreateDirectory "$INSTDIR\API\downloads"
	
    WriteRegStr "HKCR" "cml" "" "URL:cml protocol"
    WriteRegStr "HKCR" "cml" "URL Protocol" ""
    WriteRegStr "HKCR" "cml\Shell\Open\Command" "" "$\"$INSTDIR\API\CMLProtocol.exe$\" $\"%1$\""
  SectionEnd
SectionGroupEnd

Section "-Other Necessary Files"
  SetOutPath "$INSTDIR\API\Main Menu Mod"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Main Menu Mod\description.txt"
  SetOutPath "$INSTDIR\API\Main Menu Mod\DoMerge"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Main Menu Mod\DoMerge\MainMenuBottom.layout"
  SetOutPath "$INSTDIR\API\Main Menu Mod\Merge\Data\Gui\Layouts"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Main Menu Mod\Merge\Data\Gui\Layouts\MainMenuLogo.layout"
  SetOutPath "$INSTDIR\API\Main Menu Mod\Replace\Data\Gui"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Main Menu Mod\Replace\Data\Gui\gui_mainmenu_logo_cml.png"

  SetOutPath "$INSTDIR\API"
    File "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\IconMap.png"
SectionEnd

Section "-Registry and Uninstaller"
  ${INSTALL_TYPE}
  WriteUninstaller "$INSTDIR\API\uninstall.exe"

  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\App Paths\CML.exe" "" "$INSTDIR\CML.exe"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "DisplayName" "Construct Mod Loader"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "UninstallString" "$INSTDIR\API\uninstall.exe"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "DisplayIcon" "$INSTDIR\API\logo.ico"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "DisplayVersion" "${VERSION}"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "Publisher" "CML - Bennett DenBleyker"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "RegOwner" "Bennett DenBleyker"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "RegCompany" "CML"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "URLUpdateInfo" "${WEB_SITE}releases/latest"
  WriteRegStr "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader" "URLInfoAbout" "${WEB_SITE}"
  WriteRegStr "HKLM" "Software\Microsoft\Windows NT\CurrentVersion\AppCompatFlags\Layers" "$INSTDIR\API\CML.exe" "~ RUNASADMIN"

SectionEnd

Section "Uninstall"
  ${INSTALL_TYPE}
  Delete "$SMPROGRAMS\CML\Construct Mod Loader.lnk"
  RmDir "$SMPROGRAMS\CML"
  Delete "$DESKTOP\Construct Mod Loader.lnk"

  #API Folder
  Delete "$INSTDIR\API\CML.exe"
  Delete "$INSTDIR\API\ConstructModLoader.jar"
  Delete "$INSTDIR\API\logo.ico"
  Delete "$INSTDIR\API\CleanupLocks.bat"
  Delete "$INSTDIR\API\IconMap.png"
  Delete "$INSTDIR\API\CMLProtocol.exe"
  Delete "$INSTDIR\API\CMLProtocol.jar"
  Delete "$INSTDIR\API\cachehash.dat"
  Delete "$INSTDIR\API\selected.asc"
  Delete "$INSTDIR\API\folders.txt"
  Delete "$INSTDIR\API\scenicView.properties"
  RmDir /r "$INSTDIR\API\lib"
  RmDir /r "$INSTDIR\API\jre"
  RmDir /r "$INSTDIR\API\Main Menu Mod"
  RmDir /r "$INSTDIR\API\downloads"
  RmDir /r "$INSTDIR\API\steamcmd"
  RmDir "$INSTDIR\API\plugins"
  
  #Docs Folder
  Delete "$INSTDIR\docs\LICENSE.md"
  Delete "$INSTDIR\docs\CONTRIBUTING.md"
  RmDir "$INSTDIR\docs"
  
  #Mods Folder
  RmDir /r "$INSTDIR\mods\Developer Profile\Dev Mode Mod - Merge Example" ;  ▼
  RmDir /r "$INSTDIR\mods\Developer Profile\Dev Mode Mod - Patch Example" ;  ▼
  RmDir /r "$INSTDIR\mods\Developer Profile\Example Mod" ; Delete default mods in their entirety
  Delete "$INSTDIR\mods\Developer Profile\description.txt"
  Delete "$INSTDIR\mods\Developer Profile\icon.png"
  RmDir "$INSTDIR\mods\Developer Profile" ; Deletes profile only if it is empty.
  Delete "$INSTDIR\mods\Vanilla\description.txt"
  Delete "$INSTDIR\mods\Vanilla\icon.png"
  RmDir "$INSTDIR\mods\Vanilla" ; Deletes profile only if it is empty.
  RmDir /r "$INSTDIR\mods\_._patches_._" ;  Delete all patches, no matter what
  RmDir "$INSTDIR\mods"

  #Vanilla Folder
  RmDir /r "$INSTDIR\vanilla" ; Delete all files within the Vanilla folder
  
  # Registry
  DeleteRegKey "HKCR" "cml"
  DeleteRegKey "HKLM" "Software\Microsoft\Windows\CurrentVersion\App Paths\CML.exe"
  DeleteRegKey "HKLM" "Software\Microsoft\Windows\CurrentVersion\Uninstall\Construct Mod Loader"

  # Delete the uninstaller
  Delete "$INSTDIR\API\uninstall.exe"
  RmDir "$INSTDIR\API"
  RmDir "$INSTDIR"
SectionEnd

##################################################

PageEx license
  LicenseData "${LICENSE}"
  LicenseForceSelection checkbox
PageExEnd
PageEx components
  Caption ": Installation Components"
  ComponentText "Please select which components to install. Installation of the JRE is necessary if your default version of Java does not support JavaFX.$\r$\nNote: Some JREs and most JDKs support JavaFX"
PageExEnd
PageEx directory
  DirText "Please choose where you would like to install Construct Mod Loader.$\r$\nTo choose a different folder, click Browse and select another folder.$\r$\nClick Install to start the installation."
  DirVerify leave
PageExEnd
PageEx instfiles
  CompletedText "You've successfully installed Construct Mod Loader!"
PageExEnd
UninstPage uninstConfirm
UninstPage instfiles

##################################################

Function .onInit # TODO: Add ability to update
  SectionSetFlags ${DEFAULT_SECTION_1} 1
  SectionSetFlags ${REQUIRED_BOLD_SECTION_1} 25
  SectionSetFlags ${REQUIRED_SECTION_1} 17
  SectionSetFlags ${REQUIRED_SECTION_2} 17
  SectionSetFlags ${DISABLED_SECTION_1} 16
  SectionSetFlags ${DISABLED_SECTION_2} 16
  SectionSetFlags ${DISABLED_SECTION_3} 16
  SectionSetFlags ${DISABLED_SECTION_4} 16
  Call ConnectInternet
FunctionEnd

Function .onInstSuccess
  MessageBox MB_YESNO "You've successfully installed Construct Mod Loader!$\r$\nWould you like to launch CML now?" IDNO NoLaunch
    Exec '"$INSTDIR\API\CML.exe"'
    MessageBox MB_OK "If you have any issues or suggestions, please let me know via Github Issues or Discord.$\r$\n      Note: If you are placing a suggestion on Github Issues, please use the suggestion label. Thank you!"
    Goto LaunchDone
  NoLaunch:
    MessageBox MB_OK "Thank you for installing CML. You can find a shortcut on your Desktop and a link in your Start Menu.$\r$\n$\r$\nIf you have any issues or suggestions, please let me know via Github Issues or Discord.$\r$\n      Note: If you are placing a suggestion on Github Issues, please use the suggestion label. Thank you!"
  LaunchDone:
FunctionEnd

Function ConnectInternet
  doretry:
  Push $R0

  Dialer::AttemptConnect

  Pop $R0
  StrCmp $R0 "online" connected
    MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP|MB_DEFBUTTON2 "Cannot connect to the internet. If you do not establish a connection, you will not be able to download the JRE." IDABORT doabort IDRETRY doretry
    
# doignore: Disable JRE installation and continue
    SectionSetFlags ${JRE_SECTION} 16
    Goto connected
  doabort:
    Quit
  connected:
  
  Pop $R0
FunctionEnd
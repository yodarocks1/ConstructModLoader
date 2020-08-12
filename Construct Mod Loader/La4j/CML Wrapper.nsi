; Java Launcher
;--------------

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

Name "Construct Mod Loader ${VERSION_SIMPLE}"
Icon "logo.ico"
OutFile "CML.exe"
 
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow

!define JAR "ConstructModLoader.jar"
 
Section ""

  Call GetParameters
  Var /GLOBAL params
  Pop $params

  Call GetJRE
  Pop $R0
 
 
  StrCpy $0 '"$R0" -jar ${JAR} $params'
 
 
  SetOutPath $EXEDIR
  Exec $0
SectionEnd
 
Function GetJRE
;
;  Find JRE (javaw.exe)
;  1 - in .\jre directory (JRE Installed with application)
;  2 - in JAVA_HOME environment variable
;  3 - in the registry
;  4 - assume javaw.exe in current dir or PATH
 
  Push $R0
  Push $R1
 
  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""
 
  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound
 
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\javaw.exe"
 
  IfErrors 0 JreFound
  StrCpy $R0 "javaw.exe"
 
 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd

Function GetParameters
;
; GetParameters
; input, none
; output, top of stack (replaces, with e.g. whatever)
; modifies no other variables.
 
  Push $R0
  Push $R1
  Push $R2
  Push $R3
 
  StrCpy $R2 1
  StrLen $R3 $CMDLINE
 
  ;Check for quote or space
  StrCpy $R0 $CMDLINE $R2
  StrCmp $R0 '"' 0 +3
    StrCpy $R1 '"'
    Goto loop
  StrCpy $R1 " "
 
  loop:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $CMDLINE 1 $R2
    StrCmp $R0 $R1 get
    StrCmp $R2 $R3 get
    Goto loop
 
  get:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $CMDLINE 1 $R2
    StrCmp $R0 " " get
    StrCpy $R0 $CMDLINE "" $R2
 
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0
 
FunctionEnd
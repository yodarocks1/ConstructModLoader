@echo off

cd "C:\Users\benne\Documents\NetBeansProjects\Construct Mod Loader\Construct Mod Loader"
echo Awaiting user input...
for /F "tokens=* USEBACKQ" %%F in (`cscript PromptVersion.vbs`) do (
set cmlVersion=%%F
)

if %cmlVersion%=="" (
goto Cancelled
)

echo CML Version : %cmlVersion%

call set fileVersion=%cmlVersion:A=0.1%
call set fileVersion=%fileVersion:B=0.2%
call set fileVersion=%fileVersion:G=0.3%
call set fileVersion=%fileVersion:-h=.%

echo File Version : %fileVersion%
copy "..\dist\Construct Mod Loader.jar" .\La4j\ConstructModLoader.jar > NUL
cd La4j
"C:\Program Files (x86)\NSIS\makensis.exe" /DVERSION_SIMPLE=%cmlVersion% /DFILE_VERSION=%fileVersion% /V1 ".\CML Wrapper.nsi" > NUL
Powershell.exe -ExecutionPolicy remotesigned -File .\sign.ps1

"C:\Program Files (x86)\NSIS\makensis.exe" /DVERSION_SIMPLE=%cmlVersion% /DFILE_VERSION=%fileVersion% /V1 ".\Construct Mod Loader.nsi" > NUL
Powershell.exe -ExecutionPolicy remotesigned -File .\sign_installer.ps1 -VersionArg %cmlVersion%

"C:\Program Files\7-Zip\7z.exe" a -aoa UpdateAssets.zip CML.exe > NUL
"C:\Program Files\7-Zip\7z.exe" a -aoa UpdateAssets.zip ConstructModLoader.jar > NUL
"C:\Program Files\7-Zip\7z.exe" a -aoa UpdateAssets.zip ../../IconMap.png > NUL

copy CML-%cmlVersion%-setup.exe ..\Output\CML-%cmlVersion%-setup.exe > NUL
copy CML-%cmlVersion%-setup.exe ..\Output\CML-latest-setup.exe > NUL
copy UpdateAssets.zip ..\Output\UpdateAssets.zip > NUL
copy CML.exe ..\Output\CML.exe > NUL
copy ConstructModLoader.jar ..\Output\ConstructModLoader.jar > NUL

if not exist ..\Backup\%cmlVersion%\NUL mkdir ..\Backup\%cmlVersion% > NUL

copy CML-%cmlVersion%-setup.exe ..\Backup\%cmlVersion%\CML-latest-setup.exe > NUL
copy UpdateAssets.zip ..\Backup\%cmlVersion%\UpdateAssets.exe > NUL
copy CML.exe ..\Backup\%cmlVersion%\CML.exe > NUL
copy ConstructModLoader.jar ..\Backup\%cmlVersion%\ConstructModLoader.jar > NUL

del CML-%cmlVersion%-setup.exe > NUL
del CML.exe > NUL
del ConstructModLoader.jar > NUL
del UpdateAssets.zip > NUL

echo Output is in .\Construct Mod Loader\Output
echo A backup of V+%cmlVersion% has been stored in .\Construct Mod Loader\Backup\%cmlVersion%

goto EOF
:Cancelled
echo Cancelled .exe and installer build.

:EOF
timeout /t 5
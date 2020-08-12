@echo off

echo Awaiting user input...
for /F "tokens=* USEBACKQ" %%F in (`cscript PromptVersion.vbs`) do (
set cmlVersion=%%F
)

if %cmlVersion%=="" (
goto Cancelled
)

echo CML Version : %cmlVersion%


goto EOF

:Cancelled
echo Cancelled .exe and installer build.
goto EOF

:EOF
pause
del heavy_log.xml.lck > NUL
del %temp%\cml.lck > NUL
taskkill /im java.exe /t /f > NUL
echo Cleaned CML Locks
pause
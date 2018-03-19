@echo off

set nameOfWxiFile=EnvironmentVars.wxi

if %1.==. (
    set PathToFile=..\..\dist\
	set ProductVersion=1.0.0.0
	goto done
)

:loop
if [%1] equ [] goto done
set param=%1
if %param:~0,1% equ - goto checkParam
:paramError
echo Parameter error: %1 

:next
shift /1
goto loop

:checkParam
if "%1" equ "-p" goto A
if "%1" equ "-v" goto B
echo Incorrect Parameter
goto paramError

:A
    shift /1
    set PathToFile=%1
    goto next

:B
    shift /1
    set ProductVersion=%1
    goto next
	
:done
echo PathToFile=%PathToFile%	ProductVersion=%ProductVersion%

echo ^<Include^> > %nameOfWxiFile%
echo ^<^?define PathToFile ^= "%PathToFile%" ^?^> >> %nameOfWxiFile%
echo ^<^?define ProductVersion ^= "%ProductVersion%" ^?^> >> %nameOfWxiFile%
echo ^</Include^> >> %nameOfWxiFile%

echo.

echo Building: setup.msi

mkdir -p ../build

echo.
echo - Harvesting...
echo.

heat.exe dir %PathToFile%%/Tomcat_Server/webapps -o webapps.wxs -scom -frag -srd -sreg -gg -cg ComponentGroupId -dr webapps_dir
if ERRORLEVEL 1 goto error

echo.
echo - Compiling...
echo.

candle.exe -arch x64 -v -out "../build/setup.wixobj" "setup.wxs" -ext WixUtilExtension
if ERRORLEVEL 1 goto error

candle.exe -arch x64 -v -out "../build/webapps.wixobj" "webapps.wxs"
if ERRORLEVEL 1 goto error

candle.exe -arch x64 -v -out "../build/UserInterface.wixobj" "UserInterface.wxs"
if ERRORLEVEL 1 goto error

candle.exe -arch x64 -v -out "../build/Common.wixobj" "Common.wxs"
if ERRORLEVEL 1 goto error

candle.exe -arch x64 -v -out "../build/ErrorText.wixobj" "ErrorText.wxs"
if ERRORLEVEL 1 goto error

candle.exe -arch x64 -v -out "../build/ProgressText.wixobj" "ProgressText.wxs"
if ERRORLEVEL 1 goto error

echo.
echo - Linking...
echo.

light.exe -ext WixUtilExtension.dll -loc "English-US.wxl" -out "../build/Solarflare_VCP_%ProductVersion%_Installer.msi" -b "%PathToFile%/Tomcat_Server/webapps" "../build/webapps.wixobj" "../build/setup.wixobj" "../build/UserInterface.wixobj" "../build/Common.wixobj" "../build/ErrorText.wixobj" "../build/ProgressText.wixobj" 
if ERRORLEVEL 1 goto error

goto end

:error
echo.
echo - Build failed!
exit /B 1

:end
echo.
echo - Build completed successfully.
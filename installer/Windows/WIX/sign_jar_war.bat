@echo off

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
if "%1" equ "-cert" goto A
if "%1" equ "-passwd" goto B
if "%1" equ "-tsu" goto C
if "%1" equ "-alias" goto D

echo Incorrect Parameter
goto paramError

:A
    shift /1
    set PathToCACerticate=%1
    goto next

:B
    shift /1
    set CertificatePassword=%1
    goto next

:C
    shift /1
    set TimeStampURL=%1
    goto next

:D
    shift /1
    set AliasName=%1
    goto next
	
:done

set currentDirectory="%CD%"
set outDirName=..\..\dist\Tomcat_Server\webapps\solarflare-vcp\

for /R "%outDirName%" %%f in (*.zip) do (
	set dirName=%%~nf
)

set PathToJARFile=%outDirName%\%dirName%\plugins\solarflare-service.jar
set PathToWARFile=%outDirName%\%dirName%\plugins\solarflare-ui.war

echo.
echo - Unzipping %dirName%.zip ...
echo.

cd %outDirName%
mkdir %dirName%
cd %dirName%
jar -xvf ../%dirName%.zip
cd %currentDirectory%
if ERRORLEVEL 1 goto error1

goto end1

:error1
echo.
echo - unzip failed!
exit /B 1

:end1
echo.
echo - unzip completed successfully.
echo.

echo.
echo - Signing JAR and WAR files...
echo.

keytool -list -keystore "%PathToCACerticate%" -storepass %CertificatePassword%
if ERRORLEVEL 1 goto error2

jarsigner -keystore "%PathToCACerticate%" -tsa %TimeStampURL% -storepass %CertificatePassword% "%PathToJARFile%" %AliasName%
if ERRORLEVEL 1 goto error2

jarsigner -keystore "%PathToCACerticate%" -tsa %TimeStampURL% -storepass %CertificatePassword% "%PathToWARFile%" %AliasName%
if ERRORLEVEL 1 goto error2

goto end2

:error2
echo.
echo - Sign jar and war files failed!
echo - Make sure all agruments are passing correctly.
rmdir /s /q "%outDirName%\%dirName%\"
exit /B 1

:end2
echo.
:: delete zip file after unzipping successfully
if EXIST "%outDirName%\%dirName%.zip" ( 
del /s /q "%outDirName%\%dirName%.zip" 
) ELSE ( 
echo "ZIP file not exist" 
)
echo - Sign JAR and WAR files completed successfully.
echo.

echo.
echo - Zipping %dirName% ...
echo.

jar -cvMf %outDirName%\%dirName%.zip -C %outDirName%\%dirName%\ .
if ERRORLEVEL 1 goto error3

rmdir /s /q "%outDirName%\%dirName%\"
if ERRORLEVEL 1 goto error3

goto end3

:error3
echo.
echo - zip failed!
exit /B 1

:end3
echo.
echo - zip completed successfully.
echo.


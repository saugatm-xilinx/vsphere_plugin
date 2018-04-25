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

for /R "..\..\dist\Tomcat_Server\webapps\solarflare-vcp\" %%f in (*.zip) do (
	set dirName=%%~nf
)

set PathToJARFile=..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%\plugins\solarflare-service.jar
set PathToWARFile=..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%\plugins\solarflare-ui.war

echo.
echo - Unzipping %dirName%.zip ...
echo.

powershell -command "Expand-Archive -Path '..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%.zip' -DestinationPath '..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%\'"
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

keytool -list -storetype pkcs12 -keystore "%PathToCACerticate%" -storepass %CertificatePassword%
if ERRORLEVEL 1 goto error2

jarsigner -storetype pkcs12 -tsa %TimeStampURL% -keystore "%PathToCACerticate%" -storepass %CertificatePassword% "%PathToJARFile%" %AliasName%
if ERRORLEVEL 1 goto error2

jarsigner -storetype pkcs12 -tsa %TimeStampURL% -keystore "%PathToCACerticate%" -storepass %CertificatePassword% "%PathToWARFile%" %AliasName%
if ERRORLEVEL 1 goto error2

goto end2

:error2
echo.
echo - Sign jar and war files failed!
echo - Make sure all agruments are passing correctly.
rmdir /s /q "..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%\"
exit /B 1

:end2
echo.
:: delete zip file after unzipping successfully
if EXIST "..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%.zip" ( 
del /s /q "..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%.zip" 
) ELSE ( 
echo "ZIP file not exist" 
)
echo - Sign JAR and WAR files completed successfully.
echo.

echo.
echo - Zipping %dirName% ...
echo.

powershell -command "Compress-Archive -Path '..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%\*' -DestinationPath '..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%.zip'"
if ERRORLEVEL 1 goto error3

rmdir /s /q "..\..\dist\Tomcat_Server\webapps\solarflare-vcp\%dirName%\"
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


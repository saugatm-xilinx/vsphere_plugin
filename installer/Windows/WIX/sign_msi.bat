@echo off

set PRODUCT_DESC="Solarflare VCSA Web Plugin"

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
if "%1" equ "-jdkpath" goto A
if "%1" equ "-sha1" goto B
if "%1" equ "-tsu" goto C
if "%1" equ "-v" goto D
if "%1" equ "-sign" goto E

echo Incorrect Parameter
goto paramError

:A
    shift /1
    set JDKPath=%1
    goto next

:B
    shift /1
    set SHA1Fingerprint=%1
    goto next

:C
    shift /1
    set TimeStampURL=%1
    goto next

:D
    shift /1
    set ProductVersion=%1
    goto next
	
:E
    shift /1
    set SignToolPath=%1
    goto next
	
:done

echo.
echo - Signing msi ...
echo.

%SignToolPath%\signtool.exe sign  /tr %TimeStampURL% /d %PRODUCT_DESC% /sha1 %SHA1Fingerprint% /fd sha256  /td sha256 "..\build\Solarflare_VCP_%ProductVersion%_Installer.msi"


if ERRORLEVEL 1 goto error

goto end

:error
echo.
echo - Sign failed!
echo - Make sure all agruments are passing correctly. 
exit /B 1

:end
echo.
echo - Sign msi completed successfully.
echo.

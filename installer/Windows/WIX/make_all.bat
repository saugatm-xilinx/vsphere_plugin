@echo off

if ["%1"]==[""] goto usage 

set buildType=%1
if "%buildType%" equ "local" (
echo Building local installer
set TimeStampURL="http://timestamp.globalsign.com/scripts/timestamp.dll"
set LabelId="836E39711EBF5F40"
set SignToolPath="C:\Program Files (x86)\Windows Kits\8.0\bin\x64"
:: Variable ProductVersion is set by scripts/createtag script
set ProductVersion=1.2.0.1009
set SHA1Fingerprint=E9A92BF58F1C669E09B53CE2CD00521FDE1404AF
set JDKPATH="C:\Program Files\Java\jdk1.8.0_162\bin"
) else if "%buildType%" equ "release" (
echo Building release installer
set TimeStampURL="http://timestamp.globalsign.com/scripts/timestamp.dll"
set LabelId="te-d933afc6-2922-4ac6-9226-426035a34c17"
set SignToolPath="C:\Program Files (x86)\Windows Kits\10\bin\10.0.17134.0\x64\"
:: Variable ProductVersion is set by scripts/createtag script
set ProductVersion=1.2.0.1009
set SHA1Fingerprint=CB42DC129745B195C0E27DC3BB26E45208A1B470
set JDKPATH="C:\Program Files\Java\jdk1.8.0_172\bin"
) else (
echo Parameter %buildType% not valid
goto usage
)

call "sign_jar_war.bat"  -jdkpath %JDKPATH% -tsu %TimeStampURL%  -label %LabelId% 
if %ERRORLEVEL% neq 0 goto error
call "setup_make.bat" -v %ProductVersion% 
if %ERRORLEVEL% neq 0 goto error
call "sign_msi.bat" -jdkpath %JDKPATH% -tsu %TimeStampURL% -sign %SignToolPath% -v %ProductVersion% -sha1 %SHA1Fingerprint%
if %ERRORLEVEL% neq 0 goto error

echo VCSA plugin Installer signing completed 
exit /B 0

:usage
echo Please enter the option for release or local build: 
echo make_all.bat release/local
exit /B 1

:error
echo Build Failed
exit /B 1

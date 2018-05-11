@echo off

:: change the values accordingly
set PathToCACerticate="<Path to certificate(.pfx) file>"
set CertificatePassword="<certificate password>"
set TimeStampURL="<time stamp URL>"
set AliasName="<alias name associated with CA certificate>"
set SignToolPath="<Path to folder having signtool.exe>"
set ProductVersion=<msi version>

call "sign_jar_war.bat" -cert %PathToCACerticate% -passwd %CertificatePassword% -tsu %TimeStampURL% -alias %AliasName%
call "setup_make.bat" -v %ProductVersion% 
call "sign_msi.bat" -cert %PathToCACerticate% -passwd %CertificatePassword% -tsu %TimeStampURL% -sign %SignToolPath% -v %ProductVersion%
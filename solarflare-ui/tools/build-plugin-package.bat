@echo off
REM --- Windows script
REM --- (if Ant runs out of memory try defining ANT_OPTS=-Xmx512M)

@setlocal
@set argC=0
@set help = "help"
@set size=0
for %%x in (%*) do Set /A argC+=1
IF %argC% == 0 echo No arguments provided...do build-plugin-package.bat help

@IF %1% == help (
    echo "For publishing files on ivy ..."
    echo build-plugin-package.bat publish username password remote_machine_name fw_family revison_tag
    echo "For Building the package only ..."
    echo build-plugin-package.bat package
    goto end
)

@IF not defined ANT_HOME (
   @echo BUILD FAILED: You must set the env variable ANT_HOME to your Apache Ant folder
   goto end
)
@IF not defined VSPHERE_SDK_HOME (
   @echo BUILD FAILED: You must set the env variable VSPHERE_SDK_HOME to your vSphere Client SDK folder
   goto end
)
@IF not defined FLEX_HOME (
   @echo Using the Adobe Flex SDK files bundled with the vSphere Client SDK
   @set FLEX_HOME=%VSPHERE_SDK_HOME%\resources\flex_sdk_4.6.0.23201_vmw
)
@IF not exist "%VSPHERE_SDK_HOME%\libs\vsphere-client-lib.swc" (
   @echo BUILD FAILED: VSPHERE_SDK_HOME is not set to a valid vSphere Client SDK folder
   @echo %VSPHERE_SDK_HOME%\libs\vsphere-client-lib.swc is missing
   goto end
)
    @echo "Building package files ......."
    @call "%ANT_HOME%\bin\ant" -f %~dp0\build-plugin-package.xml

@set publish="publish"
@IF %1% == publish (
   @echo "Publishing files on ivy........."
   wget -q http://source.uk.solarflarecom.com/hg/incoming/esxi_sfc/rawfile/firmware_vib/fetch_image.c
   wget -q http://source.uk.solarflarecom.com/hg/incoming/esxi_sfc/rawfile/firmware_vib/fetch_image.h
   wget -q http://source.uk.solarflarecom.com/hg/incoming/esxi_sfc/rawfile/firmware_vib/fetch_firmware_image.py
   gcc -shared fetch_image.h -o libfetch_image.dll fetch_image.c
   python fetch_firmware_image.py ../../installer/dist/Tomcat_Server/webapps/ %5 %2 %4
   plink -ssh -l %2 -pw %3 chisel.uk.solarflarecom.com "mkdir firmware_publish;chmod -R 777 firmware_publish"
   @echo Please wait ...it's going to take some time....
   pscp -q -r -l %2 -pw %3 ../../installer chisel.uk.solarflarecom.com:/home/%2%/firmware_publish
   plink -ssh -l %2 -pw %3 chisel.uk.solarflarecom.com "cd firmware_publish; zip -r installer.zip installer/; mv installer.zip installer/Scripts/"
   plink -ssh -l %2 -pw %3 chisel.uk.solarflarecom.com "cd firmware_publish/installer/Scripts/; python publish_to_ivy.py -r %6; cd ../../../; rm -rf firmware_publish"

   del fetch_image.c
   del fetch_image.h
   del fetch_firmware_image.py
   del libfetch_image.dll
)

:end

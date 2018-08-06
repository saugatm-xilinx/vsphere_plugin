1.  Unzip the zipped file to a particular directory.
2.  Two subfolders are created - 
    Windows 
    dist
3.  The dist directory contains the jar/war files, 
    dat files, tomcat server and other items to be packaged.
4.  The Windows subfolder has two subfolders-
4.a images - images required for the Windows msi installer
4.b WIX - This contains all the scripts for signing and building the installer.

5.  In Wix we have a script make_all.bat The make_all.bat script takes one
    input: "release type" which can be of value either "local" or "release".
    For signing with Solarflare Certificate, "release type" parameter has to  be 
    set to "release".
6.  In this script all the variables are defined.
    The following variables have been modifed as per your set up
	
	LabelId="A5D13503AD7E9DB2"
	SignToolPath="C:\Program Files (x86)\Windows Kits\10\bin\10.0.17134.0\x64\"
	SHA1Fingerprint=96B043779AF1A21B9AC47B7EA80769823C1FC2CD
	JDKPATH="C:\Program Files\Java\jdk1.8.0_172\bin"
	
	Other Variables which are set are -
	TimeStampURL="http://timestamp.globalsign.com/scripts/timestamp.dll"
	ProductVersion=1.2.0.0002 ---> This shall be updated for every release
7.  The make_all.bat script calls three different scripts with the above arguments -
7.a sign_jar_war.bat - Signs the jar/war file.
7.b setup_make.bat - Creates the Windows installer.
7.3 sign_msi.bat - Signs the installer.

8.  Execute the make_all.bat in command prompt in administrtor mode.
9`  The output msi file is created in Windows\build subfolder.

Debugging -
1. Please ensure all Paths are set correctly.
2. The environment variable path should have the WIX toolset & JDK bin folder path.
3. In <JDK_PATH>/bin eToken.cfg is copied.
4. The Etoken is inserted correctly.



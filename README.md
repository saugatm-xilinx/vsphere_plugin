Generated Plugin
================
This generated plugin uses a new tech stack and development process:
- Angular, see https://angular.io/
- Typescript, see https://www.typescriptlang.org/docs/tutorial.html
- VMware Clarity, see https://vmware.github.io/clarity/
- Angular-CLI, see https://github.com/angular/angular-cli


[Section #1]
=============
#### Setting up build process
*Prerequisites*: 
- Java 8 and Apache ANT
- Install vSphere Client SDK 6.5, or latest SDK Fling version (https://labs.vmware.com/flings/vsphere-html5-web-client)
- Install node.js version 6.9 or higher (https://nodejs.org/en/)
- Install Angular-CLI by following [these instructions](https://github.com/angular/angular-cli#installation). [command:  npm install -g @angular/cli]
   

## 1. To build the project you need to create an environment variable VSPHERE_SDK_HOME.
	
	VSPHERE_SDK_HOME : Path to sdk directory where you downloaded and unzipped the sdk files.
		(Ex. D:\VMware\SDK\html-client-sdk)
	
## 2. Clone repo from http://code.sfguest.lan/source/vsphere_plugin on your local machine. Select branch for ex. P2.
	Make sure any directory name doesn't have any space in checkout path. (Directory like 'solarflare vcp' gives error while building the project due to space in directory name.) 

## 3. Install npm Dependencies 
	• Move to the \vsphere_plugin\solarflare-ui directory in terminal or command prompt.
	• Install npm dependencies by running: npm install 

## 4. Change plugin version if required in \vsphere_plugin\solarflare-ui\tools\build-plugin-package.xml file. 

## 5. Open a command prompt in \vsphere_plugin\solarflare-ui\tools\ directory and run build-plugin-package.bat (or .sh) script. 
    • It will create plugin zip file in \vsphere_plugin\installer\dist\Tomcat_Server\webapps directory.


[Section #2]
=============
#### Packaging latest firmware binaries
    • Create 'firmware' folder (if not exists) under  \vsphere_plugin\installer\dist\Tomcat_Server\ path and put FirmwareMetadata.json and latest binary files in it.
    
[Section #3]
=============
#### Build Windows Installer
    • Follow 'BuildInformation_Windows MSI_Installer' doc present in \vsphere_plugin\installer\Windows directory. 


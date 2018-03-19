This directory should contain the following dirs.

source/
├── jre
├── tomcat
├── utility
└── vcp
	├── firmware
	│   └── firmware
	│       ├── FirmwareMetadata.json
	│       ├── bootrom
	│       │   └── SFC9140.dat
	│       └── controller
	│           └── mcfw.dat
	├── plugin-registration.war
	└── solarflare-vcp
	    └── solarflare-1.97.0.zip


Please remove this file before generating rpm


'jre' directory should consist of JRE package with LICENSE included

'tomcat' directory should consist of tomcat 6/7/8/9 with LICENSE included. server.xml in tomcat/conf should have SSL(8443) open with certstore.jks configured.

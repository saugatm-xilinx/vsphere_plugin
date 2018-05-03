####Dependencies for Package building (RHEL/CentOs) : rpm-build rpmdevtools

**generate**

* It will copy contents of source directory inside DEB and RPM
  under a temporary folder src.
* And call the scripts buildrpm.sh 

<p> How to run the script: </p>

 > $ ./generate software-version release-version

* * *

**buildrpm.sh**
* Creates .rpmmacros file which contains %_topdir macro.
* Creates rpm directory structure SOURCES, SPECS
* Make *TEMPLATE* changes for global variables in spec file
* Generates source tar.gz from src/vcp and places it in SOURCES directory.
* Build the rpm package.

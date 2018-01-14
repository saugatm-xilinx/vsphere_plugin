#!/bin/bash -x
#
# Copyright 2017 Solarflare
#
# Author: Ashish Koushik (ashish.koushik@msystechnologies.com)
#
# Description:
# This script builds .rpm (EL and OpenSUSE) for Solarflare VCP.
#
#
# ----------------------------------------------------------------------------
# If there were errors produced in the script, it will be duly noted, check
#
#              <parent-dir>/build/logs for more details.
#
# Serves as a complete dump of STDERR and STDOUT, not controlled by the script itself.
# -----------------------------------------------------------------------------
#
# ----------------------------------------------------------------------------
#   Load Current Dir in a var.
# ----------------------------------------------------------------------------
#
current_dir=$(dirname `readlink -f $0`)
# ----------------------------------------------------------------------------
# ----------------------------------------------------------------------------
#  Global Variables for RPM Build
# ----------------------------------------------------------------------------
#

RELEASE_VERSION=$2
SOFTWARE_VERSION=$1

SPEC_FILE=sfvcp.spec

# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
#  Function to replace global variable template for deb building
# ----------------------------------------------------------------------------
#
f_sed() {
          sed -i "s|$1|$2|g"  $3
        }
# ----------------------------------------------------------------------------

echo "Preparing for building package binaries for Solarflare VCenter Plugin"

# ----------------------------------------------------------------------------
#   Change Directory and copy contents to prepare building.
# ----------------------------------------------------------------------------
#
mkdir -p $current_dir/rpm/build/{SOURCES,SPECS}

rm -rf $current_dir/rpm/vcp

cp -r $current_dir/src/* $current_dir/rpm/vcp

cp $current_dir/sfvcp.spec $current_dir/rpm

cd $current_dir/rpm
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
#   Create rpm macros for user configuration.
# ----------------------------------------------------------------------------
#
echo "%_topdir $current_dir/rpm/build" > ~/.rpmmacros
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
#   Replace template variables with actual values.
# ----------------------------------------------------------------------------
#
set "$SPEC_FILE"
  for i
  do
    f_sed SOFTWARE_VERSION_TEMPLATE $SOFTWARE_VERSION $i
    f_sed RELEASE_VERSION_TEMPLATE $RELEASE_VERSION $i
  done
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
#   Create a source tar package before handing over to rpmbuild.
# ----------------------------------------------------------------------------
#
tar -czvf sfvcp-$SOFTWARE_VERSION-$RELEASE_VERSION.tar.gz vcp/ > /dev/null 2>&1

mv sfvcp-$SOFTWARE_VERSION-$RELEASE_VERSION.tar.gz $current_dir/rpm/build/SOURCES

cp sfvcp.spec $current_dir/rpm/build/SPECS/sfvcp.spec
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Actual rpm build command for el dist
# ----------------------------------------------------------------------------
#
rpmbuild -bb $current_dir/rpm/build/SPECS/sfvcp.spec
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Copy release binary to parent-dir/build
# ----------------------------------------------------------------------------
#
cp $current_dir/rpm/build/RPMS/x86_64/sfvcp-$SOFTWARE_VERSION-$RELEASE_VERSION.x86_64.rpm $current_dir/../build/el_sfvcp-$SOFTWARE_VERSION-$RELEASE_VERSION.x86_64.rpm

if [ $? -ne 0 ]; then
  echo "Oops! Looks like Enterprise Linux build was a failure. Use '$ ./generate <ver> <rel>' "
fi
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Cleanup
# ----------------------------------------------------------------------------
#
rm -rf $current_dir/rpm
rm -rf $current_dir/src
rm ~/.rpmmacros
# ----------------------------------------------------------------------------

#!/bin/sh
# This script remove all chassis related code in the current plugin project
# It must be run from the /tools directory

if [[ $1 ]]; then
    toolsDir=$1
    echo "Removing all chassis related code in this project!"
    response="y"
else
    toolsDir=`dirname $0`
    echo "Remove all chassis related code in this project? Y or N [N]"
    read response
fi
if [[ $response == "y" || $response == "Y" ]]; then
   # Remove chassis specific files
   rm -rf $toolsDir/../src/app/services/chassis
   rm -f  $toolsDir/../src/app/services/testing/fake-chassis.*
   rm -rf $toolsDir/../src/app/views/chassis-summary
   rm -f  $toolsDir/../src/app/views/modals/edit-chassis.component.*

   # Remove chassis code in all other files (.ts, .html, .xml, .properties only)
   find $toolsDir/../src -type f \( -name '*.ts' -o -name '*.html' -o -name '*.xml' -o -name '*.properties' \) \
   -exec sed -i '' -e '/removable-chassis-line/d' -e '/removable-chassis-code/,/end-chassis-code/d' {} \;

else
   echo "-> stopped"
   exit 1
fi


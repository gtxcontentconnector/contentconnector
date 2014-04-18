#!/bin/bash
REMOTE_HOST=gentics@www.gentics.com
REMOTE_DEST=/var/www/genticscom/gcc/changelog
TMPDIR=target

SCRIPT="`readlink -f $0`" 
BASEDIR="`dirname "$SCRIPT"`"

cd $BASEDIR

# Generic error check and abort method
function handleError() {
  if [ "$1" != "0" ]; then
     echo -e "\n\nERROR: $2"
     echo -e "Aborting with errorcode $1 \n\n"
     exit 10
  fi
}

echo -e "\n * Uploading Changelog. Press enter to continue"
read
  
  echo -e "\n * Cleanup"
    rm $TMPDIR/*single*.zip
    ssh $REMOTE_HOST "cd $REMOTE_DEST/changelog ; rm *.zip"
    handleError $? "Could not cleanup remote changelog directory"
  echo "Done."
  
  echo -e "\n * Transfering and extracting changelog"
    scp $TMPDIR/*.zip  $REMOTE_HOST:$REMOTE_DEST/changelog
    handleError $? "Could not transfer changelog"
    ssh $REMOTE_HOST "cd $REMOTE_DEST/changelog ; unzip -o *.zip" 
    handleError $? "Could not extract changelog"
  echo "Done."

echo "Done."

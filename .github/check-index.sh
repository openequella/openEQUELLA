# A helper function to check the size of folder 'freetext'.
function checkIndexFolderSize {
 echo `du -sh ../autotest/equella-install/freetext | cut -f 1`
}


TIMEOUT=10 # Represent 10 minutes
TIMER=0 # A timer used to check if timeout is reached

LAST_FOLDER_SIZE=`checkIndexFolderSize`
NEW_FOLDER_SIZE=

while [[ $NEW_FOLDER_SIZE != $LAST_FOLDER_SIZE ]]
do
 sleep 3m
 ((TIMER++))
 echo "timer: $TIMER"

 if [[ $TIMER -gt $TIMEOUT ]]; then
   echo "Timeout for checking freetext index files."
   exit 1
 fi

 NEW_FOLDER_SIZE=`checkIndexFolderSize`
 echo "freetext folder size: $NEW_FOLDER_SIZE"
done

echo "All freetext index files are generated."

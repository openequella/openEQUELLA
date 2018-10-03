#!/usr/bin/env bash
cd /home/equella/repo/Equella/Source/Server/equellaserver/target/
numOfZips=$(find . -maxdepth 1 -name '*.zip' | wc -l)
if [ $numOfZips -gt 0 ]
then
    newdir = /artifacts/upgrader-build-$(date)
    mv *.zip /artifacts/$newdir
    echo Moved $numOfZips to $newDir
else
    echo No upgrade zips to move!
fi

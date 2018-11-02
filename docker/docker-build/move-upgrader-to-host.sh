#!/usr/bin/env bash
cd /home/equella/repo/Equella/Source/Server/equellaserver/target/
numOfZips=$(find . -maxdepth 1 -name '*.zip' | wc -l)
if [ $numOfZips -gt 0 ]
then
    newDir=upgrader-build-$(date '+%Y-%m-%d-%H-%M-%S')
    mkdir /artifacts/$newDir
    mv *.zip /artifacts/$newDir
    echo Moved $numOfZips to /artifacts/$newDir
else
    echo No upgrade zips to move!
fi

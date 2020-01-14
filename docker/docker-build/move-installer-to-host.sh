#!/usr/bin/env bash
cd /home/oeq/repo/openEQUELLA/Installer/target/
numOfZips=$(find . -maxdepth 1 -name '*.zip' | wc -l)
if [ $numOfZips -gt 0 ]
then
    newDir=installer-build-$(date '+%Y-%m-%d-%H-%M-%S')
    mkdir /artifacts/$newDir
    mv *.zip /artifacts/$newDir
    echo Moved $numOfZips to /artifacts/$newDir
else
    echo No installer zips to move!
fi

#! /bin/bash

## Run Checkstyle for all the Java files in the project.
## If the number of errors is either greater or smaller than the threshold, output an error message and exit with status 1.

thresholdNumber=449

result=$(checkstyle -c checkstyle-config.xml -o checkstyle-report.txt . 2>&1)
echo $result

errorNumber=$(echo $result | grep -oP '\d+')
if [ $errorNumber -gt $thresholdNumber ]
then
    echo "Checkstyle error threshold ($thresholdNumber) exceeded with error count of $errorNumber"
    exit 1
elif [ $errorNumber -lt $thresholdNumber ]
then
    echo "The number of Checkstyle errors has been reduced. Threshold of $thresholdNumber has been reduced to $errorNumber. Please reduce thresholdNumber in checkstyle.sh."
    exit 1
fi

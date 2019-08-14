#!/bin/sh

echo Copying coverage dump - for later merging and report generation
aws s3 $S3_REGION_OPT cp autotest/target/jacoco.exec \
  ${S3_DEST_BUILD}coverage/jacoco-${TRAVIS_JOB_NUMBER}.exec \
  --only-show-errors

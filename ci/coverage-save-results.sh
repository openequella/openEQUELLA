#!/bin/sh

SCRIPTS_DIR=`dirname $0`

echo Copying coverage dump - for later merging and report generation
${SCRIPTS_DIR}/s3cp.sh autotest/target/jacoco.exec \
  ${S3_DEST_BUILD}coverage/jacoco-${TRAVIS_JOB_NUMBER}.exec \
  --only-show-errors

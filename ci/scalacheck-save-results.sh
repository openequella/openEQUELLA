#!/bin/sh

SCRIPTS_DIR=`dirname $0`
SUFFIX=scalacheck-$1

echo Saving Scalacheck results
aws s3 $S3_REGION_OPT cp autotest/Tests/target/test-reports \
  ${S3_DEST_BUILD}${SUFFIX}/ \
  $S3_CP_OPTS

echo Copying oEQ log
aws s3 $S3_REGION_OPT cp autotest/equella-install/logs \
  ${S3_DEST_BUILD}oeq-logs/${SUFFIX} \
  $S3_CP_OPTS

echo Saving screenshots
${SCRIPTS_DIR}/tests-save-screenshots.sh ${SUFFIX}

${SCRIPTS_DIR}/coverage-save-results.sh

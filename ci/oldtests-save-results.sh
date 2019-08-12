#!/bin/sh

SCRIPTS_DIR=`dirname $0`
S3_CP_OPTS="--only-show-errors --recursive"

echo Copying TestNG results
aws s3 $S3_REGION_OPT cp autotest/OldTests/target/testng \
  ${S3_DEST_BUILD}testng-${OLD_TEST_SUITE}/ \
  $S3_CP_OPTS

echo Copying oEQ log
aws s3 $S3_REGION_OPT cp autotest/equella-install/logs \
  ${S3_DEST_BUILD}oeq-logs/oldTests-${OLD_TEST_SUITE}/ \
  $S3_CP_OPTS

${SCRIPTS_DIR}/coverage-save-results.sh

#!/bin/sh

SCRIPTS_DIR=`dirname $0`
SUFFIX=testng-${OLD_TEST_SUITE}
if [ ${OLD_TEST_NEWUI} ]; then
    SUFFIX="${SUFFIX}-new"
fi

echo Copying TestNG results
${SCRIPTS_DIR}/s3cp.sh autotest/OldTests/target/testng \
  ${S3_DEST_BUILD}${SUFFIX}/ \
  $S3_CP_OPTS

echo Copying oEQ log
${SCRIPTS_DIR}/s3cp.sh autotest/equella-install/logs \
  ${S3_DEST_BUILD}oeq-logs/${SUFFIX}/ \
  $S3_CP_OPTS

echo Saving screenshots
${SCRIPTS_DIR}/tests-save-screenshots.sh ${SUFFIX}

${SCRIPTS_DIR}/coverage-save-results.sh

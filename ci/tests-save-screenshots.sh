#!/bin/sh

SCRIPTS_DIR=`dirname $0`
SUFFIX=$1

${SCRIPTS_DIR}/s3cp.sh autotest/Tests/target/test-reports/screenshots \
  ${S3_DEST_BUILD}screenshots/${SUFFIX}/ \
  $S3_CP_OPTS

#!/bin/sh

SUFFIX=$1

aws s3 $S3_REGION_OPT cp autotest/Tests/target/test-reports/screenshots \
  ${S3_DEST_BUILD}screenshots/${SUFFIX}/ \
  $S3_CP_OPTS

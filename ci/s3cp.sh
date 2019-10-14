#!/bin/bash

set -e

if [ -n "$AWS_ACCESS_KEY_ID" ] && [ -n "$AWS_SECRET_ACCESS_KEY" ];
then
  aws s3 $S3_REGION_OPT cp "$@"
else
  echo "AWS Credentials not present, skipping S3 copy."
fi

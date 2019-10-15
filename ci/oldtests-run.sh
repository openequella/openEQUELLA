#!/bin/sh

df -h

sbt -jvm-opts autotest/.jvmopts "project autotest" \
        installEquella startEquella configureInstall setupForTests \
        OldTests/test dumpCoverage

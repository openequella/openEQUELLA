#!/bin/sh

sbt -jvm-opts autotest/.jvmopts "project autotest" \
        installEquella startEquella configureInstall setupForTests \
        OldTests/test

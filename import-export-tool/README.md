# Import and Export Tools

## Introduction

The Import and Export Tools are used for bulk import and export of items in openEQUELLA using the
SOAP web services.

IMPORTANT: oEQ has been updated to support Java 21 in 2024.1. However, the Gradle version in this project
is not compatible with Java 21. We have tried to upgrade Gradle to v8.5 which has the full support for
Java 21, but the WSDL builder plugin does work properly with Gradle v8.5. As a result, we agree to stop
building this project on CI. We will check whether the WSDL builder plugin supports Gradle v8.5 when working
on OEQ-1786.

## Building

This part of the code base uses Gradle, but also calls out to the parent source to build some shared
code via SBT.

To build, simply run the following in this directory:

    ./gradlew build

Following which you'll find the two resultant JARs (one for the Import Tool, and one for the Export
Tool) in `build/libs`.

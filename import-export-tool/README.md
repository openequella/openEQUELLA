# Import and Export Tools

## Introduction

The Import and Export Tools are used for bulk import and export of items in openEQUELLA using the
SOAP web services.

## Building

This part of the code base uses Gradle, but also calls out to the parent source to build some shared
code via SBT.

To build, simply run the following in this directory:

    ./gradlew build

Following which you'll find the two resultant JARs (one for the Import Tool, and one for the Export
Tool) in `build/libs`.

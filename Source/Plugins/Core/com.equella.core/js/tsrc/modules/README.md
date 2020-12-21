# Modules Directory

This directory should contain all the various `XyzModule.ts` files. These
'Modules' function as the business layer. They interact with oEQ via the
`@openequella/rest-api-client`, and they should be decoupled from the
UI layer. Instead, the UI layer will utilise them to provide functionality.

## Current Situation

We currently have `XyzModule.ts` files scattered across the directory structure.
An effort will be undertaken to consolidate them here.

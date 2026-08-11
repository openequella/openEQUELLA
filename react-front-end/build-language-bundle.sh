#! /bin/bash

## Build the New UI language bundle: compile the generator with tsc, bundle it with Parcel, then
## run it to emit jsbundle.json.

set -euo pipefail

generatorSrc=build-tools/BuildLanguageBundle.ts
tscOut=target/compile-language-bundle
langDir=target/resources/lang

## Parcel owns where it writes the languageBundle target, so read the location back from its config
## rather than restating it here. Note we cannot pass --dist-dir instead: for a *named* target
## Parcel appends the target name to it, giving target/tools/languageBundle/.
bundledGeneratorDir=$(node -p "require('./package.json').targets.languageBundle.distDir")

mkdir -p "$langDir"

tsc --ignoreConfig --types node --target es2020 --module commonjs \
  --outDir "$tscOut" "$generatorSrc"

parcel build "$tscOut/build-tools/BuildLanguageBundle.js" \
  --no-optimize --no-source-maps --target languageBundle

node "$bundledGeneratorDir/BuildLanguageBundle.js" > "$langDir/jsbundle.json"

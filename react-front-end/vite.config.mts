/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { resolve } from "node:path";
import { defineConfig } from "vite";

const UPLOAD_LIST_OUT = "scripts/uploadlist.js";
const ASSETS_DIR = "assets";

// Production bundle, picked up by build.sbt's `buildReactFrontEnd`.
const PROD_OUT_DIR = "target/resources/web/reactjs";
// Dev bundle, written straight into the plugin's compiled classes so a running
// server serves it - in dev mode RenderNewTemplate reparses the entry HTML on
// every request instead of caching it.
const DEV_OUT_DIR =
  "../Source/Plugins/Core/com.equella.core/target/scala-2.13/classes/web/reactjs";

const projectDir = import.meta.dirname;
const entrypointDir = resolve(projectDir, "entrypoint");

const isDevMode = (mode: string) => mode === "development";

export default defineConfig(({ mode }) => ({
  builder: {
    async buildApp(builder) {
      // "client" is Vite's implicit default environment - it needs no entry
      // under `environments` below, and the top-level `build` block is its
      // configuration.
      // Order matters: it empties the shared output directory.
      await builder.build(builder.environments.client);
      await builder.build(builder.environments.uploadList);
    },
  },
  root: entrypointDir,
  // Use relative base path.
  base: "./",
  // entrypoint/public holds the pages that have no <script src> of their own -
  // they render only as a body fragment with an inline
  // `window["oEQRender"].xxxPage()` call, relying on the main "index" bundle
  // already being loaded on the page (e.g. inside Selection Session).
  // Having no module entry, they cannot be rolldown inputs, so publicDir
  // copies them across untouched instead. The backend loads each by filename
  // via RenderNewTemplate.parseEntryHtml, so the names must survive.
  publicDir: "public",
  // .env files live next to package.json rather than in `root` (entrypoint/),
  // which is where envDir would otherwise point.
  envDir: projectDir,
  build: {
    // Resolved to an absolute path because outDir defaults to being resolved
    // against `root` (entrypoint/ above) - a relative string here would land
    // at entrypoint/target/...
    outDir: resolve(projectDir, isDevMode(mode) ? DEV_OUT_DIR : PROD_OUT_DIR),
    // Directory relative from `outDir` where the built js/css/image assets will be placed.
    assetsDir: ASSETS_DIR,
    // Skipped in dev mode because that is the mode the `dev` script watches in,
    // and under `--watch` `builder.build()` resolves as soon as the watcher is
    // up rather than once output is written. The `await`s in buildApp above
    // then guarantee nothing, and this build can end up emptying the directory
    // after the uploadList environment has written its file into it.
    emptyOutDir: !isDevMode(mode),
    // Syntax floor for the whole bundle, dependencies included. Driven by our
    // browser support policy - Edge, Chrome and Firefox, all evergreen - not by
    // tsconfig's compilerOptions.target. Vite never invokes tsc, so the two are
    // independent knobs that we keep aligned by hand.
    target: "es2022",
    // Sized just above RichTextEditor (~1.6MB, bundles TinyMCE), our largest
    // chunk. Both it and Template (~780kB) are lazily loaded rather than in an
    // entry's preload list, so warning about them is noise - but anything
    // bigger, or any growth in these two, still gets flagged.
    // TODO: revisit once https://github.com/vitejs/vite/issues/21276 lands -
    // there is currently no way to exempt specific chunks from this check.
    chunkSizeWarningLimit: 1700,
    rolldownOptions: {
      input: {
        index: resolve(entrypointDir, "index.html"),
        advancedSearchPage: resolve(entrypointDir, "AdvancedSearchPage.html"),
        settings: resolve(entrypointDir, "Settings.html"),
      },
    },
  },
  environments: {
    // uploadlist must be a self-contained classic script, not an ES module:
    // `UniversalWebControlNew.scala` pulls it in with an `IncludeFile`, which can
    // only emit a plain <script src>. Inlining every dependency is what makes
    // it self-contained, so it cannot share chunks with the HTML entries, and
    // the `output` format applies to a whole build - hence it needs its own environment.
    uploadList: {
      // Without this, a non-client environment externalises node_modules
      // instead of bundling them, leaving React & co. as undefined globals.
      consumer: "client",
      // FileUploaderRender's lazy import of ThemeModule pulls in Vite's
      // preload helper, which reads `import.meta.url` to locate the chunks to
      // preload - of which this build, inlining everything, has none. Doing
      // the substitution rolldown would do anyway keeps its EMPTY_IMPORT_META
      // warning quiet.
      define: { "import.meta": "{}" },
      build: {
        // This environment shares its output directory with the client one,
        // which runs first and has already written the app there. Emptying it
        // here would wipe those files, and re-copying publicDir would just
        // redo work the client build has done.
        emptyOutDir: false,
        copyPublicDir: false,
        rolldownOptions: {
          input: resolve(entrypointDir, "scripts/uploadlist.js"),
          output: {
            // The format that runs in the plain <script src> above.
            format: "iife",
            // UniversalWebControlNew.scala hardcodes the URL
            // "reactjs/scripts/uploadlist.js", so set a fixed name here as well.
            entryFileNames: UPLOAD_LIST_OUT,
          },
        },
      },
    },
  },
}));

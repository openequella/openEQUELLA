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
import { defineConfig } from "vite";
import path from "path";

export default defineConfig(({ mode }) => ({
  // swagger-ui's deps read process.env.NODE_ENV directly; without this it's undefined at runtime.
  define: {
    "process.env.NODE_ENV": JSON.stringify(
      mode === "production" ? "production" : "development",
    ),
  },
  build: {
    outDir:
      mode === "production"
        ? "target"
        : path.resolve("../target/scala-2.13/classes/web/apidocs/"),
    // dev mode's outDir also holds sbt's copied swagger-ui.css; don't wipe it on rebuild
    emptyOutDir: false,
    lib: {
      entry: "index.js",
      // iife: self-contained, immediately-invoked script for a plain <script> tag.
      formats: ["iife"],
      // required by iife/umd output.
      name: "SwaggerUIBundle",
      fileName: () => "bundle.js",
    },
  },
}));

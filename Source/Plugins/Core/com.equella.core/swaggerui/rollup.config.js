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
import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import json from "@rollup/plugin-json";
import nodePolyfills from "rollup-plugin-polyfill-node";

// `npm run build` -> `production` is true
// `npm run dev` -> `production` is false
const production = !process.env.ROLLUP_WATCH;

const outputDir = production
  ? "target/"
  : "../target/scala-2.13/classes/web/apidocs/";

export default {
  input: "index.js",
  output: {
    file: `${outputDir}bundle.js`,
    format: "iife", // immediately-invoked function expression — suitable for <script> tags
    sourcemap: !production,
    // To address issue with `global` undefined errors in console - and swagger-ui not loading
    // See https://github.com/rollup/rollup-plugin-commonjs/issues/6#issuecomment-519537010
    intro: "const global = window;",
  },
  plugins: [commonjs(), json(), nodePolyfills(), resolve()],
};

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
// This config is intentionally scoped to node_modules only. It exists purely
// so Jest can transpile ESM-only dependencies (e.g. query-string and its
// transitive deps decode-uri-component, filter-obj, split-on-first) that
// would otherwise fail to parse under Jest's CommonJS module system. Parcel
// (used for the production bundle via `build:bundle`) also auto-discovers
// this file and, unlike Jest, applies it project-wide by default - an
// unscoped @babel/preset-env would then be run over our own TypeScript/JSX
// source, which it can't parse, breaking the Parcel build. Restricting via
// `overrides` to only files under node_modules avoids touching our own
// source while still covering whichever transitive deps need transpiling.
module.exports = {
  overrides: [
    {
      test: /[\\/]node_modules[\\/]/,
      presets: ["@babel/preset-env"],
    },
  ],
};

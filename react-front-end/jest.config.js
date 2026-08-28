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
module.exports = {
  preset: "ts-jest/presets/js-with-babel",
  testEnvironment: "jsdom",
  testMatch: ["**/?(*.)+(spec|test).[jt]s?(x)"],
  setupFilesAfterEnv: ["./jest.setup.ts"],
  // One worker per physical core on both the CI runners and a typical dev machine. This lives here
  // rather than on the `npm test` command line so that it also applies when Jest is run directly -
  // from an IDE run configuration, for example - where the default of `cores - 1` oversubscribes the
  // machine badly enough to make tests time out.
  maxWorkers: "50%",
  // Tests here drive real component trees through jsdom, which is slow enough that the Jest default
  // of 5s left no headroom on CI runners roughly 3x slower per thread than a dev machine. A single
  // generous budget is deliberate: it replaces a scattering of per-file `jest.setTimeout` values
  // that had been tuned by trial and error. Keep it well clear of `asyncUtilTimeout` in
  // `jest.setup.ts`, so a failed `waitFor` reports its own much better error rather than tripping
  // this.
  testTimeout: 30000,
  transformIgnorePatterns: [
    // Using the following negative look-ahead, we're requesting that the transforms only apply to `query-string`.
    // This was required because query-string only supports ESM from v8, and it is a dependency of
    // '@openequella/rest-api-client' (oeq-ts-rest-api) which is consumed here as a local file dependency.
    "node_modules/(?!query-string)/",
  ],
  globals: {
    renderData: {
      baseResources: "p/r/2020.2.0/com.equella.core/",
      newUI: true,
      autotestMode: false,
    },
  },
  // Workaround for the failure of importing axios. Check this link(https://github.com/axios/axios/issues/5026) for details.
  moduleNameMapper: {
    "^axios$": require.resolve("axios"),
    // Mocking CSS modules as per the Jest documentation: https://jestjs.io/docs/webpack#mocking-css-modules
    // Required due to the use of pragmatic-drag-and-drop which has directly imported CSS files.
    "\\.(css|less)$": "identity-obj-proxy",
    // Mocking static files as per the jest documentation: https://jestjs.io/docs/webpack#handling-static-assets
    "\\.(png|jpg|jpeg)$": "<rootDir>/__mocks__/fileMock.ts",
  },
};

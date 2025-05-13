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
  preset: "ts-jest",
  testEnvironment: "jsdom",
  testMatch: ["**/?(*.)+(spec|test).[jt]s?(x)"],
  setupFilesAfterEnv: ["./jest.setup.ts"],
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
    // Mock the implementation of 'axios-cookiejar-support' as it is really not what the react-front-end
    // Jest tests need.
    "^axios-cookiejar-support$":
      "<rootDir>/__mocks__/axios-cookiejar-support.js",
  },
};

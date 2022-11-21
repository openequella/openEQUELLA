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
  moduleNameMapper: {
    // Since Jest v28, some tests fail to run due to the error "unexpected token: export" and this error is caused by
    // the use of 'uuidjs'. Although the latest version of 'uuidjs' (v9) fixes the error, it requires polyfills for other
    // issues. So the below workaround is the most acceptable fix for now. Please check the links for more details.
    // https://github.com/uuidjs/uuid/issues/451#issuecomment-1112328417
    // https://github.com/uuidjs/uuid#getrandomvalues-not-supported
    uuid: require.resolve("uuid"),
  },
};

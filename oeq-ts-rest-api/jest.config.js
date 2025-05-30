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
// For a detailed explanation regarding each configuration property, visit:
// https://jestjs.io/docs/en/configuration.html

module.exports = {
  clearMocks: true,
  coverageDirectory: 'coverage',
  // Required (instead of plan 'ts-jest') to support axios-cookiejar-support.
  preset: 'ts-jest/presets/js-with-babel',
  testEnvironment: 'node',
  transformIgnorePatterns: [
    // Using the following negative look-ahead, we're requesting that the transforms only apply to `axios-cookiejar-support`.
    // This was required because axios-cookiejar-support only supports ESM from v6.
    'node_modules/(?!axios-cookiejar-support)/',
  ],
  setupFiles: ['./jest.setup.ts'],
};

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
/* eslint-disable-next-line @typescript-eslint/no-var-requires */
const path = require('path');

module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es6: true,
  },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/eslint-recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:jest/recommended',
    'plugin:jest/style',
    'plugin:prettier/recommended',
  ],
  globals: {
    Atomics: 'readonly',
    SharedArrayBuffer: 'readonly',
  },
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 2018,
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint', 'notice', 'unused-imports'],
  rules: {
    'unused-imports/no-unused-imports': 'error',
    'notice/notice': [
      'error',
      {
        templateFile: `${path.resolve(__dirname)}/../licenseHeader.js`,
      },
    ],
  },
  overrides: [
    {
      files: ['*.test.ts'],
      rules: {
        // It is useful in tests to be able to use non-null-assertions - especially for values
        // which will then be checked with expect matchers.
        '@typescript-eslint/no-non-null-assertion': 'off',
      },
    },
  ],
  settings: {
    jest: {
      version: 27,
    },
  },
};

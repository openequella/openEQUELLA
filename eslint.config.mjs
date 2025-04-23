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
import { defineConfig } from "eslint/config";
import typescriptEslint from "@typescript-eslint/eslint-plugin";
import notice from "eslint-plugin-notice";
import unusedImports from "eslint-plugin-unused-imports";
import globals from "globals";
import tsParser from "@typescript-eslint/parser";
import path from "node:path";
import { fileURLToPath } from "node:url";
import js from "@eslint/js";
import { FlatCompat } from "@eslint/eslintrc";
import reactHooks from "eslint-plugin-react-hooks";
import jsxA11y from "eslint-plugin-jsx-a11y";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all,
});

export default defineConfig([
  {
    extends: compat.extends(
      "eslint:recommended",
      "plugin:@typescript-eslint/eslint-recommended",
      "plugin:@typescript-eslint/recommended",
      "plugin:react/recommended",
      "plugin:jest/recommended",
      "plugin:jest/style",
      "plugin:jsx-a11y/recommended",
      "plugin:prettier/recommended",
    ),

    plugins: {
      "@typescript-eslint": typescriptEslint,
      notice,
      "unused-imports": unusedImports,
      "react-hooks": reactHooks,
      "jsx-a11y": jsxA11y,
    },

    languageOptions: {
      globals: {
        ...globals.node,
        ...globals.browser,
        ...globals.jquery,
      },
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
      },
      parser: tsParser,
    },

    settings: {
      jest: {
        version: 29,
      },
      react: {
        version: "18",
      },
    },

    rules: {
      "no-eval": "error",
      // Disable this rule since we import a lot of variables and functions from mock file and pass it to the render function.
      "jest/no-mocks-import": "off",
      // Some legacy code uses require() to import modules.
      "@typescript-eslint/no-require-imports": "off",
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          // Ignore the arg "_".
          argsIgnorePattern: "^_$",
          ignoreRestSiblings: true,
        },
      ],
      "@typescript-eslint/no-unused-expressions": "off",
      "jsx-a11y/no-autofocus": "off",
      "jest/consistent-test-it": "error",
      "jest/require-top-level-describe": "error",
      "jest/expect-expect": [
        "warn",
        {
          assertFunctionNames: ["expect*"],
        },
      ],
      "notice/notice": [
        "error",
        {
          templateFile: "licenseHeader.js",
        },
      ],
      "no-restricted-syntax": [
        "error",
        {
          selector:
            "ImportDeclaration[source.value='react'] :matches(ImportDefaultSpecifier)",
          message: "Use namespace import to import React",
        },
      ],
      "react/display-name": "off",
      "react/jsx-key": "warn",
      "react/jsx-boolean-value": "error",
      "react/jsx-curly-brace-presence": "error",
      "react/jsx-fragments": "error",
      "react/jsx-no-useless-fragment": "error",
      "react/prefer-stateless-function": "error",
      "react-hooks/exhaustive-deps": "error",
      "unused-imports/no-unused-imports": "error",
      "react/no-unescaped-entities": "off",
    },
  },
  // Overrides for files in react-front-end.
  {
    files: [
      "react-front-end/tsrc/**/*.{ts,tsx}",
      "react-front-end/__test__/**/*.{ts,tsx}",
      "react-front-end/__stories__/**/*.{ts,tsx}",
      "react-front-end/__mocks__/**/*.{ts,tsx}",
    ],
    languageOptions: {
      parserOptions: {
        project: "react-front-end/tsconfig.json",
      },
    },
    rules: {
      "no-var": "error",
      "prefer-const": "error",
      "@typescript-eslint/consistent-type-definitions": ["error", "interface"],
      "@typescript-eslint/no-explicit-any": "error",
      "@typescript-eslint/no-inferrable-types": "error",
      "@typescript-eslint/no-non-null-assertion": "error",
      "@typescript-eslint/no-unnecessary-type-assertion": "error",
      "@typescript-eslint/prefer-optional-chain": "error",
    },
  },
  {
    files: ["**/*.test.ts"],

    rules: {
      // It is useful in tests to be able to use non-null-assertions - especially for values
      // which will then be checked with expect matchers.
      "@typescript-eslint/no-non-null-assertion": "off",
    },
  },
  {
    ignores: ["**/node_modules", "**/target", "**/output"],
  },
]);

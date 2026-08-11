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
import type { StorybookConfig } from "@storybook/react-vite";

const config: StorybookConfig = {
  staticDirs: [
    // TinyMCE skin CSS is needed statically (for RichEditor.stories.tsx).
    { from: "../node_modules/tinymce/skins", to: "/tinymce/skins" },
    "../__stories__/static-files",
  ],
  stories: ["../__stories__/**/*.tsx"],
  addons: ["@storybook/addon-a11y", "@storybook/addon-docs"],
  framework: "@storybook/react-vite",
  typescript: {
    // Explicitly set reactDocgen to react-docgen-typescript, since our root
    // babel.config.json will make the default reactDocgen (babel-based) skip its
    // typescript/jsx parser plugins - it only checks that a babel config file
    // exists, not whether it actually applies here.
    reactDocgen: "react-docgen-typescript",
    reactDocgenTypescriptOptions: {
      exclude: [
        // "**/*.stories.tsx" is just this plugin's own default exclude, repeated
        // here because setting `exclude` replaces the default instead of extending it.
        "**/*.stories.tsx",
        // ".storybook/**" is the pattern we're actually adding: without it,
        // preview.tsx matches **/*.tsx and reaches the plugin's docgen step, which
        // then can't find it in its resolved TypeScript project files and logs a
        // "not included in the active TypeScript project" warning instead of just
        // skipping it quietly.
        ".storybook/**",
      ],
    },
  },
};
export default config;

import type { StorybookConfig } from '@storybook/react-webpack5';
import * as path from 'path';

const config: StorybookConfig = {
  staticDirs: ["../node_modules", "../__stories__/static-files"],
  stories: ["../__stories__/**/*.tsx"],
  addons: [
    "@storybook/addon-essentials",
    "@storybook/addon-a11y",
    "@storybook/addon-controls",
    "@storybook/addon-webpack5-compiler-swc",
  ],
  framework: {
    name: '@storybook/react-webpack5',
    options: { fastRefresh: true },
  },
  babel: (options) => ({
    ...options,
    configFile: path.resolve(__dirname, '.babelrc.json'),
  }),
};
export default config;

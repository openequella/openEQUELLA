module.exports = {
  staticDirs: ["../node_modules", "../__stories__/static-files"],
  stories: ["../__stories__/**/*"],
  addons: [
    "@storybook/addon-essentials",
    "@storybook/addon-a11y",
    "@storybook/addon-controls",
  ],
};

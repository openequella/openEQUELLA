module.exports = {
  testEnvironment: "jsdom",
  snapshotSerializers: ["enzyme-to-json/serializer"],
  setupFilesAfterEnv: ["<rootDir>/tsrc/setupEnzyme.js"],
  testPathIgnorePatterns: ["<rootDir>/target", ".[t]sx?$"]
};

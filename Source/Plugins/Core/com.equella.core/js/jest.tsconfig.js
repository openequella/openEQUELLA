module.exports = {
  preset: "ts-jest",
  testEnvironment: "jsdom",
  snapshotSerializers: ["enzyme-to-json/serializer"],
  setupFilesAfterEnv: ["<rootDir>/tsrc/setupEnzyme.js"],
  testPathIgnorePatterns: ["<rootDir>/target", ".[j]sx?$"]
};

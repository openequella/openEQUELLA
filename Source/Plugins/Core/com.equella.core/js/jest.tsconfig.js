module.exports = {
  displayName: "TypeScript tests",
  preset: "ts-jest",
  testEnvironment: "jsdom",
  snapshotSerializers: ["enzyme-to-json/serializer"],
  setupFilesAfterEnv: ["<rootDir>/tsrc/setupEnzyme.js"],
  testPathIgnorePatterns: ["<rootDir>/target", ".[j]sx?$"]
};

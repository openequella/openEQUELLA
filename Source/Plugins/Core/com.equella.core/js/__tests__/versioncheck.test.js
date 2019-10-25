// versioncheck.js is not processed by Parcel and it only serves to the browser.
// So here need to read the file and then evaluate its content
const fs = require("fs");
const version_check_js_file = fs.readFileSync(
  "../resources/web/js/versioncheck.js",
  "utf8"
);
eval(version_check_js_file);

const mockData = require("../__mocks__/versioncheck_mock_data");
global.$ = require("jquery");

test("Test major updates", () => {
  const checkResult = createCheckResult("2018.1.0", mockData.mockReleases);
  expect(checkResult.newer).toBe(true);
  expect(checkResult.newerReleases.majorUpdate.major).toBe("2019");
});

test("Test minor updates", () => {
  const checkResult = createCheckResult("2019.1.1", mockData.mockReleases);
  expect(checkResult.newer).toBe(true);

  const minorUpdateRelease = checkResult.newerReleases.minorUpdate;
  expect(minorUpdateRelease.major).toBe("2019");
  expect(minorUpdateRelease.minor).toBe("2");
});

test("Test patch updates", () => {
  const checkResult = createCheckResult("2019.2.0", mockData.mockReleases);
  expect(checkResult.newer).toBe(true);

  const patchUpdateRelease = checkResult.newerReleases.patchUpdate;
  expect(patchUpdateRelease.major).toBe("2019");
  expect(patchUpdateRelease.minor).toBe("2");
  expect(patchUpdateRelease.patch).toBe("1");
});

test("Test no updates", () => {
  const checkResult = createCheckResult("2020.1.0", mockData.mockReleases);
  expect(checkResult.newer).toBe(false);

  expect(checkResult.newerReleases.majorUpdate).toBe(null);
  expect(checkResult.newerReleases.minorUpdate).toBe(null);
  expect(checkResult.newerReleases.patchUpdate).toBe(null);
});

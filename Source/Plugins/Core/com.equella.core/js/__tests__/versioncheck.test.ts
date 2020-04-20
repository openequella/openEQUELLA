import * as mockData from "../__mocks__/versioncheck_mock_data";
import rewire = require("rewire");

const versioncheck = rewire("../../resources/web/js/versioncheck.js");
const createCheckResult = versioncheck.__get__("createCheckResult");

describe("versioncheck", () => {
  describe("createCheckResult", () => {
    it("should support major updates", () => {
      const checkResult = createCheckResult("2018.1.0", mockData.mockReleases);
      expect(checkResult.newer).toBe(true);
      expect(checkResult.newerReleases.majorUpdate.major).toBe("2019");
    });

    it("should support minor updates", () => {
      const checkResult = createCheckResult("2019.1.1", mockData.mockReleases);
      expect(checkResult.newer).toBe(true);

      const minorUpdateRelease = checkResult.newerReleases.minorUpdate;
      expect(minorUpdateRelease.major).toBe("2019");
      expect(minorUpdateRelease.minor).toBe("2");
    });

    it("should support patch updates", () => {
      const checkResult = createCheckResult("2019.2.0", mockData.mockReleases);
      expect(checkResult.newer).toBe(true);

      const patchUpdateRelease = checkResult.newerReleases.patchUpdate;
      expect(patchUpdateRelease.major).toBe("2019");
      expect(patchUpdateRelease.minor).toBe("2");
      expect(patchUpdateRelease.patch).toBe("1");
    });

    it("should do nothing when there are no updates", () => {
      const checkResult = createCheckResult("2020.1.0", mockData.mockReleases);
      expect(checkResult.newer).toBe(false);

      expect(checkResult.newerReleases.majorUpdate).toBeNull();
      expect(checkResult.newerReleases.minorUpdate).toBeNull();
      expect(checkResult.newerReleases.patchUpdate).toBeNull();
    });
  });
});

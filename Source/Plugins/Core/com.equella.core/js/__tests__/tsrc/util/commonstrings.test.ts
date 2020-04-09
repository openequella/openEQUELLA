import { commonString } from "../../../tsrc/util/commonstrings";

describe("commonstrings", () => {
  describe("commonString", () => {
    it("should be an object", () => {
      expect(typeof commonString).toBe("object");
    });
  });
});

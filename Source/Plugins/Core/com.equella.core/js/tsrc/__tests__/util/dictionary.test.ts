import { properties } from "../../util/dictionary";

describe("dictionary", () => {
  describe("properties", () => {
    it("should handle empty object", () => {
      expect(properties({})).toEqual([]);
    });

    it("should handle handle own properties", () => {
      expect(properties({ test: true })).toEqual(["test"]);
    });
  });
});

import { isInteger } from "../../util/validation";

describe("validation", () => {
  describe("isInteger", () => {
    it("should handle not required undefined", () => {
      expect(isInteger()).toBe(true);
    });

    it("should handle integer", () => {
      expect(isInteger(1)).toBe(true);
    });

    it("should recognize a non integer", () => {
      expect(isInteger(1.1)).toBe(false);
    });

    it("should handle undefined when required", () => {
      expect(isInteger(undefined, true)).toBe(false);
    });

    it("should handle negative value with positive validation", () => {
      expect(isInteger(-1, true, true)).toBe(false);
    });
  });
});

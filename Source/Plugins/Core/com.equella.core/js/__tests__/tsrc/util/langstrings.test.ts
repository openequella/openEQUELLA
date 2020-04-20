import {
  formatSize,
  Sizes,
  prepLangStrings,
  initStrings
} from "../../../tsrc/util/langstrings";

describe("langstrings", () => {
  describe("formatSize", () => {
    const strings: Sizes = {
      zero: "zero",
      one: "one",
      more: "more"
    };

    it("should format zero", () => {
      expect(formatSize(0, strings)).toBe("zero");
    });

    it("should format one", () => {
      expect(formatSize(1, strings)).toBe("one");
    });

    it("should format more", () => {
      expect(formatSize(40, strings)).toBe("more");
    });

    it("should replace placeholders", () => {
      expect(formatSize(0, { ...strings, zero: "%1$o" })).toBe("0");
    });
  });

  describe("prepLangStrings", () => {
    it("should handle undefined bundle", () => {
      expect(prepLangStrings("example", { string: "test" })).toEqual({
        string: "test"
      });
    });

    it("should handle bundle", () => {
      Object.defineProperty(global, "bundle", {
        value: { testPrefix: "example" },
        writable: true
      });

      expect(prepLangStrings("testPrefix", { string: "test" })).toEqual({
        string: "test"
      });

      Object.defineProperty(global, "bundle", {
        value: undefined,
        writable: true
      });
    });

    describe("initStrings", () => {
      it("should not throw an exception", () => {
        expect(() => initStrings()).not.toThrow();
      });
    });
  });
});

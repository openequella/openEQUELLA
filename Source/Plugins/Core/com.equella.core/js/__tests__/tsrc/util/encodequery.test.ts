import { encodeQuery } from "../../../tsrc/util/encodequery";

describe("encodequery", () => {
  describe("encodeQuery", () => {
    it("should handle empty query", () => {
      expect(encodeQuery({})).toBe("");
    });

    it("should handle string", () => {
      expect(encodeQuery({ example: "string" })).toBe("?example=string");
    });

    it("should handle string array", () => {
      expect(encodeQuery({ example: ["one", "two"] })).toBe(
        "?example=one&example=two"
      );
    });

    it("should handle boolean", () => {
      expect(encodeQuery({ example: false })).toBe("?example=false");
    });

    it("should handle number", () => {
      expect(encodeQuery({ example: 3 })).toBe("?example=3");
    });

    it("should handle undefined", () => {
      expect(encodeQuery({ example: undefined })).toBe("");
    });
  });
});

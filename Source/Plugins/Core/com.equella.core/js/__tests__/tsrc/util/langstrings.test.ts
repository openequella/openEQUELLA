/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {
  formatSize,
  Sizes,
  prepLangStrings,
  initStrings,
} from "../../../tsrc/util/langstrings";

describe("langstrings", () => {
  describe("formatSize", () => {
    const strings: Sizes = {
      zero: "zero",
      one: "one",
      more: "more",
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
        string: "test",
      });
    });

    it("should handle bundle", () => {
      Object.defineProperty(global, "bundle", {
        value: { testPrefix: "example" },
        writable: true,
      });

      expect(prepLangStrings("testPrefix", { string: "test" })).toEqual({
        string: "test",
      });

      Object.defineProperty(global, "bundle", {
        value: undefined,
        writable: true,
      });
    });

    describe("initStrings", () => {
      it("should not throw an exception", () => {
        expect(() => initStrings()).not.toThrow();
      });
    });
  });
});

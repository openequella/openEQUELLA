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
  isInteger,
  isNonEmptyString,
  isValidURL,
} from "../../../tsrc/util/validation";

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

  describe("isValidURL", () => {
    it("should handle random text", () => {
      expect(isValidURL("httpppp://test.com")).toBe(false);
    });

    it("should handle random value", () => {
      expect(isValidURL(123456)).toBe(false);
    });

    it("should recognize http://", () => {
      expect(isValidURL("http://test2.comn")).toBe(true);
    });

    it("should recognize https://", () => {
      expect(isValidURL("https://test3.org")).toBe(true);
    });
  });

  describe("isNonEmptyString", () => {
    it("should handle random value", () => {
      expect(isNonEmptyString(true)).toBe(false);
    });

    it("should handle empty string", () => {
      expect(isNonEmptyString("")).toBe(false);
    });

    it("should handle non-empty string", () => {
      expect(isNonEmptyString("test")).toBe(true);
    });
  });
});

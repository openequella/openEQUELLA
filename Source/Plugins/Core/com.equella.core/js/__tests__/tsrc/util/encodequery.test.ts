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

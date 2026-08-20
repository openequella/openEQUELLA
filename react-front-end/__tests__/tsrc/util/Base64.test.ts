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
import { decodeBase64, encodeBase64 } from "../../../tsrc/util/Base64";
import { pipe } from "fp-ts/function";

// Each case pairs a plain string with its standard base64 encoding.
const cases = [
  {
    name: "empty string",
    plain: "",
    encoded: "",
  },
  {
    name: "ASCII text",
    plain: "D, David",
    encoded: "RCwgRGF2aWQ=",
  },
  {
    name: "accented characters",
    plain: "Zoë Café",
    encoded: "Wm/DqyBDYWbDqQ==",
  },
  {
    name: "non-Latin characters",
    plain: "测试",
    encoded: "5rWL6K+V",
  },
  {
    name: "characters outside the basic multilingual plane",
    plain: "🎓",
    encoded: "8J+Okw==",
  },
];

describe("encodeBase64", () => {
  it.each(cases)("encodes $name", ({ plain, encoded }) =>
    expect(encodeBase64(plain)).toBe(encoded),
  );
});

describe("decodeBase64", () => {
  it.each(cases)("decodes $name", ({ plain, encoded }) =>
    expect(decodeBase64(encoded)).toBe(plain),
  );

  it("throws for input which is not valid base64", () =>
    expect(() => decodeBase64("not base64!")).toThrow());
});

describe("encodeBase64 and decodeBase64", () => {
  it.each(cases)("round trips $name", ({ plain }) =>
    expect(pipe(plain, encodeBase64, decodeBase64)).toBe(plain),
  );
});

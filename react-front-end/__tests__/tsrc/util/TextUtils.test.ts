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
import { highlight, buildOEQServerString } from "../../../tsrc/util/TextUtils";

describe("Highlighting of Text", () => {
  const className = "highlighted";
  it.each([
    [
      "The quick brown fox jumps over the lazy dog",
      ["The", "fox", "dog"],
      `<span class="${className}">The</span> quick brown <span class="${className}">fox</span> jumps over <span class="${className}">the</span> lazy <span class="${className}">dog</span>`,
    ],
    [
      "The life of kelpies explained",
      ["kelp*"],
      `The life of <span class="${className}">kelpies</span> explained`,
    ],
    [
      // Test we're only matching from the start of words - i.e. `an` at the end
      // of Australian should not be highlighted.
      "There was an Australian",
      ["an*"],
      `There was <span class="${className}">an</span> Australian`,
    ],
  ])(
    "Produces a string containing the text highlighted with <span>s - %s",
    (text, highlights, expected) => {
      expect(highlight(text, highlights, className)).toEqual(expected);
    }
  );
});

describe("Build oEQ server language strings", () => {
  it("supports oEQ server defined language string formats", () => {
    const format =
      "Maximum attachment number is {0} and please remove {1} attachments.";
    expect(buildOEQServerString(format, 1, 2)).toBe(
      "Maximum attachment number is 1 and please remove 2 attachments."
    );
  });
});

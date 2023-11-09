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
  buildOEQServerString,
  highlight,
  validateGrouping,
} from "../../../tsrc/util/TextUtils";

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
    [
      // If highlight text only contains special characters, it should return the original text.
      "This is a random text",
      ["()"],
      "This is a random text",
    ],
    [
      // If highlight text contains both normal word and special characters,
      // the normal word should still be highlighted.
      "This is a random word",
      ["word ()"],
      `This is a random <span class="${className}">word</span>`,
    ],
    [
      "The quick brown fox",
      ["quick brown"],
      `The <span class="${className}">quick brown</span> fox`,
    ],
  ])(
    "Produces a string containing the text highlighted with <span>s - %s",
    (text, highlights, expected) => {
      expect(highlight(text, highlights, className)).toEqual(expected);
    },
  );
});

describe("Build oEQ server language strings", () => {
  it("supports oEQ server defined language string formats", () => {
    const format =
      "Maximum attachment number is {0} and please remove {1} attachments.";
    expect(buildOEQServerString(format, 1, 2)).toBe(
      "Maximum attachment number is 1 and please remove 2 attachments.",
    );
  });
});

describe("validateGrouping", () => {
  it.each([
    ["empty string", "", true],
    ["one double quote", '"', false],
    ["without closing double quote", '"text', false],
    ["without opening double quote", 'text"', false],
    ["without closing parenthesis", "(text", false],
    ["without opening parenthesis", "text)", false],
    ["with unmatched group symbols", '"text)', false],
    ["proper use of double quote", '"text"', true],
    ["proper use of parentheses", "(text)", true],
    ["proper mixed use of double quote and parentheses", '("text")', true],
    ["with proper mixed use of parentheses and double quote", '"(text)"', true],
    ["with an extra closing parenthesis double quote", "(text))", false],
    ["with an extra double quote", '"text""', false],
  ])(
    "given a text %s : %s the group validation result should be %s",
    (_, input, expected) => {
      expect(validateGrouping(input)).toBe(expected);
    },
  );
});

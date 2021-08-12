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
/**
 * Based on work from com.tle.web.sections.render.TextUtils
 */

const removeNonWordCharacters = (highlight: string): string =>
  highlight
    .replace(/\\/g, "")
    .replace(/\./g, "")
    .replace(/\(/g, "")
    .replace(/\)/g, "")
    .replace(/\[/g, "")
    .replace(/]/g, "")
    .replace(/\+/g, "")
    .replace(/\?/g, ".?")
    .replace(/\*/g, "\\w*")
    .replace(/\|/g, "");

const highlightsAsRegex = (highlights: string[]) =>
  highlights
    .map((h) => h.trim())
    .filter((h) => h.length > 0)
    .map((h) => removeNonWordCharacters(h))
    .filter((h) => h.length > 0)
    .join("|");

/**
 * Takes a line of text and a list of words which should be highlighted within that line. It returns
 * a `string` which contains the text, and the listed words encapsulated within `span`s
 * having the specified `class`.
 *
 * @param text Plain text to be highlighted
 * @param highlights A list of words to highlight
 * @param cssClass The CSS class to apply to the highlighting spans
 */
export const highlight = (
  text: string,
  highlights: string[],
  cssClass: string
): string => {
  const highlightsRegex = highlightsAsRegex(highlights);
  if (highlights.length < 1) {
    // Nothing to highlight
    return text;
  }

  // The follow Regex attempts to break a block of string into:
  // * Group 1 (optional) - text before that which is to be highlighted
  //   /^(.*?)/ : Non-greedily match anything from the start of the line upto the next group
  // * Group 2 - The text which should be highlighted
  //   /\b(<highlightsRegex>)\b/ : Using the generated highlightsRegex (typically in the form
  //                               of /word|word|word/) match whole words. (Using word boundary
  //                               specifiers on either side - /\b/.)
  // * Group 3 (optional) - The remaining text (after that which should be highlighted)
  //   /(.*)$/ : Match zero or more characters up to the end of the line of text.
  //
  // Basic example use cases:
  // 1. Input: "This and that other thing"; highlightsRegex: "and"
  //  Group 1: "This "
  //  Group 2: "and"
  //  Group 3: " that other thing"
  // 2. Input: "This and that other thing"; highlightsRegex: "this"
  //  Group 1: ""
  //  Group 2: "This"
  //  Group 3: " and that other thing"
  // 3. Input: "This and that other thing"; highlightsRegex: "thing"
  //  Group 1: "This and that other "
  //  Group 2: "thing"
  //  Group 3: ""
  const re = new RegExp("^(.*?)\\b(" + highlightsRegex + ")\\b(.*)$", "is");
  const highlightWords = (_text: string): string => {
    const matches = _text.match(re);
    if (!matches) {
      return _text;
    }
    const [, before, highlight, after] = matches;
    return (
      before +
      `<span class="${cssClass}">${highlight}</span>` +
      highlightWords(after)
    );
  };

  return highlightWords(text);
};

/**
 * This function is created to support building oEQ server defined language strings based on formats.
 * The format usually has one or more placeholders wrapped by curly brackets.
 * e.g. Maximum attachment number is {0} and please remove {1} attachment.
 *
 * @param format The format provided by oEQ Server
 * @param args A list of values used to replace the format's placeholders.
 */
export const buildOEQServerString = (
  format: string,
  ...args: number[]
): string =>
  format.replace(/{(\d+)}/g, (match, number) =>
    typeof args[number] !== "undefined" ? args[number].toString() : match
  );

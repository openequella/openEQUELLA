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
import * as A from "fp-ts/Array";
import * as E from "fp-ts/Either";
import { constFalse, constTrue, flow, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import * as t from "io-ts";

/**
 * Based on work from com.tle.web.sections.render.TextUtils
 */

// Removes all non-word characters from a string, except for asterisks and spaces between words.
const removeNonWordCharacters = (highlight: string): string =>
  highlight.replace(/[^\w\s*]/g, "").trim();

/**
 * Replaces asterisks in the input string with "\w*" if any non-asterisk and non-empty characters are present.
 *
 * Examples:
 * replaceAsterisks("*hello*") returns "\w*hello\w*"
 * replaceAsterisks("*") returns empty string
 * replaceAsterisks(" *") returns empty string
 * replaceAsterisks("example*text") returns "example\w*text"
 */
const replaceAsterisks = (highlight: string): string =>
  /[^\s*]/.test(highlight) ? highlight.replace(/\*/g, "\\w*") : S.empty;

/**
 * Takes a list of words/phrase and returns a regex text which will match these words/phrases.
 *
 * Since the words/phrase can be in some strange formats,
 * such as when the user's query is `"quick ()"` (including the quotation marks),
 * it will get a highlight phrase `quick (), and the regex should match the word `quick`, not `quick `.
 * Therefore, there is a trim operation after the `removeNonWordCharacters`.
 */
const highlightsAsRegex = (highlights: string[]) =>
  highlights
    .map((h) => h.trim())
    .filter((h) => h.length > 0)
    .map(removeNonWordCharacters)
    .map(replaceAsterisks)
    .filter((h) => h.length > 0)
    .join("|");

/**
 * Takes a line of text and a list of words which should be highlighted within that line. It returns
 * a `string` which contains the text, and the listed words encapsulated within `span`s
 * having the specified `class`.
 *
 * @param text Plain text to be highlighted
 * @param highlights A list of words/phrase to highlight
 * @param cssClass The CSS class to apply to the highlighting spans
 */
export const highlight = (
  text: string,
  highlights: string[],
  cssClass: string,
): string => {
  const highlightsRegex = highlightsAsRegex(highlights);
  // Return the original text if either the list of highlight or the regex generated from the list is empty.
  if (A.isEmpty(highlights) || S.isEmpty(highlightsRegex)) {
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
    typeof args[number] !== "undefined" ? args[number].toString() : match,
  );

/**
 * Given a text which has groups constructed by parentheses or double quotes, validate whether the
 * groups have a pair of opening and closing chars.
 *
 * Examples:
 * - validateGrouping('"Hello, World!') returns false, because the closing double quote is missing.
 * - validateGrouping('word A) B') returns false because the opening parenthesis is missing.
 * - validateGrouping('(word A) B') returns true.
 *
 * @return `false` if either an opening or closing char is missing; otherwise `true`.
 */
export const validateGrouping = (input: string): boolean => {
  // double quote,
  const dq = t.literal('"');
  // opening parenthesis,
  const op = t.literal("(");
  // closing parenthesis
  const cp = t.literal(")");

  const GroupingSymbolUnion = t.union([dq, op, cp]);
  type GroupingSymbol = t.TypeOf<typeof GroupingSymbolUnion>;

  // represents a pair of symbols fetched from the input string,
  // for example: `"` and `"` or `(` and `)`
  interface Grouping {
    opening: GroupingSymbol;
    closing?: GroupingSymbol;
  }

  // Check if opening symbol and closing symbol are real opening and closing symbols,
  // and then check if they are matched or not.
  const areSymbolsMatches = (
    opening: GroupingSymbol,
    closing: GroupingSymbol,
  ): boolean =>
    (dq.is(opening) && dq.is(closing)) || (op.is(opening) && cp.is(closing));

  // Split all the collected symbols into two arrays, and later check if elements are matched and are real closing and opening symbols.
  const groupingSymbols = (symbols: GroupingSymbol[]): Grouping[] =>
    pipe(
      symbols,
      A.splitAt(A.size(symbols) / 2),
      ([opening, closing]) => [opening, A.reverse(closing)],
      ([opening, closing]) =>
        opening.map((openingSymbol, i) => ({
          opening: openingSymbol,
          closing: pipe(closing, A.lookup(i), O.toUndefined),
        })),
    );

  /**
   * Reduce function to process each symbol group, checking if it has correct closing and opening symbols.
   *
   * For example, given the string `"(A B)"`, the symbol group array would be:
   * ```
   * [
   *   {
   *     opening: ",
   *     closing: "
   *   },
   *   {
   *     opening: (,
   *     closing: )
   *   }
   * ]
   * ```
   * The function checks if the elements in each symbol group are matched and are real closing and opening symbols.
   * Finally, this result array helps determine whether all symbol groups are properly closed.
   */
  const processSymbolGroup = (
    areMatched: E.Either<string, GroupingSymbol>[],
    { opening, closing }: Grouping,
  ): E.Either<string, GroupingSymbol>[] => {
    const result = pipe(
      closing,
      E.fromNullable("No corresponding symbol found"),
      E.chainW(
        E.fromPredicate(
          (closingSymbol) => areSymbolsMatches(opening, closingSymbol),
          () => "Symbol not matched",
        ),
      ),
    );
    return [result, ...areMatched];
  };

  const validating: (groups: Grouping[]) => boolean = flow(
    A.reduce<Grouping, E.Either<string, GroupingSymbol>[]>(
      [],
      processSymbolGroup,
    ),
    E.sequenceArray,
    E.fold(constFalse, constTrue),
  );

  return pipe(
    Array.from(input),
    A.filter(GroupingSymbolUnion.is),
    // If the number of symbols is odd, then the symbols are definitely not closed properly
    O.fromPredicate((array) => A.size(array) % 2 === 0),
    O.map(flow(groupingSymbols, validating)),
    O.getOrElse(constFalse),
  );
};

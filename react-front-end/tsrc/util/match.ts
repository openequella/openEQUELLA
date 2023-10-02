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
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";

/**
 * A simple function to provide an alternative to `switch` when we don't have a pattern match
 * framework.
 *
 * Example:
 * ```
 * const result = pipe(
 * 2,
 * simpleMatch<string>({
 *    1: () => "You have selected option 1",
 *    2: () => "You have selected option 2",
 *    _: () => "Sorry, I don't know that option"
 *  }),
 *  console.log // output: You have selected option 2
 * );
 * ```
 * @param matchers A list of strings or numbers to match against, followed by a function to
 *                 execute. The default match is signified with `_`.
 */
export const simpleMatch =
  <R>(matchers: {
    [key: string]: (s: string | number) => R;
    _: (s: string | number) => R;
  }) =>
  (match: string | number): R =>
    match in matchers ? matchers[match](match) : matchers._(match);

/**
 * A simple function to provide an alternative to `switch` when we don't have a pattern match
 * framework. This one providing support for Dynamic identifiers - such as variables and constants
 * or ones generated functionally.
 *
 * Example:
 * ```
 * const makeOption = (option: number) => `option-${option}`;
 * pipe(
 *   "option-2",
 *   simpleMatchD([
 *       [makeOption(1), (o) => `You have selected option 1 [${o}]`],
 *       [makeOption(2), (o) => `You have selected option 2 [${o}]`],
 *       [makeOption(3), (o) => `You have selected option 3 [${o}]`]
 *     ],
 *     (o) => {
 *      throw new TypeError(`Unknown option was selected: ${o}`);
 *     }
 *   ),
 *   console.log // output: You have selected option 2 [option-2]
 * );
 * ```
 * @param matchers A list of strings or numbers to match against, followed by a function to
 *                 execute.
 * @param noMatch A function to execute when no match is found
 */

type MatchFn<R> = (s: string) => R;
type Matcher<R> = [string, MatchFn<R>];
export const simpleMatchD =
  <R>(matchers: Matcher<R>[], noMatch: MatchFn<R>): MatchFn<R> =>
  (match: string): R =>
    pipe(
      matchers,
      A.findFirstMap(([m, f]) => (m === match ? O.some(f) : O.none)),
      O.getOrElse(() => noMatch),
      (f: MatchFn<R>) => f(match),
    );

/**
 * Similar to {@link simpleMatch} but for simple Union types where each member is either a string literal
 * or a number literal. An optional function can be provided to return a default value for members that do
 * not have specific matchers. However, if neither a specific matcher nor a default matcher is available for
 * a member, an error will be thrown.
 *
 * Example:
 *
 * ```
 * type TestUnion = "hello" | "world" | 1;
 *
 * pipe(
 *  "hello",
 *  simpleUnionTypeMatch<TestUnion, string>(
 *   {
 *    "hello": () => "HELLO",
 *    "world": () => "WORLD",
 *   }, () => "default"
 *  ),
 *  console.log, // output: HELLO
 * )
 *
 * pipe(
 *  1,
 *  simpleUnionTypeMatch<TestUnion, string>(
 *   {
 *    "hello": () => "HELLO",
 *    "world": () => "WORLD",
 *   }, () => "default"
 *  ),
 *  console.log, // output: default
 * )
 *
 * pipe(
 *  1,
 *  simpleUnionTypeMatch<TestUnion, string>(
 *   {
 *    "hello": () => "HELLO",
 *    "world": () => "WORLD",
 *   }
 *  ), // throw an error due to no matcher for 1
 *  console.log,
 * )
 * ```
 *
 * @typeParam T Union type for strings or numbers only
 * @typeParam R Return type for the execution of matchers
 *
 * @param matchers Record where keys are members of the union type and values are functions for each member.
 * @param defaultMatcher Optional function to return the default value.
 */
export const simpleUnionMatch =
  <T extends string | number, R>(
    matchers: Partial<Record<T, (match: T) => R>>,
    defaultMatcher?: (other: T) => R,
  ) =>
  (value: T): R =>
    pipe(
      matchers[value],
      O.fromNullable,
      O.alt(() => O.fromNullable(defaultMatcher)),
      O.map((matcher) => matcher(value)),
      O.getOrElseW(() => {
        throw new Error(`Missing matcher for literal type: ${value}`);
      }),
    );

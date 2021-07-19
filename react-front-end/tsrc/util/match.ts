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
import { identity, pipe } from "fp-ts/function";
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
 * framework. This one providing support for Dynamic identifiers.
 *
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
      A.findFirst(([m, _]) => m === match),
      O.map(([_, f]) => f),
      O.match(() => noMatch, identity),
      (f) => f(match)
    );

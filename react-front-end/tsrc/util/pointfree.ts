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
import * as E from "fp-ts/Either";
import { identity, pipe } from "fp-ts/function";

/**
 * Point-free ternary. For cases where you'd typically use a ternary, but want to reap the benefits
 * of point-free code. For example:
 *
 * ```typescript
 * const isLoud = (m: string): boolean => m.match(/.+!/) !== null;
 * const shout = (m: string): string => m.toUpperCase();
 * const whisper = (m: string): string => m.toLowerCase();
 *
 * const msg = "Announcement!";
 * isLoud(msg) ? shout(msg) : whisper(msg);
 *
 * // Now point-free:
 * pipe(
 *   msg,
 *   pfTernary(isLoud, shout, whisper)
 * );
 * ```
 */
export const pfTernary =
  <A, B>(
    predicate: (a: A) => boolean,
    onRight: (a: A) => B,
    onLeft: (a: A) => B,
  ) =>
  (a: A): B =>
    pipe(a, E.fromPredicate(predicate, identity), E.match(onLeft, onRight));

/**
 * Similar to {@link pfTernary}, but the predicate is a type guard.
 */
export const pfTernaryTypeGuard =
  <A, B, C>(
    guard: (a: A | B) => a is A,
    onRight: (a: A) => C,
    onLeft: (a: B) => C,
  ) =>
  (a: A | B): C =>
    pipe(
      a,
      (a) => (guard(a) ? E.right(a) : E.left(a)),
      E.match(onLeft, onRight),
    );

/**
 * Point-free function to slice a string.
 *
 * For example
 *
 * ```typescript
 *
 * const substring = pipe("abcd", pfSlice(0, 2));
 * console.log(substring);
 * ```
 * The output of above code is 'ab'.
 *
 * @param start The index to the beginning of the sub string.
 * @param end The index to the end of the sub string.
 */
export const pfSlice =
  (start: number, end?: number) =>
  (s: string): string =>
    s.slice(start, end);

/**
 * Point-free function to split a string into two parts.
 *
 * For example
 *
 * ```typescript
 *
 * const [first, second] = pipe("abcd", pfSplitAt(2));
 * console.log( first + "," + second);
 * ```
 * The output of above code is 'ab,cd'.
 *
 * @param index Index from where to split the string.
 */
export const pfSplitAt =
  (index: number) =>
  (s: string): [string, string] => [s.substring(0, index), s.substring(index)];

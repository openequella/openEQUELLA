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
import { constFalse, pipe } from "fp-ts/function";
import * as N from "fp-ts/number";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";

/**
 * Since an LTI claim can be hierarchical and have multiple paths, the custom username
 * claim must be provided in the format of bracket notation. That is, each path of the
 * claim must be wrapped by a pair of square brackets.
 */

// Regex to extract a list of chars that does not contain a '[' or ']' from
// a pair of square brackets.
const USERNAME_CLAIM_REGEX = new RegExp(`\\[([^\\[\\]]+?)]`, "g");

/**
 * Check if the provided username claim is constructed in the correct format in three steps:
 * 1. Using regexp to extract all the paths wrapped by square brackets; and
 * 2. Sum the length of all the extracted paths and compare it with the length of the claim.
 * 3. If the lengths are equal, the claim is valid. If not, the claim must not be constructed
 *    properly in the bracket notation format such as missing a closing bracket.
 *
 * @param claim The claim configured in the LTI platform configuration page.
 * @returns `true` if the claim is valid; otherwise `false`.
 */
export const validateUsernameClaim = (claim: string): boolean =>
  pipe(
    claim.match(USERNAME_CLAIM_REGEX),
    O.fromNullable,
    O.map(A.foldMap(N.MonoidSum)(S.size)),
    O.map((total) => total === claim.length),
    O.getOrElse(constFalse),
  );

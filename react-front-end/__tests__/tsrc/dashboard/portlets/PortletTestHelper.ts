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
import { within } from "@testing-library/dom";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";

/**
 * Helper function to find the count value displayed in a chip next to a given text.
 *
 * @param name The text of the list item to search within.
 * @param getByText The `getByText` query function from RTL.
 * @returns The count as a number, or `undefined` if not found.
 */
export const getCountForItem = (
  name: string,
  getByText: (text: string) => HTMLElement,
): number | undefined => {
  const nameEl = getByText(name);
  const digitRegex = /^[0-9]+$/;

  const getBadgeValue = (container: HTMLElement) =>
    pipe(
      within(container).queryAllByText((c) => digitRegex.test(c)),
      O.fromPredicate(A.isNonEmpty),
      O.map(NEA.head),
    );

  const parseBadge = (badge: HTMLElement) =>
    parseInt(badge.textContent ?? "", 10);

  const isNumber = O.fromPredicate(not(Number.isNaN));

  return pipe(
    O.fromNullable(nameEl.parentElement),
    O.chain(getBadgeValue),
    O.map(parseBadge),
    O.chain(isNumber),
    O.toUndefined,
  );
};

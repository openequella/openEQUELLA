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
import { pipe, flow } from "fp-ts/function";
import * as J from "fp-ts/Json";
import * as O from "fp-ts/Option";
import { getBaseUrl } from "../AppConfig";

const localStorage = window.localStorage;

const buildStorageKey = (key: string) => `${getBaseUrl()}_${key}`;

/**
 * Retrieve data from browser local storage by a key. Return undefined if
 * no data is found, cannot be parsed or fails validation.
 *
 * @param key Key of the data to be read.
 * @param validator A function to perform type checking against the parsed value.
 */
export const readDataFromLocalStorage = <T>(
  key: string,
  validator: (value: unknown) => value is T
): T | undefined =>
  pipe(
    localStorage.getItem(buildStorageKey(key)),
    O.fromNullable,
    O.chain(
      flow(
        J.parse,
        E.mapLeft((error) => `Failed to parse data due to ${error}`),
        E.filterOrElse(
          validator,
          () => "Data format mismatch with data read from storage"
        ),
        E.mapLeft(console.error),
        O.fromEither
      )
    ),
    O.toUndefined
  );

/**
 * Save data to browser local storage.
 *
 * @param key Key of the data.
 * @param value Value of the data.
 */
export const saveDataToLocalStorage = (key: string, value: unknown): void =>
  pipe(
    value,
    J.stringify,
    E.mapLeft(
      (error) =>
        `Failed to stringify the provided value to JSON format: ${error}`
    ),
    E.fold(console.error, (right) =>
      localStorage.setItem(buildStorageKey(key), right)
    )
  );

/**
 * Clear data from browser local storage.
 *
 * @param key Key of the data.
 */
export const clearDataFromLocalStorage = (key: string): void =>
  localStorage.removeItem(buildStorageKey(key));

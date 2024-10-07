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
import * as S from "fp-ts/string";

/**
 * Check if a number is an integer.
 * Optionally can also validate required/optional values and sign of number
 *
 * @param val number to validate
 * @param required optionally validate that a value is passed
 * @param positive optionally validate the number is positive
 */
export function isInteger(
  val?: number,
  required?: boolean,
  positive?: boolean,
): boolean {
  if (typeof val === "undefined") {
    return !required;
  }
  const intVal = parseInt(val.toString(), 10);
  if (positive && intVal <= 0) {
    return false;
  }
  return val === intVal;
}

/**
 * Check if a string is a valid URL and starts with http:// or https://.
 *
 * @param str Text to validate.
 */
export const isValidURL = (str: string): boolean =>
  pipe(
    E.tryCatch(
      () => new URL(str),
      () => false,
    ),
    E.fold(
      identity,
      // match http:// or https://
      ({ protocol }) => ["http:", "https:"].includes(protocol),
    ),
  );

/**
 * Check if the provided value is a non-empty string.
 */
export const isNonEmptyString = (v: unknown): v is string =>
  S.isString(v) && !S.isEmpty(v);

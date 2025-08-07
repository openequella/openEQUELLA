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
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as t from "io-ts";
import { DateTime } from "luxon";

/**
 * Represent a date range which has an optional start and end.
 */
export interface DateRange {
  /**
   * The start date of a date range.
   */
  start?: Date;
  /**
   * The end date of a date range.
   */
  end?: Date;
}

/**
 * IO-TS codec for date range where the values of range start and end are both a string.
 */
export const DateRangeFromString = t.partial({
  start: t.string,
  end: t.string,
});

/**
 * Standard ISO date formats.
 */
export type ISODateFormat = "yyyy-MM-dd" | "yyyy-MM" | "yyyy";

/**
 * Convert a date to string in ISO format but keep time unchanged.
 * One should call 'toISOString()' to get the UTC date in ISO format.
 * @param date The date to be converted to a string in ISO format.
 */
export const getISODateString = (date?: Date) =>
  pipe(
    O.fromNullable(date),
    O.map((d) => DateTime.fromJSDate(d).toISODate()),
    // If the result of toISODate is null then return undefined.
    O.chain(O.fromNullable),
    O.toUndefined,
  );

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
 * Returns a function that will modify a given date by a specified number of days.
 *
 * @param days The number of days to add (positive) or subtract (negative).
 * @returns A function that takes a Date and returns a new, modified Date.
 */
export const modifyDateByDays =
  (days: number) =>
  (date: Date): Date => {
    const newDate = new Date(date);
    newDate.setDate(newDate.getDate() + days);
    return newDate;
  };

/**
 * Converts a Date object to a string in 'YYYY-MM-DD' format.
 *
 * @param date The Date object to format.
 * @returns The formatted date string.
 */
export const buildISODate = (date: Date): string => {
  const year = date.getFullYear();
  // getMonth() is zero-based, so we add 1.
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
};

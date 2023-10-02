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
import * as OEQ from "@openequella/rest-api-client";
import * as E from "fp-ts/Either";
import { flow, identity, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import { DateTime } from "luxon";
import * as React from "react";
import type { DateRange } from "../../util/Date";
import { getISODateString } from "../../util/Date";
import { pfTernary } from "../../util/pointfree";
import {
  DatePickerCustomProps,
  DateRangeSelector,
  buildDatePickerCustomProps,
} from "../DateRangeSelector";
import { WizardControlBasicProps } from "./WizardHelper";
import { WizardLabel } from "./WizardLabel";

export interface WizardCalendarProps extends WizardControlBasicProps {
  /**
   * Currently selected date range which can be
   * 1. `undefined`;
   * 2. An array that has two strings in ISO date format.
   * 3. An array that has one string is ISO date format and one empty string.
   *
   * When the array is converted into a date range, the first string represents date of start
   * and the second one represents date of end. Empty string will be converted to `undefined`.
   */
  values?: string[];
  /**
   * On change handler.
   *
   * @param values An array which can be:
   * 1. An empty array;
   * 2. An array that has two strings in ISO date format.
   * 3. An array that has one string is ISO date format and one empty string.
   */
  onChange: (values: string[]) => void;
  /**
   * Date format to be used in the date picker.
   */
  dateFormat?: OEQ.WizardCommonTypes.WizardDateFormat;
  /**
   * `true` to display two date pickers and `false` for one picker.
   * todo: This field is always true in the context of Advanced search, so we will add more supports for Contribution Wizard.
   */
  isRange: boolean;
}

export const WizardCalendar = ({
  id,
  label,
  description,
  mandatory,
  onChange,
  values,
  dateFormat = "DMY",
}: WizardCalendarProps) => {
  const convertToDate = (dateString: string): Date | undefined =>
    pipe(
      dateString,
      Date.parse,
      E.fromPredicate<number, string>(
        (n) => !isNaN(n),
        () => `invalid date string: ${dateString}`,
      ),
      E.fold(
        (left) => {
          console.error(left);
          return undefined;
        },
        (right) => DateTime.fromMillis(right).toJSDate(),
      ),
    );

  const processDateString = (dateString?: string): Date | undefined =>
    pipe(
      dateString,
      O.fromNullable,
      O.chain(
        flow(
          pfTernary(S.isEmpty, () => undefined, identity), // Return undefined for empty string.
          O.fromNullable,
        ),
      ),
      O.map(convertToDate),
      O.toUndefined,
    );

  const dateRange: DateRange | undefined = pipe(
    values,
    O.fromNullable,
    O.map(([start, end]) => ({
      start: processDateString(start),
      end: processDateString(end),
    })),
    O.toUndefined,
  );

  // If both start and end are undefined, return an empty array. If either one is undefined, use
  // an empty string as its value.
  const dateRangeToStringArray = ({ start, end }: DateRange): string[] =>
    start === undefined && end === undefined
      ? []
      : [start, end].map((d) => getISODateString(d) ?? "");

  const onDateRangeChange = (dateRange?: DateRange) =>
    pipe(
      dateRange,
      O.fromNullable,
      O.map(dateRangeToStringArray),
      O.getOrElse<string[]>(() => []),
      onChange,
    );

  // Function to build DatePicker custom props based on the provided Wizard Control date format.
  const buildDatePickerPropsForWizardControl = (): DatePickerCustomProps => {
    switch (dateFormat) {
      case "DMY":
        return buildDatePickerCustomProps();
      case "MY":
        return {
          ...buildDatePickerCustomProps("yyyy-MM"),
          views: ["year", "month"],
          transformDate: (d: DateTime) => d.startOf("month"),
        };
      case "Y":
        return {
          ...buildDatePickerCustomProps("yyyy"),
          views: ["year"],
          transformDate: (d: DateTime) => d.startOf("year"),
        };
      default:
        return buildDatePickerCustomProps();
    }
  };

  return (
    <>
      <WizardLabel
        mandatory={mandatory}
        label={label}
        description={description}
        labelFor={id}
      />
      <DateRangeSelector
        quickModeEnabled={false}
        onDateRangeChange={onDateRangeChange}
        onQuickModeChange={() => {}}
        showModeSwitch={false}
        dateRange={dateRange}
        datePickerCustomPropsProvider={buildDatePickerPropsForWizardControl}
        id={id}
      />
    </>
  );
};

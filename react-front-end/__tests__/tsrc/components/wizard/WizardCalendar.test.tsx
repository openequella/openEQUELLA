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
import { DateTime } from "luxon";
import * as React from "react";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { WizardCalendar } from "../../../../tsrc/components/wizard/WizardCalendar";
import * as OEQ from "@openequella/rest-api-client";
import { ISODateFormat } from "../../../../tsrc/util/Date";

describe("<WizardCalendar/>", () => {
  const today = "2023-05-16";
  const tomorrow = "2023-05-17";
  const yearStart = "2023-01-01";
  const monthStart = "2023-05-01";

  const onChange = jest.fn();

  it("displays the date range initially provided", () => {
    const providedDateRange = [today, tomorrow];
    const { container } = render(
      <WizardCalendar
        isRange
        mandatory
        onChange={onChange}
        values={providedDateRange}
      />,
    );

    const displayedRange = Array.from(
      container.querySelectorAll<HTMLInputElement>("input"),
    ).map((input) => input.value);
    expect(displayedRange).toEqual(providedDateRange);
  });

  it.each<
    [string, OEQ.WizardCommonTypes.WizardDateFormat, ISODateFormat, string[]]
  >([
    ["year only", "Y", "yyyy", [yearStart, ""]],
    ["year and month", "MY", "yyyy-MM", [monthStart, ""]],
    ["year, month and day", "DMY", "yyyy-MM-dd", [today, ""]],
  ])(
    "supports date format: %s",
    async (
      _: string,
      wizardControlDateformat: OEQ.WizardCommonTypes.WizardDateFormat,
      displayFormat: ISODateFormat,
      dateRange: string[],
    ) => {
      const { container } = render(
        <WizardCalendar
          isRange
          mandatory
          onChange={onChange}
          dateFormat={wizardControlDateformat}
        />,
      );

      const start = container.querySelectorAll<HTMLInputElement>("input")[0]; // The component must have at least one DatePicker.
      await userEvent.type(start, today);
      expect(start.value).toEqual(
        DateTime.fromISO(today).toFormat(displayFormat),
      );

      // We only set `start` so the
      expect(onChange).toHaveBeenLastCalledWith(dateRange);
    },
  );
});

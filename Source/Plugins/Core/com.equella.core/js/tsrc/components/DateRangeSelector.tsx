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
import * as React from "react";
import {
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
} from "@material-ui/core";
import { DatePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import SettingsToggleSwitch from "./SettingsToggleSwitch";
import { ReactNode } from "react";
import { languageStrings } from "../util/langstrings";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import LuxonUtils from "@date-io/luxon";
import { DateTime } from "luxon";

/**
 * Type of date range.
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

export interface DateRangeSelectorProps {
  /**
   * Fired when date range is changed.
   * @param dateRange A new date range.
   */
  onDateRangeChange: (dateRange?: DateRange) => void;
  /**
   * Fired when the status of Quick mode is changed.
   * @param enabled The new status of Quick mode
   */
  onQuickModeChange: (enabled: boolean) => void;
  /**
   * Initially selected date range.
   */
  dateRange?: DateRange;
  /**
   * Initial status of Quick mode.
   */
  quickModeEnabled?: boolean;
  /**
   * Label for the Quick option Dropdown.
   */
  quickOptionDropdownLabel?: string;
  /**
   * Label for start date picker.
   */
  startDatePickerLabel?: string;
  /**
   * Label for end date picker.
   */
  endDatePickerLabel?: string;
}

/**
 * As a refine control, this component is used to filter search results by last modified dates.
 * Depending on what mode is selected, a Quick option Dropdown or two custom date pickers are displayed.
 */
export const DateRangeSelector = ({
  onDateRangeChange,
  onQuickModeChange,
  dateRange,
  quickModeEnabled,
  quickOptionDropdownLabel,
  startDatePickerLabel,
  endDatePickerLabel,
}: DateRangeSelectorProps) => {
  const {
    quickOptionSwitchLabel,
    quickOptionLabels,
    defaultDropdownLabel,
    defaultStartDatePickerLabel,
    defaultEndDatePickerLabel,
  } = languageStrings.dateRangeSelector;
  const quickOptionLabel = quickOptionDropdownLabel ?? defaultDropdownLabel;
  const startLabel = startDatePickerLabel ?? defaultStartDatePickerLabel;
  const endLabel = endDatePickerLabel ?? defaultEndDatePickerLabel;

  /**
   * Provide labels and values for options of pre-defined date ranges.
   */
  const getDateRangeOptions = (): Map<string, DateTime | undefined> => {
    const {
      today,
      lastSevenDays,
      lastMonth,
      thisYear,
      all,
    } = quickOptionLabels;
    const now = DateTime.local();
    return new Map([
      [today, now],
      [lastSevenDays, now.minus({ days: 7 })],
      [lastMonth, now.minus({ month: 1 })],
      [thisYear, DateTime.local(now.year)],
      [all, undefined],
    ]);
  };

  /**
   * Convert a Quick date option to a date range.
   * The value of start depends on what Quick date option is selected.
   * The value of end is always undefined.
   *
   * @param option An option selected from the Quick date options.
   */
  const dateOptionToDateRangeConverter = (option: string): DateRange => {
    const start = getDateRangeOptions().get(option);
    return {
      start: start?.toJSDate(),
      end: undefined,
    };
  };

  /**
   * Return label of a Quick date option based on date range.
   * If the provided date range is undefined, or defined but start is undefined, or no matched Quick option is found,
   * then return the Quick option label "All".
   * Otherwise, returns the Quick option label whose value is equal to start in ISO Date format.
   *
   * @param dateRange A date range to be converted to a Quick date option label.
   */
  const dateRangeToDateOptionConverter = (dateRange?: DateRange): string => {
    let option = quickOptionLabels.all;
    if (!dateRange || !dateRange.start) {
      return option;
    }
    const start = DateTime.fromJSDate(dateRange.start);
    getDateRangeOptions().forEach(
      (dateTime: DateTime | undefined, label: string) => {
        if (dateTime && dateTime.toISODate() === start.toISODate()) {
          option = label;
        }
      }
    );

    return option;
  };

  /**
   * Fired when a different Quick option is selected.
   * @param option A Quick option.
   */
  const handleQuickDateOptionChange = (option: string): void => {
    const dateRange = dateOptionToDateRangeConverter(option);
    onDateRangeChange(dateRange);
  };

  const quickOptionSelector: ReactNode = (
    <FormControl variant="outlined" fullWidth>
      <InputLabel id="date_range_selector_label">{quickOptionLabel}</InputLabel>
      <Select
        value={dateRangeToDateOptionConverter(dateRange)}
        id="date_range_selector"
        labelId="date_range_selector_label"
        onChange={(event) =>
          handleQuickDateOptionChange(event.target.value as string)
        }
        label={quickOptionLabel}
      >
        {Array.from(getDateRangeOptions()).map(([option, _]) => (
          <MenuItem key={option} value={option}>
            {option}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );

  const customDatePicker: ReactNode = (
    <MuiPickersUtilsProvider utils={LuxonUtils}>
      <Grid container spacing={2}>
        {[
          [startLabel, dateRange?.start],
          [endLabel, dateRange?.end],
        ].map(([label, value]) => (
          <Grid item>
            <DatePicker
              disableFuture
              variant="inline"
              inputVariant="outlined"
              autoOk
              labelFunc={(value, invalidLabel) =>
                value?.toLocaleString() ?? invalidLabel
              }
              label={label}
              value={value}
              onChange={(newDate: MaterialUiPickersDate) =>
                onDateRangeChange({
                  ...dateRange,
                  end: newDate?.toJSDate(),
                })
              }
            />
          </Grid>
        ))}
      </Grid>
    </MuiPickersUtilsProvider>
  );

  return (
    <Grid container>
      <Grid item xs={12}>
        {quickModeEnabled ? quickOptionSelector : customDatePicker}
      </Grid>
      <Grid item>
        <SettingsToggleSwitch
          id="modified_date_selector_mode_switch"
          label={quickOptionSwitchLabel}
          value={quickModeEnabled}
          setValue={(value) => onQuickModeChange(value)}
        />
      </Grid>
    </Grid>
  );
};

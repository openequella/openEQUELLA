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
import {
  KeyboardDatePicker,
  MuiPickersUtilsProvider,
} from "@material-ui/pickers";
import SettingsToggleSwitch from "./SettingsToggleSwitch";
import { ReactNode, useEffect, useRef, useState } from "react";
import { languageStrings } from "../util/langstrings";
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

interface DatePickerProps {
  /**
   * The field which a date picker controls.
   */
  field: "start" | "end";
  /**
   * The label of a date picker.
   */
  label: string;
  /**
   * The value of a date picker.
   */
  value?: Date;
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
   * @param dateRange Date range updated due to the mode change.
   */
  onQuickModeChange: (enabled: boolean, dateRange?: DateRange) => void;
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
   * MUI KeyboardDatePicker requires its value to be controlled by state,
   * in order to properly parse and validate TextField inputs.
   */
  const [stateDateRange, setStateDateRange] = useState<DateRange | undefined>(
    dateRange
  );

  const isInitialRender = useRef(true);
  useEffect(() => {
    if (isInitialRender.current) {
      isInitialRender.current = false;
      return;
    }
    // When state is cleared, do not proceed to avoid duplicated search.
    if (!stateDateRange) {
      return;
    }
    const start = stateDateRange?.start;
    const end = stateDateRange?.end;
    const isStartValid = start && DateTime.fromJSDate(start).isValid;
    const isEndValid = end && DateTime.fromJSDate(end).isValid;

    // start is undefined and end is a valid date.
    const openStart = !start && isEndValid;
    // End is undefined and start is a valid date.
    const openEnd = !end && isStartValid;
    // Both are undefined.
    const openRange = !start && !end;
    // Both are valid dates and start is equal or less than end.
    const closedRange =
      start && end ? isStartValid && isEndValid && start <= end : false;
    // Call onDateRangeChange for above four cases.
    if (openStart || openEnd || openRange || closedRange) {
      onDateRangeChange(stateDateRange);
    }
  }, [stateDateRange]);

  // Only when prop dateRange is cleared, clear stateDateRange as well.
  useEffect(() => {
    if (!dateRange) {
      setStateDateRange(dateRange);
    }
  }, [dateRange]);

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
   * If end is defined but it is not equal to today, then return "All", too.
   * Otherwise, returns the Quick option label whose value is equal to start in ISO Date format.
   *
   * @param dateRange A date range to be converted to a Quick date option label.
   */
  const dateRangeToDateOptionConverter = (dateRange?: DateRange): string => {
    let option = quickOptionLabels.all;
    if (
      !dateRange ||
      !dateRange.start ||
      (dateRange.end &&
        dateRange.end.toDateString() !== new Date().toDateString())
    ) {
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
    setStateDateRange(dateRange);
  };

  const quickOptionSelector: ReactNode = (
    <FormControl variant="outlined" fullWidth>
      <InputLabel id="date_range_selector_label">{quickOptionLabel}</InputLabel>
      <Select
        value={dateRangeToDateOptionConverter(stateDateRange)}
        id="date_range_selector"
        labelId="date_range_selector_label"
        onChange={(event) =>
          handleQuickDateOptionChange(event.currentTarget.value as string)
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

  /**
   * Generate two date pickers and wrap them with Grid items.
   */
  const getDatePickers = (): ReactNode => {
    const dateRangePickers: DatePickerProps[] = [
      {
        field: "start",
        label: startLabel,
        value: stateDateRange?.start,
      },
      {
        field: "end",
        label: endLabel,
        value: stateDateRange?.end,
      },
    ];

    return dateRangePickers.map(({ field, label, value }) => {
      const isStart = field === "start";
      return (
        <Grid item key={field} xs={12} md={6}>
          <KeyboardDatePicker
            disableFuture
            variant="dialog"
            clearable
            inputVariant="outlined"
            autoOk
            // Show date in ISO format string, or nothing if date is null.
            labelFunc={(date, _) => {
              return date?.toISODate() ?? "";
            }}
            // TextField inputs are parsed to this format.
            format="yyyy-MM-dd"
            // The maximum start date is the range's end whereas minimum end date is the range's start.
            minDate={!isStart ? stateDateRange?.start : undefined}
            maxDate={isStart ? stateDateRange?.end : undefined}
            label={label}
            // When value is undefined use null instead so nothing is displayed in the TextField.
            value={value ?? null}
            onChange={(newDate, _) =>
              setStateDateRange({
                ...stateDateRange,
                [field]: newDate?.toJSDate(),
              })
            }
          />
        </Grid>
      );
    });
  };

  const customDatePicker: ReactNode = (
    <MuiPickersUtilsProvider
      utils={LuxonUtils}
      locale={DateTime.local().locale}
    >
      <Grid container spacing={2}>
        {getDatePickers()}
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
          setValue={(value) => {
            // If selected custom date range matches the option `All` then clear both start and end.
            const isAllSelected =
              dateRangeToDateOptionConverter(stateDateRange) ===
              quickOptionLabels.all;
            const updatedRange = isAllSelected
              ? undefined
              : { ...dateRange, end: undefined };
            onQuickModeChange(value, updatedRange);
          }}
        />
      </Grid>
    </Grid>
  );
};

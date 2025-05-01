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
import { FormControl, Grid, InputLabel, MenuItem, Select } from "@mui/material";
import { styled } from "@mui/material/styles";
import type { DateView } from "@mui/x-date-pickers";
import { DesktopDatePicker } from "@mui/x-date-pickers/DesktopDatePicker";
import { AdapterLuxon } from "@mui/x-date-pickers/AdapterLuxon";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import clsx from "clsx";
import { flow, pipe, constVoid } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { DateTime } from "luxon";
import * as React from "react";
import { ReactNode, useEffect, useState } from "react";
import type { DateRange, ISODateFormat } from "../util/Date";
import { languageStrings } from "../util/langstrings";
import SettingsToggleSwitch from "./SettingsToggleSwitch";

const PREFIX = "DateRangeSelector";

const classes = {
  hideCalenderIcon: `${PREFIX}-hideCalenderIcon`,
  datePickerWidth: `${PREFIX}-datePickerWidth`,
};

const StyledGrid = styled(Grid)({
  [`& .${classes.hideCalenderIcon}`]: {
    display: "none",
  },
  [`& .${classes.datePickerWidth}`]: {
    width: "100%",
  },
});

/**
 * Type definition for DatePicker props that needs customisation.
 */
export interface DatePickerCustomProps {
  /**
   * A list of DateView controlling the views of DatePicker.
   */
  views: DateView[];
  /**
   * Standard date format used in the DatePicker.
   */
  format: ISODateFormat;
  /**
   * Function to transform a given date to another date (e.g. convert a day to the first day of a month).
   */
  transformDate: (d: DateTime) => DateTime;
}

/**
 * Function to build a DatePickerCustomProps based on the provided ISO date format. The format
 * defaults to "yyyy-MM-dd".
 *
 * @param format Date format which must one of the three standard ISO date formats.
 */
export const buildDatePickerCustomProps = (
  format: ISODateFormat = "yyyy-MM-dd",
): DatePickerCustomProps => ({
  views: ["year", "month", "day"],
  format,
  transformDate: (d: DateTime) => d,
});

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
   * The value of a date picker. (Uses DateTime to match the AdapterLuxon adapter used with the
   * DatePicker.)
   */
  value?: DateTime;
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
  /**
   * `true` to show the switch used to change the Selector mode.
   */
  showModeSwitch?: boolean;
  /**
   * Function to provide DatePickerCustomProps.
   */
  datePickerCustomPropsProvider?: () => DatePickerCustomProps;
  /**
   * DOM id.
   */
  id?: string;
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
  showModeSwitch = true,
  datePickerCustomPropsProvider = buildDatePickerCustomProps,
  id = "date-range-selector",
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
    dateRange,
  );
  const [showCalenderIcon, setShowCalenderIcon] = useState<boolean>(false);

  // Only when prop dateRange is cleared, clear stateDateRange as well.
  useEffect(() => {
    setStateDateRange(dateRange);
  }, [dateRange]);

  const handleDateRangeChange = (range: DateRange): void => {
    const { start, end } = range;
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
      onDateRangeChange({ start: range.start, end: range.end });
    }
  };
  /**
   * Provide labels and values for options of pre-defined date ranges.
   */
  const getDateRangeOptions = (): Map<string, DateTime | undefined> => {
    const { today, lastSevenDays, lastMonth, thisYear, all } =
      quickOptionLabels;
    const now = DateTime.local();
    return new Map([
      [today, now],
      [lastSevenDays, now.minus({ days: 7 })],
      [lastMonth, now.minus({ months: 1 })],
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
      !dateRange?.start ||
      (dateRange?.end &&
        dateRange.end.toDateString() !== new Date().toDateString())
    ) {
      return option;
    }

    const start = DateTime.fromJSDate(dateRange.start);
    getDateRangeOptions().forEach(
      (dateTime: DateTime | undefined, label: string) => {
        if (dateTime?.toISODate() === start.toISODate()) {
          option = label;
        }
      },
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
    handleDateRangeChange(dateRange);
  };

  const quickOptionSelector: ReactNode = (
    <FormControl variant="outlined" fullWidth>
      <InputLabel id={`${id}-quick-option-label`}>
        {quickOptionLabel}
      </InputLabel>
      <Select
        value={dateRangeToDateOptionConverter(stateDateRange)}
        id={`${id}-quick-options`}
        labelId={`${id}-quick-option-label`}
        onChange={(event) => handleQuickDateOptionChange(event.target.value)}
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
    // The DatePicker is used with a Luxon adapter, so we want all our date types aligned.
    const asLuxonDate = (d: Date | undefined): DateTime | undefined =>
      pipe(d, O.fromNullable, O.map(DateTime.fromJSDate), O.toUndefined);

    const { views, format, transformDate } = datePickerCustomPropsProvider();
    const dateRangePickers: DatePickerProps[] = [
      {
        field: "start",
        label: startLabel,
        value: asLuxonDate(stateDateRange?.start),
      },
      {
        field: "end",
        label: endLabel,
        value: asLuxonDate(stateDateRange?.end),
      },
    ];

    return dateRangePickers.map(({ field, label, value }) => {
      const isStart = field === "start";
      return (
        <StyledGrid key={field} size={{ xs: 12, lg: 6 }}>
          {/*Ideally this would be DatePicker, however the responsive switching between Desktop and
          Mobile was too troublesome culminating in this comment on GitHub:
          https://github.com/mui/mui-x/issues/4644#issuecomment-1343851120*/}
          <DesktopDatePicker
            views={views}
            // Ensure the picker still takes full width when calendar icon is hidden.
            className={classes.datePickerWidth}
            disableFuture
            closeOnSelect
            slotProps={{
              textField: { id: `${id}-${field}` },
              actionBar: {
                actions: ["clear", "cancel", "accept"],
              },
              openPickerButton: {
                className: clsx(!showCalenderIcon && classes.hideCalenderIcon),
              },
            }}
            format={format}
            // The maximum start date is the range's end whereas minimum end date is the range's start.
            minDate={asLuxonDate(!isStart ? stateDateRange?.start : undefined)}
            maxDate={asLuxonDate(isStart ? stateDateRange?.end : undefined)}
            label={label}
            // When value is undefined use null instead so nothing is displayed in the TextField.
            value={value ?? null}
            onChange={(newValue, { validationError }) => {
              pipe(
                O.fromNullable(validationError),
                O.match(
                  () =>
                    pipe(
                      O.fromNullable(newValue),
                      O.map(flow(transformDate, (d) => d.toJSDate())),
                      O.toUndefined,
                      (d: Date | undefined) => {
                        const newRange = {
                          ...stateDateRange,
                          [field]: d,
                        };
                        setStateDateRange(newRange);
                        handleDateRangeChange(newRange);
                      },
                    ),
                  constVoid,
                ),
              );
            }}
          />
        </StyledGrid>
      );
    });
  };

  const customDatePicker: ReactNode = (
    <LocalizationProvider
      dateAdapter={AdapterLuxon}
      adapterLocale={DateTime.local().locale ?? undefined}
    >
      <Grid
        container
        spacing={2}
        // The reason for putting these two events here is because this Grid contains two date pickers.
        // Either one receives the focus should make both display their calendar icons.
        // And swapping focus between the two pickers should still keep the icon.
        onFocus={() => setShowCalenderIcon(true)}
        onBlur={(event) => {
          const { relatedTarget } = event;
          if (!event.currentTarget.contains(relatedTarget as Node)) {
            setShowCalenderIcon(false);
          }
        }}
      >
        {getDatePickers()}
      </Grid>
    </LocalizationProvider>
  );

  return (
    <Grid container id={id}>
      <Grid size={12}>
        {quickModeEnabled ? quickOptionSelector : customDatePicker}
      </Grid>
      <Grid>
        {showModeSwitch && (
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
        )}
      </Grid>
    </Grid>
  );
};

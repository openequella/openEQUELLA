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
import SettingsToggleSwitch from "../../components/SettingsToggleSwitch";
import { ReactNode } from "react";
import { languageStrings } from "../../util/langstrings";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import LuxonUtils from "@date-io/luxon";
import { DateTime } from "luxon";

/**
 * Type of Last modified date range.
 */
export interface LastModifiedDateRange {
  /**
   * The date before which items are modified.
   */
  modifiedBefore?: Date;
  /**
   * The date after which items are modified.
   */
  modifiedAfter?: Date;
}

export interface ModifiedDateSelectorProps {
  /**
   * Fired when date range is changed.
   * @param dateRange A new date range.
   */
  onDateRangeChange: (dateRange?: LastModifiedDateRange) => void;
  /**
   * Fired when the status of Quick mode is changed.
   * @param enabled The new status of Quick mode
   */
  onQuickModeChange: (enabled: boolean) => void;
  /**
   * Initially selected date range.
   */
  dateRange?: LastModifiedDateRange;
  /**
   * Initial status of Quick mode.
   */
  quickModeEnabled?: boolean;
}

type DatePickerInputVariant = "standard" | "outlined" | "filled";
type DatePickerWrapperVariant = "dialog" | "inline" | "static";

/**
 * General props for Material ui Date Picker.
 *
 * @see BaseDatePickerProps
 * @see BasePickerProps
 */
export interface GeneralDatePickerProps {
  disableFuture: boolean;
  variant: DatePickerWrapperVariant;
  inputVariant: DatePickerInputVariant;
  autoOk: boolean;
  labelFunc: (value: MaterialUiPickersDate, invalidLabel: string) => string;
}

/**
 * As a refine control, this component is used to filter search results by last modified dates.
 * Depending on what mode is selected, a Quick option Dropdown or two custom date pickers are displayed.
 */
export const ModifiedDateSelector = ({
  onDateRangeChange,
  onQuickModeChange,
  dateRange,
  quickModeEnabled,
}: ModifiedDateSelectorProps) => {
  const {
    quickOptionLabel,
    quickOptionSwitchLabel,
    modifiedBeforeLabel,
    modifiedAfterLabel,
    optionsLabel,
  } = languageStrings.searchpage.modifiedDateSelector;

  const datePickerProps: GeneralDatePickerProps = {
    disableFuture: true,
    variant: "inline",
    inputVariant: "outlined",
    autoOk: true,
    labelFunc: (value, invalidLabel) => value?.toLocaleString() ?? invalidLabel,
  };

  /**
   * Return a map in which keys are language strings and values are instances of DateTime or undefined.
   */
  const getLastModifiedDateOptions = (): Map<string, DateTime | undefined> => {
    const { today, lastSevenDays, lastMonth, thisYear, all } = optionsLabel;
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
   * Convert a quick date option to a date range.
   * The value of field 'modifiedAfter' depends on what Quick date option is selected.
   * The value of field 'modifiedBefore' is always undefined.
   *
   * @param option  An option selected from the Quick date options.
   */
  const dateOptionToDateRangeConverter = (
    option: string
  ): LastModifiedDateRange => {
    const modifiedAfter = getLastModifiedDateOptions().get(option);
    return {
      modifiedAfter: modifiedAfter?.toJSDate(),
      modifiedBefore: undefined,
    };
  };

  /**
   * Convert a date range to a Quick date option.
   * If the provided date range is undefined, or defined but the value of field modifiedAfter is undefined,
   * then return the Quick option "All".
   * Otherwise, returns the Quick option whose value is equal to modifiedAfter in ISO Date format.
   *
   * @param dateRange A date range to be converted to a Quick date range
   */
  const dateRangeToDateOptionConverter = (
    dateRange?: LastModifiedDateRange
  ): string => {
    let option = optionsLabel.all;
    if (!dateRange || !dateRange.modifiedAfter) {
      return option;
    }
    const modifiedAfter = DateTime.fromJSDate(dateRange.modifiedAfter);
    getLastModifiedDateOptions().forEach(
      (value: DateTime | undefined, key: string) => {
        if (value && value.toISODate() === modifiedAfter.toISODate()) {
          option = key;
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
      <InputLabel id="modified_date_range_selector_label">
        {quickOptionLabel}
      </InputLabel>
      <Select
        value={dateRangeToDateOptionConverter(dateRange)}
        id="modified_date_range_selector"
        labelId="modified_date_range_selector_label"
        onChange={(event) =>
          handleQuickDateOptionChange(event.target.value as string)
        }
        label={quickOptionLabel}
      >
        {Array.from(getLastModifiedDateOptions()).map(([option, _]) => (
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
        <Grid item>
          <DatePicker
            {...datePickerProps}
            label={modifiedAfterLabel}
            value={dateRange?.modifiedAfter}
            onChange={(newDate: MaterialUiPickersDate) =>
              onDateRangeChange({
                ...dateRange,
                modifiedAfter: newDate?.toJSDate(),
              })
            }
          />
        </Grid>
        <Grid item>
          <DatePicker
            {...datePickerProps}
            label={modifiedBeforeLabel}
            value={dateRange?.modifiedBefore}
            onChange={(newDate: MaterialUiPickersDate) =>
              onDateRangeChange({
                ...dateRange,
                modifiedBefore: newDate?.toJSDate(),
              })
            }
          />
        </Grid>
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

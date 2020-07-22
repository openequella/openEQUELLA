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
import { DatePicker } from "@material-ui/pickers";
import {
  dateOptionToDateRangeConverter,
  dateRangeToDateOptionConverter,
  LastModifiedDateOption,
  LastModifiedDateRange,
} from "../SearchModule";
import SettingsToggleSwitch from "../../components/SettingsToggleSwitch";
import { ReactNode } from "react";
import { languageStrings } from "../../util/langstrings";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";

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
  const modifiedDateSelectorStrings =
    languageStrings.searchpage.modifiedDateSelector;

  const handleQuickDateOptionChange = (
    option: LastModifiedDateOption
  ): void => {
    const dateRange = dateOptionToDateRangeConverter(option);
    onDateRangeChange(dateRange);
  };

  const quickOptions: ReactNode = (
    <FormControl variant="outlined" fullWidth>
      <InputLabel id="modified_date_range_selector_label">
        {modifiedDateSelectorStrings.quickOptionLabel}
      </InputLabel>
      <Select
        value={dateRangeToDateOptionConverter(dateRange)}
        id="modified_date_range_selector"
        labelId="modified_date_range_selector_label"
        onChange={(event) =>
          handleQuickDateOptionChange(
            event.target.value as LastModifiedDateOption
          )
        }
        label={modifiedDateSelectorStrings.quickOptionLabel}
      >
        {Object.values(LastModifiedDateOption).map((value) => (
          <MenuItem key={value} value={value}>
            {value}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );

  const customDateRange: ReactNode = (
    <Grid container spacing={2}>
      <Grid item>
        <DatePicker
          disableFuture
          variant="inline"
          label={modifiedDateSelectorStrings.modifiedAfter}
          value={dateRange?.modifiedAfter}
          onChange={(newDate: MaterialUiPickersDate) =>
            onDateRangeChange({
              ...dateRange,
              modifiedAfter: newDate?.toISODate(),
            })
          }
        />
      </Grid>
      <Grid item>
        <DatePicker
          disableFuture
          variant="inline"
          label={modifiedDateSelectorStrings.modifiedBefore}
          value={dateRange?.modifiedBefore}
          onChange={(newDate: MaterialUiPickersDate) =>
            onDateRangeChange({
              ...dateRange,
              modifiedBefore: newDate?.toISODate(),
            })
          }
        />
      </Grid>
    </Grid>
  );

  return (
    <Grid container>
      <Grid item xs={12}>
        {quickModeEnabled ? quickOptions : customDateRange}
      </Grid>
      <Grid item>
        <SettingsToggleSwitch
          id="modified_date_selector_mode_switch"
          label={modifiedDateSelectorStrings.quickOptionSwitchLabel}
          value={quickModeEnabled}
          setValue={(value) => onQuickModeChange(value)}
        />
      </Grid>
    </Grid>
  );
};

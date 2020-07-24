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
  DateRangeSelector,
  DateRangeSelectorProps,
} from "../../tsrc/components/DateRangeSelector";
import { action } from "@storybook/addon-actions";
import { object, text } from "@storybook/addon-knobs";
import { MuiPickersUtilsProvider } from "@material-ui/pickers";
import LuxonUtils from "@date-io/luxon";

export default {
  title: "ModifiedDateSelector",
  component: DateRangeSelector,
};

const actions: DateRangeSelectorProps = {
  onDateRangeChange: action("onDateRangeChange called"),
  onQuickModeChange: action("onQuickModeChange called"),
};

export const QuickOptionMode = () => (
  <MuiPickersUtilsProvider utils={LuxonUtils}>
    <DateRangeSelector
      quickModeEnabled
      dateRange={object("Date range", {
        start: new Date(),
        end: undefined,
      })}
      quickOptionDropdownLabel={text(
        "Quick option Dropdown label",
        "Quick date range options"
      )}
      {...actions}
    />
  </MuiPickersUtilsProvider>
);

export const CustomDateMode = () => (
  <MuiPickersUtilsProvider utils={LuxonUtils}>
    <DateRangeSelector
      quickModeEnabled={false}
      dateRange={object("Date range", {
        start: new Date(),
        end: new Date(),
      })}
      startDatePickerLabel={text("Start date", "Start date")}
      endDatePickerLabel={text("End date", "End date")}
      {...actions}
    />
  </MuiPickersUtilsProvider>
);

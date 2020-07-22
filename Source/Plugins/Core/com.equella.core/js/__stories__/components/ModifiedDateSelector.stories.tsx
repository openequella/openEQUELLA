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
  ModifiedDateSelector,
  ModifiedDateSelectorProps,
} from "../../tsrc/search/components/ModifiedDateSelector";
import { action } from "@storybook/addon-actions";
import { boolean, object } from "@storybook/addon-knobs";
import { MuiPickersUtilsProvider } from "material-ui-pickers";
import LuxonUtils from "@date-io/luxon";

export default {
  title: "ModifiedDateSelector",
  component: ModifiedDateSelector,
};

const actions: ModifiedDateSelectorProps = {
  onDateRangeChange: action("onDateRangeChange called"),
  onQuickModeChange: action("onQuickModeChange called"),
};

export const QuickOptionMode = () => (
  <MuiPickersUtilsProvider utils={LuxonUtils}>
    <ModifiedDateSelector
      quickModeEnabled={boolean("Enable Quick mode", true)}
      dateRange={object("Date range", {
        modifiedAfter: "2020-07-01",
        modifiedBefore: undefined,
      })}
      {...actions}
    />
  </MuiPickersUtilsProvider>
);

export const CustomDateMode = () => (
  <MuiPickersUtilsProvider utils={LuxonUtils}>
    <ModifiedDateSelector
      quickModeEnabled={boolean("Enable Quick mode", false)}
      dateRange={object("Date range", {
        modifiedAfter: "2020-07-01",
        modifiedBefore: "2020-07-21",
      })}
      {...actions}
    />
  </MuiPickersUtilsProvider>
);

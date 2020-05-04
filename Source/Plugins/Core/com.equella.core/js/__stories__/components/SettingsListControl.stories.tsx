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
import { boolean, number, text } from "@storybook/addon-knobs";
import SettingsListControl from "../../tsrc/components/SettingsListControl";
import SettingsToggleSwitch from "../../tsrc/components/SettingsToggleSwitch";
import { Mark, Slider } from "@material-ui/core";
import { action } from "@storybook/addon-actions";

export default {
  title: "SettingsListControl",
  component: SettingsListControl,
};

const marks: Mark[] = [
  { label: "Off", value: 0 },
  { label: "x0.25", value: 1 },
  { label: "x0.5", value: 2 },
  { label: "No boost", value: 3 },
  { label: "x1.5", value: 4 },
  { label: "x2", value: 5 },
  { label: "x4", value: 6 },
  { label: "x8", value: 7 },
];

export const ToggleSwitchControl = () => (
  <SettingsListControl
    secondaryText={text("Secondary Text", "Box for checking")}
    control={
      <SettingsToggleSwitch
        disabled={boolean("Disabled", false)}
        id={"toggle"}
        setValue={action("Value of checkbox changed")}
        value={boolean("Toggle state", false)}
      />
    }
    divider={boolean("divider", false)}
    primaryText={text("primaryText", "Checkbox")}
  />
);
export const SliderControl = () => (
  <SettingsListControl
    secondaryText={text("Secondary Text", "Slide for sliding")}
    control={
      <Slider
        marks={marks}
        onChangeCommitted={action("Value of slider changed")}
        min={number("min", 0)}
        max={number("max", 7)}
        step={null}
      />
    }
    divider={boolean("divider", false)}
    primaryText={text("primaryText", "SliderControl")}
  />
);

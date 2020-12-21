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
import type { Meta, Story } from "@storybook/react";
import SettingsListControl, {
  SettingsListControlProps,
} from "../../tsrc/components/SettingsListControl";
import SettingsToggleSwitch, {
  SettingsToggleSwitchProps,
} from "../../tsrc/components/SettingsToggleSwitch";
import { Mark, Slider, SliderProps } from "@material-ui/core";

export default {
  title: "SettingsListControl",
  component: SettingsListControl,
  argTypes: {
    setValue: { action: "setValue" },
  },
} as Meta<SettingsListControlProps>;

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

type ToggleSwitchControlProps = Pick<
  SettingsListControlProps,
  "primaryText" | "secondaryText" | "divider"
> &
  Pick<SettingsToggleSwitchProps, "disabled" | "setValue" | "value">;
export const ToggleSwitchControl: Story<ToggleSwitchControlProps> = (args) => (
  <SettingsListControl
    primaryText={args.primaryText}
    secondaryText={args.secondaryText}
    divider={args.divider}
    control={
      <SettingsToggleSwitch
        disabled={args.disabled}
        id="toggle"
        setValue={args.setValue}
        value={args.value}
      />
    }
  />
);
ToggleSwitchControl.args = {
  primaryText: "Checkbox",
  secondaryText: "Box for checking",
  divider: false,
  disabled: false,
  value: false,
};

type SliderControlProps = Pick<
  SettingsListControlProps,
  "primaryText" | "secondaryText" | "divider"
> &
  Pick<SliderProps, "min" | "max" | "onChangeCommitted">;
export const SliderControl: Story<SliderControlProps> = (args) => (
  <SettingsListControl
    primaryText={args.primaryText}
    secondaryText={args.secondaryText}
    divider={args.divider}
    control={
      <Slider
        marks={marks}
        onChangeCommitted={args.onChangeCommitted}
        min={args.min}
        max={args.max}
        step={null}
      />
    }
  />
);
SliderControl.args = {
  primaryText: "SliderControl",
  secondaryText: "Slides for sliding",
  divider: false,
  min: 0,
  max: 7,
};

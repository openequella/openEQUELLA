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

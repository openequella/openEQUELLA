import * as React from "react";
import { boolean, number, text } from "@storybook/addon-knobs";
import SettingsListControl from "../../tsrc/components/SettingsListControl";
import SettingsToggleSwitch from "../../tsrc/components/SettingsToggleSwitch";
import { Mark, Slider } from "@material-ui/core";
import { action } from "@storybook/addon-actions";
import SettingsList from "../../tsrc/components/SettingsList";

export default {
  title: "SettingsList",
  component: SettingsList
};

const marks: Mark[] = [
  { label: "Off", value: 0 },
  { label: "x0.25", value: 1 },
  { label: "x0.5", value: 2 },
  { label: "No boost", value: 3 },
  { label: "x1.5", value: 4 },
  { label: "x2", value: 5 },
  { label: "x4", value: 6 },
  { label: "x8", value: 7 }
];

export const ListWithTwoItems = () => (
  <SettingsList subHeading={text("Sub Heading", "Sub Heading")}>
    <SettingsListControl
      secondaryText={text("Item 1 Secondary Text", "Box for checking")}
      control={
        <SettingsToggleSwitch
          disabled={boolean("Item 1 Disabled", false)}
          id={"toggle"}
          setValue={action("Item 1 Value of checkbox changed")}
          value={boolean("Item 1 Toggle state", false)}
        />
      }
      divider={boolean("Item 1 divider", true)}
      primaryText={text("Item 1 primaryText", "Checkbox")}
    />
    <SettingsListControl
      secondaryText={text("Item 2 Secondary Text", "Slide for sliding")}
      control={
        <Slider
          marks={marks}
          onChangeCommitted={action("Item 2 Value of slider changed")}
          min={number("Item 2 min", 0)}
          max={number("Item 2 max", 7)}
          step={null}
        />
      }
      divider={boolean("Item 2 divider", false)}
      primaryText={text("Item 2 primaryText", "SliderControl")}
    />
  </SettingsList>
);

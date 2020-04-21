import * as React from "react";
import { text } from "@storybook/addon-knobs";
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
      secondaryText={"Box for checking"}
      control={
        <SettingsToggleSwitch
          setValue={action("Checkbox")}
          disabled={false}
          id={"toggle"}
        />
      }
      divider={true}
      primaryText={"Checkbox"}
    />
    <SettingsListControl
      secondaryText={"Slide for sliding"}
      control={<Slider marks={marks} min={0} max={7} step={null} />}
      divider={false}
      primaryText={"SliderControl"}
    />
  </SettingsList>
);

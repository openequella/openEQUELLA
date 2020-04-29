import * as React from "react";
import { boolean } from "@storybook/addon-knobs";
import SettingsToggleSwitch from "../../tsrc/components/SettingsToggleSwitch";
import { action } from "@storybook/addon-actions";

export default {
  title: "SettingsToggleSwitch",
  component: SettingsToggleSwitch,
};

export const ToggleSwitchControl = () => (
  <SettingsToggleSwitch
    disabled={boolean("Disabled", false)}
    id={"toggle"}
    setValue={action("Value of checkbox changed")}
    value={boolean("Toggle state", false)}
  />
);

import SearchSettingFormControl from "./SearchSettingFormControl";
import { Switch } from "@material-ui/core";
import * as React from "react";

export interface SettingsToggleSwitchProps {
  value: boolean;
  setValue: (value: boolean) => void;
  disabled?: boolean;
  label?: string;
  title?: string;
  formHelperText?: string;
  id: string;
}
/*
 * This component is used to define a boolean toggle control using a Material UI switch.
 *
 * @param value                 The value of the switch
 * @param setValue              the function called when the value changes
 * @param disabled              Whether or not the control is interactible
 * @param label                 Optional label
 * @param title                 Optional title string
 * @param formHelperText        Optional secondary label
 * @param id                    ID of the control
 */
export default function SettingsToggleSwitch({
  disabled,
  label,
  title,
  formHelperText,
  value,
  setValue,
  id
}: SettingsToggleSwitchProps) {
  return (
    <SearchSettingFormControl
      disabled={disabled}
      onChange={(_, checked) => setValue(checked)}
      control={<Switch id={id} checked={value} />}
      label={label}
      title={title}
      formHelperText={formHelperText}
    />
  );
}

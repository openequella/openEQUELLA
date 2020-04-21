import SearchSettingFormControl from "./SearchSettingFormControl";
import { Switch } from "@material-ui/core";
import * as React from "react";

export interface SettingsToggleSwitchProps {
  /**
   * The value of the switch.
   */
  value?: boolean;
  /**
   * A function that gets called upon change of the control value.
   * @param {boolean} value - The new value of the control.
   */
  setValue: (value: boolean) => void;
  /**
   * Determines whether the control is interactive.
   */
  disabled?: boolean;
  /**
   * Optional label.
   */
  label?: string;
  /**
   * Optional title string.
   */
  title?: string;
  /**
   * Optional secondary label.
   */
  formHelperText?: string;
  /**
   * ID of the control.
   */
  id: string;
}
/**
 * This component is used to define a boolean toggle control using a Material UI switch.
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

import SearchSettingFormControl from "./SearchSettingFormControl";
import { Switch } from "@material-ui/core";
import * as React from "react";

export interface SettingsToggleSwitchProps {
  value: boolean;
  setValue: (value: boolean) => void;
  disabled?: boolean;
  label?: string;
  title?: string;
  helperText?: string;
  id: string;
}
export default function SettingsToggleSwitch({
  disabled,
  label,
  title,
  helperText,
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
      helperText={helperText}
    />
  );
}

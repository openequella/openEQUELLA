import SearchSettingFormControl from "./SearchSettingFormControl";
import { Checkbox } from "@material-ui/core";
import * as React from "react";

export interface SettingsCheckbox {
  value: boolean;
  setValue: (value: boolean) => void;
  disabled?: boolean;
  label?: string;
  title?: string;
  helperText?: string;
  id: string;
}
export default function SettingsCheckbox({
  disabled,
  label,
  title,
  helperText,
  value,
  setValue,
  id
}: SettingsCheckbox) {
  return (
    <SearchSettingFormControl
      disabled={disabled}
      onChange={(_, checked) => setValue(checked)}
      control={<Checkbox id={id} checked={value} />}
      label={label}
      title={title}
      helperText={helperText}
    />
  );
}

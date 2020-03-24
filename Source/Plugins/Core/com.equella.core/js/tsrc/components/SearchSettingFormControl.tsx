import {
  FormControl,
  FormControlLabel,
  FormHelperText,
  FormLabel
} from "@material-ui/core";
import * as React from "react";
export interface SearchSettingFormControlProps {
  title?: string;
  label?: string;
  helperText?: string;
  disabled?: boolean;
  control: React.ReactElement;
  onChange: (event: React.ChangeEvent<{}>, checked: boolean) => void;
}
export default function SearchSettingFormControl({
  title,
  label,
  helperText,
  disabled,
  control,
  onChange
}: SearchSettingFormControlProps) {
  return (
    <FormControl>
      {title && <FormLabel>{title}</FormLabel>}
      {helperText && <FormHelperText>{helperText}</FormHelperText>}
      <FormControlLabel
        disabled={disabled}
        label={label}
        control={control}
        onChange={onChange}
      />
    </FormControl>
  );
}

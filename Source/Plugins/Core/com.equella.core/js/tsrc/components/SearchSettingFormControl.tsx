import {
  FormControl,
  FormControlLabel,
  FormHelperText,
  FormLabel
} from "@material-ui/core";
import * as React from "react";

export default function SearchSettingFormControl(props: {
  title?: string;
  label?: string;
  helperText?: string;
  disabled: boolean;
  control: React.ReactElement;
  onChange: (event: React.ChangeEvent<{}>, checked: boolean) => void;
}) {
  const { title, label, disabled, control, onChange, helperText } = props;
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

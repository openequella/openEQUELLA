import {
  FormControl,
  FormControlLabel,
  FormHelperText,
  FormLabel,
} from "@material-ui/core";
import * as React from "react";
import { makeStyles } from "@material-ui/styles";
export interface SearchSettingFormControlProps {
  title?: string;
  label?: string;
  formHelperText?: string;
  disabled?: boolean;
  control: React.ReactElement;
  onChange: (event: React.ChangeEvent<{}>, checked: boolean) => void;
}
const useStyles = makeStyles({
  formControlLabel: {
    marginRight: 0,
  },
});
export default function SearchSettingFormControl({
  title,
  label,
  formHelperText,
  disabled,
  control,
  onChange,
}: SearchSettingFormControlProps) {
  const classes = useStyles();
  return (
    <FormControl>
      {title && <FormLabel>{title}</FormLabel>}
      <FormControlLabel
        className={classes.formControlLabel}
        disabled={disabled}
        label={label}
        control={control}
        onChange={onChange}
      />
      {formHelperText && <FormHelperText>{formHelperText}</FormHelperText>}
    </FormControl>
  );
}

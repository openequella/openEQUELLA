/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

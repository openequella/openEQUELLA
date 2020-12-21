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
  id,
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

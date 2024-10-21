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
import { TextField, Typography } from "@mui/material";
import { pipe } from "fp-ts/function";
import * as R from "fp-ts/Record";
import * as React from "react";
import SettingsList from "./SettingsList";
import SettingsListControl from "./SettingsListControl";
import { languageStrings } from "../util/langstrings";
import { OrdAsIs } from "../util/Ord";

/**
 * The base field render options for the general details section.
 */
export interface FieldRenderOptions {
  /** The label show in the left hand side for the input. */
  label: string;
  /** The secondary text showed under the label. */
  desc?: string;
  /** `true` if the field must have a non-empty value. */
  required: boolean;
  /** Function to check whether the value meets certain requirements. */
  validate?: (value: unknown) => boolean;
  /** Component to be rendered for the field. */
  component: React.JSX.Element;
}

export interface GeneralDetailsSectionProps {
  /**
   * The title of the general details section.
   */
  title: string;
  /**
   * A record that contains the render options for each general detail field.
   */
  fields: Record<string, FieldRenderOptions>;
}

/**
 * Render a text field component for the general details section.
 *
 * @param name The name of the text field.
 * @param value The value of the text field.
 * @param disabled The disabled flag for the text field.
 * @param required The required flag for the text field.
 * @param onChange The function to handle the change event.
 * @param showValidationErrors The flag to indicate whether to show validation errors.
 * @param validate The function to validate the value of the text field.
 */
export const textFiledComponent = (
  name: string,
  value: string | undefined,
  disabled: boolean,
  required: boolean,
  onChange: (value: string) => void,
  showValidationErrors: boolean,
  validate?: (value: unknown) => boolean,
) => (
  <TextField
    fullWidth
    error={validate && showValidationErrors ? !validate(value) : false}
    aria-label={name}
    required={required}
    value={value}
    size="small"
    disabled={disabled}
    onChange={(event) => onChange(event.target.value)}
  />
);

/**
 * This component is used to display and edit different types of input details in a form view.
 */
const GeneralDetailsSection = ({
  title,
  fields,
}: GeneralDetailsSectionProps) => (
  <SettingsList subHeading={title}>
    {pipe(
      fields,
      R.collect(OrdAsIs)((key, renderOption) => {
        const { label, desc, required, component } = renderOption;
        return (
          <SettingsListControl
            primaryText={
              <>
                {label}
                {required ? " *" : ""}
              </>
            }
            secondaryText={desc}
            key={key}
            control={component}
          />
        );
      }),
    )}
    <Typography variant="caption" color="textSecondary">
      {languageStrings.common.required}
    </Typography>
  </SettingsList>
);

export default GeneralDetailsSection;

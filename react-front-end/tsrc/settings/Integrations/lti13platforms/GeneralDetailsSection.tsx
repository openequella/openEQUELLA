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
import * as OEQ from "@openequella/rest-api-client";
import * as E from "fp-ts/Either";
import { identity, pipe } from "fp-ts/function";
import * as R from "fp-ts/Record";
import * as S from "fp-ts/string";
import * as React from "react";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import { languageStrings } from "../../../util/langstrings";
import { OrdAsIs } from "../../../util/Ord";

const {
  name,
  title,
  platformId,
  clientId,
  platformAuthenticationRequestURL,
  platformKeysetURL,
  usernamePrefix,
  usernameSuffix,
  needUrl,
} =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .generalDetails;

type GeneralDetailsType = Omit<
  OEQ.LtiPlatform.LtiPlatformBase,
  "enabled" | "allowExpression" | "unknownUserHandling"
>;

/**
 * Render options for each text input in general details sections.
 *
 * @param name The label show in the left hand side for the input.
 * @param desc The secondary text shows under the name.
 * @param required The mandatory flag for the final submit value. `true` means empty value are not allowed.
 * @param value Current value.
 * @param disabled The flag to disable the input.
 * @param validate Function used to validate current value, if return false display the error outline.
 */
export type GeneralDetailsSectionRenderOptions = {
  [key in keyof GeneralDetailsType]-?: {
    name: string;
    desc?: string;
    required: boolean;
    value: string;
    disabled?: boolean;
    validate?: (value: string) => boolean;
  };
};

const isValidURL = (str: string): boolean =>
  pipe(
    E.tryCatch(
      () => new URL(str),
      () => false
    ),
    E.fold(
      identity,
      // match http:// or https://
      ({ protocol }) => ["http:", "https:"].includes(protocol)
    )
  );

/**
 * Generate the default render options for GeneralDetailsSection.
 */
export const generalDetailsDefaultRenderOption: GeneralDetailsSectionRenderOptions =
  {
    platformId: {
      name: platformId,
      value: "",
      required: true,
      validate: (v) => !S.isEmpty(v),
    },
    name: {
      name: name,
      required: true,
      value: "",
      validate: (v) => !S.isEmpty(v),
    },
    clientId: {
      name: clientId,
      required: true,
      value: "",
      validate: (v) => !S.isEmpty(v),
    },
    authUrl: {
      name: platformAuthenticationRequestURL,
      desc: needUrl,
      required: true,
      value: "",
      validate: (v) => !S.isEmpty(v) && isValidURL(v),
    },
    keysetUrl: {
      name: platformKeysetURL,
      desc: needUrl,
      required: true,
      value: "",
      validate: (v) => !S.isEmpty(v) && isValidURL(v),
    },
    usernamePrefix: {
      name: usernamePrefix,
      required: false,
      value: "",
    },
    usernameSuffix: {
      name: usernameSuffix,
      required: false,
      value: "",
    },
  };

interface GeneralDetailsSectionProps {
  /**
   * An object that contains the render options for each general detail.
   */
  value: GeneralDetailsSectionRenderOptions;
  /**
   * A boolean flag that indicates whether to display validation error outline.
   */
  showValidationErrors: boolean;
  /**
   *  A callback function that handles updates to the general details render options.
   */
  onChange: (newValue: GeneralDetailsSectionRenderOptions) => void;
}

/**
 * This component is used to display and edit general details of an LTI platform
 * within the LTI 1.3 platform creation page.
 */
const GeneralDetailsSection = ({
  value,
  showValidationErrors,
  onChange,
}: GeneralDetailsSectionProps) => (
  <SettingsList subHeading={title}>
    {pipe(
      value,
      R.collect(OrdAsIs)((key, renderOption) => {
        const {
          name,
          desc,
          value: controlValue,
          disabled,
          required,
          validate,
        } = renderOption;

        return (
          <SettingsListControl
            primaryText={
              <>
                {name}
                {required ? "*" : ""}
              </>
            }
            secondaryText={desc}
            key={key}
            control={
              <TextField
                fullWidth
                error={
                  validate && showValidationErrors
                    ? !validate(controlValue)
                    : false
                }
                aria-label={name}
                required={required}
                value={controlValue}
                size="small"
                disabled={disabled}
                onChange={(event) =>
                  onChange({
                    ...value,
                    [key]: {
                      ...renderOption,
                      value: event.target.value,
                    },
                  })
                }
              />
            }
          />
        );
      })
    )}
    <Typography variant="caption" color="textSecondary">
      {languageStrings.common.required}
    </Typography>
  </SettingsList>
);

export default GeneralDetailsSection;

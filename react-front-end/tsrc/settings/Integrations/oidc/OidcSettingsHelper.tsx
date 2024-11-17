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
import Switch from "@mui/material/Switch";
import {
  FieldRenderOptions,
  passwordMask,
  passwordTextFiled,
  plainTextFiled,
} from "../../../components/GeneralDetailsSection";
import { FormControl, MenuItem, Select } from "@mui/material";
import { constTrue, pipe } from "fp-ts/function";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";
import { isNonEmptyString, isValidURL } from "../../../util/validation";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as M from "fp-ts/Map";
import * as S from "fp-ts/string";

const {
  enable: enableLabel,
  issuer: issuerLabel,
  issuerDesc,
  authCodeClientId: authCodeClientIdLabel,
  authCodeClientSecret: authCodeClientSecretLabel,
  authUrl: authUrlLabel,
  authUrlDesc,
  keysetUrl: keysetUrlLabel,
  keysetUrlDesc,
  tokenUrl: tokenUrlLabel,
  tokenUrlDesc,
  usernameClaim: usernameClaimLabel,
  usernameClaimDesc,
} = languageStrings.settings.integration.oidc.generalDetails;
const {
  platform: platformLabel,
  generic: {
    apiUrl: apiUrlLabel,
    apiUrlDesc,
    apiClientId: apiClientIdLabel,
    apiClientIdDesc,
    apiClientSecret: apiClientSecretLabel,
    apiClientSecretDesc,
  },
} = languageStrings.settings.integration.oidc.apiDetails;
const { select: selectLabel } = languageStrings.common.action;
const { missingValue, invalidUrl } = languageStrings.error;

export const platforms = new Map<OEQ.Oidc.IdentityProviderPlatform, string>([
  ["GENERIC", "Generic"],
]);

// Use 'platform' as the discriminator
export interface GenericApiDetails
  extends Pick<
    OEQ.Oidc.GenericIdentityProvider,
    "platform" | "apiUrl" | "apiClientId" | "apiClientSecret"
  > {}

//TODO: Add more platform API details: Azure, Cognito, Google, Okta.
export type ApiDetails = GenericApiDetails;

export const defaultGeneralDetails: OEQ.Oidc.IdentityProvider = {
  enabled: false,
  platform: "GENERIC",
  issuer: "",
  authCodeClientId: "",
  authCodeClientSecret: "",
  authUrl: "",
  keysetUrl: "",
  tokenUrl: "",
  defaultRoles: new Set(),
};

export const defaultGenericApiDetails: GenericApiDetails = {
  platform: "GENERIC",
  apiUrl: "",
  apiClientId: "",
  apiClientSecret: "",
};

//TODO: Update default values for other platforms.
export const defaultApiDetailsMap: Record<
  OEQ.Oidc.IdentityProviderPlatform,
  ApiDetails
> = {
  GENERIC: defaultGenericApiDetails,
  AZURE: defaultGenericApiDetails,
  COGNITO: defaultGenericApiDetails,
  GOOGLE: defaultGenericApiDetails,
  OKTA: defaultGenericApiDetails,
};

const platformSelector = (
  value: OEQ.Oidc.IdentityProviderPlatform,
  onChange: (value: string) => void,
) => (
  <FormControl size="small" fullWidth>
    <Select
      aria-label={`${selectLabel} ${platformLabel}`}
      value={value}
      onChange={(event) => onChange(event.target.value)}
      variant="outlined"
    >
      {pipe(
        platforms,
        M.toArray(S.Ord),
        A.map(([platform, label]) => (
          <MenuItem key={platform} value={platform}>
            {label}
          </MenuItem>
        )),
      )}
    </Select>
  </FormControl>
);

/**
 * Build the render options for GeneralDetailsSection.
 *
 * @param fields Contains value of each field.
 * @param onChange Function to be called when a field is changed.
 * @param showValidationErrors Whether to show validation errors for each field.
 * @param isConfigured Whether the server already has the general details.
 */
export const generateGeneralDetails = (
  {
    enabled,
    issuer,
    authCodeClientId,
    authCodeClientSecret,
    authUrl,
    keysetUrl,
    tokenUrl,
    usernameClaim,
  }: OEQ.Oidc.IdentityProvider,
  onChange: (key: string, value: unknown) => void,
  showValidationErrors: boolean,
  isConfigured: boolean,
): Record<string, FieldRenderOptions> => ({
  enabled: {
    label: enableLabel,
    required: true,
    component: (
      <Switch
        required
        checked={enabled}
        onChange={(event) => onChange("enabled", event.target.checked)}
      />
    ),
  },
  issuer: {
    label: issuerLabel,
    desc: issuerDesc,
    required: true,
    validate: isNonEmptyString,
    component: plainTextFiled({
      name: issuerLabel,
      value: issuer,
      disabled: false,
      required: true,
      onChange: (value) => onChange("issuer", value),
      showValidationErrors,
      validate: isNonEmptyString,
      errorMessage: missingValue,
    }),
  },
  authCodeClientId: {
    label: authCodeClientIdLabel,
    required: true,
    validate: isNonEmptyString,
    component: plainTextFiled({
      name: authCodeClientIdLabel,
      value: authCodeClientId,
      disabled: false,
      required: true,
      onChange: (value) => onChange("authCodeClientId", value),
      showValidationErrors,
      validate: isNonEmptyString,
      errorMessage: missingValue,
    }),
  },
  authCodeClientSecret: {
    label: authCodeClientSecretLabel,
    required: true,
    // Validation is not required for updating but required for the initial creation.
    validate: isConfigured ? constTrue : isNonEmptyString,
    component: passwordTextFiled({
      name: authCodeClientSecretLabel,
      value: authCodeClientSecret,
      disabled: false,
      required: true,
      onChange: (value) => onChange("authCodeClientSecret", value),
      showValidationErrors,
      validate: isConfigured ? constTrue : isNonEmptyString,
      errorMessage: missingValue,
      placeholder: isConfigured ? passwordMask : undefined,
    }),
  },
  authUrl: {
    label: authUrlLabel,
    desc: authUrlDesc,
    required: true,
    validate: isValidURL,
    component: plainTextFiled({
      name: authUrlLabel,
      value: authUrl,
      disabled: false,
      required: true,
      onChange: (value) => onChange("authUrl", value),
      showValidationErrors,
      validate: isValidURL,
      errorMessage: invalidUrl,
    }),
  },
  keysetUrl: {
    label: keysetUrlLabel,
    desc: keysetUrlDesc,
    required: true,
    validate: isValidURL,
    component: plainTextFiled({
      name: keysetUrlLabel,
      value: keysetUrl,
      disabled: false,
      required: true,
      onChange: (value) => onChange("keysetUrl", value),
      showValidationErrors,
      validate: isValidURL,
      errorMessage: invalidUrl,
    }),
  },
  tokenUrl: {
    label: tokenUrlLabel,
    desc: tokenUrlDesc,
    required: true,
    validate: isValidURL,
    component: plainTextFiled({
      name: tokenUrlLabel,
      value: tokenUrl,
      disabled: false,
      required: true,
      onChange: (value) => onChange("tokenUrl", value),
      showValidationErrors,
      validate: isValidURL,
      errorMessage: invalidUrl,
    }),
  },
  usernameClaim: {
    label: usernameClaimLabel,
    desc: usernameClaimDesc,
    required: false,
    component: plainTextFiled({
      name: usernameClaimLabel,
      value: usernameClaim,
      disabled: false,
      required: false,
      onChange: (value) => onChange("usernameClaim", value),
      showValidationErrors,
    }),
  },
});

const genericApiDetails = (
  onChange: (key: string, value: unknown) => void,
  showValidationErrors: boolean,
  apiDetails: GenericApiDetails,
  isConfigured: boolean,
) => {
  const { apiUrl, apiClientId, apiClientSecret } = apiDetails;

  return {
    apiUrl: {
      label: apiUrlLabel,
      desc: apiUrlDesc,
      required: true,
      validate: isValidURL,
      component: plainTextFiled({
        name: apiUrlLabel,
        value: apiUrl,
        disabled: false,
        required: true,
        onChange: (value) => onChange("apiUrl", value),
        showValidationErrors,
        validate: isValidURL,
        errorMessage: invalidUrl,
      }),
    },
    apiClientId: {
      label: apiClientIdLabel,
      desc: apiClientIdDesc,
      required: true,
      validate: isNonEmptyString,
      component: plainTextFiled({
        name: apiClientIdLabel,
        value: apiClientId,
        disabled: false,
        required: true,
        onChange: (value) => onChange("apiClientId", value),
        showValidationErrors,
        validate: isNonEmptyString,
        errorMessage: missingValue,
      }),
    },
    apiClientSecret: {
      label: apiClientSecretLabel,
      desc: apiClientSecretDesc,
      required: true,
      // Validation is not required for updating but required for the initial creation.
      validate: isConfigured ? constTrue : isNonEmptyString,
      component: passwordTextFiled({
        name: apiClientSecretLabel,
        value: apiClientSecret,
        disabled: false,
        required: true,
        onChange: (value) => onChange("apiClientSecret", value),
        showValidationErrors,
        validate: isConfigured ? constTrue : isNonEmptyString,
        errorMessage: missingValue,
        placeholder: isConfigured ? passwordMask : undefined,
      }),
    },
  };
};

/**
 * Generate the render options for the platform selector.
 *
 * @param platform Current selected platform.
 * @param platformOnChange Function to be called when the platform is changed.
 */
export const generatePlatform = (
  platform: OEQ.Oidc.IdentityProviderPlatform,
  platformOnChange: (newValue: string) => void,
) => ({
  platform: {
    label: platformLabel,
    required: true,
    validate: isNonEmptyString,
    component: platformSelector(platform, platformOnChange),
  },
});

/**
 * Generate the render options for the API configuration of different identity providers.
 *

 * @param apiDetails The value of the platform specific details.

 * @param apiDetailsOnChange Function to be called when a platform specific field is changed.
 * @param showValidationErrors Whether to show validation errors for each field.
 * @param isConfigured Whether the server already has the API details.
 */
export const generateApiDetails = (
  apiDetails: ApiDetails,
  apiDetailsOnChange: (key: string, value: unknown) => void,
  showValidationErrors: boolean,
  isConfigured: boolean,
) => {
  const p = apiDetails.platform;
  switch (p) {
    case "GENERIC":
      return genericApiDetails(
        apiDetailsOnChange,
        showValidationErrors,
        apiDetails,
        isConfigured,
      );
    default:
      throw new Error(`Unsupported platform: ${p}`);
  }
};

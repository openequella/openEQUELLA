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
import { getBaseUrl } from "../../../AppConfig";
import {
  FieldRenderOptions,
  passwordMask,
  passwordTextFiled,
  plainTextFiled,
} from "../../../components/GeneralDetailsSection";
import {
  FormControl,
  ListItem,
  ListItemText,
  MenuItem,
  Select,
} from "@mui/material";
import { absurd, constTrue, pipe } from "fp-ts/function";
import * as React from "react";
import SettingsList from "../../../components/SettingsList";
import SettingsListConfiguration from "../../../components/SettingsListConfiguration";
import { keysetUrlDetails } from "../../../modules/Lti13PlatformsModule";
import { languageStrings } from "../../../util/langstrings";
import { isNonEmptyString, isValidURL } from "../../../util/validation";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import * as M from "fp-ts/Map";
import * as S from "fp-ts/string";
import * as R from "fp-ts/Record";

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
const {
  title: oeqDetailsTitle,
  desc: oeqDetailsDesc,
  redirect: redirectTitle,
} = languageStrings.settings.integration.oidc.oeqDetails;

const baseUrl = getBaseUrl();

export const platforms = new Map<OEQ.Oidc.IdentityProviderPlatform, string>([
  ["ENTRA_ID", "Entra ID"],
  ["AUTH0", "Auth0"],
  ["OKTA", "Okta"],
]);

export const defaultApiDetails: OEQ.Oidc.RestApiDetails = {
  apiUrl: "",
  apiClientId: "",
};

export const defaultConfig: OEQ.Oidc.IdentityProvider = {
  enabled: false,
  platform: "ENTRA_ID",
  issuer: "",
  authCodeClientId: "",
  authCodeClientSecret: "",
  authUrl: "",
  keysetUrl: "",
  tokenUrl: "",
  defaultRoles: new Set(),
  ...defaultApiDetails,
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

const apiDetails = (
  onChange: (key: string, value: unknown) => void,
  showValidationErrors: boolean,
  { apiUrl, apiClientId, apiClientSecret }: OEQ.Oidc.IdentityProvider,
  isSecretConfigured: boolean,
): Record<string, FieldRenderOptions> => {
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
      validate: isSecretConfigured ? constTrue : isNonEmptyString,
      component: passwordTextFiled({
        name: apiClientSecretLabel,
        value: apiClientSecret,
        disabled: false,
        required: true,
        onChange: (value) => onChange("apiClientSecret", value),
        showValidationErrors,
        validate: isSecretConfigured ? constTrue : isNonEmptyString,
        errorMessage: missingValue,
        placeholder: isSecretConfigured ? passwordMask : undefined,
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
): Record<string, FieldRenderOptions> => ({
  platform: {
    label: platformLabel,
    required: true,
    validate: isNonEmptyString,
    component: platformSelector(platform, platformOnChange),
  },
});

/**
 * Generate the render options for the API configuration of the selected identity providers.
 *
 * @param idpDetails Contains the value of the platform specific details.
 * @param apiDetailsOnChange Function to be called when a platform specific field is changed.
 * @param showValidationErrors Whether to show validation errors for each field.
 * @param isSecretConfigured Whether the server already has the API secrete.
 */
export const generateApiDetails = (
  idpDetails: OEQ.Oidc.IdentityProvider,
  apiDetailsOnChange: (key: string, value: unknown) => void,
  showValidationErrors: boolean,
  isSecretConfigured: boolean,
): Record<string, FieldRenderOptions> => {
  const platform = idpDetails.platform;

  const apiCommonFields = apiDetails(
    apiDetailsOnChange,
    showValidationErrors,
    idpDetails,
    isSecretConfigured,
  );
  const { apiUrl, apiClientId, apiClientSecret } = apiCommonFields;

  switch (platform) {
    case "AUTH0":
      return apiCommonFields;
    case "ENTRA_ID":
      return { apiClientId, apiClientSecret };
    case "OKTA":
      return { apiUrl, apiClientId };
    default:
      return absurd(platform);
  }
};

const oeqDetails = {
  redirect: {
    name: redirectTitle,
    value: `${baseUrl}oidc/callback`,
  },
  keysetUrl: keysetUrlDetails,
};

/*** A list of OEQ details for the OIDC settings. **/
export const oeqDetailsList = (
  <SettingsList subHeading={oeqDetailsTitle}>
    <ListItem>
      <ListItemText>{oeqDetailsDesc}</ListItemText>
    </ListItem>
    {pipe(
      oeqDetails,
      R.toEntries,
      A.map(([key, { name, value }]) => (
        <SettingsListConfiguration key={key} title={name} value={value} />
      )),
    )}
  </SettingsList>
);

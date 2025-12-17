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
import "@testing-library/jest-dom";
import { pipe } from "fp-ts/function";
import { languageStrings } from "../../../../../tsrc/util/langstrings";
import { mockRoleAndGroupApis } from "../../../components/securityentitydialog/SelectEntityDialogTestHelper";
import {
  inputMuiTextFieldByAriaLabel,
  getMuiTextFieldByAriaLabel,
  getMuiTextFieldValueByAriaLabel,
} from "../../../MuiTestHelpers";
import {
  clickEnableOidcSwitch,
  clickSaveButton,
  fillAllRequiredFields,
  getLoadingCircle,
  getSaveButton,
  queryValidationMessage,
  renderOidcSettings,
  selectPlatform,
} from "./OidcSettingsTestHelper";
import * as OEQ from "@openequella/rest-api-client";
import * as OidcModule from "../../../../../tsrc/modules/OidcModule";
import * as R from "fp-ts/Record";

const { missingValue, invalidUrl } = languageStrings.error;
const { title: customRolesTitle } = languageStrings.customRolesMappingControl;
const { roleClaim: roleClaimTitle, userIdAttributeLabel } =
  languageStrings.settings.integration.oidc.mappings;
const {
  issuer: issuerLabel,
  authCodeClientId: authCodeClientIdLabel,
  authCodeClientSecret: authCodeClientSecretLabel,
  authUrl: authUrlLabel,
  keysetUrl: keysetUrlLabel,
  tokenUrl: tokenUrlLabel,
  usernameClaim: usernameClaimLabel,
} = languageStrings.settings.integration.oidc.generalDetails;
const {
  generic: {
    apiUrl: apiUrlLabel,
    apiClientId: apiClientIdLabel,
    apiClientSecret: apiClientSecretLabel,
  },
} = languageStrings.settings.integration.oidc.apiDetails;

jest.setTimeout(10000);

mockRoleAndGroupApis();

const mockedOidcSettings: OEQ.Oidc.IdentityProvider = {
  platform: "OKTA",
  issuer: "https://issuer.com/",
  authCodeClientId: "authCodeClientId",
  authUrl: "https://auth.com",
  keysetUrl: "https://keyset/.well-known/jwks.json",
  tokenUrl: "https://token.com",
  usernameClaim: "",
  defaultRoles: new Set(),
  enabled: false,
  apiUrl: "https://test.com",
  apiClientId: "test ID",
};

describe("General details section", () => {
  const allTextFields = [
    issuerLabel,
    authCodeClientIdLabel,
    authCodeClientSecretLabel,
    authUrlLabel,
    keysetUrlLabel,
    tokenUrlLabel,
    usernameClaimLabel,
  ];

  it.each(allTextFields)(
    "should render the text field with label '%s'",
    async (label) => {
      const { container } = await renderOidcSettings();

      const textField = getMuiTextFieldByAriaLabel(container, label);

      expect(textField).toBeInTheDocument();
    },
  );
});

describe("Platform details section", () => {
  const entraIdFields = [apiClientIdLabel, apiClientSecretLabel];
  const auth0Fields = [apiUrlLabel, apiClientIdLabel, apiClientSecretLabel];
  const oktaFields = [apiUrlLabel, apiClientIdLabel];

  it.each<[OEQ.Oidc.IdentityProviderPlatform, string[]]>([
    ["ENTRA_ID", entraIdFields],
    ["AUTH0", auth0Fields],
    ["OKTA", oktaFields],
  ])("should render fields for platform '%s'", async (platform, fields) => {
    const { container } = await renderOidcSettings();

    await selectPlatform(container, platform);

    fields.forEach((field) => {
      const textField = getMuiTextFieldByAriaLabel(container, field);
      expect(textField).toBeInTheDocument();
    });
  });

  it("If user switch back to the original IdP it should display the original value", async () => {
    const { container } = await renderOidcSettings(mockedOidcSettings);

    await selectPlatform(container, "AUTH0");
    await selectPlatform(container, "OKTA");

    expect(getMuiTextFieldValueByAriaLabel(container, apiUrlLabel)).toBe(
      mockedOidcSettings.apiUrl,
    );
    expect(getMuiTextFieldValueByAriaLabel(container, apiClientIdLabel)).toBe(
      mockedOidcSettings.apiClientId,
    );
  });
});

describe("Mapping section", () => {
  const roleClaimValue = "test value";
  const mockedOidcWithRoleClaim: OEQ.Oidc.IdentityProvider = {
    ...mockedOidcSettings,
    roleConfig: {
      roleClaim: roleClaimValue,
      customRoles: new Map(),
    },
  };

  it("Hide custom role field when role claims is undefined", async () => {
    const { queryByText } = await renderOidcSettings();

    expect(queryByText(customRolesTitle)).not.toBeInTheDocument();
  });

  it("Hide custom role field when user clear the role claims value", async () => {
    const { container, queryByText } = await renderOidcSettings(
      mockedOidcWithRoleClaim,
    );

    await inputMuiTextFieldByAriaLabel(container, roleClaimTitle);

    expect(queryByText(customRolesTitle)).not.toBeInTheDocument();
  });

  it("Display custom role field if role claims is not empty", async () => {
    const { queryByText } = await renderOidcSettings(mockedOidcWithRoleClaim);

    expect(queryByText(customRolesTitle)).toBeInTheDocument();
  });

  it("renders a user ID text field", async () => {
    const { container, queryByText } = await renderOidcSettings();
    await selectPlatform(container, "ENTRA_ID");
    const textField = queryByText(userIdAttributeLabel);

    expect(textField).toBeInTheDocument();
  });
});

describe("Save button", () => {
  jest
    .spyOn(OidcModule, "updateOidcSettings")
    .mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 1000)),
    );

  it("Enable save button if settings are changed", async () => {
    const { container } = await renderOidcSettings();
    expect(getSaveButton(container)).toBeDisabled();

    await inputMuiTextFieldByAriaLabel(
      container,
      authCodeClientIdLabel,
      "test value",
    );
    expect(getSaveButton(container)).toBeEnabled();
  });

  it("Disable save button if settings are changed back to original value", async () => {
    const { container } = await renderOidcSettings();

    await inputMuiTextFieldByAriaLabel(container, issuerLabel, "test issuer");
    expect(getSaveButton(container)).toBeEnabled();

    await inputMuiTextFieldByAriaLabel(container, issuerLabel);
    expect(getSaveButton(container)).toBeDisabled();
  });

  it("Disable save button while saving", async () => {
    const { container } = await renderOidcSettings();

    await fillAllRequiredFields(container);
    await clickSaveButton(container);

    expect(getSaveButton(container)).toBeDisabled();
  });

  it("Display loading circle while saving", async () => {
    const { container } = await renderOidcSettings();

    await fillAllRequiredFields(container);
    await clickSaveButton(container);

    expect(getLoadingCircle(container)).toBeInTheDocument();
  });
});

describe("Oidc settings page", () => {
  it.each<[OEQ.Oidc.IdentityProviderPlatform, Record<string, string>]>([
    [
      "AUTH0",
      {
        "API endpoint": invalidUrl,
        "API Client ID": missingValue,
        "API Client Secret": missingValue,
      },
    ],
    [
      "ENTRA_ID",
      {
        "API Client ID": missingValue,
        "API Client Secret": missingValue,
      },
    ],
    [
      "OKTA",
      {
        "API endpoint": invalidUrl,
        "API Client ID": missingValue,
      },
    ],
  ])(
    "Display validation error for %s after user clicks saving",
    async (details: OEQ.Oidc.IdentityProviderPlatform, validationMessages) => {
      const { container } = await renderOidcSettings();

      await clickEnableOidcSwitch(container);
      await selectPlatform(container, details);
      await clickSaveButton(container);

      const realMessages = pipe(
        validationMessages,
        R.mapWithIndex(
          (title, message) =>
            queryValidationMessage(container, title, message)?.textContent,
        ),
      );

      expect(realMessages).toEqual(validationMessages);
    },
  );

  it.each([
    ["Issuer", missingValue],
    ["Client ID", missingValue],
    ["Client secret", missingValue],
    ["Identity Provider Login URL", invalidUrl],
    ["Public Key Endpoint URL", invalidUrl],
    ["Token URL", invalidUrl],
  ])(
    "Display validation error for General Details %s after user clicks saving",
    async (title, message) => {
      const { container } = await renderOidcSettings();
      await clickEnableOidcSwitch(container);

      await clickSaveButton(container);

      expect(
        queryValidationMessage(container, title, message),
      ).toBeInTheDocument();
    },
  );

  it("Should be able to save the settings after filling all the required fields", async () => {
    const mockUpdateOidcSettings = jest.fn();
    jest
      .spyOn(OidcModule, "updateOidcSettings")
      .mockImplementationOnce(mockUpdateOidcSettings);

    const expectedResult = {
      enabled: false,
      issuer: "https://test.issuer.com",
      apiClientId: "id",
      apiClientSecret: "secret",
      keysetUrl: "https://test.key.com",
      tokenUrl: "https://test.token.com",
      authUrl: "https://test.login.com",
      defaultRoles: new Set(),
      platform: "ENTRA_ID",
      apiUrl: "",
      authCodeClientId: "test ID",
      authCodeClientSecret: "test secret",
    };

    const { container } = await renderOidcSettings();

    await fillAllRequiredFields(container);
    await clickSaveButton(container);

    expect(mockUpdateOidcSettings).toHaveBeenLastCalledWith(expectedResult);
  });
});

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

import { languageStrings } from "../../../../../tsrc/util/langstrings";
import {
  fillMuiTextFieldByAriaLabel,
  getMuiTextFieldByAriaLabel,
} from "../../../MuiTestHelpers";
import { renderOidcSettings, selectPlatform } from "./OidcSettingsTestHelper";
import * as OEQ from "@openequella/rest-api-client";

const { title: customRolesTitle } = languageStrings.customRolesMappingControl;
const { roleClaim: roleClaimTitle } =
  languageStrings.settings.integration.oidc.roleMappings;
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
  const genericFields = [apiUrlLabel, apiClientIdLabel, apiClientSecretLabel];

  it.each<[OEQ.Oidc.IdentityProviderPlatform, string[]]>([
    ["AUTH0", genericFields],
  ])("should render fields for platform '%s'", async (platform, fields) => {
    const { container } = await renderOidcSettings();

    await selectPlatform(container, platform);

    fields.forEach((field) => {
      const textField = getMuiTextFieldByAriaLabel(container, field);
      expect(textField).toBeInTheDocument();
    });
  });
});

describe("Role mapping section", () => {
  it("Hide custom role field when role claims is empty", async () => {
    const { queryByText } = await renderOidcSettings();

    expect(queryByText(customRolesTitle)).not.toBeInTheDocument();
  });

  it("Display custom role field if role claims is not empty", async () => {
    const { container, queryByText } = await renderOidcSettings();

    await fillMuiTextFieldByAriaLabel(container, roleClaimTitle, "Test Value");

    expect(queryByText(customRolesTitle)).toBeInTheDocument();
  });
});

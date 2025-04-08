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
import userEvent from "@testing-library/user-event";
import { pipe } from "fp-ts/function";
import { Router } from "react-router-dom";
import OidcSettings from "../../../../../tsrc/settings/Integrations/oidc/OidcSettings";
import {
  getByLabelText,
  getByText,
  queryByText,
  render,
  RenderResult,
  waitFor,
} from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { platforms } from "../../../../../tsrc/settings/Integrations/oidc/OidcSettingsHelper";
import { languageStrings } from "../../../../../tsrc/util/langstrings";
import {
  inputMuiTextFieldByAriaLabel,
  selectOption,
} from "../../../MuiTestHelpers";
import * as O from "fp-ts/Option";
import * as OidcModule from "../../../../../tsrc/modules/OidcModule";

const { save: saveText, select: selectText } = languageStrings.common.action;
const {
  generalDetails: { title: generalDetailsTitle, enable: enableLabel },
  apiDetails: { platform: platformLabel },
} = languageStrings.settings.integration.oidc;

/**
 * Helper to query the loading circle.
 *
 * @param container The container where the loading circle is located.
 */
export const getLoadingCircle = (container: HTMLElement) =>
  container.querySelector(".MuiCircularProgress-circle");

/**
 * Helper to render OidcSettings page.
 *
 * @param initialSettings Mock data for the initial OIDC settings fetched from the server.
 */
export const renderOidcSettings = async (
  initialSettings?: OEQ.Oidc.IdentityProvider,
): Promise<RenderResult> => {
  const props = {
    updateTemplate: () => {},
  };

  if (initialSettings) {
    jest
      .spyOn(OidcModule, "getOidcSettings")
      .mockResolvedValueOnce(initialSettings);
  }

  const history = createMemoryHistory();
  const result = render(
    <Router history={history}>
      <OidcSettings {...props} />
    </Router>,
  );
  // wait for OIDC configuration page rendered.
  await waitFor(() => result.getByText(generalDetailsTitle));
  // The loading circle should not be displayed.
  await waitFor(() =>
    expect(getLoadingCircle(result.container)).not.toBeInTheDocument(),
  );
  return result;
};

/**
 * Helper to select a platform in the OIDC settings page.
 *
 * @param container The container where the platform is located.
 * @param platform The platform to select.
 */
export const selectPlatform = async (
  container: HTMLElement,
  platform: OEQ.Oidc.IdentityProviderPlatform,
) => {
  const label = `${selectText} ${platformLabel}`;
  const platformText = pipe(
    platforms.get(platform),
    O.fromNullable,
    O.getOrElseW(() => {
      throw new Error("Can't find platform:" + platform);
    }),
  );

  await selectOption(container, `div[aria-label='${label}'] div`, platformText);
};

/**
 * Helper to get the save button.
 *
 * @param container The container where the save button is located.
 */
export const getSaveButton = (container: HTMLElement) =>
  getByLabelText(container, saveText);

/**
 * Helper to click the save button.
 *
 * @param container The container where the save button is located.
 */
export const clickSaveButton = async (container: HTMLElement) =>
  userEvent.click(getSaveButton(container));

/**
 * Helper to get the validation message for setting.
 *
 * @param container The container where the validation message is located.
 * @param label The label of the setting.
 * @param message The error message of the validation.
 */
export const queryValidationMessage = (
  container: HTMLElement,
  label: string,
  message: string,
) => {
  const inputContainer = getByLabelText(container, label);
  return queryByText(inputContainer, message);
};

/**
 * Helper function to click the enable switch.getSetting(container, title)
 *
 * @param container The container where the enable switch is located.
 */
export const clickEnableOidcSwitch = async (container: HTMLElement) => {
  const enableSwitch = getByText(
    container,
    enableLabel + " *",
  ).parentElement?.parentElement?.querySelector("input[type='checkbox']");

  if (!enableSwitch) {
    throw new Error(`Can't find enable switch`);
  }

  await userEvent.click(enableSwitch);
};

/**
 * Helper function to fill all required fields in the OIDC settings page.
 */
export const fillAllRequiredFields = async (container: HTMLElement) => {
  const oidcSettings = {
    Issuer: "https://test.issuer.com",
    "Client ID": "test ID",
    "Client secret": "test secret",
    "Identity Provider Login URL": "https://test.login.com",
    "Public Key Endpoint URL": "https://test.key.com",
    "Token URL": "https://test.token.com",
    "API Client ID": "id",
    "API Client Secret": "secret",
  };

  for (const [label, value] of Object.entries(oidcSettings)) {
    await inputMuiTextFieldByAriaLabel(container, label, value);
  }
};

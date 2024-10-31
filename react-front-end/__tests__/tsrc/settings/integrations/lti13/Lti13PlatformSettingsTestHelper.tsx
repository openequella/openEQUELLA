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
import * as OEQ from "@openequella/rest-api-client";
import {
  getByLabelText,
  getByText,
  render,
  RenderResult,
  waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import { Router } from "react-router-dom";
import {
  getPlatforms,
  platforms,
} from "../../../../../__mocks__/Lti13PlatformsModule.mock";
import Lti13PlatformsSettings, {
  Lti13PlatformsSettingsProps,
} from "../../../../../tsrc/settings/Integrations/lti13/Lti13PlatformsSettings";
import * as React from "react";
import { languageStrings } from "../../../../../tsrc/util/langstrings";

const { platformsTitle } =
  languageStrings.settings.integration.lti13PlatformsSettings;

const commonProps: Lti13PlatformsSettingsProps = {
  updateTemplate: () => {},
  getPlatformsProvider: getPlatforms,
};

const { delete: deleteLabel } = languageStrings.common.action;

/**
 * Helper to render Lti13PlatformsSettings and wait for component under test
 */
export const renderLti13PlatformsSettings = async (
  updateEnabledPlatformsProvider: (
    enabledStatus: OEQ.LtiPlatform.LtiPlatformEnabledStatus[],
  ) => Promise<OEQ.BatchOperationResponse.BatchOperationResponse[]> = jest.fn(),
  deletePlatformsProvider: (
    platformIds: string[],
  ) => Promise<OEQ.BatchOperationResponse.BatchOperationResponse[]> = jest.fn(),
): Promise<RenderResult> => {
  const props = {
    ...commonProps,
    deletePlatformsProvider: deletePlatformsProvider,
    updateEnabledPlatformsProvider: updateEnabledPlatformsProvider,
  };
  const history = createMemoryHistory();
  const result = render(
    <Router history={history}>
      <Lti13PlatformsSettings {...props} />
    </Router>,
  );
  // wait for platform list rendered
  await waitFor(() => result.getByText(platforms[0].name));
  return result;
};

/**
 * Helper to get the platforms section (the section which contains the platforms list).
 */
export const getPlatformsSection = (container: HTMLElement) => {
  const platformsSection = getByText(container, platformsTitle).parentElement;

  if (!platformsSection) {
    throw Error(`Not able to find platforms section`);
  }

  return platformsSection;
};

/**
 * Helper to mock click enable/disable toggle button for the provided platform entry (located by platform name).
 */
export const clickEnabledSwitchForPlatform = async (
  page: HTMLElement,
  platformId: string,
) => {
  const enabledSwitch = getByText(
    page,
    platformId,
  ).parentElement?.parentElement?.querySelector("input[type='checkbox']");

  if (!enabledSwitch) {
    throw Error(`Not able to find enabled switch for platform ${platformId}`);
  }

  await userEvent.click(enabledSwitch);
};

/**
 * Helper to mock click delete button for the provided platform entry (located by platform name).
 */
export const clickDeleteButtonForPlatform = async (
  container: HTMLElement,
  platformName: string,
) => {
  const deleteButton = getByLabelText(
    container,
    `${deleteLabel} ${platformName}`,
  );

  if (!deleteButton) {
    throw Error(`Not able to find delete button for platform ${platformName}`);
  }

  await userEvent.click(deleteButton);
};

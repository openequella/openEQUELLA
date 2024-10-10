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
import { render, RenderResult, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { listRoles } from "../../../../../__mocks__/RoleModule.mock";

import LtiCustomRolesMapping, {
  LtiCustomRolesMappingProps,
} from "../../../../../tsrc/settings/Integrations/lti13platforms/LtiCustomRolesMapping";
import { languageStrings } from "../../../../../tsrc/util/langstrings";
import { clickSelect } from "../../../MuiTestHelpers";
import * as React from "react";

const { customRoleSelectLtiRole } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .roleMappings;

export const commonLtiCustomRolesMappingProps = {
  value: new Map(),
  onChange: jest.fn(),
  searchRoleProvider: listRoles,
};

/**
 * Helper to render LtiCustomRolesMapping.
 */
export const renderLtiCustomRolesMapping = (
  props: LtiCustomRolesMappingProps = commonLtiCustomRolesMappingProps,
): RenderResult => render(<LtiCustomRolesMapping {...props} />);

/**
 * Select a LTI role from the select in dialog.
 */
export const selectLtiRole = async (dialog: HTMLElement, ltiRole: string) => {
  await clickSelect(dialog, `div[aria-label='${customRoleSelectLtiRole}'] div`);
  const option = await screen.findByText(ltiRole);
  // click the option in the list
  await userEvent.click(option);
};

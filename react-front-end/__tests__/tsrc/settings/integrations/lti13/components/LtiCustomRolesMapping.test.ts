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
import * as SET from "fp-ts/Set";
import { roles } from "../../../../../../__mocks__/RoleModule.mock";
import { languageStrings } from "../../../../../../tsrc/util/langstrings";
import {
  clickOkButton,
  mockRoleAndGroupApis,
} from "../../../../components/securityentitydialog/SelectEntityDialogTestHelper";
import {
  doSearchAndSelectRole,
  openDialog,
} from "../../../../components/CustomRolesMappingControlTestHelper";
import {
  commonLtiCustomRolesMappingProps,
  renderLtiCustomRolesMapping,
  selectLtiRole,
} from "./LtiCustomRolesMappingTestHelper";

const { ltiRoles } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage;

mockRoleAndGroupApis();

describe("LtiCustomRolesMapping", () => {
  it("Should be able to select different LTI roles", async () => {
    const expectedResult = new Map().set(
      "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Guest",
      SET.singleton(roles[0].id),
    );
    const selectedRoleName = roles[0].name;
    const onChange = jest.fn();
    const { container, getByRole } = renderLtiCustomRolesMapping({
      ...commonLtiCustomRolesMappingProps,
      onChange,
    });

    await openDialog(container);
    const dialog = getByRole("dialog");
    await selectLtiRole(dialog, ltiRoles.institution.Guest);
    await doSearchAndSelectRole(dialog, selectedRoleName, selectedRoleName);
    await clickOkButton(dialog);

    const result = onChange.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  });
});

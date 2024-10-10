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
import { getByText, render, RenderResult } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import { defaultACLEntityResolvers } from "../../../../../__mocks__/ACLExpressionBuilder.mock";
import { listGroups } from "../../../../../__mocks__/GroupModule.mock";
import { listRoles } from "../../../../../__mocks__/RoleModule.mock";
import { listUsers } from "../../../../../__mocks__/UserModule.mock";
import CreateLti13Platform, {
  CreateLti13PlatformProps,
} from "../../../../../tsrc/settings/Integrations/lti13platforms/CreateLti13Platform";
import { languageStrings } from "../../../../../tsrc/util/langstrings";
import { selectAllAndConfirm } from "../../../components/aclexpressionbuilder/ACLExpressionBuilderTestHelper";
import { clickOkButton } from "../../../components/securityentitydialog/SelectEntityDialogTestHelper";
import { searchAndSelectGroup } from "../../../components/securityentitydialog/SelectGroupDialogTestHelper";
import { searchAndSelectRole } from "../../../components/securityentitydialog/SelectRoleDialogTestHelper";
import { searchUser } from "../../../components/securityentitysearch/UserSearchTestHelpler";
import { selectOption } from "../../../MuiTestHelpers";
import { doSearchAndSelectRole } from "../../../components/CustomRolesMappingControlTestHelper";
import { selectLtiRole } from "./LtiCustomRolesMappingTestHelper";

const {
  select: selectLabel,
  edit: editLabel,
  save: saveLabel,
} = languageStrings.common.action;
const {
  accessControl: {
    usableBy: usableByLabel,
    unknownUserHandling: unknownUserHandlingLabel,
    groups: selectGroupsLabel,
  },
  roleMappings: {
    instructorRoles: instructorRolesLabel,
    customRoles: customRolesLabel,
    unknownRoles: unknownRolesLabel,
  },
} = languageStrings.settings.integration.lti13PlatformsSettings.createPage;

export const commonCreateLti13PlatformProps: CreateLti13PlatformProps = {
  updateTemplate: () => {},
  searchUserProvider: listUsers,
  searchGroupProvider: listGroups,
  searchRoleProvider: listRoles,
  aclEntityResolversProvider: defaultACLEntityResolvers,
};

/**
 * Helper to render CreateLti13Platform page.
 */
export const renderCreateLti13Platform = async (
  props: CreateLti13PlatformProps = commonCreateLti13PlatformProps,
): Promise<RenderResult> => {
  const history = createMemoryHistory();
  const renderResult = render(
    <Router history={history}>
      <CreateLti13Platform {...props} />
    </Router>,
  );

  await renderResult.findByText(saveLabel);

  return renderResult;
};

/**
 * Helper to get general details input by aria label.
 */
const getGeneralDetailsInput = (
  container: HTMLElement,
  label: string,
): Element => {
  const input = container.querySelector(`div[aria-label='${label}'] input`);
  if (!input) {
    throw new Error(`Unable to find text-field: ${label}`);
  }
  return input;
};

/**
 * Helper to get general details input parent element (outline) by aria label.
 */
export const getGeneralDetailsInputOutline = (
  container: HTMLElement,
  label: string,
) => {
  const outline = getGeneralDetailsInput(container, label).parentElement;
  if (!outline) {
    throw new Error(`Unable to find text-field's parent element: ${label}`);
  }

  return outline;
};

/**
 * Helper to type in general details input.
 */
export const typeGeneralDetails = async (
  container: HTMLElement,
  label: string,
  value: string,
): Promise<void> => {
  const input = getGeneralDetailsInput(container, label);
  await userEvent.type(input, `${value}`);
};

/**
 * Helper function to fill general details controls based on the provided map.
 *
 * @param container - An HTMLElement contains the controls to be filled.
 * @param detailsMap - A Map where each key-value pair represents an input field's label and the value to be filled.
 */
export const configureGeneralDetails = async (
  container: HTMLElement,
  detailsMap: Map<string, string>,
): Promise<void> => {
  for (const [label, value] of Array.from(detailsMap)) {
    await typeGeneralDetails(container, label, value);
  }
};

/**
 * Helper function to fill UsableBy controls based on the provided username.
 *
 * @param renderResult - The renderResult contains the controls to be filled.
 * @param username - The name of the user to be added to the UsableBy controls.
 */
export const configureUsableBy = async (
  renderResult: RenderResult,
  username: string,
): Promise<void> => {
  // open usable by control
  await userEvent.click(
    renderResult.getByLabelText(`${editLabel} ${usableByLabel}`),
  );
  // add a new user in acl expression builder
  const aclExpressionBuilderDialog = renderResult.getByRole("dialog");
  await searchUser(aclExpressionBuilderDialog, username);
  await selectAllAndConfirm(aclExpressionBuilderDialog);
};

/**
 * Helper function to fill UnknownUserHandling controls based on the provided username.
 *
 * @param renderResult - The renderResult contains the controls to be filled.
 * @param option - The option of UnknownUserHandling.
 * @param groupName - The name of the group to be added to the controls.
 */
export const configureUnknownUserHandling = async (
  renderResult: RenderResult,
  option: string,
  groupName: string,
): Promise<void> => {
  //choose the value provided in `option` for unknown user handling
  await selectOption(
    renderResult.container,
    `div[aria-label='${selectLabel} ${unknownUserHandlingLabel}'] div`,
    option,
  );
  const selectGroupsButton = await renderResult.findByText(selectGroupsLabel);
  await userEvent.click(selectGroupsButton);
  const selectGroupDialog = renderResult.getByRole("dialog");
  await searchAndSelectGroup(selectGroupDialog, "", groupName);
  await clickOkButton(selectGroupDialog);
};

/**
 * Helper function to select role from SelectRoleDialog based on the provided role name.
 *
 * @param renderResult - The renderResult contains the trigger button for dialog.
 * @param label - The label text for the button which can open the dialog.
 * @param roleName - The name of the role to be added to the dialog.
 */
const configureSelectRoleDialog = async (
  renderResult: RenderResult,
  label: string,
  roleName: string,
): Promise<void> => {
  await userEvent.click(renderResult.getByLabelText(`${editLabel} ${label}`));
  const dialog = renderResult.getByRole("dialog");
  await searchAndSelectRole(dialog, "", roleName);
  await clickOkButton(dialog);
};

/**
 * Helper function to select instructor role based on the provided role name.
 *
 * @param renderResult - The renderResult contains the controls to be filled.
 * @param roleName - The name of the role to be added to the controls.
 */
export const configureInstructorRoles = async (
  renderResult: RenderResult,
  roleName: string,
): Promise<void> =>
  configureSelectRoleDialog(renderResult, instructorRolesLabel, roleName);

/**
 * Helper function to select unknown role based on the provided role name.
 *
 * @param renderResult - The renderResult contains the controls to be filled.
 * @param roleName - The name of the role to be added to the controls.
 */
export const configureUnknownRoles = async (
  renderResult: RenderResult,
  roleName: string,
): Promise<void> =>
  configureSelectRoleDialog(renderResult, unknownRolesLabel, roleName);

/**
 * Helper function to build custom role mappings based on the provided lti role name and oEQ role name.
 *
 * @param renderResult - The renderResult contains the controls to be filled.
 * @param ltiRoleName - The name of the lti role to be selected.
 * @param oeqRoleName - The name of the role to be selected.
 */
export const configureCustomRoles = async (
  renderResult: RenderResult,
  ltiRoleName: string,
  oeqRoleName: string,
): Promise<void> => {
  // select custom roles
  await userEvent.click(
    renderResult.getByLabelText(`${editLabel} ${customRolesLabel}`),
  );
  const selectCustomRoleDialog = renderResult.getByRole("dialog");
  await selectLtiRole(selectCustomRoleDialog, ltiRoleName);
  await doSearchAndSelectRole(selectCustomRoleDialog, oeqRoleName, oeqRoleName);
  await clickOkButton(selectCustomRoleDialog);
};

/** Helper function to click save button. */
export const savePlatform = (container: HTMLElement) =>
  userEvent.click(getByText(container, saveLabel));

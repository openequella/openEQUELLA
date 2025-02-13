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
import { getByLabelText, render, RenderResult } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import {
  findRolesByIds,
  searchRoles,
} from "../../../__mocks__/RoleModule.mock";
import CustomRolesMappingControl, {
  CustomRolesMappingControlProps,
} from "../../../tsrc/components/CustomRolesMappingControl";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  doSearch,
  searchAndSelect,
} from "./securityentitydialog/SelectEntityDialogTestHelper";
import { getMuiTextField } from "../MuiQueries";

const { edit: editLabel } = languageStrings.common.action;
const { queryFieldLabel } = languageStrings.roleSearchComponent;
const { customRoles } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .roleMappings;
const { customRoleLabel } = languageStrings.selectCustomRoleDialog;

export const commonCustomRolesMappingControlProps: CustomRolesMappingControlProps =
  {
    initialMappings: new Map(),
    onChange: jest.fn(),
    searchRolesProvider: searchRoles,
    findRolesByIdsProvider: findRolesByIds,
  };

/**
 * Helper to render CustomRolesMappingControl.
 */
export const renderCustomRolesMappingControl = (
  props: CustomRolesMappingControlProps = commonCustomRolesMappingControlProps,
): RenderResult => render(<CustomRolesMappingControl {...props} />);

/**
 * Open select custom role dialog.
 */
export const openDialog = (container: HTMLElement) =>
  userEvent.click(getByLabelText(container, `${editLabel} ${customRoles}`));

const searchRole = (dialog: HTMLElement, queryName: string) =>
  doSearch(dialog, queryFieldLabel, queryName);

/**
 * Do search and select a role in the dialog.
 */
export const doSearchAndSelectRole = async (
  dialog: HTMLElement,
  searchFor: string,
  selectEntityName: string,
) => searchAndSelect(dialog, searchFor, selectEntityName, searchRole);

/**
 * Input custom role name in the text field.
 */
export const inputCustomRoleName = async (
  dialog: HTMLElement,
  roleName: string,
) => {
  const input = getMuiTextField(dialog, customRoleLabel);
  await userEvent.type(input, roleName);
};

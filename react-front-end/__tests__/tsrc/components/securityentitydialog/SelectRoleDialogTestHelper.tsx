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
import { render, RenderResult } from "@testing-library/react";
import * as React from "react";

import SelectRoleDialog, {
  SelectRoleDialogProps,
} from "../../../../tsrc/components/securityentitydialog/SelectRoleDialog";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  doSearch,
  searchAndSelect,
  waitForEntityDialogToRender,
} from "./SelectEntityDialogTestHelper";
import { searchRoles } from "../../../../__mocks__/RoleModule.mock";

const { queryFieldLabel } = languageStrings.roleSearchComponent;

export const commonSelectRoleDialogProps: SelectRoleDialogProps = {
  open: true,
  value: new Set<OEQ.UserQuery.RoleDetails>(),
  onClose: jest.fn(),
  searchRolesProvider: searchRoles,
};

/**
 * Helper to render SelectRoleDialog.
 */
export const renderSelectRoleDialog = async (
  props: SelectRoleDialogProps = commonSelectRoleDialogProps,
): Promise<RenderResult> => {
  const result = render(<SelectRoleDialog {...props} />);
  await waitForEntityDialogToRender(result);
  return result;
};

const searchRole = (dialog: HTMLElement, queryName: string) =>
  doSearch(dialog, queryFieldLabel, queryName);

/**
 * Do search and select a role in SelectRoleDialog.
 */
export const searchAndSelectRole = (
  dialog: HTMLElement,
  searchFor: string,
  selectEntityName: string,
) => searchAndSelect(dialog, searchFor, selectEntityName, searchRole);

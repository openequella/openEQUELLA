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
import { ListItemText } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { render, RenderResult, waitFor } from "@testing-library/react";
import * as React from "react";
import * as RoleModuleMock from "../../../../__mocks__/RoleModule.mock";
import { BaseSearchProps } from "../../../../tsrc/components/securityentitysearch/BaseSearch";
import RoleSearch, {
  RoleSearchProps,
} from "../../../../tsrc/components/securityentitysearch/RoleSearch";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  commonSearchProps,
  doSearch,
  findEntityFromMockData,
  generateDefaultCheckboxModeProps,
} from "./BaseSearchTestHelper";

const { queryFieldLabel } = languageStrings.roleSearchComponent;

export const defaultRoleSearchProps: BaseSearchProps<OEQ.UserQuery.RoleDetails> =
  {
    ...commonSearchProps,
    mode: generateDefaultCheckboxModeProps<OEQ.UserQuery.GroupDetails>(),
    search: RoleModuleMock.searchRoles,
    itemDetailsToEntry: ({ name }: OEQ.UserQuery.RoleDetails) => (
      <ListItemText primary={name} />
    ),
  };

/**
 * Helper to render RoleSearch and wait for component under test
 */
export const renderRoleSearch = async (
  props: RoleSearchProps = defaultRoleSearchProps,
): Promise<RenderResult> => {
  const renderResult = render(<RoleSearch {...props} />);

  // Wait for it to be rendered
  await waitFor(() => renderResult.getByText(queryFieldLabel));

  return renderResult;
};

/**
 * Helper function to do the steps of entering a submitting a search in the <RoleSearch/>
 * component.
 *
 * @param dialog The Role Search Dialog
 * @param queryValue the value to put in the query field before pressing enter
 */
export const searchRole = (dialog: HTMLElement, queryValue: string) =>
  doSearch(dialog, queryFieldLabel, queryValue);

/**
 * Helper function to assist in finding the specific Role from mock data.
 */
export const findRoleFromMockData = (name: string): OEQ.UserQuery.RoleDetails =>
  findEntityFromMockData(
    RoleModuleMock.roles,
    (u: OEQ.UserQuery.RoleDetails) => u.name === name,
    name,
  );

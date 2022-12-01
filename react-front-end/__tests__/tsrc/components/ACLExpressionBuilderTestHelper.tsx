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
import { render, RenderResult, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { resolveGroups, listGroups } from "../../../__mocks__/GroupModule.mock";
import { listRoles } from "../../../__mocks__/RoleModule.mock";
import { listUsers } from "../../../__mocks__/UserModule.mock";
import ACLExpressionBuilder, {
  ACLExpressionBuilderProps,
} from "../../../tsrc/components/aclexpressionbuilder/ACLExpressionBuilder";
import { languageStrings } from "../../../tsrc/util/langstrings";

const { homeTab, searchFilters } = languageStrings.aclExpressionBuilder;
const { groups: groupsRadioLabel, roles: rolesRadioLabel } = searchFilters;

const defaultACLExpressionBuilderProps = {
  searchUserProvider: listUsers,
  searchGroupProvider: listGroups,
  searchRoleProvider: listRoles,
  resolveGroupsProvider: resolveGroups,
};

// Helper to render ACLExpressionBuilder and wait for component under test
export const renderACLExpressionBuilder = async (
  props: ACLExpressionBuilderProps = defaultACLExpressionBuilderProps
): Promise<RenderResult> => {
  const renderResult = render(<ACLExpressionBuilder {...props} />);

  // Wait for it to be rendered
  await waitFor(() => renderResult.getByText(homeTab));

  return renderResult;
};

// Helper function to mock selecting `groups` radio.
export const selectGroupsRadio = ({ getByText }: RenderResult) =>
  userEvent.click(getByText(groupsRadioLabel));

// Helper function to mock selecting `roles` radio.
export const selectRolesRadio = ({ getByText }: RenderResult) =>
  userEvent.click(getByText(rolesRadioLabel));

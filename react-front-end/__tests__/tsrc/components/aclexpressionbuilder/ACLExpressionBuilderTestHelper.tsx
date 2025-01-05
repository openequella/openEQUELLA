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
import {
  findByText,
  getByText,
  render,
  RenderResult,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { defaultACLEntityResolvers } from "../../../../__mocks__/ACLExpressionBuilder.mock";
import {
  searchGroups,
  findGroupsByIds,
} from "../../../../__mocks__/GroupModule.mock";
import { searchRoles } from "../../../../__mocks__/RoleModule.mock";
import { getTokens, listUsers } from "../../../../__mocks__/UserModule.mock";
import ACLExpressionBuilder, {
  ACLExpressionBuilderProps,
} from "../../../../tsrc/components/aclexpressionbuilder/ACLExpressionBuilder";
import type { ReferrerType } from "../../../../tsrc/components/aclexpressionbuilder/ACLHTTPReferrerInput";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { selectOption } from "../../MuiTestHelpers";

const { ok: okLabel } = languageStrings.common.action;

const {
  aclExpressionBuilder: {
    otherACLDescriptions: {
      exactReferrer: exactReferrerDesc,
      containReferrer: containReferrerDesc,
    },
  },
} = languageStrings;

export const defaultACLExpressionBuilderProps: ACLExpressionBuilderProps = {
  onFinish: jest.fn(),
  searchUserProvider: listUsers,
  searchGroupProvider: searchGroups,
  searchRoleProvider: searchRoles,
  resolveGroupsProvider: findGroupsByIds,
  aclEntityResolversProvider: defaultACLEntityResolvers,
  ssoTokensProvider: getTokens,
};

// Helper to render ACLExpressionBuilder and wait for component under test
export const renderACLExpressionBuilder = (
  props: ACLExpressionBuilderProps = defaultACLExpressionBuilderProps,
): RenderResult => render(<ACLExpressionBuilder {...props} />);

/**
 * Select all the entities shown in the `EntitySearch` (User/Group/RoleSearch) result list by
 * clicking the `Select all` button and then `ok` button.
 */
export const selectAllAndConfirm = async (container: HTMLElement) => {
  // Select all entities.
  const selectAllButton = await findByText(container, "Select all", {
    selector: "button",
  });
  await userEvent.click(selectAllButton);
  await userEvent.click(getByText(container, okLabel));
};

/**
 * Attempts to select the entities shown in the `EntitySearch` (User/Group/RoleSearch) result list.
 * It then clicks 'select' followed by the 'OK' button, and then gets the updated `ACLExpression` result from `onFinish`.
 */
export const selectAndFinished = async (
  container: HTMLElement,
  onFinish = jest.fn(),
): Promise<string> => {
  await selectAllAndConfirm(container);
  // get the result of ACLExpression
  return onFinish.mock.lastCall[0];
};

/**
 * Helper function to mock select a recipient type in `Other` panel.
 */
export const selectRecipientType = async (
  container: HTMLElement,
  recipientLabel: string,
) => selectOption(container, `#recipient-type-select`, recipientLabel);

/**
 * Helper function to mock select a referrer type in ACLHTTPReferrerInput.
 */
export const selectReferrerType = async (
  { getByText }: RenderResult,
  type: ReferrerType,
) => {
  const text = type === "Contain" ? containReferrerDesc : exactReferrerDesc;
  const radio = getByText(text);
  await userEvent.click(radio);
};

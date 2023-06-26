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
  listGroups,
  resolveGroups,
} from "../../../../__mocks__/GroupModule.mock";
import { listRoles } from "../../../../__mocks__/RoleModule.mock";
import { getTokens, listUsers } from "../../../../__mocks__/UserModule.mock";
import ACLExpressionBuilder, {
  ACLExpressionBuilderProps,
} from "../../../../tsrc/components/aclexpressionbuilder/ACLExpressionBuilder";
import type { ReferrerType } from "../../../../tsrc/components/aclexpressionbuilder/ACLHTTPReferrerInput";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { selectOption } from "../../MuiTestHelpers";

const { select: selectLabel, ok: okLabel } = languageStrings.common.action;

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
  searchGroupProvider: listGroups,
  searchRoleProvider: listRoles,
  resolveGroupsProvider: resolveGroups,
  aclEntityResolversProvider: defaultACLEntityResolvers,
  ssoTokensProvider: getTokens,
};

// Helper to render ACLExpressionBuilder and wait for component under test
export const renderACLExpressionBuilder = (
  props: ACLExpressionBuilderProps = defaultACLExpressionBuilderProps
): RenderResult => render(<ACLExpressionBuilder {...props} />);

/**
 * Attempt to select the entities show in the `EntitySearch` (User/Group/RoleSearch) result list.
 * It then clicks `select` and then `ok` button.
 */
export const selectAndConfirm = async (
  container: HTMLElement,
  selectNames: string[]
) => {
  // Wait for the results, and then click all entities
  for (const name of selectNames) {
    await userEvent.click(await findByText(container, name));
  }

  // click select button
  await userEvent.click(getByText(container, selectLabel));
  // click ok button
  await userEvent.click(getByText(container, okLabel));
};

/**
 * Attempts to select the entities shown in the `EntitySearch` (User/Group/RoleSearch) result list.
 * It then clicks 'select' followed by the 'OK' button, and then gets the updated `ACLExpression` result from `onFinish`.
 */
export const selectAndFinished = async (
  container: HTMLElement,
  selectNames: string[],
  onFinish = jest.fn()
): Promise<string> => {
  await selectAndConfirm(container, selectNames);
  // get the result of ACLExpression
  return onFinish.mock.lastCall[0];
};

/**
 * Helper function to mock select a recipient type in `Other` panel.
 */
export const selectRecipientType = async (
  container: HTMLElement,
  recipientLabel: string
) => selectOption(container, `#recipient-type-select`, recipientLabel);

/**
 * Helper function to mock select a referrer type in ACLHTTPReferrerInput.
 */
export const selectReferrerType = async (
  { getByText }: RenderResult,
  type: ReferrerType
) => {
  const text = type === "Contain" ? containReferrerDesc : exactReferrerDesc;
  const radio = getByText(text);
  await userEvent.click(radio);
};

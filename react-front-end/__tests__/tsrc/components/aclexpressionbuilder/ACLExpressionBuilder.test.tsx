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
import "@testing-library/jest-dom/extend-expect";
import userEvent from "@testing-library/user-event";
import {
  group100RecipientWithName,
  group200RecipientWithName,
  group300RecipientWithName,
  group400RecipientWithName,
  role100RecipientWithName,
  role200RecipientWithName,
  user100RecipientWithName,
  user200RecipientWithName,
  user300RecipientWithName,
  user400RecipientWithName,
} from "../../../../__mocks__/ACLRecipientModule.mock";
import { ACLExpression } from "../../../../tsrc/modules/ACLExpressionModule";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  defaultACLExpressionBuilderProps,
  renderACLExpressionBuilder,
  selectAndFinished,
} from "./ACLExpressionBuilderTestHelper";
import { searchGroup } from "../securityentitysearch/GroupSearchTestHelper";
import { searchRole } from "../securityentitysearch/RoleSearchTestHelper";
import { searchUser } from "../securityentitysearch/UserSearchTestHelpler";
import { selectOperatorNode } from "./ACLExpressionTreeTestHelper";

const { queryFieldLabel: userSearchQueryFieldLabel } =
  languageStrings.userSearchComponent;
const { queryFieldLabel: groupSearchQueryFieldLabel } =
  languageStrings.groupSearchComponent;
const { queryFieldLabel: roleSearchQueryFieldLabel } =
  languageStrings.roleSearchComponent;

const { searchFilters } = languageStrings.aclExpressionBuilder;
const {
  users: usersRadioLabel,
  groups: groupsRadioLabel,
  roles: rolesRadioLabel,
} = searchFilters;

describe("<ACLExpressionBuilder/>", () => {
  it("displays home panel's user search on initial render", () => {
    const { queryByText } = renderACLExpressionBuilder();

    expect(queryByText(userSearchQueryFieldLabel)).toBeInTheDocument();
  });

  describe("home panel", () => {
    const initialACLExpression: ACLExpression = {
      id: "root",
      operator: "OR",
      recipients: [
        {
          expression: "df950ee3-c5f2-4c09-90af-38bb9b73dc29",
          name: "Root User",
          type: "U",
        },
      ],
      children: [
        {
          id: "test",
          operator: "OR",
          recipients: [],
          children: [],
        },
      ],
    };

    const userACLExpression: ACLExpression = {
      id: "default-acl-expression-id",
      operator: "OR",
      recipients: [
        user100RecipientWithName,
        user200RecipientWithName,
        user400RecipientWithName,
        user300RecipientWithName,
      ],
      children: [],
    };

    const groupACLExpression: ACLExpression = {
      children: [],
      id: "default-acl-expression-id",
      operator: "OR",
      recipients: [
        group100RecipientWithName,
        group200RecipientWithName,
        group300RecipientWithName,
        group400RecipientWithName,
      ],
    };

    const roleACLExpression: ACLExpression = {
      children: [],
      id: "default-acl-expression-id",
      operator: "OR",
      recipients: [role100RecipientWithName, role200RecipientWithName],
    };

    it("displays home panel's group search when users select groups radio", () => {
      const { getByText, queryByText } = renderACLExpressionBuilder();

      userEvent.click(getByText(groupsRadioLabel));

      expect(queryByText(groupSearchQueryFieldLabel)).toBeInTheDocument();
    });

    it("displays home panel's role search when users select roles radio", () => {
      const { getByText, queryByText } = renderACLExpressionBuilder();

      userEvent.click(getByText(rolesRadioLabel));

      expect(queryByText(roleSearchQueryFieldLabel)).toBeInTheDocument();
    });

    it.each<
      [
        string,
        string,
        string,
        string[],
        (dialog: HTMLElement, queryValue: string) => void,
        ACLExpression
      ]
    >([
      [
        "users",
        usersRadioLabel,
        "user",
        ["user100", "user200", "user300", "user400"],
        searchUser,
        userACLExpression,
      ],
      [
        "groups",
        groupsRadioLabel,
        "group",
        ["group100", "group200", "group300", "group400"],
        searchGroup,
        groupACLExpression,
      ],
      [
        "roles",
        rolesRadioLabel,
        "role",
        ["role100", "role200"],
        searchRole,
        roleACLExpression,
      ],
    ])(
      "should be able to select multiple %s search result to the expression",
      async (
        _,
        entityRadioLabel,
        searchFor,
        selectEntitiesName,
        searchEntity,
        expectedACLExpressionResult
      ) => {
        const onFinish = jest.fn();
        const renderResult = renderACLExpressionBuilder({
          ...defaultACLExpressionBuilderProps,
          onFinish,
        });
        const { getByText, container } = renderResult;

        // select entity search radio
        userEvent.click(getByText(entityRadioLabel));
        // Attempt search for a specific entity
        searchEntity(container, searchFor);

        const result = await selectAndFinished(
          renderResult,
          selectEntitiesName,
          onFinish
        );

        expect(result).toEqual(expectedACLExpressionResult);
      }
    );

    it.each<
      [
        string,
        string,
        string,
        string[],
        (dialog: HTMLElement, queryValue: string) => void,
        ACLExpression
      ]
    >([
      [
        "users",
        usersRadioLabel,
        "user",
        ["user100", "user200", "user300", "user400"],
        searchUser,
        {
          ...initialACLExpression,
          children: [{ ...userACLExpression, id: "test" }],
        },
      ],
      [
        "groups",
        groupsRadioLabel,
        "group",
        ["group100", "group200", "group300", "group400"],
        searchGroup,
        {
          ...initialACLExpression,
          children: [{ ...groupACLExpression, id: "test" }],
        },
      ],
      [
        "roles",
        rolesRadioLabel,
        "role",
        ["role100", "role200"],
        searchRole,
        {
          ...initialACLExpression,
          children: [{ ...roleACLExpression, id: "test" }],
        },
      ],
    ])(
      "should be able to select multiple %s search result and add them within the currently selected grouping",
      async (
        _,
        entityRadioLabel,
        searchFor,
        selectEntitiesName,
        searchEntity,
        expectedACLExpressionResult
      ) => {
        const onFinish = jest.fn();
        const renderResult = renderACLExpressionBuilder({
          ...defaultACLExpressionBuilderProps,
          aclExpression: initialACLExpression,
          onFinish: onFinish,
        });
        const { getByText, container } = renderResult;

        // select entity search radio
        userEvent.click(getByText(entityRadioLabel));
        // Attempt search for a specific entity
        searchEntity(container, searchFor);

        // expend the root node
        selectOperatorNode(container, "root");
        // select the test node
        selectOperatorNode(container, "test");

        const result = await selectAndFinished(
          renderResult,
          selectEntitiesName,
          onFinish
        );

        expect(result).toEqual(expectedACLExpressionResult);
      }
    );
  });
});

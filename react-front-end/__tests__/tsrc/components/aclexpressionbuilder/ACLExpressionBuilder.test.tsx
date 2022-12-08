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
        {
          expression: "20483af2-fe56-4499-a54b-8d7452156895",
          name: "Fabienne Hobson [user100]",
          type: "U",
        },
        {
          expression: "f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a",
          name: "Racheal Carlyle [user200]",
          type: "U",
        },
        {
          expression: "1c2ff1d0-9040-4985-a450-0ff6422ba5ef",
          name: "Ronny Southgate [user400]",
          type: "U",
        },
        {
          expression: "eb75a832-6533-4d72-93f4-2b7a1b108951",
          name: "Yasmin Day [user300]",
          type: "U",
        },
      ],
      children: [],
    };

    const groupACLExpression: ACLExpression = {
      children: [],
      id: "default-acl-expression-id",
      operator: "OR",
      recipients: [
        {
          expression: "303e758c-0051-4aea-9a8e-421f93ed9d1a",
          name: "group100",
          type: "G",
        },
        {
          expression: "d7dd1907-5731-4244-9a65-e0e847f68604",
          name: "group200",
          type: "G",
        },
        {
          expression: "f921a6e3-69a6-4ec4-8cf8-bc193beda5f6",
          name: "group300",
          type: "G",
        },
        {
          expression: "a2576dea-bd5c-490b-a065-637068e1a4fb",
          name: "group400",
          type: "G",
        },
      ],
    };

    const roleACLExpression: ACLExpression = {
      children: [],
      id: "default-acl-expression-id",
      operator: "OR",
      recipients: [
        {
          expression: "fda99983-9eda-440a-ac68-0f746173fdcb",
          name: "role100",
          type: "R",
        },
        {
          expression: "1de3a6df-dc81-4a26-b69e-e61f8474594a",
          name: "role200",
          type: "R",
        },
      ],
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

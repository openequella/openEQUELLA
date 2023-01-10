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
import { waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  initialACLExpression,
  initialACLExpressionWithValidChild,
} from "../../../../__mocks__/ACLExpressionBuilder.mock";
import {
  everyoneRecipientWithName,
  group100RecipientWithName,
  group200RecipientWithName,
  group300RecipientWithName,
  group400RecipientWithName,
  ownerRecipientWithName,
  role100RecipientWithName,
  role200RecipientWithName,
  roleGuestRecipientWithName,
  roleLoggedRecipientWithName,
  user100RecipientWithName,
  user200RecipientWithName,
  user300RecipientWithName,
  user400RecipientWithName,
} from "../../../../__mocks__/ACLRecipientModule.mock";
import {
  ACLExpression,
  ACLOperatorType,
  createACLExpression,
} from "../../../../tsrc/modules/ACLExpressionModule";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { ignoreId } from "../../modules/ACLExpressionModuleTestHelper";
import {
  defaultACLExpressionBuilderProps,
  renderACLExpressionBuilder,
  selectAndFinished,
  selectRecipientType,
} from "./ACLExpressionBuilderTestHelper";
import { searchGroup } from "../securityentitysearch/GroupSearchTestHelper";
import { searchRole } from "../securityentitysearch/RoleSearchTestHelper";
import { searchUser } from "../securityentitysearch/UserSearchTestHelpler";
import {
  clickDeleteButtonForRecipient,
  clickDeleteButtonForOperatorNode,
  selectOperatorForNode,
  selectOperatorNode,
} from "./ACLExpressionTreeTestHelper";

const { queryFieldLabel: userSearchQueryFieldLabel } =
  languageStrings.userSearchComponent;
const { queryFieldLabel: groupSearchQueryFieldLabel } =
  languageStrings.groupSearchComponent;
const { queryFieldLabel: roleSearchQueryFieldLabel } =
  languageStrings.roleSearchComponent;
const { ok: okLabel, add: addLabel } = languageStrings.common.action;

const {
  searchFilters,
  addGroup: addGroupLabel,
  otherTab,
  otherACLTypes: {
    everyone: everyoneType,
    owner: ownerType,
    logged: loggedType,
    guest: guestType,
  },
} = languageStrings.aclExpressionBuilder;
const {
  users: usersRadioLabel,
  groups: groupsRadioLabel,
  roles: rolesRadioLabel,
} = searchFilters;

const NODE_NAME_ROOT = "root";
const NODE_NAME_TEST = "test";

describe("<ACLExpressionBuilder/>", () => {
  it("displays home panel's user search on initial render", () => {
    const { queryByText } = renderACLExpressionBuilder();

    expect(queryByText(userSearchQueryFieldLabel)).toBeInTheDocument();
  });

  it("should be able to delete recipient for ACLExpression", async () => {
    const expectedResult = {
      ...initialACLExpressionWithValidChild,
      children: [
        {
          ...initialACLExpressionWithValidChild.children[0],
          recipients: [],
        },
      ],
    };
    const onFinish = jest.fn();
    const renderResult = renderACLExpressionBuilder({
      ...defaultACLExpressionBuilderProps,
      aclExpression: initialACLExpressionWithValidChild,
      onFinish,
    });
    const { container, getByText } = renderResult;

    // expand root node
    selectOperatorNode(container, NODE_NAME_ROOT);
    // expand test node
    selectOperatorNode(container, NODE_NAME_TEST);
    // delete recipient
    await clickDeleteButtonForRecipient(
      renderResult,
      user100RecipientWithName.name
    );
    // click ok button to see if the result is what we want
    userEvent.click(getByText(okLabel));

    const result = onFinish.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  });

  it.each<[string, string, ACLOperatorType, ACLExpression]>([
    [
      "top level",
      NODE_NAME_ROOT,
      "AND",
      {
        ...initialACLExpressionWithValidChild,
        operator: "AND",
      },
    ],
    [
      "nested",
      NODE_NAME_TEST,
      "OR",
      {
        ...initialACLExpressionWithValidChild,
        children: [
          {
            ...initialACLExpressionWithValidChild.children[0],
            operator: "OR",
          },
        ],
      },
    ],
  ])(
    "should be able to change the grouping method for the %s group",
    async (_, nodeId, operator, expectedResult) => {
      const onFinish = jest.fn();
      const { container, getByText } = renderACLExpressionBuilder({
        ...defaultACLExpressionBuilderProps,
        aclExpression: initialACLExpressionWithValidChild,
        onFinish,
      });

      // expand root node
      selectOperatorNode(container, NODE_NAME_ROOT);
      // update operator option for provided node
      await selectOperatorForNode(container, nodeId, operator);
      // click ok to see if the result is what we want
      userEvent.click(getByText(okLabel));

      const result = onFinish.mock.lastCall[0];
      expect(result).toEqual(expectedResult);
    }
  );

  it("should be able to add a sub group for ACLExpression", () => {
    const expectedResult = createACLExpression(
      "OR",
      [],
      [createACLExpression("OR")]
    );
    const onFinish = jest.fn();
    const { getByText, getByLabelText } = renderACLExpressionBuilder({
      ...defaultACLExpressionBuilderProps,
      onFinish,
    });

    // click add group button
    userEvent.click(getByLabelText(addGroupLabel));
    // click ok button to see if the result is what we want
    userEvent.click(getByText(okLabel));

    const result = onFinish.mock.lastCall[0];
    expect(result).toEqual(ignoreId(expectedResult));
  });

  it("should be able to delete a sub group in ACLExpression", () => {
    const expectedResult = {
      ...initialACLExpression,
      children: [],
    };
    const onFinish = jest.fn();
    const { container, getByText } = renderACLExpressionBuilder({
      ...defaultACLExpressionBuilderProps,
      aclExpression: initialACLExpression,
      onFinish,
    });

    // expand root node
    selectOperatorNode(container, NODE_NAME_ROOT);
    // click delete group button in node `test`
    clickDeleteButtonForOperatorNode(container, NODE_NAME_TEST);
    // click ok button to see if the result is what we want
    userEvent.click(getByText(okLabel));

    const result = onFinish.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  });

  describe("home panel", () => {
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

    it.each([
      [
        "user",
        usersRadioLabel,
        "user100",
        searchUser,
        {
          ...userACLExpression,
          recipients: [user100RecipientWithName],
        },
      ],
      [
        "group",
        groupsRadioLabel,
        group100RecipientWithName.name,
        searchGroup,
        {
          ...groupACLExpression,
          recipients: [group100RecipientWithName],
        },
      ],
      [
        "role",
        rolesRadioLabel,
        role100RecipientWithName.name,
        searchRole,
        {
          ...roleACLExpression,
          recipients: [role100RecipientWithName],
        },
      ],
    ])(
      "should be able to add one %s result to the expression by clicking the add button in each entry",
      async (
        _: string,
        entityRadioLabel: string,
        entityToSelect: string,
        searchEntity: (dialog: HTMLElement, queryValue: string) => void,
        expectedACLExpressionResult: ACLExpression
      ) => {
        const onFinish = jest.fn();
        const { getByText, container, getByLabelText } =
          renderACLExpressionBuilder({
            ...defaultACLExpressionBuilderProps,
            onFinish,
          });

        // select entity search radio
        userEvent.click(getByText(entityRadioLabel));
        // attempt search for a specific entity
        searchEntity(container, entityToSelect);
        // wait for search result
        await waitFor(() => getByText(entityToSelect));

        // click the add button
        userEvent.click(getByLabelText(addLabel));

        // click ok button to get the result
        userEvent.click(getByText(okLabel));

        const result = onFinish.mock.lastCall[0];
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
          children: [{ ...userACLExpression, id: NODE_NAME_TEST }],
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
          children: [{ ...groupACLExpression, id: NODE_NAME_TEST }],
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
          children: [{ ...roleACLExpression, id: NODE_NAME_TEST }],
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

        // expand the root node
        selectOperatorNode(container, NODE_NAME_ROOT);
        // select the test node
        selectOperatorNode(container, NODE_NAME_TEST);

        const result = await selectAndFinished(
          renderResult,
          selectEntitiesName,
          onFinish
        );

        expect(result).toEqual(expectedACLExpressionResult);
      }
    );
  });

  describe("other panel", () => {
    const initialACLExpression: ACLExpression = {
      operator: "OR",
      id: NODE_NAME_ROOT,
      recipients: [],
      children: [],
    };

    it.each([
      [
        "Everyone",
        everyoneType,
        everyoneRecipientWithName.name,
        { ...initialACLExpression, recipients: [everyoneRecipientWithName] },
      ],
      [
        "Owner",
        ownerType,
        ownerRecipientWithName.name,
        { ...initialACLExpression, recipients: [ownerRecipientWithName] },
      ],
      [
        "Logged",
        loggedType,
        roleLoggedRecipientWithName.name,
        { ...initialACLExpression, recipients: [roleLoggedRecipientWithName] },
      ],
      [
        "Guest",
        guestType,
        roleGuestRecipientWithName.name,
        { ...initialACLExpression, recipients: [roleGuestRecipientWithName] },
      ],
    ])(
      "should be able to add %s recipient to the ACLExpression",
      async (
        _,
        recipientLabel: string,
        recipientName: string | undefined,
        expectedResult: ACLExpression
      ) => {
        const onFinish = jest.fn();
        const renderResult = renderACLExpressionBuilder({
          ...defaultACLExpressionBuilderProps,
          aclExpression: initialACLExpression,
          onFinish: onFinish,
        });
        const { findAllByText, getByText, container } = renderResult;

        // click other panel
        userEvent.click(getByText(otherTab));
        // select a recipient type
        await selectRecipientType(container, recipientLabel);
        // click add button
        userEvent.click(getByText(addLabel));
        // expand root node to let recipients displayed in UI
        selectOperatorNode(container, NODE_NAME_ROOT);
        // wait for adding action
        await findAllByText(recipientName ?? "");

        // click ok button to check the result
        userEvent.click(getByText(okLabel));

        const result = onFinish.mock.lastCall[0];
        expect(result).toEqual(expectedResult);
      }
    );
  });
});

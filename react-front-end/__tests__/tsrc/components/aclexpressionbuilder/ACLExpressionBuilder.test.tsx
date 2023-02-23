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
  initialACLExpressionString,
  initialACLExpressionWithValidChildString,
} from "../../../../__mocks__/ACLExpressionBuilder.mock";
import {
  everyoneRecipientRawExpression,
  everyoneRecipientWithName,
  group100RecipientWithName,
  ipRecipientWithName,
  ownerRecipientRawExpression,
  ownerRecipientWithName,
  referRecipientWithName,
  role100RecipientWithName,
  roleGuestRecipientRawExpression,
  roleGuestRecipientWithName,
  roleLoggedRecipientRawExpression,
  roleLoggedRecipientWithName,
  ssoMoodleRecipientRawExpression,
  ssoMoodleRecipientWithName,
  user200RecipientWithName,
} from "../../../../__mocks__/ACLRecipientModule.mock";
import { ReferrerType } from "../../../../tsrc/components/aclexpressionbuilder/ACLHTTPReferrerInput";
import type { ACLOperatorType } from "../../../../tsrc/modules/ACLExpressionModule";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { selectOption } from "../../MuiTestHelpers";
import { searchGroup } from "../securityentitysearch/GroupSearchTestHelper";
import { searchRole } from "../securityentitysearch/RoleSearchTestHelper";
import { searchUser } from "../securityentitysearch/UserSearchTestHelpler";
import { typeInIpInput, typeInNetmaskInput } from "../IPv4CIDRInputTestHelper";
import {
  defaultACLExpressionBuilderProps,
  renderACLExpressionBuilder,
  selectAndFinished,
  selectRecipientType,
  selectReferrerType,
} from "./ACLExpressionBuilderTestHelper";
import {
  clickDeleteButtonForOperatorNode,
  clickDeleteButtonForRecipient,
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
    ip: ipType,
    referrer: referrerType,
    sso: ssoType,
  },
  otherACLDescriptions: { referrerLabel },
} = languageStrings.aclExpressionBuilder;
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

  it("should be able to delete recipient for ACLExpression", async () => {
    const expectedResult =
      "U:20483af2-fe56-4499-a54b-8d7452156895 R:TLE_GUEST_USER_ROLE OR";
    const onFinish = jest.fn();
    const { container, getByText } = renderACLExpressionBuilder({
      ...defaultACLExpressionBuilderProps,
      aclExpression: initialACLExpressionWithValidChildString,
      onFinish,
    });

    // expand root node
    await selectOperatorNode(container, 0);
    // expand child node
    await selectOperatorNode(container, 1);

    // delete recipient
    await clickDeleteButtonForRecipient(
      container,
      user200RecipientWithName.name
    );
    // click ok button to see if the result is what we want
    await userEvent.click(getByText(okLabel));

    const result = onFinish.mock.lastCall[0];
    expect(result).toEqual(expectedResult);
  });

  it.each<[string, number, ACLOperatorType, string]>([
    [
      "top level",
      0,
      "AND",
      "U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a AND R:TLE_GUEST_USER_ROLE AND",
    ],
    [
      "nested",
      1,
      "OR",
      "U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a OR R:TLE_GUEST_USER_ROLE OR",
    ],
  ])(
    "should be able to change the grouping method for the %s group",
    async (_, nodeIndex, operator, expectedResult) => {
      const onFinish = jest.fn();
      const { container, getByText } = renderACLExpressionBuilder({
        ...defaultACLExpressionBuilderProps,
        aclExpression: initialACLExpressionWithValidChildString,
        onFinish,
      });

      // expand root node
      await selectOperatorNode(container, 0);
      // update operator option for provided node
      await selectOperatorForNode(container, nodeIndex, operator);
      // click ok to see if the result is what we want
      await userEvent.click(getByText(okLabel));

      const result = onFinish.mock.lastCall[0];
      expect(result).toEqual(expectedResult);
    }
  );

  it("should be able to add a sub group for ACLExpression", async () => {
    const expectedResult =
      "U:20483af2-fe56-4499-a54b-8d7452156895 U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef U:eb75a832-6533-4d72-93f4-2b7a1b108951 AND OR";
    const onFinish = jest.fn();
    const { getByLabelText, container } = renderACLExpressionBuilder({
      ...defaultACLExpressionBuilderProps,
      onFinish,
      aclExpression: initialACLExpressionString,
    });

    // expend root node
    await selectOperatorNode(container, 0);
    // click add group button
    await userEvent.click(getByLabelText(addGroupLabel));
    // change operator type (if operator of sub-group is same with the root it will be combined after return)
    await selectOperatorForNode(container, 1, "AND");

    // add user300 and user400 to new group (if group is empty it will be removed when return, and
    // if group only have one recipient it will be combined into root group).
    await searchUser(container, "user");
    const result = await selectAndFinished(
      container,
      ["user300", "user400"],
      onFinish
    );

    expect(result).toEqual(expectedResult);
  });

  it("should be able to delete a sub group in ACLExpression", async () => {
    const onFinish = jest.fn();
    const { container, getByText } = renderACLExpressionBuilder({
      ...defaultACLExpressionBuilderProps,
      aclExpression: initialACLExpressionWithValidChildString,
      onFinish,
    });

    // expand root node
    await selectOperatorNode(container, 0);
    // click delete group button in child node
    await clickDeleteButtonForOperatorNode(container, 1);
    // click ok button to see if the result is what we want
    await userEvent.click(getByText(okLabel));

    const result = onFinish.mock.lastCall[0];
    expect(result).toEqual(initialACLExpressionString);
  });

  describe("home panel", () => {
    it("displays home panel's group search when users select groups radio", async () => {
      const { getByText, queryByText } = renderACLExpressionBuilder();

      await userEvent.click(getByText(groupsRadioLabel));

      expect(queryByText(groupSearchQueryFieldLabel)).toBeInTheDocument();
    });

    it("displays home panel's role search when users select roles radio", async () => {
      const { getByText, queryByText } = renderACLExpressionBuilder();

      await userEvent.click(getByText(rolesRadioLabel));

      expect(queryByText(roleSearchQueryFieldLabel)).toBeInTheDocument();
    });

    it.each([
      [
        "user",
        usersRadioLabel,
        "user100",
        searchUser,
        "U:20483af2-fe56-4499-a54b-8d7452156895",
      ],
      [
        "group",
        groupsRadioLabel,
        group100RecipientWithName.name,
        searchGroup,
        "G:303e758c-0051-4aea-9a8e-421f93ed9d1a",
      ],
      [
        "role",
        rolesRadioLabel,
        role100RecipientWithName.name,
        searchRole,
        "R:fda99983-9eda-440a-ac68-0f746173fdcb",
      ],
    ])(
      "should be able to add one %s result to the expression by clicking the add button in each entry",
      async (
        _: string,
        entityRadioLabel: string,
        entityToSelect: string,
        searchEntity: (
          dialog: HTMLElement,
          queryValue: string
        ) => Promise<void>,
        expectedACLExpressionResult: string
      ) => {
        const onFinish = jest.fn();
        const { getByText, container, getByLabelText } =
          renderACLExpressionBuilder({
            ...defaultACLExpressionBuilderProps,
            onFinish,
          });

        // select entity search radio
        await userEvent.click(getByText(entityRadioLabel));
        // attempt search for a specific entity
        await searchEntity(container, entityToSelect);
        // wait for search result
        await waitFor(() => getByText(entityToSelect));

        // click the add button
        await userEvent.click(getByLabelText(addLabel));

        // click ok button to get the result
        await userEvent.click(getByText(okLabel));

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
        (dialog: HTMLElement, queryValue: string) => Promise<void>,
        string
      ]
    >([
      [
        "users",
        usersRadioLabel,
        "user",
        ["user100", "user200", "user300", "user400"],
        searchUser,
        "U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a OR U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef U:eb75a832-6533-4d72-93f4-2b7a1b108951 OR",
      ],
      [
        "groups",
        groupsRadioLabel,
        "group",
        ["group100", "group200", "group300", "group400"],
        searchGroup,
        "G:303e758c-0051-4aea-9a8e-421f93ed9d1a G:d7dd1907-5731-4244-9a65-e0e847f68604 OR G:f921a6e3-69a6-4ec4-8cf8-bc193beda5f6 G:a2576dea-bd5c-490b-a065-637068e1a4fb OR",
      ],
      [
        "roles",
        rolesRadioLabel,
        "role",
        ["role100", "role200"],
        searchRole,
        "R:fda99983-9eda-440a-ac68-0f746173fdcb R:1de3a6df-dc81-4a26-b69e-e61f8474594a OR",
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
        const { getByText, container } = renderACLExpressionBuilder({
          ...defaultACLExpressionBuilderProps,
          onFinish,
        });

        // select entity search radio
        await userEvent.click(getByText(entityRadioLabel));
        // Attempt search for a specific entity
        await searchEntity(container, searchFor);

        const result = await selectAndFinished(
          container,
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
        (dialog: HTMLElement, queryValue: string) => Promise<void>,
        string
      ]
    >([
      [
        "users",
        usersRadioLabel,
        "user",
        ["user300", "user400"],
        searchUser,
        "U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a R:TLE_GUEST_USER_ROLE AND U:1c2ff1d0-9040-4985-a450-0ff6422ba5ef U:eb75a832-6533-4d72-93f4-2b7a1b108951 AND OR",
      ],
      [
        "groups",
        groupsRadioLabel,
        "group",
        ["group100", "group200", "group300", "group400"],
        searchGroup,
        "U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a R:TLE_GUEST_USER_ROLE AND G:303e758c-0051-4aea-9a8e-421f93ed9d1a G:d7dd1907-5731-4244-9a65-e0e847f68604 AND G:f921a6e3-69a6-4ec4-8cf8-bc193beda5f6 G:a2576dea-bd5c-490b-a065-637068e1a4fb AND OR",
      ],
      [
        "roles",
        rolesRadioLabel,
        "role",
        ["role100", "role200"],
        searchRole,
        "U:20483af2-fe56-4499-a54b-8d7452156895 U:f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a R:TLE_GUEST_USER_ROLE AND R:fda99983-9eda-440a-ac68-0f746173fdcb R:1de3a6df-dc81-4a26-b69e-e61f8474594a AND OR",
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
        const { getByText, container } = renderACLExpressionBuilder({
          ...defaultACLExpressionBuilderProps,
          aclExpression: initialACLExpressionWithValidChildString,
          onFinish: onFinish,
        });

        // select entity search radio
        await userEvent.click(getByText(entityRadioLabel));
        // Attempt search for a specific entity
        await searchEntity(container, searchFor);

        // expand the root node
        await selectOperatorNode(container, 0);
        // select the child node
        await selectOperatorNode(container, 1);

        const result = await selectAndFinished(
          container,
          selectEntitiesName,
          onFinish
        );

        expect(result).toEqual(expectedACLExpressionResult);
      }
    );
  });

  describe("other panel", () => {
    it.each([
      [
        "Everyone",
        everyoneType,
        everyoneRecipientWithName.name,
        everyoneRecipientRawExpression,
      ],
      [
        "Owner",
        ownerType,
        ownerRecipientWithName.name,
        ownerRecipientRawExpression,
      ],
      [
        "Logged",
        loggedType,
        roleLoggedRecipientWithName.name,
        roleLoggedRecipientRawExpression,
      ],
      [
        "Guest",
        guestType,
        roleGuestRecipientWithName.name,
        roleGuestRecipientRawExpression,
      ],
    ])(
      "should be able to add %s recipient to the ACLExpression",
      async (
        _,
        recipientLabel: string,
        recipientName: string | undefined,
        expectedResult: string
      ) => {
        const onFinish = jest.fn();
        const { findAllByText, getByText, container } =
          renderACLExpressionBuilder({
            ...defaultACLExpressionBuilderProps,
            onFinish: onFinish,
          });

        // click other panel
        await userEvent.click(getByText(otherTab));
        // select a recipient type
        await selectRecipientType(container, recipientLabel);
        // click add button
        await userEvent.click(getByText(addLabel));
        // expand root node to let recipients displayed in UI
        await selectOperatorNode(container, 0);
        // wait for adding action
        await findAllByText(recipientName ?? "");

        // click ok button to check the result
        await userEvent.click(getByText(okLabel));

        const result = onFinish.mock.lastCall[0];
        expect(result).toEqual(expectedResult);
      }
    );

    it("should be able to add an IPv4 CIDR specifier to the expression", async () => {
      const ipRecipient = ipRecipientWithName("192.168.1.1/24");
      const expectedResult = "I:192.168.1.1/24";

      const onFinish = jest.fn();
      const { findByText, getByText, container } = renderACLExpressionBuilder({
        ...defaultACLExpressionBuilderProps,
        onFinish: onFinish,
      });

      // click other panel
      await userEvent.click(getByText(otherTab));
      // select ip recipient type
      await selectRecipientType(container, ipType);
      // input an ip address
      await typeInIpInput(container, "192", 0);
      await typeInIpInput(container, "168", 1);
      await typeInIpInput(container, "1", 2);
      await typeInIpInput(container, "1", 3);
      await typeInNetmaskInput(container, "24");

      // click add button
      await userEvent.click(getByText(addLabel));
      // expand root node to let recipient displayed in UI
      await selectOperatorNode(container, 0);
      // wait for adding action
      await findByText(ipRecipient.name);

      // click ok button to check the result
      await userEvent.click(getByText(okLabel));
      const result = onFinish.mock.lastCall[0];

      expect(result).toEqual(expectedResult);
    });

    it.each<[string, ReferrerType, string]>([
      ["contain", "Contain", "*edalex.com*"],
      ["exact", "Exact", "edalex.com"],
    ])(
      "should be able to add an HTTP Referrer (%s type) to the expression",
      async (_, httpReferrerType: ReferrerType, expectedReferrer) => {
        const httpReferrerRecipient = referRecipientWithName(expectedReferrer);
        const expectedResult = `F:${expectedReferrer}`;
        const onFinish = jest.fn();
        const renderResult = renderACLExpressionBuilder({
          ...defaultACLExpressionBuilderProps,
          onFinish: onFinish,
        });
        const { findByText, getByText, getByLabelText, container } =
          renderResult;

        // click other panel
        await userEvent.click(getByText(otherTab));
        // select http recipient type
        await selectRecipientType(container, referrerType);
        // input an referrer
        await userEvent.type(getByLabelText(referrerLabel), "edalex.com");
        // select http referrer type
        await selectReferrerType(renderResult, httpReferrerType);

        // click add button
        await userEvent.click(getByText(addLabel));
        // expand root node to let recipient displayed in UI
        await selectOperatorNode(container, 0);
        // wait for adding action
        await findByText(httpReferrerRecipient.name);

        // click ok button to check the result
        await userEvent.click(getByText(okLabel));
        const result = onFinish.mock.lastCall[0];
        expect(result).toEqual(expectedResult);
      }
    );

    it("should be able to add a SSO token ID to the expression", async () => {
      const onFinish = jest.fn();
      const { findByText, getByText, container } = renderACLExpressionBuilder({
        ...defaultACLExpressionBuilderProps,
        onFinish: onFinish,
      });

      // click other panel
      await userEvent.click(getByText(otherTab));
      // select a recipient type
      await selectRecipientType(container, ssoType);
      // wait for getting tokens from API
      await findByText(ssoType);
      // select a sso token
      await selectOption(
        container,
        "#sso-select",
        ssoMoodleRecipientWithName.expression
      );
      // click add button
      await userEvent.click(getByText(addLabel));
      // expand root node to let recipients displayed in UI
      await selectOperatorNode(container, 0);
      // wait for adding action
      await findByText(ssoMoodleRecipientWithName.name);

      // click ok button to check the result
      await userEvent.click(getByText(okLabel));

      const result = onFinish.mock.lastCall[0];
      expect(result).toEqual(ssoMoodleRecipientRawExpression);
    });

    it("should be able to display an error message if it failed to get SSO tokens", async () => {
      const error = new Error("Failed to get tokens");
      const renderResult = renderACLExpressionBuilder({
        ...defaultACLExpressionBuilderProps,
        ssoTokensProvider: () => Promise.reject(error),
      });
      const { findByText, getByText, container } = renderResult;

      // click other panel
      await userEvent.click(getByText(otherTab));
      // select a recipient type
      await selectRecipientType(container, ssoType);
      // wait for getting tokens from API
      await findByText(ssoType);
      // get error message
      const errorMessage = await getByText(
        `Failed to get SSO tokens: ${error}`
      );

      expect(errorMessage).toBeInTheDocument();
    });
  });
});

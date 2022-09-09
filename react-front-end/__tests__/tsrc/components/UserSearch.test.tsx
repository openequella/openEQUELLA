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
import "@testing-library/jest-dom/extend-expect";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { pipe } from "fp-ts/function";
import * as React from "react";
import * as A from "fp-ts/Array";
import * as RSET from "fp-ts/ReadonlySet";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
import { sprintf } from "sprintf-js";
import * as UserModuleMock from "../../../__mocks__/UserModule.mock";
import * as UserSearchMock from "../../../__mocks__/UserSearch.mock";
import { GroupFilter } from "../../../__stories__/components/UserSearch.stories";
import UserSearch from "../../../tsrc/components/UserSearch";
import { eqUserById } from "../../../tsrc/modules/UserModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { doSearch, getUserList } from "./UserSearchTestHelpers";

describe("<UserSearch/>", () => {
  // Helper to render and wait for component under test
  const renderUserSearch = async (
    enableMultipleSelection: boolean,
    selections: ReadonlySet<OEQ.UserQuery.UserDetails> = new Set(),
    onChange: (_: ReadonlySet<OEQ.UserQuery.UserDetails>) => void = jest.fn(),
    groupFilter?: ReadonlySet<string>
  ): Promise<HTMLElement> => {
    const { container } = render(
      <UserSearch
        enableMultiSelection={enableMultipleSelection}
        selections={selections}
        onChange={onChange}
        groupFilter={groupFilter}
        userListProvider={UserSearchMock.userDetailsProvider}
      />
    );

    // Wait for it to be rendered
    await waitFor(() =>
      screen.getByText(languageStrings.userSearchComponent.queryFieldLabel)
    );

    return container;
  };

  const findUserFromMockData = (username: string): OEQ.UserQuery.UserDetails =>
    pipe(
      UserModuleMock.users,
      A.findFirst((u) => u.username === username),
      O.getOrElseW(() => {
        throw new Error(
          "Looks like mocked data set has changed, unable to find test user: " +
            username
        );
      })
    );

  /**
   * Renders the UserSearch component and then executes a search for users with the searchFor query,
   * after which it will attempt to select the user identified by selectUser.
   * It then returns the selections from UserSearch.
   * The operation and state of UserSearch can be modified with initialSelections and enableMultipleSelection.
   *
   * @param searchFor A keyword will be used to do a search action.
   * @param selectUser A user will be selected after search action.
   * @param initialSelections Initial selections used for UserSearch.
   * @param enableMultipleSelection `true` to enable the multiple selection.
   * */
  const getUserSelection = async (
    searchFor: string,
    selectUser: OEQ.UserQuery.UserDetails,
    initialSelections: ReadonlySet<OEQ.UserQuery.UserDetails> = RSET.empty,
    enableMultipleSelection: boolean = false
  ): Promise<ReadonlySet<OEQ.UserQuery.UserDetails>> => {
    const onChange = jest.fn();
    const container = await renderUserSearch(
      enableMultipleSelection,
      initialSelections,
      onChange
    );

    // Attempt search for a specific user
    doSearch(container, searchFor);

    // Wait for the results, and then click our user of interest
    userEvent.click(
      await screen.findByText(new RegExp(`.*${selectUser.lastName}.*`))
    );

    // The handler should've been triggered once with target user returned
    expect(onChange).toHaveBeenCalledTimes(1);

    const argToFirstCall: ReadonlySet<OEQ.UserQuery.UserDetails> =
      onChange.mock.calls[0][0];

    return argToFirstCall;
  };

  describe("general features", () => {
    it("displays the search bar and no users on initial render", async () => {
      const container = await renderUserSearch(false);

      // Ensure the user list section is not present
      expect(getUserList(container)).toBeFalsy();
    });

    it("displays a notice if the results will be filtered by group", async () => {
      await renderUserSearch(
        false,
        new Set(),
        jest.fn(),
        GroupFilter.args!.groupFilter
      );

      expect(
        screen.queryByText(
          languageStrings.userSearchComponent.filterActiveNotice
        )
      ).toBeInTheDocument();
    });

    it("displays an error when it can't find requested user", async () => {
      const container = await renderUserSearch(false);

      // Attempt search for rubbish value
      const noSuchUser = "la blah blah";
      doSearch(container, noSuchUser);

      // Ensure an error was displayed
      await waitFor(() =>
        screen.getByText(
          sprintf(
            languageStrings.userSearchComponent.failedToFindUsersMessage,
            noSuchUser
          )
        )
      ).then((value: HTMLElement) => {
        expect(value).toBeDefined();
      });
    });

    it("displays users if there are any returned", async () => {
      const container = await renderUserSearch(false);

      // Attempt search for known users
      doSearch(container, "user");

      // Await for search results
      const results = await waitFor(() => screen.getAllByText(/user\d00/));
      expect(results).toHaveLength(4);
    });

    it("should return the user details when a user clicks on a result", async () => {
      // Prepare test values - aligning with mock data and function
      const username = "admin999";
      const selectUser = findUserFromMockData(username);
      // The expected result for the first arg of the first call of onChange function.
      const resultingSelections = new Set([selectUser]);

      expect(await getUserSelection(username, selectUser, RSET.empty)).toEqual(
        resultingSelections
      );
    });
  });

  describe("single selection mode", () => {
    it("should still return only one user details after clicking when a user is already selected", async () => {
      const searchWord = "user";
      const initialSelections = new Set([findUserFromMockData("user100")]);
      const selectUser = findUserFromMockData("user200");
      const resultingSelections = new Set([selectUser]);

      expect(
        await getUserSelection(searchWord, selectUser, initialSelections)
      ).toEqual(resultingSelections);
    });
  });

  describe("multiple selection mode", () => {
    const initialSelections = pipe(
      UserModuleMock.users,
      A.filter((user) => pipe(user.username, S.includes("user"))),
      RSET.fromReadonlyArray(eqUserById)
    );

    it("returns the single selection when there are no previous selections", async () => {
      const clickedUser = findUserFromMockData("user200");
      const resultingSelections = new Set([clickedUser]);

      expect(
        await getUserSelection("user", clickedUser, RSET.empty, true)
      ).toEqual(resultingSelections);
    });

    it("adds additional selection to the initial selections", async () => {
      const selectUser = findUserFromMockData("admin999");
      const resultingSelections = pipe(
        initialSelections,
        RSET.insert(eqUserById)(selectUser)
      );

      expect(
        await getUserSelection("", selectUser, initialSelections, true)
      ).toEqual(resultingSelections);
    });

    it("removes previous selections when clicked again", async () => {
      const selectUser = findUserFromMockData("user100");
      const resultingSelections = pipe(
        initialSelections,
        RSET.remove(eqUserById)(selectUser)
      );

      expect(
        await getUserSelection("", selectUser, initialSelections, true)
      ).toEqual(resultingSelections);
    });
  });
});

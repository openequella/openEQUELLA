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
import { render, screen, waitFor, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { sprintf } from "sprintf-js";
import { groups } from "../../../__mocks__/GroupModule.mock";
import * as UserModuleMock from "../../../__mocks__/UserModule.mock";
import * as UserSearchMock from "../../../__mocks__/UserSearch.mock";
import { GroupFilter } from "../../../__stories__/components/UserSearch.stories";
import UserSearch from "../../../tsrc/components/UserSearch";
import * as GroupModule from "../../../tsrc/modules/GroupModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { doSearch, getUserList } from "./UserSearchTestHelpers";

const resolveGroupsPromise = jest
  .spyOn(GroupModule, "resolveGroups")
  .mockResolvedValue(groups);
describe("<UserSearch/>", () => {
  // Helper to render and wait for component under test
  const renderUserSearch = async (
    onSelect: (username: OEQ.UserQuery.UserDetails) => void = jest.fn(),
    groupFilter?: ReadonlySet<string>
  ): Promise<HTMLElement> => {
    const { container } = render(
      <UserSearch
        onSelect={onSelect}
        groupFilter={groupFilter}
        userListProvider={UserSearchMock.userDetailsProvider}
      />
    );

    // Wait for it to be rendered
    await waitFor(() =>
      screen.getByText(languageStrings.userSearchComponent.queryFieldLabel, {
        selector: "label",
      })
    );

    return container;
  };

  it("displays the search bar and no users on initial render", async () => {
    const container = await renderUserSearch();

    // Ensure the user list section is not present
    expect(getUserList(container)).toBeFalsy();
  });

  it("displays a notice if the results will be filtered by group", async () => {
    await renderUserSearch(jest.fn(), GroupFilter.args!.groupFilter);

    // Group filter is provided so also wait for the promise to be resolved.
    await act(async () => {
      await resolveGroupsPromise;
    });

    expect(
      screen.queryByText(languageStrings.userSearchComponent.filterActiveNotice)
    ).toBeInTheDocument();
  });

  it("displays an error when it can't find requested user", async () => {
    const container = await renderUserSearch();

    // Attempt search for rubbish value
    const noSuchUser = "la blah blah";
    await doSearch(container, noSuchUser);

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
    const container = await renderUserSearch();

    // Attempt search for known users
    await doSearch(container, "user");

    // Await for search results
    const results = await waitFor(() => screen.getAllByText(/user\d00/));
    expect(results).toHaveLength(4);
  });

  it("should return the user details when a user clicks on a result", async () => {
    const onSelect = jest.fn();
    const container = await renderUserSearch(onSelect);

    // Prepare test values - aligning with mock data and function
    const username = "admin999";
    const testUser = UserModuleMock.users.find(
      (user: OEQ.UserQuery.UserDetails) => user.username === username
    );
    if (!testUser) {
      throw new Error(
        "Looks like mocked data set has changed, unable to find test user: " +
          username
      );
    }

    // Attempt search for a specific user
    await doSearch(container, username);

    // Wait for the results, and then click our user of interest
    const testUserResult = await waitFor<HTMLElement>(() =>
      screen.getByText(new RegExp(`.*${testUser.lastName}.*`))
    );
    await userEvent.click(testUserResult);

    // The handler should've been triggered once with target user returned
    expect(onSelect).toHaveBeenCalledTimes(1);
    const argToFirstCall: OEQ.UserQuery.UserDetails = onSelect.mock.calls[0][0];
    expect(argToFirstCall).toEqual(testUser);
  });
});

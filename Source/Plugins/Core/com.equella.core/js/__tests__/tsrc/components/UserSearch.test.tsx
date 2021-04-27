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
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import UserSearch from "../../../tsrc/components/UserSearch";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { sprintf } from "sprintf-js";
import * as UserSearchMock from "../../../__mocks__/UserSearch.mock";
import { doSearch, getUserList } from "./UserSearchTestHelpers";

describe("<UserSearch/>", () => {
  // Helper to render and wait for component under test
  const renderUserSearch = async (
    onSelect: (username: OEQ.UserQuery.UserDetails) => void = jest.fn()
  ): Promise<HTMLElement> => {
    const { container } = render(
      <UserSearch
        onSelect={onSelect}
        userListProvider={UserSearchMock.userDetailsProvider}
      />
    );

    // Wait for it to be rendered
    await waitFor(() =>
      screen.getByText(languageStrings.userSearchComponent.queryFieldLabel)
    );

    return container;
  };

  it("displays the search bar and no users on initial render", async () => {
    const container = await renderUserSearch();

    // Ensure the user list section is not present
    expect(getUserList(container)).toBeFalsy();
  });

  it("displays an error when it can't find requested user", async () => {
    const container = await renderUserSearch();

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
    const container = await renderUserSearch();

    // Attempt search for known users
    doSearch(container, "user");

    // Await for search results
    const results = await waitFor(() => screen.getAllByText(/user\d00/));
    expect(results).toHaveLength(4);
  });

  it("should return the user details when a user clicks on a result", async () => {
    const onSelect = jest.fn();
    const container = await renderUserSearch(onSelect);

    // Prepare test values - aligning with mock data and function
    const username = "admin999";
    const testUser = UserSearchMock.users.find(
      (user: OEQ.UserQuery.UserDetails) => user.username === username
    );
    if (!testUser) {
      throw new Error(
        "Looks like mocked data set has changed, unable to find test user: " +
          username
      );
    }

    // Attempt search for a specific user
    doSearch(container, username);

    // Wait for the results, and then click our user of interest
    const testUserResult = await waitFor<HTMLElement>(() =>
      screen.getByText(new RegExp(`.*${testUser.lastName}.*`))
    );
    fireEvent.click(testUserResult);

    // The handler should've been triggered once with target user returned
    expect(onSelect).toHaveBeenCalledTimes(1);
    const argToFirstCall: OEQ.UserQuery.UserDetails = onSelect.mock.calls[0][0];
    expect(argToFirstCall).toEqual(testUser);
  });
});

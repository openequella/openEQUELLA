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
import { render, screen, waitFor } from "@testing-library/react";
import * as React from "react";
import * as UserSearchMock from "../../../../__mocks__/UserSearch.mock";
import * as GroupSearchMock from "../../../../__mocks__/GroupSearch.mock";
import { GroupFilter } from "../../../../__stories__/components/securityentitysearch/UserSearch.stories";
import UserSearch, {
  UserSearchProps,
} from "../../../../tsrc/components/securityentitysearch/UserSearch";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const { queryFieldLabel } = languageStrings.userSearchComponent;

describe("<UserSearch/>", () => {
  const defaultUserSearchProps: UserSearchProps = {
    enableMultiSelection: false,
    selections: new Set(),
    onChange: jest.fn(),
    userListProvider: UserSearchMock.userDetailsProvider,
  };

  // Helper to render and wait for component under test
  const renderUserSearch = async (
    props: UserSearchProps = defaultUserSearchProps
  ): Promise<HTMLElement> => {
    const { container } = render(<UserSearch {...props} />);

    // Wait for it to be rendered
    await waitFor(() => screen.getByText(queryFieldLabel));

    return container;
  };

  describe("general features", () => {
    it("displays a notice if the results will be filtered by group", async () => {
      await renderUserSearch({
        ...defaultUserSearchProps,
        groupFilter: GroupFilter.args!.groupFilter,
        resolveGroupsProvider: GroupSearchMock.resolveGroupsProvider,
      });

      expect(
        screen.queryByText(
          languageStrings.baseSearchComponent.filterActiveNotice
        )
      ).toBeInTheDocument();
    });
  });
});

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
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import { sprintf } from "sprintf-js";
import * as GroupSearchMock from "../../../../__mocks__/GroupSearch.mock";
import * as UserModuleMock from "../../../../__mocks__/UserModule.mock";
import { GroupFilter } from "../../../../__stories__/components/securityentitysearch/UserSearch.stories";
import { eqUserById } from "../../../../tsrc/modules/UserModule";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  renderBaseSearch,
  searchEntity,
  getItemList,
  defaultBaseSearchProps,
} from "./BaseSearchTestHelper";
import { findUserFromMockData } from "./UserSearchTestHelpler";

const { failedToFindMessage } = languageStrings.baseSearchComponent;

/**
 * BaseSearch is a generic component used for searching entities.
 * Different types of entities won't affect its behaviours, thus
 * here just use UserDetails type entity to test its functionalities.
 */
describe("<BaseSearch/>", () => {
  /**
   * Renders the `BaseSearch` component and then executes a search for entities with the `searchFor` query,
   * after which it will attempt to select the entity identified by selectEntity.
   * It then returns the selections from BaseSearch.
   * The operation and state of BaseSearch can be modified with initialSelections and enableMultipleSelection.
   *
   * @param searchFor A keyword will be used to do a search action.
   * @param selectEntity An entity which will be selected after search action.
   * @param initialSelections Initial selections used for EntitySearch.
   * @param enableMultipleSelection `true` to enable the multiple selection.
   * */
  const getEntitySelection = async (
    searchFor: string,
    selectEntity: OEQ.UserQuery.UserDetails,
    initialSelections: ReadonlySet<OEQ.UserQuery.UserDetails> = RSET.empty,
    enableMultiSelection: boolean = false
  ): Promise<ReadonlySet<OEQ.UserQuery.UserDetails>> => {
    const onChange = jest.fn();
    const container = await renderBaseSearch({
      ...defaultBaseSearchProps,
      selections: initialSelections,
      enableMultiSelection,
      onChange,
    });

    // Attempt search for a specific entity
    searchEntity(container, searchFor);

    // Wait for the results, and then click our entity of interest
    userEvent.click(
      await screen.findByText(new RegExp(`.*${selectEntity.lastName}.*`))
    );

    // The handler should've been triggered once with target entity returned
    expect(onChange).toHaveBeenCalledTimes(1);

    const argToFirstCall: ReadonlySet<OEQ.UserQuery.UserDetails> =
      onChange.mock.calls[0][0];

    return argToFirstCall;
  };

  describe("general features", () => {
    it("displays the search bar and no entities on initial render", async () => {
      const container = await renderBaseSearch();

      // Ensure the entity list section is not present
      expect(getItemList(container)).toBeFalsy();
    });

    it("displays the details of filters used in the search", async () => {
      await renderBaseSearch({
        ...defaultBaseSearchProps,
        groupFilter: GroupFilter.args!.groupFilter,
        resolveGroupsProvider: GroupSearchMock.resolveGroupsProvider,
      });

      expect(
        screen.queryByText(
          languageStrings.baseSearchComponent.filterActiveNotice
        )
      ).toBeInTheDocument();
    });

    it("displays an error when it can't find requested entity", async () => {
      const container = await renderBaseSearch();

      // Attempt search for rubbish value
      const noSuchEntity = "la blah blah";
      searchEntity(container, noSuchEntity);

      // Ensure an error was displayed
      await waitFor(() =>
        screen.getByText(sprintf(failedToFindMessage, noSuchEntity))
      ).then((value: HTMLElement) => {
        expect(value).toBeDefined();
      });
    });

    it("displays users if there are any returned", async () => {
      const container = await renderBaseSearch();

      // Attempt search for known entities
      searchEntity(container, "user");

      // Await for search results
      const results = await waitFor(() => screen.getAllByText(/user\d00/));
      expect(results).toHaveLength(4);
    });

    it("should return the entity details when a entity clicks on a result", async () => {
      const queryName = "admin999";
      // Prepare test values - aligning with mock data and function
      const selectEntity = findUserFromMockData(queryName);

      // The expected result for the first arg of the first call of onChange function.
      const resultingSelections = new Set([selectEntity]);

      expect(
        await getEntitySelection(queryName, selectEntity, RSET.empty)
      ).toEqual(resultingSelections);
    });
  });

  describe("single selection mode", () => {
    it("should still return only one entity details after clicking when a entity is already selected", async () => {
      const queryName = "user";
      const selectEntity = findUserFromMockData("user100");
      const initialSelections = new Set([findUserFromMockData("admin999")]);
      const resultingSelections = new Set([selectEntity]);

      expect(
        await getEntitySelection(queryName, selectEntity, initialSelections)
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
      const clickedEntity = findUserFromMockData("user200");
      const resultingSelections = new Set([clickedEntity]);

      expect(
        await getEntitySelection("user", clickedEntity, RSET.empty, true)
      ).toEqual(resultingSelections);
    });

    it("adds additional selection to the initial selections", async () => {
      const selectUser = findUserFromMockData("admin999");
      const resultingSelections = pipe(
        initialSelections,
        RSET.insert(eqUserById)(selectUser)
      );

      expect(
        await getEntitySelection("", selectUser, initialSelections, true)
      ).toEqual(resultingSelections);
    });

    it("removes previous selections when clicked again", async () => {
      const selectEntity = findUserFromMockData("user100");
      const resultingSelections = pipe(
        initialSelections,
        RSET.remove(eqUserById)(selectEntity)
      );

      expect(
        await getEntitySelection("", selectEntity, initialSelections, true)
      ).toEqual(resultingSelections);
    });
  });
});

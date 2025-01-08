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
import "@testing-library/jest-dom";
import { RenderResult, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as RSET from "fp-ts/ReadonlySet";
import * as S from "fp-ts/string";
import { sprintf } from "sprintf-js";
import * as GroupModuleMock from "../../../../__mocks__/GroupModule.mock";
import * as UserModuleMock from "../../../../__mocks__/UserModule.mock";
import { GroupFilter } from "../../../../__stories__/components/securityentitysearch/UserSearch.stories";
import {
  eqGroupById,
  groupIds,
  groupOrd,
} from "../../../../tsrc/modules/GroupModule";
import { eqUserById } from "../../../../tsrc/modules/UserModule";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  renderBaseSearch,
  searchEntity,
  defaultBaseSearchProps,
  searchAndSelect,
  searchGroupFilter,
  clickFilterByGroupButton,
  clickEntitySelectButton,
  findGroupFilterSearch,
  clickEditGroupFilterButton,
  clickCancelGroupFilterButton,
  querySearchResultList,
  selectEntitiesInOneClickMode,
  generateDefaultCheckboxModeProps,
} from "./BaseSearchTestHelper";
import { findUserFromMockData } from "./UserSearchTestHelpler";

const {
  edit: editLabel,
  clear: clearLabel,
  select: selectLabel,
  selectAll: selectAllLabel,
  selectNone: clearAllLabel,
} = languageStrings.common.action;

const { failedToFindMessage, provideQueryMessage } =
  languageStrings.baseSearchComponent;

/**
 * BaseSearch is a generic component used for searching entities.
 * Different types of entities won't affect its behaviours, thus
 * here just use UserDetails type entity to test its functionalities.
 */
describe("<BaseSearch/>", () => {
  const defaultCheckboxModeProps =
    generateDefaultCheckboxModeProps<OEQ.UserQuery.UserDetails>();

  // do search and select entity
  const searchAndSelectEntity = (
    renderResult: RenderResult,
    searchFor: string,
    selectEntityName: string,
    onChange = jest.fn(),
  ) =>
    searchAndSelect(
      renderResult,
      searchFor,
      selectEntityName,
      onChange,
      searchEntity,
    );

  describe("general features", () => {
    it("displays the search bar and the promote query message on initial render", async () => {
      const renderResult = await renderBaseSearch();

      // Ensure the provide query message is present
      expect(renderResult.queryByText(provideQueryMessage)).toBeInTheDocument();
    });

    it("displays the details of filters used in the search", async () => {
      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        groupFilter: GroupFilter.args!.groupFilter,
        resolveGroupsProvider: GroupModuleMock.findGroupsByIds,
      });

      const activeNotice = await renderResult.findByText(
        languageStrings.baseSearchComponent.filterActiveNotice,
      );
      expect(activeNotice).toBeInTheDocument();
    });

    it("displays an error when it can't find requested entity", async () => {
      const { container, findByText } = await renderBaseSearch();

      // Attempt search for rubbish value
      const noSuchEntity = "la blah blah";
      await searchEntity(container, noSuchEntity);

      // Ensure an error was displayed
      const errorMessage = await findByText(
        sprintf(failedToFindMessage, noSuchEntity),
      );
      expect(errorMessage).toBeInTheDocument();
    });

    it("displays users if there are any returned", async () => {
      const { container, findAllByText } = await renderBaseSearch();

      // Attempt search for known entities
      await searchEntity(container, "user");

      // Await for search results
      const results = await findAllByText(/user\d00/);
      expect(results).toHaveLength(4);
    });

    it("should return the entity details when a entity clicks on a result", async () => {
      const queryName = "admin999";
      // Prepare test values - aligning with mock data and function
      const selectEntity = findUserFromMockData(queryName);

      // The expected result for the first arg of the first call of onChange function.
      const expectedSelections = new Set([selectEntity]);

      const onChange = jest.fn();
      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        mode: {
          ...defaultCheckboxModeProps,
          onChange: onChange,
        },
      });

      const selections = await searchAndSelectEntity(
        renderResult,
        queryName,
        selectEntity.username,
        onChange,
      );
      expect(selections).toEqual(expectedSelections);
    });

    it("should return all search results with current selections when users click the `select all` button", async () => {
      const queryName = "user";

      const initialSelect = findUserFromMockData("admin999");
      const initialSelections = pipe(
        [initialSelect],
        RSET.fromReadonlyArray(eqUserById),
      );

      const onSelectAll = jest.fn();

      const expectedResult = pipe(
        UserModuleMock.users,
        A.filter((user) => pipe(user.username, S.includes(queryName))),
        A.concat([initialSelect]),
        RSET.fromReadonlyArray(eqUserById),
      );

      const { container, getByText, findAllByText } = await renderBaseSearch({
        ...defaultBaseSearchProps,
        mode: { ...defaultCheckboxModeProps, selections: initialSelections },
        onSelectAll: onSelectAll,
      });

      // Attempt search for known entities
      await searchEntity(container, queryName);
      // Await for search results
      await findAllByText(new RegExp(`${queryName}\\d00`));
      // await waitFor(() =>
      //   pipe(new RegExp(`${queryName}\\d00`), renderResult.getAllByText)
      // );

      // Click `select all` button
      await userEvent.click(getByText(selectAllLabel));

      const argToFirstCall = onSelectAll.mock.calls[0][0];

      expect(onSelectAll).toHaveBeenCalledTimes(1);
      expect(argToFirstCall).toEqual(expectedResult);
    });

    it("should return an empty set when users click the `clear all` (select none) button", async () => {
      const onClearAll = jest.fn();

      const { container, findByText } = await renderBaseSearch({
        ...defaultBaseSearchProps,
        onClearAll: onClearAll,
      });

      // do search to let clear all button displayed
      await searchEntity(container, "");
      // click `clear all` button
      const clearAll = await findByText(clearAllLabel);
      await userEvent.click(clearAll);

      const argToFirstCall = onClearAll.mock.calls[0][0];

      expect(onClearAll).toHaveBeenCalledTimes(1);
      expect(argToFirstCall).toEqual(RSET.empty);
    });

    it("should return an empty set when search action is triggered", async () => {
      const onChange = jest.fn();

      const { container, findByText } = await renderBaseSearch({
        ...defaultBaseSearchProps,
        mode: { ...defaultCheckboxModeProps, onChange },
      });

      // trigger a search action
      await searchEntity(container, "");
      // wait for search completed
      await waitFor(() => findByText("user100"));

      expect(onChange.mock.lastCall[0]).toEqual(RSET.empty);
    });
  });

  describe("group filter editable mode", () => {
    const groupFilterSelections = pipe(
      GroupModuleMock.groups,
      A.filter((group) => pipe(group.name, S.includes("group"))),
      RSET.fromReadonlyArray(eqGroupById),
    );
    const groupFilterIds = groupIds(groupFilterSelections);

    it("switch to GroupSearch when users click `filterByGroup` button", async () => {
      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        groupFilterEditable: true,
      });

      await clickFilterByGroupButton(renderResult);

      expect(
        await findGroupFilterSearch(renderResult.container),
      ).toBeInTheDocument();
    });

    it("switch to GroupSearch when users choose to edit group filter", async () => {
      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        groupFilterEditable: true,
        groupFilter: GroupFilter.args!.groupFilter,
      });

      const editGroupFilterButton = await renderResult.findByText(editLabel);
      await userEvent.click(editGroupFilterButton);

      expect(
        await findGroupFilterSearch(renderResult.container),
      ).toBeInTheDocument();
    });

    it("select button should be disabled if there is no group filter selected", async () => {
      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        groupFilterEditable: true,
        groupFilter: RSET.empty,
      });

      await clickFilterByGroupButton(renderResult);

      const selectButton = renderResult
        .getByText(selectLabel)
        .closest("button");

      expect(selectButton).toBeDisabled();
    });

    it.each([
      [
        "should update group filter after users edit and click select button",
        clickEntitySelectButton,
        groupFilterIds,
      ],
      [
        "should be able to cancel the changes in group filter search",
        clickCancelGroupFilterButton,
        RSET.empty,
      ],
    ])("%s", async (_, clickActionButton, expectedResult) => {
      const search = jest.fn().mockResolvedValue("test");

      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        search: search,
        groupFilterEditable: true,
      });
      const { container } = renderResult;

      await clickFilterByGroupButton(renderResult);

      // search for groups with name `group`
      await searchGroupFilter(container, "group");

      // Wait for the results, and then click each group
      for (const group of pipe(
        groupFilterSelections,
        RSET.toReadonlyArray(groupOrd),
      )) {
        await userEvent.click(await screen.findByText(group.name));
      }

      // click button
      await clickActionButton(renderResult);

      // trigger search action so that we can check what groups are in use when passed to the search handler
      await searchEntity(container, "");
      // wait for search completed
      await waitFor(() => querySearchResultList(container));

      expect(search).toHaveBeenCalledTimes(1);

      // get the groupFilter param
      const argToFirstCall: ReadonlySet<OEQ.UserQuery.UserDetails> =
        search.mock.calls[0][1];

      expect(argToFirstCall).toEqual(expectedResult);
    });

    it("should be able to see the group filter search when users click the edit button", async () => {
      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        groupFilterEditable: true,
        groupFilter: GroupFilter.args!.groupFilter,
      });

      await clickEditGroupFilterButton(renderResult);

      expect(
        await findGroupFilterSearch(renderResult.container),
      ).toBeInTheDocument();
    });

    it("should clear all group filter when users click clear button", async () => {
      const search = jest.fn().mockResolvedValue("test");

      const { container, findByText } = await renderBaseSearch({
        ...defaultBaseSearchProps,
        search: search,
        groupFilterEditable: true,
        groupFilter: GroupFilter.args!.groupFilter,
      });

      // find and click clear button
      const clearGroupFilterButton = await findByText(clearLabel);
      await userEvent.click(clearGroupFilterButton);

      // trigger a search action so that we can make sure the search is then done with
      // no group filter - confirming it was cleared.
      await searchEntity(container, "");
      // wait for search completed
      await querySearchResultList(container);

      expect(search).toHaveBeenCalledTimes(1);

      // get the groupFilter parameter
      const argToFirstCall: ReadonlySet<OEQ.UserQuery.UserDetails> =
        search.mock.calls[0][1];

      expect(argToFirstCall).toEqual(RSET.empty);
    });
  });

  describe("single selection mode", () => {
    it("should still return only one entity details after clicking when a entity is already selected", async () => {
      const queryName = "user";
      const selectEntity = findUserFromMockData("user100");
      const initialSelections = new Set([findUserFromMockData("admin999")]);
      const expectedSelections = new Set([selectEntity]);
      const onChange = jest.fn();

      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        mode: {
          ...defaultCheckboxModeProps,
          onChange,
          selections: initialSelections,
        },
      });

      expect(
        await searchAndSelectEntity(
          renderResult,
          queryName,
          selectEntity.username,
          onChange,
        ),
      ).toEqual(expectedSelections);
    });
  });

  describe("multiple selection mode", () => {
    const initialSelections = pipe(
      UserModuleMock.users,
      A.filter((user) => pipe(user.username, S.includes("user"))),
      RSET.fromReadonlyArray(eqUserById),
    );

    it("returns the single selection when there are no previous selections", async () => {
      const clickedEntity = findUserFromMockData("user200");
      const expectedSelections = new Set([clickedEntity]);
      const onChange = jest.fn();

      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        mode: {
          ...defaultCheckboxModeProps,
          onChange,
          enableMultiSelection: true,
        },
      });

      expect(
        await searchAndSelectEntity(
          renderResult,
          "user",
          clickedEntity.username,
          onChange,
        ),
      ).toEqual(expectedSelections);
    });

    it("adds additional selection to the initial selections", async () => {
      const selectUser = findUserFromMockData("admin999");
      const expectedSelections = pipe(
        initialSelections,
        RSET.insert(eqUserById)(selectUser),
      );
      const onChange = jest.fn();

      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        mode: {
          ...defaultCheckboxModeProps,
          onChange,
          selections: initialSelections,
          enableMultiSelection: true,
        },
      });

      expect(
        await searchAndSelectEntity(
          renderResult,
          "",
          selectUser.username,
          onChange,
        ),
      ).toEqual(expectedSelections);
    });

    it("removes previous selections when clicked again", async () => {
      const selectEntity = findUserFromMockData("user100");
      const expectedSelections = pipe(
        initialSelections,
        RSET.remove(eqUserById)(selectEntity),
      );
      const onChange = jest.fn();

      const renderResult = await renderBaseSearch({
        ...defaultBaseSearchProps,
        mode: {
          ...defaultCheckboxModeProps,
          onChange,
          selections: initialSelections,
          enableMultiSelection: true,
        },
      });

      expect(
        await searchAndSelectEntity(
          renderResult,
          "",
          selectEntity.username,
          onChange,
        ),
      ).toEqual(expectedSelections);
    });
  });

  describe("one click mode", () => {
    it("returns the result when users click add icon", async () => {
      const clickedEntity = findUserFromMockData("user200");
      const onAdd = jest.fn();

      const { container } = await renderBaseSearch({
        ...defaultBaseSearchProps,
        mode: {
          type: "one_click",
          onAdd,
        },
      });

      await searchEntity(container, "user");
      // Wait for the results, and then click our entity of interest
      await selectEntitiesInOneClickMode(container, [clickedEntity.username]);

      expect(onAdd.mock.lastCall[0]).toEqual(clickedEntity);
    });
  });
});

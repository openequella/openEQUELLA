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
import { ListItemText } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import {
  findByText,
  getByLabelText,
  render,
  RenderResult,
  waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { Predicate } from "fp-ts/Predicate";
import * as TE from "../../../../tsrc/util/TaskEither.extended";
import * as React from "react";
import * as GroupModuleMock from "../../../../__mocks__/GroupModule.mock";
import * as UserModuleMock from "../../../../__mocks__/UserModule.mock";
import BaseSearch, {
  BaseSearchProps,
  CheckboxMode,
} from "../../../../tsrc/components/securityentitysearch/BaseSearch";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { queryMuiTextField } from "../../MuiQueries";
import * as RS from "fp-ts/ReadonlySet";

const {
  edit: editLabel,
  cancel: cancelLabel,
  select: selectLabel,
} = languageStrings.common.action;

const { queryFieldLabel: baseQueryFieldLabel, filterByGroupsButtonLabel } =
  languageStrings.baseSearchComponent;

const { queryFieldLabel: groupQueryFieldLabel } =
  languageStrings.groupSearchComponent;

/**
 * Generic helper function to do the steps of submitting a search in the <BaseSearch/>
 * component. It needs a `queryName` to locate the component.
 *
 * @param dialog The Base Search Dialog
 * @param queryName the label value displayed in the query bar, and here use it to locate the search bar
 * @param queryValue the value to put in the query field before pressing enter
 */
export const doSearch = async (
  dialog: HTMLElement,
  queryName: string,
  queryValue: string,
) => {
  const queryField = queryMuiTextField(dialog, queryName);
  if (!queryField) {
    throw new Error("Unable to find query field!");
  }
  await userEvent.type(queryField, `${queryValue}{enter}`);
};

/**
 * Helper function to do the steps of submitting a search in the <BaseSearch/>.
 * It uses `baseQueryFieldLabel` to locate the component.
 *
 * @param dialog The Base Search Dialog
 * @param queryValue the value to put in the query field before pressing enter
 */
export const searchEntity = (dialog: HTMLElement, queryValue: string) =>
  doSearch(dialog, baseQueryFieldLabel, queryValue);

/**
 * Helper function to do the steps of submitting a group filter search in the <BaseSearch/>.
 *
 * @param dialog The Base Search Dialog
 * @param queryValue the value to put in the query field before pressing enter
 */
export const searchGroupFilter = (dialog: HTMLElement, queryValue: string) =>
  doSearch(dialog, groupQueryFieldLabel, queryValue);

/**
 * Clicks an element based on identifying text (found with text).
 *
 * @param renderResult The Base Search Dialog render result.
 * @param text The value used to get the element.
 */
const clickByText = async ({ findByText }: RenderResult, text: string) =>
  await userEvent.click(await findByText(text));

/**
 * Helper function to mock clicking `filterByGroup` button.
 *
 * @param renderResult a root container within which <BaseSearch/> exists
 */
export const clickFilterByGroupButton = async (
  renderResult: RenderResult,
): Promise<void> => await clickByText(renderResult, filterByGroupsButtonLabel);

/**
 * Helper function to mock clicking `select` button.
 *
 * @param renderResult a root container within which <BaseSearch/> exists
 */
export const clickEntitySelectButton = async (
  renderResult: RenderResult,
): Promise<void> => await clickByText(renderResult, selectLabel);

/**
 * Helper function to mock clicking `cancel` button.
 *
 * @param renderResult a root container within which <BaseSearch/> exists
 */
export const clickCancelGroupFilterButton = async (
  renderResult: RenderResult,
): Promise<void> => await clickByText(renderResult, cancelLabel);

/**
 * Helper function to mock clicking `edit` button.
 *
 * @param renderResult a root container within which <BaseSearch/> exists
 */
export const clickEditGroupFilterButton = (
  renderResult: RenderResult,
): Promise<void> => clickByText(renderResult, editLabel);

/**
 * Helper function to assist in finding GroupFilterSearch when user choose to edit the group filter.
 *
 * @param container a root container within which <BaseSearch/> exists
 */
export const findGroupFilterSearch = (
  container: HTMLElement,
): Promise<Element> =>
  waitFor(() => {
    const search = container.querySelector("#GroupFilter-BaseSearch");
    if (!search) {
      throw new Error("Can't find group filter search");
    }
    return search;
  });

/**
 * Helper function to assist in finding the search result list.
 */
export const querySearchResultList = (container: HTMLElement): Element | null =>
  container.querySelector("#item-search-list");

/**
 * Some common shared props to render the `EntitySearch`.
 */
export const commonSearchProps = {
  enableMultiSelection: false,
  onChange: jest.fn(),
  groupSearch: GroupModuleMock.searchGroups,
  resolveGroupsProvider: GroupModuleMock.findGroupsByIds,
};

/**
 * Generate checkbox mode props with type info.
 */
export const generateDefaultCheckboxModeProps = <T,>(): CheckboxMode<T> => ({
  type: "checkbox",
  selections: RS.empty,
  onChange: jest.fn(),
});

export const defaultBaseSearchProps: BaseSearchProps<OEQ.UserQuery.UserDetails> =
  {
    ...commonSearchProps,
    mode: generateDefaultCheckboxModeProps<OEQ.UserQuery.UserDetails>(),
    search: UserModuleMock.listUsers,
    itemDetailsToEntry: ({
      username,
      firstName,
      lastName,
    }: OEQ.UserQuery.UserDetails) => (
      <ListItemText primary={username} secondary={`${firstName} ${lastName}`} />
    ),
  };

// Helper to render BaseSearch and wait for component under test
export const renderBaseSearch = async (
  props: BaseSearchProps<OEQ.UserQuery.UserDetails> = defaultBaseSearchProps,
): Promise<RenderResult> => {
  const renderResult = render(<BaseSearch {...props} />);

  // Wait for it to be rendered
  await waitFor(() => renderResult.getByText(baseQueryFieldLabel));

  return renderResult;
};

/**
 * Renders the `ACLEntitySearch` (BaseSearch/UserSearch/GroupSearch/RoleSearch) component
 * and then executes a search for entities with the `searchFor` query,
 * after which it will attempt to select the entity identified by selectEntityName.
 * It then returns the selections from `EntitySearch`.
 *
 * @param renderResult The jest render result of ACLEntitySearch.
 * @param searchFor A keyword will be used to do a search action.
 * @param selectEntityName The name of the entity which will be selected after search action.
 * @param onChange A mock function that will be called after select action.
 * @param doSearch A function that will trigger the search action.
 * */
export const searchAndSelect = async <T,>(
  renderResult: RenderResult,
  searchFor: string,
  selectEntityName: string,
  onChange = jest.fn(),
  doSearch: (dialog: HTMLElement, queryValue: string) => Promise<void>,
): Promise<ReadonlySet<T>> => {
  const { container } = renderResult;
  // Attempt search for a specific entity
  await doSearch(container, searchFor);

  // Wait for the results, and then click our entity of interest
  const entityName = await renderResult.findByText(
    new RegExp(`.*${selectEntityName}.*`),
  );
  await userEvent.click(entityName);

  return onChange.mock.lastCall[0];
};

/**
 * Generic helper function to assist in finding the specific entity from mock data.
 *
 * @param mockEntities A mock entity list data.
 * @param predicate The predicate function used to find the entity.
 * @param name The name of the entity we wanted.
 */
export const findEntityFromMockData = <T,>(
  mockEntities: T[],
  predicate: Predicate<T>,
  name: string,
): T =>
  pipe(
    mockEntities,
    A.findFirst(predicate),
    O.getOrElseW(() => {
      throw new Error(
        "Looks like mocked data set has changed, unable to find test entity: " +
          name,
      );
    }),
  );

export const selectEntitiesInOneClickMode = async (
  container: HTMLElement,
  selectNames: string[],
) => {
  // Wait for the results, and then click entity
  const selectEntity = async (name: string) => {
    const entityName = await findByText(container, name);
    const entity = entityName.parentElement?.parentElement;
    if (!entity) {
      throw new Error(`Can't find entity ${name}`);
    }
    await userEvent.click(getByLabelText(entity, selectLabel));
  };

  const task = (name: string) => TE.tryCatch(() => selectEntity(name), String);

  await pipe(selectNames, A.map(task), TE.sequenceSeqArray, TE.getOrThrow)();
};

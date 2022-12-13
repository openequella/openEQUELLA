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
import { ListItemText } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import { render, RenderResult, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { Predicate } from "fp-ts/Predicate";
import * as React from "react";
import * as GroupModuleMock from "../../../../__mocks__/GroupModule.mock";
import * as UserModuleMock from "../../../../__mocks__/UserModule.mock";
import BaseSearch, {
  BaseSearchProps,
} from "../../../../tsrc/components/securityentitysearch/BaseSearch";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { queryMuiTextField } from "../../MuiQueries";

const {
  edit: editLabel,
  cancel: cancelLabel,
  select: selectLabel,
} = languageStrings.common.action;

const {
  queryFieldLabel: baseQueryFieldLabel,
  filterByGroupsButtonLabel,
  groupFilterSearchHintMessage,
} = languageStrings.baseSearchComponent;

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
export const doSearch = (
  dialog: HTMLElement,
  queryName: string,
  queryValue: string
) => {
  const queryField = queryMuiTextField(dialog, queryName);
  if (!queryField) {
    throw new Error("Unable to find query field!");
  }
  userEvent.type(queryField, `${queryValue}{enter}`);
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
const clickByText = ({ getByText }: RenderResult, text: string) =>
  userEvent.click(getByText(text));

/**
 * Helper function to mock clicking `filterByGroup` button.
 *
 * @param renderResult a root container within which <BaseSearch/> exists
 */
export const clickFilterByGroupButton = (renderResult: RenderResult): void =>
  clickByText(renderResult, filterByGroupsButtonLabel);

/**
 * Helper function to mock clicking `select` button.
 *
 * @param renderResult a root container within which <BaseSearch/> exists
 */
export const clickEntitySelectButton = (renderResult: RenderResult): void =>
  clickByText(renderResult, selectLabel);

/**
 * Helper function to mock clicking `cancel` button.
 *
 * @param renderResult a root container within which <BaseSearch/> exists
 */
export const clickCancelGroupFilterButton = (
  renderResult: RenderResult
): void => clickByText(renderResult, cancelLabel);

/**
 * Helper function to mock clicking `edit` button.
 *
 * @param renderResult a root container within which <BaseSearch/> exists
 */
export const clickEditGroupFilterButton = (renderResult: RenderResult): void =>
  clickByText(renderResult, editLabel);

/**
 * Helper function to assist in finding GroupFilterSearch when user choose to edit the group filter.
 *
 * @param container a root container within which <BaseSearch/> exists
 */
export const queryGroupFilterSearch = (
  renderResult: RenderResult
): Element | null => renderResult.queryByText(groupFilterSearchHintMessage);

/**
 * Some common shared props to render the `EntitySearch`.
 */
export const commonSearchProps = {
  enableMultiSelection: false,
  onChange: jest.fn(),
  groupSearch: GroupModuleMock.listGroups,
  resolveGroupsProvider: GroupModuleMock.resolveGroups,
};

export const defaultBaseSearchProps: BaseSearchProps<OEQ.UserQuery.UserDetails> =
  {
    ...commonSearchProps,
    selections: new Set(),
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
  props: BaseSearchProps<OEQ.UserQuery.UserDetails> = defaultBaseSearchProps
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
  doSearch: (dialog: HTMLElement, queryValue: string) => void
): Promise<ReadonlySet<T>> => {
  const { container } = renderResult;
  // Attempt search for a specific entity
  doSearch(container, searchFor);

  // Wait for the results, and then click our entity of interest
  userEvent.click(
    await renderResult.findByText(new RegExp(`.*${selectEntityName}.*`))
  );

  const argToFirstCall: ReadonlySet<T> = onChange.mock.lastCall[0];

  return argToFirstCall;
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
  name: string
): T =>
  pipe(
    mockEntities,
    A.findFirst(predicate),
    O.getOrElseW(() => {
      throw new Error(
        "Looks like mocked data set has changed, unable to find test entity: " +
          name
      );
    })
  );

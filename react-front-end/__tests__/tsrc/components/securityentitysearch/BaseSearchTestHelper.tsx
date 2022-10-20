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
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import * as React from "react";
import * as UserSearchMock from "../../../../__mocks__/UserSearch.mock";
import BaseSearch, {
  BaseSearchProps,
} from "../../../../tsrc/components/securityentitysearch/BaseSearch";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { queryMuiTextField } from "../../MuiQueries";

const { queryFieldLabel: baseQueryFieldLabel } =
  languageStrings.baseSearchComponent;

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
  fireEvent.change(queryField, { target: { value: queryValue } });
  fireEvent.keyDown(queryField, { key: "Enter", code: "Enter" });
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
 * Helper function to assist in finding the list of items after a search.
 *
 * @param container a root container within which <BaseSearch/> exists
 */
export const getItemList = (container: HTMLElement): Element | null =>
  container.querySelector("#BaseSearch-ItemList");

export const defaultBaseSearchProps: BaseSearchProps<OEQ.UserQuery.UserDetails> =
  {
    enableMultiSelection: false,
    selections: new Set(),
    onChange: jest.fn(),
    itemListProvider: UserSearchMock.userDetailsProvider,
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
): Promise<HTMLElement> => {
  const { container } = render(<BaseSearch {...props} />);

  // Wait for it to be rendered
  await waitFor(() => screen.getByText(baseQueryFieldLabel));

  return container;
};

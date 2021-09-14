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
import { act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  initialiseEssentialMocks,
  mockCollaborators,
  queryClassificationPanel,
  queryCollectionSelector,
  renderSearchPage,
} from "./SearchPageTestHelper";

const {
  showAdvancedSearchFilter: filterButtonLabel,
  AdvancedSearchPanel: { title: panelTitle },
} = languageStrings.searchpage;

const {
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearch,
  mockSearchSettings,
} = mockCollaborators();
initialiseEssentialMocks({
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearchSettings,
});
const searchPromise = mockSearch.mockResolvedValue(getSearchResult);

const testUuid = "4be6ae54-68ca-4d8b-acd0-0ca96fc39280";

const renderAdvancedSearchPage = async () =>
  await renderSearchPage(searchPromise, undefined, testUuid);

describe("Display of Advanced Search Criteria panel", () => {
  it("is hidden for normal searching", async () => {
    const { queryByText } = await renderSearchPage(searchPromise);
    expect(queryByText(panelTitle)).not.toBeInTheDocument();
  });

  it("is displayed in advanced search mode", async () => {
    const { queryByText } = await renderAdvancedSearchPage();
    expect(queryByText(panelTitle)).toBeInTheDocument();
  });
});

describe("Advanced Search filter button", () => {
  it("is available in advanced search mode", async () => {
    const { queryByLabelText } = await renderAdvancedSearchPage();
    expect(queryByLabelText(filterButtonLabel)).toBeInTheDocument();
  });

  it("is hidden for normal searching", async () => {
    const { queryByLabelText } = await renderSearchPage(searchPromise);
    expect(queryByLabelText(filterButtonLabel)).not.toBeInTheDocument();
  });

  it("toggles the AdvancedSearchPanel when clicked", async () => {
    const { getByLabelText, queryByText } = await renderAdvancedSearchPage();
    const togglePanel = () =>
      act(() => userEvent.click(getByLabelText(filterButtonLabel)));
    const queryAdvSearchPanel = () => queryByText(panelTitle);

    // First the panel is displayed for Advanced Search mode, so toggle to hide
    togglePanel();
    expect(queryAdvSearchPanel()).not.toBeInTheDocument();

    // Toggle to show again
    togglePanel();
    expect(queryAdvSearchPanel()).toBeInTheDocument();
  });
});

describe("Hide components", () => {
  it("does not show Collection Selector", async () => {
    const { container } = await renderAdvancedSearchPage();
    expect(queryCollectionSelector(container)).not.toBeInTheDocument();
  });

  it("does not list Classifications and show Classification panel", async () => {
    mockListClassification.mockClear();
    const { container } = await renderAdvancedSearchPage();
    expect(mockListClassification).not.toHaveBeenCalled();
    expect(queryClassificationPanel(container)).not.toBeInTheDocument();
  });
});

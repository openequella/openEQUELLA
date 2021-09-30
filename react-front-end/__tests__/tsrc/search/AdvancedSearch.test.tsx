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
import { act, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { getAdvancedSearchDefinition } from "../../../__mocks__/AdvancedSearchModule.mock";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  validateControlValue,
  controlLabelsAndValues,
  editBoxTitle,
  controls,
  oneEditBoxWizard,
  updateControlValue,
} from "./AdvancedSearchTestHelper";
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
  mockGetAdvancedSearchByUuid,
} = mockCollaborators();
initialiseEssentialMocks({
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearchSettings,
});
const searchPromise = mockSearch.mockResolvedValue(getSearchResult);

// Mock out collaborator which retrieves an Advanced search
mockGetAdvancedSearchByUuid.mockResolvedValue(getAdvancedSearchDefinition);

const testUuid = "4be6ae54-68ca-4d8b-acd0-0ca96fc39280";

const renderAdvancedSearchPage = async () =>
  await renderSearchPage(searchPromise, undefined, testUuid);

const togglePanel = () =>
  act(() => userEvent.click(screen.getByLabelText(filterButtonLabel)));

const queryAdvSearchPanel = (container: Element): HTMLElement | null =>
  container.querySelector("#advanced-search-panel");

const clickSearchButton = async (container: Element): Promise<void> => {
  const searchButton = container.querySelector(
    "#advanced-search-panel-searchBtn"
  );
  if (!searchButton) {
    throw new Error("Failed to locate Advanced Search 'search' button.");
  }

  await waitFor(() => userEvent.click(searchButton));
};

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
    const { container } = await renderAdvancedSearchPage();

    // First the panel is displayed for Advanced Search mode, so toggle to hide
    togglePanel();
    expect(queryAdvSearchPanel(container)).not.toBeInTheDocument();

    // Toggle to show again
    togglePanel();
    expect(queryAdvSearchPanel(container)).toBeInTheDocument();
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

describe("Rendering of wizard", () => {
  it("shows an explanatory caption for mandatory fields", async () => {
    mockGetAdvancedSearchByUuid.mockResolvedValue(oneEditBoxWizard(true));
    const { queryByText } = await renderAdvancedSearchPage();

    // Mandatory controls should be suffixed '*'
    expect(queryByText(`${editBoxTitle} *`)).toBeInTheDocument();
    expect(queryByText(languageStrings.common.required)).toBeInTheDocument();
  });

  it("does not show the explanatory caption for mandatory fields when there are none", async () => {
    mockGetAdvancedSearchByUuid.mockResolvedValue(oneEditBoxWizard(false));
    const { queryByText } = await renderAdvancedSearchPage();

    // Mandatory controls should be suffixed '*' - let's match against the title without it here.
    expect(queryByText(`${editBoxTitle}`)).toBeInTheDocument();
    expect(
      queryByText(languageStrings.common.required)
    ).not.toBeInTheDocument();
  });

  // Correct fields are rendered
  // Values are set
  // Values remain present once a search has been triggered - i.e. stored in state
  it("stores values in state when search is clicked, and then re-uses them when the wizard is re-rendered", async () => {
    const advancedSearchDefinition: OEQ.AdvancedSearch.AdvancedSearchDefinition =
      {
        ...getAdvancedSearchDefinition,
        controls: controls,
      };
    mockGetAdvancedSearchByUuid.mockResolvedValue(advancedSearchDefinition);
    const { container } = await renderAdvancedSearchPage();

    // For each control, trigger an event to update or select their values.
    controlLabelsAndValues.forEach(({ labels, values, controlType }) => {
      updateControlValue(container, labels, values, controlType);
    });

    // Click search - so as to persist values
    await clickSearchButton(container);

    // Collapse the panel
    togglePanel();
    expect(queryAdvSearchPanel(container)).not.toBeInTheDocument();

    // And bring it back
    togglePanel();

    // Make sure all the values are there as expected
    controlLabelsAndValues.forEach(({ labels, values, controlType }) => {
      validateControlValue(container, labels, values, controlType);
    });
  });
});

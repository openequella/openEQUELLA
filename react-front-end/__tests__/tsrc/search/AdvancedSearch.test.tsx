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
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import { getAdvancedSearchDefinition } from "../../../__mocks__/AdvancedSearchModule.mock";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import { elapsedTime, startTimer } from "../../../tsrc/util/debug";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  editBoxEssentials,
  filterEmptyValues,
  generateMockedControls,
  getControlValue,
  MockedControlValue,
  oneEditBoxWizard,
  updateControlValue,
  WizardControlLabelValue,
} from "./AdvancedSearchTestHelper";
import {
  initialiseEssentialMocks,
  mockCollaborators,
  queryClassificationPanel,
  queryCollectionSelector,
  renderSearchPage,
} from "./SearchPageTestHelper";

// This has some big tests for rendering the Search Page, but also going through and testing
// all components as one big wizard - e.g.:
// "stores values in state when search is clicked, and then re-uses them when the wizard is re-rendered"
jest.setTimeout(20000);

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
  mockGetTokensForText,
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

const togglePanel = () =>
  act(() => userEvent.click(screen.getByLabelText(filterButtonLabel)));

const renderAdvancedSearchPage = async () => {
  const page = await renderSearchPage(searchPromise, undefined, testUuid);
  // Due to the UI change - hiding the panel when a search is triggered, the tests should
  // also get updated accordingly. However, as we will make further changes for the UI,
  // just manually open the panel for now so that we can keep the tests as they are.
  // We will rework here later when we figure out how to nicely display the panel.
  togglePanel();

  return page;
};

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

const clickClearButton = async (container: Element) => {
  const clearButton = container.querySelector(
    "#advanced-search-panel-clearBtn"
  );
  if (!clearButton) {
    throw new Error("Failed to locate Advanced Search 'clear' button.");
  }

  await waitFor(() => userEvent.click(clearButton));
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

  it("indicates whether advanced search criteria has been set", async () => {
    mockGetAdvancedSearchByUuid.mockResolvedValue(oneEditBoxWizard(false));
    const { container, getByLabelText } = await renderAdvancedSearchPage();

    const getHighlightedFilterButton = () =>
      getByLabelText(
        languageStrings.searchpage.showAdvancedSearchFilter
      ).querySelector(".MuiSvgIcon-colorSecondary");

    // The filter button is not highlighted yet.
    expect(getHighlightedFilterButton()).not.toBeInTheDocument();

    // Put some texts in the EditBox.
    userEvent.type(getByLabelText(`${editBoxEssentials.title}`), "text");
    await clickSearchButton(container);
    // Now the filter button is highlighted in Secondary color.
    expect(getHighlightedFilterButton()).toBeInTheDocument();

    // Open the panel and clear out the EditBox's content.
    togglePanel();
    userEvent.clear(getByLabelText(`${editBoxEssentials.title}`));
    await clickSearchButton(container);
    // Now the filter button is not highlighted again.
    expect(getHighlightedFilterButton()).not.toBeInTheDocument();
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
  // Function to build mocked Wizard controls. The parameter determines whether to use
  // each control's default values or the values specified in `controlValues`.
  const buildMockedControls = (
    useDefaultValues: boolean = false
  ): MockedControlValue[] => generateMockedControls(useDefaultValues);

  it("shows an explanatory caption for mandatory fields", async () => {
    mockGetAdvancedSearchByUuid.mockResolvedValue(oneEditBoxWizard(true));
    const { queryByText } = await renderAdvancedSearchPage();

    // Mandatory controls should be suffixed '*'
    expect(queryByText(`${editBoxEssentials.title} *`)).toBeInTheDocument();
    expect(queryByText(languageStrings.common.required)).toBeInTheDocument();
  });

  it("does not show the explanatory caption for mandatory fields when there are none", async () => {
    mockGetAdvancedSearchByUuid.mockResolvedValue(oneEditBoxWizard(false));
    const { queryByText } = await renderAdvancedSearchPage();

    // Mandatory controls should be suffixed '*' - let's match against the title without it here.
    expect(queryByText(`${editBoxEssentials.title}`)).toBeInTheDocument();
    expect(
      queryByText(languageStrings.common.required)
    ).not.toBeInTheDocument();
  });

  // Correct fields are rendered
  // Values are set
  // Values remain present once a search has been triggered - i.e. stored in state
  it("stores values in state when search is clicked, and then re-uses them when the wizard is re-rendered", async () => {
    const mockedControls = buildMockedControls();
    const [controls, mockedLabelsAndValues] = A.unzip(mockedControls);

    const advancedSearchDefinition: OEQ.AdvancedSearch.AdvancedSearchDefinition =
      {
        ...getAdvancedSearchDefinition,
        controls,
      };
    mockGetAdvancedSearchByUuid.mockResolvedValue(advancedSearchDefinition);
    const { container } = await renderAdvancedSearchPage();

    // The follow section is known to be long running because it has to manipulate all the controls
    // which in turn triggers re-renders etc. It is due to this block that we set jest.setTimeout
    // at the top of the file. To track things though, we've added the various time tracking
    // and console.table call(s) below.
    const setValuesTimeSummary = [];
    const setValuesTimer = startTimer("Set control values - TOTAL");
    // For each control, trigger an event to update or select their values.
    mockedControls.forEach(([{ controlType }, controlLabelValue]) => {
      const controlTimer = startTimer(`Control [${controlType}]`);

      // Set the value
      updateControlValue(container, controlLabelValue, controlType);

      setValuesTimeSummary.push(elapsedTime(controlTimer));
    });
    setValuesTimeSummary.push(elapsedTime(setValuesTimer));
    console.table(setValuesTimeSummary);

    // Click search - so as to persist values
    await clickSearchButton(container);

    // Open the panel again.
    togglePanel();

    // Collect all labels and values.
    const labelsAndValues = mockedControls.map(([c, controlValue]) =>
      getControlValue(
        container,
        Array.from(controlValue.keys()),
        c.controlType,
        true
      )
    );

    expect(labelsAndValues).toEqual(mockedLabelsAndValues);
  });

  it("shows each control's description if there is any", async () => {
    const [controls] = A.unzip(buildMockedControls());
    const advancedSearchDefinition: OEQ.AdvancedSearch.AdvancedSearchDefinition =
      {
        ...getAdvancedSearchDefinition,
        controls,
      };
    mockGetAdvancedSearchByUuid.mockResolvedValue(advancedSearchDefinition);

    const { getByText } = await renderAdvancedSearchPage();
    const descriptions: string[] = controls
      .map(({ description }) => description)
      .filter((d): d is string => typeof d !== "undefined");

    // We must have controls that have descriptions.
    expect(descriptions.length).toBeGreaterThan(0);
    descriptions.forEach((d) => {
      expect(getByText(d)).toBeInTheDocument();
    });

    expect.assertions(descriptions.length + 1); // Plus the one for checking the number of descriptions.
  });

  it("shows each control's default value, and check if the clear button works", async () => {
    const mockedControls = buildMockedControls(true);
    const [controls, mockedLabelsAndValues] = A.unzip(mockedControls);

    const advancedSearchDefinition: OEQ.AdvancedSearch.AdvancedSearchDefinition =
      {
        ...getAdvancedSearchDefinition,
        controls,
      };
    mockGetAdvancedSearchByUuid.mockResolvedValue(advancedSearchDefinition);
    const { container } = await renderAdvancedSearchPage();

    // Collect _all_ values.
    const getCurrentLabelsAndValues = (): (
      | WizardControlLabelValue
      | undefined
    )[] =>
      buildMockedControls().map(([c, controlValue]) =>
        getControlValue(
          container,
          Array.from(controlValue.keys()),
          c.controlType
        )
      );

    // Make sure the current values match the default
    expect(getCurrentLabelsAndValues()).toEqual(mockedLabelsAndValues);

    // Click clear button
    await clickClearButton(container);
    expect(pipe(getCurrentLabelsAndValues(), filterEmptyValues)).toEqual([]);
  });
});

describe("search with Advanced search criteria", () => {
  it("supports searching with raw Lucene query", async () => {
    jest.clearAllMocks();
    const defaultValue = "hello world";
    mockGetAdvancedSearchByUuid.mockResolvedValue(
      oneEditBoxWizard(false, [defaultValue])
    );
    await renderAdvancedSearchPage();

    // Should do a tokenisation.
    expect(mockGetTokensForText).toHaveBeenCalledWith(defaultValue);

    // The raw Lucene query should be part of the SearchOptions.
    const [searchOptions] = mockSearch.mock.calls[0];
    expect(searchOptions.customLuceneQuery).toBe(
      "(/item/name\\*:(hello world))"
    );
  });
});

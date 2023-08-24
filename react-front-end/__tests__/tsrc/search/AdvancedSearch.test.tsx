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
import { ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import "@testing-library/jest-dom/extend-expect";
import { act, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as T from "fp-ts/Task";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import {
  getAdvancedSearchDefinition,
  mockWizardControlFactory,
} from "../../../__mocks__/AdvancedSearchModule.mock";
import { createMatchMedia } from "../../../__mocks__/MockUseMediaQuery";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import { mockedAdvancedSearchCriteria } from "../../../__mocks__/WizardHelper.mock";
import { AppContext } from "../../../tsrc/mainui/App";
import AdvancedSearchPage from "../../../tsrc/search/AdvancedSearchPage";
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
  queryRemoteSearchSelector,
} from "./SearchPageTestHelper";

// This has some big tests for rendering the Search Page, but also going through and testing
// all components as one big wizard - e.g.:
// "stores values in state when search is clicked, and then re-uses them when the wizard is re-rendered"
jest.setTimeout(25000);

const {
  showAdvancedSearchFilter: filterButtonLabel,
  AdvancedSearchPanel: { title: defaultPanelTitle },
} = languageStrings.searchpage;

const panelTitle = getAdvancedSearchDefinition.name ?? defaultPanelTitle;

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

const togglePanel = async () =>
  await userEvent.click(screen.getByLabelText(filterButtonLabel));

const renderAdvancedSearchPage = async () => {
  window.matchMedia = createMatchMedia(1280, true);

  const history = createMemoryHistory();
  history.push("/page/advancedsearch/4be6ae54-68ca-4d8b-acd0-0ca96fc39280");

  const page = render(
    <ThemeProvider theme={createTheme()}>
      <Router history={history}>
        <AppContext.Provider
          value={{
            appErrorHandler: jest.fn(),
            refreshUser: jest.fn(),
            currentUser: getCurrentUserMock,
          }}
        >
          <AdvancedSearchPage updateTemplate={jest.fn()} />
        </AppContext.Provider>
      </Router>
    </ThemeProvider>
  );
  // Wait for the first completion of initial search
  await act(async () => await searchPromise);

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

  await userEvent.click(searchButton);
};

const clickClearButton = async (container: Element) => {
  const clearButton = container.querySelector(
    "#advanced-search-panel-clearBtn"
  );
  if (!clearButton) {
    throw new Error("Failed to locate Advanced Search 'clear' button.");
  }

  await userEvent.click(clearButton);
};

describe("Display of Advanced Search Criteria panel", () => {
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

  it("toggles the AdvancedSearchPanel when clicked", async () => {
    const { container } = await renderAdvancedSearchPage();

    // First the panel is displayed for Advanced Search mode, so toggle to hide
    await togglePanel();
    expect(queryAdvSearchPanel(container)).not.toBeInTheDocument();

    // Toggle to show again
    await togglePanel();
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
    await userEvent.type(getByLabelText(`${editBoxEssentials.title}`), "text");
    await clickSearchButton(container);
    // Now the filter button is highlighted in Secondary color.
    expect(getHighlightedFilterButton()).toBeInTheDocument();

    // Open the panel and clear out the EditBox's content.
    await togglePanel();
    await userEvent.clear(getByLabelText(`${editBoxEssentials.title}`));
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

  it("does not show Remote Search Selector", async () => {
    const { container } = await renderAdvancedSearchPage();
    expect(queryRemoteSearchSelector(container)).not.toBeInTheDocument();
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
    const updateControlValueTask =
      ([
        { controlType },
        controlLabelValue,
      ]: MockedControlValue): T.Task<void> =>
      async () => {
        const controlTimer = startTimer(`Control [${controlType}]`);

        // Set the value
        await updateControlValue(container, controlLabelValue, controlType);

        setValuesTimeSummary.push(elapsedTime(controlTimer));
      };

    // Now update all the controls in sequential tasks.
    await pipe(
      mockedControls,
      A.traverse(T.ApplicativeSeq)(updateControlValueTask)
    )();

    setValuesTimeSummary.push(elapsedTime(setValuesTimer));
    console.table(setValuesTimeSummary);

    // Click search - so as to persist values
    await clickSearchButton(container);

    // Open the panel again.
    await togglePanel();

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
  it("supports searching with Advanced search criteria", async () => {
    jest.clearAllMocks();
    const defaultValue = ["hello world"];
    mockGetAdvancedSearchByUuid.mockResolvedValue(
      oneEditBoxWizard(false, defaultValue)
    );
    await renderAdvancedSearchPage();

    const [searchOptions] = mockSearch.mock.calls[0];
    expect(searchOptions.advancedSearchCriteria).toStrictEqual(
      mockedAdvancedSearchCriteria
    );
  });
});

describe("Detect duplicated control target", () => {
  const duplicateWarning =
    languageStrings.searchpage.AdvancedSearchPanel.duplicateTargetWarning;

  it("shows a warning when there is any same control type targeting to same node", async () => {
    mockGetAdvancedSearchByUuid.mockResolvedValueOnce({
      ...getAdvancedSearchDefinition,
      controls: A.replicate(2, mockWizardControlFactory(editBoxEssentials)),
    });

    const { queryByText } = await renderAdvancedSearchPage();

    expect(queryByText(duplicateWarning)).toBeInTheDocument();
  });

  it("does not show the warning if same node is targeted by different control types", async () => {
    mockGetAdvancedSearchByUuid.mockResolvedValueOnce({
      ...getAdvancedSearchDefinition,
      controls: [
        mockWizardControlFactory(editBoxEssentials),
        mockWizardControlFactory({
          ...editBoxEssentials,
          controlType: "calendar",
        }),
      ],
    });

    const { queryByText } = await renderAdvancedSearchPage();

    expect(queryByText(duplicateWarning)).not.toBeInTheDocument();
  });
});

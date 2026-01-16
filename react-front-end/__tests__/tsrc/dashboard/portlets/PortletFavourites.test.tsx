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
import "@testing-library/jest-dom";
import { composeStories } from "@storybook/react";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { MemoryRouter } from "react-router-dom";
import { sprintf } from "sprintf-js";
import { getFavouriteSearchesResp } from "../../../../__mocks__/Favourites.mock";
import { basicSearchObj } from "../../../../__mocks__/searchresult_mock_data";
import * as stories from "../../../../__stories__/dashboard/portlets/Favourites.stories";
import { FAVOURITES_TYPE_PARAM } from "../../../../tsrc/favourites/FavouritesPageHelper";
import { NEW_FAVOURITES_PATH } from "../../../../tsrc/mainui/routes";
import { FavouritesType } from "../../../../tsrc/modules/FavouriteModule";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const {
  ErrorResources,
  ErrorSearches,
  NoResources,
  NoResourcesAndNoSearches,
  NoSearches,
  Simple,
} = composeStories(stories);

const strings = {
  ...languageStrings.dashboard.portlets.favourites,
  actionShowAll: languageStrings.common.action.showAll,
};
const noResourcesLabel = sprintf(strings.noneFound, strings.resourcesTabName);
const noSearchesLabel = sprintf(strings.noneFound, strings.searchesTabName);

const setup = async (element: React.ReactElement) => {
  const user = userEvent.setup();
  const renderResult = render(<MemoryRouter>{element}</MemoryRouter>);

  // Check that Show All button is present to ensure component has loaded
  await renderResult.findByRole("link", { name: strings.actionShowAll });

  const clickResourcesTab = async () => {
    await user.click(
      renderResult.getByRole("tab", { name: strings.resourcesTabName }),
    );
  };
  const clickSearchesTab = async () => {
    await user.click(
      renderResult.getByRole("tab", { name: strings.searchesTabName }),
    );
  };

  return { user, ...renderResult, clickResourcesTab, clickSearchesTab };
};

describe("<PortletFavourites />", () => {
  it("renders without crashing", async () => {
    const { getByText } = await setup(<Simple />);

    expect(getByText(basicSearchObj.name!)).toBeInTheDocument();
  });

  it("changes between tabs when clicked", async () => {
    const { clickResourcesTab, clickSearchesTab, getByRole } = await setup(
      <Simple />,
    );

    const expectShowAllButtonLink = (favouritesType: FavouritesType) => {
      const showAllButton = getByRole("link", {
        name: strings.actionShowAll,
      });
      const expectedLink = `${NEW_FAVOURITES_PATH}?${FAVOURITES_TYPE_PARAM}=${favouritesType}`;
      expect(showAllButton).toHaveAttribute("href", expectedLink);
    };
    const expectTabSelected = (tabName: string, isSelected: boolean) => {
      const tab = getByRole("tab", { name: tabName });
      expect(tab).toHaveAttribute("aria-selected", `${isSelected}`);
    };
    const expectResourcesTabActive = () => {
      expectTabSelected(strings.resourcesTabName, true);
      expectTabSelected(strings.searchesTabName, false);
      expectShowAllButtonLink("resources");
    };
    const expectSearchesTabActive = () => {
      expectTabSelected(strings.resourcesTabName, false);
      expectTabSelected(strings.searchesTabName, true);
      expectShowAllButtonLink("searches");
    };

    // Initially resources tab should be active
    expectResourcesTabActive();

    // Click on searches tab
    await clickSearchesTab();

    // Searches tab should now be active
    expectSearchesTabActive();

    // Click back to resources tab
    await clickResourcesTab();

    // Resources tab should be active again
    expectResourcesTabActive();
  });

  describe("Successful results", () => {
    it("displays favourite searches when searches tab is active", async () => {
      const { clickSearchesTab, getByText } = await setup(<Simple />);

      await clickSearchesTab();

      expect(
        getByText(getFavouriteSearchesResp.results[0].name),
      ).toBeInTheDocument();
    });

    it("displays favourite resources when resources tab is active", async () => {
      const { getByText } = await setup(<Simple />);

      // Resource results should already be visible (resources tab is active by default)
      expect(getByText(basicSearchObj.name!)).toBeInTheDocument();
    });
  });

  describe("No results found", () => {
    it("shows no results message for favourite searches", async () => {
      const { clickSearchesTab, getByText } = await setup(<NoSearches />);

      await clickSearchesTab();

      expect(getByText(noSearchesLabel)).toBeInTheDocument();
    });

    it("shows no results message for favourite resources", async () => {
      const { getByText } = await setup(<NoResources />);

      // No results message should already be visible (resources tab is active by default)
      expect(getByText(noResourcesLabel)).toBeInTheDocument();
    });

    it("shows no results for both tabs when no data available", async () => {
      const { clickSearchesTab, getByText } = await setup(
        <NoResourcesAndNoSearches />,
      );

      // Check resources tab (default)
      expect(getByText(noResourcesLabel)).toBeInTheDocument();

      // Switch to searches tab
      await clickSearchesTab();

      // Check searches tab
      expect(getByText(noSearchesLabel)).toBeInTheDocument();
    });
  });

  describe("Error handling", () => {
    it("handles error from resources provider gracefully", async () => {
      const { getByText } = await setup(<ErrorResources />);

      // Should show no results instead of crashing
      expect(getByText(noResourcesLabel)).toBeInTheDocument();
    });

    it("handles error from searches provider gracefully", async () => {
      const { clickSearchesTab, getByText } = await setup(<ErrorSearches />);

      await clickSearchesTab();

      // Wait for component to handle error
      expect(getByText(noSearchesLabel)).toBeInTheDocument();
    });
  });
});

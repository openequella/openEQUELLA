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
import * as OEQ from "@openequella/rest-api-client";
import { composeStories } from "@storybook/react";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { MemoryRouter } from "react-router-dom";
import { publicRecentContributionsPortlet } from "../../../../__mocks__/Dashboard.mock";
import {
  itemWithAttachment,
  itemWithLongDescription,
} from "../../../../__mocks__/SearchResult.mock";
import * as stories from "../../../../__stories__/dashboard/portlets/RecentContributions.stories";
import { PortletRecentContributions } from "../../../../tsrc/dashboard/portlet/PortletRecentContributions";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const strings = {
  ...languageStrings.dashboard.portlets.recentContributions,
  actionShowAll: languageStrings.common.action.showAll,
};

// Mock react-router to capture history.push calls
const mockHistoryPush = jest.fn();
jest.mock("react-router", () => ({
  ...jest.requireActual("react-router"),
  useHistory: () => ({
    push: mockHistoryPush,
  }),
}));

const { Default, TitleOnly, NoResults, ErrorOnSearch, SlowLoading } =
  composeStories(stories);

/** Helper function to wait for portlet to be ready */
const waitForPortletReady = async (
  findByRole: (role: string, options: { name: string }) => Promise<HTMLElement>,
) => await findByRole("button", { name: strings.actionShowAll });

/** Setup function without waiting for portlet to be ready. Focused on supporting Storybook stories. */
const setupNoWait = (element: React.ReactElement) => {
  const user = userEvent.setup();
  const renderResult = render(<MemoryRouter>{element}</MemoryRouter>);

  return { user, ...renderResult };
};

/** Setup function that waits for portlet to be ready. Focused on supporting Storybook stories. */
const setup = async (element: React.ReactElement) => {
  const renderResult = setupNoWait(element);

  // Check that Show All button is present to ensure component has loaded
  await waitForPortletReady(renderResult.findByRole);

  return renderResult;
};

/** Setup function for tests that need to mock search provider and history
 */
const setupWithMocks = (
  searchResponse: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>,
  portletConfig: OEQ.Dashboard.RecentContributionsPortlet = publicRecentContributionsPortlet,
) => {
  const user = userEvent.setup();

  // Create mock search provider
  const mockSearchProvider = jest.fn().mockResolvedValue(searchResponse);

  // Clear previous history calls
  mockHistoryPush.mockClear();

  const renderResult = render(
    <MemoryRouter>
      <PortletRecentContributions
        cfg={portletConfig}
        searchProvider={mockSearchProvider}
        position={{ order: 0, column: 0 }}
        highlight={false}
      />
    </MemoryRouter>,
  );

  return {
    user,
    mockSearchProvider,
    mockHistory: mockHistoryPush,
    ...renderResult,
  };
};

describe("<PortletRecentContributions />", () => {
  it("renders without crashing and shows results", async () => {
    const { getByText } = await setup(<Default />);

    // Should show results from mock data
    expect(getByText(itemWithAttachment.name!)).toBeInTheDocument();
    expect(getByText(itemWithAttachment.description!)).toBeInTheDocument();
  });

  it("shows only titles when isShowTitleOnly is true", async () => {
    const { getByText, queryByText } = await setup(<TitleOnly />);

    // Should show the item name/title
    expect(getByText(itemWithAttachment.name!)).toBeInTheDocument();

    // Should NOT show the description
    expect(
      queryByText(itemWithAttachment.description!),
    ).not.toBeInTheDocument();
  });

  it("shows no results message when search returns nothing", async () => {
    const { getByText } = await setup(<NoResults />);

    expect(getByText(strings.noneFound)).toBeInTheDocument();
  });

  it("handles specific query configuration", async () => {
    // Custom portlet config with specific query
    const customConfig: OEQ.Dashboard.RecentContributionsPortlet = {
      ...publicRecentContributionsPortlet,
      commonDetails: {
        ...publicRecentContributionsPortlet.commonDetails,
        name: "Hitchhiker's Guide Items",
      },
      query: "hitchhiker guide galaxy",
      collectionUuids: [
        "c1a2b3d4-e5f6-7890-abcd-1234567890ef",
        "f0e1d2c3-b4a5-6789-0fed-cba987654321",
        "12345678-90ab-cdef-1234-567890abcdef",
      ],
      itemStatus: "DRAFT",
    };

    const searchResponse = {
      start: 0,
      length: 2,
      available: 2,
      results: [itemWithLongDescription, itemWithAttachment],
      highlight: [],
    };

    const { getByText, findByRole, mockSearchProvider } = setupWithMocks(
      searchResponse,
      customConfig,
    );
    await waitForPortletReady(findByRole);

    // Verify the search provider was called with the correct query
    expect(mockSearchProvider).toHaveBeenCalledWith(
      expect.objectContaining({
        query: customConfig.query,
        collections: customConfig.collectionUuids,
        status: [customConfig.itemStatus],
        order: "datemodified", // Default ordering as per requirements
        length: 5, // Default length as per requirements
      }),
    );

    // Should show the custom portlet name
    expect(getByText(customConfig.commonDetails.name)).toBeInTheDocument();

    // Should show results
    expect(getByText(itemWithLongDescription.name!)).toBeInTheDocument();
  });

  it("shows empty state when search provider fails", async () => {
    const { findByText } = await setup(<ErrorOnSearch />);

    // Should show no results message when search fails
    expect(await findByText(strings.noneFound)).toBeInTheDocument();
  });

  it("shows loading state for slow searches", async () => {
    const { findByTestId } = setupNoWait(<SlowLoading />);

    // Should show loading skeleton while waiting
    // Note: The loading state is managed by DraggablePortlet's isLoading prop
    const skeleton = await findByTestId("portlet-item-skeleton");
    expect(skeleton).toBeInTheDocument();
  });

  it("navigates to search page when Show All button is clicked", async () => {
    const searchResponse = {
      start: 0,
      length: 1,
      available: 1,
      results: [itemWithAttachment],
      highlight: [],
    };

    const { user, mockHistory, findByRole, getByRole } =
      setupWithMocks(searchResponse);
    await waitForPortletReady(findByRole);

    // Click the Show All button
    const showAllButton = getByRole("button", { name: strings.actionShowAll });
    await user.click(showAllButton);

    // Verify that history.push was called
    expect(mockHistory).toHaveBeenCalledTimes(1);

    // Verify that it was called with a search page route
    expect(mockHistory).toHaveBeenCalledWith(
      expect.stringContaining("/page/search"),
    );
  });

  it("uses default maxAge of 30 days when not specified", async () => {
    // Custom portlet config WITHOUT maxAge specified
    const configWithoutMaxAge = {
      ...publicRecentContributionsPortlet,
      maxAge: undefined, // Explicitly using 'undefined' to test default
      query: "",
    };

    const searchResponse = {
      start: 0,
      length: 1,
      available: 1,
      results: [itemWithAttachment],
      highlight: [],
    };

    const { mockSearchProvider, findByRole } = setupWithMocks(
      searchResponse,
      configWithoutMaxAge,
    );
    await waitForPortletReady(findByRole);

    expect(mockSearchProvider).toHaveBeenCalled();

    // Calculate what the modifiedAfter date should be (30 days ago)
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
    const expectedModifiedAfter = thirtyDaysAgo.toISOString().split("T")[0];

    // Verify the search provider was called with modifiedAfter set to 30 days ago
    expect(mockSearchProvider).toHaveBeenCalledWith(
      expect.objectContaining({
        modifiedAfter: expectedModifiedAfter,
        // All the other default params
        order: "datemodified",
        length: 5,
        collections: undefined,
        query: "",
        status: undefined,
      }),
    );
  });
});

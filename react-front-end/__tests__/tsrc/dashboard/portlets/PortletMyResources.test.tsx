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
import { render, type RenderResult } from "@testing-library/react";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";

import * as React from "react";
import { MemoryRouter } from "react-router-dom";
import { getMyResourceCategoriesTransformedResp } from "../../../../__mocks__/myResourcesCategories_mock_data";
import * as stories from "../../../../__stories__/dashboard/portlets/MyResources.stories";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { updateMockGetBaseUrl } from "../../BaseUrlHelper";
import { getCountForItem } from "./PortletTestHelper";

const { showAll: showAllText } = languageStrings.common.action;

const { Simple, ScrapbookDisabled, ErrorOnLoad } = composeStories(stories);

/** Ready state check function that waits for Alert component to be present */
const waitForAlert = async (renderResult: RenderResult): Promise<void> => {
  await renderResult.findByRole("alert");
};

const setup = async (
  element: React.ReactElement,
  readyStateCheck?: (renderResult: RenderResult) => Promise<void>,
) => {
  const renderResult = render(<MemoryRouter>{element}</MemoryRouter>);

  const defaultReadyStateCheck = async (ctx: typeof renderResult) => {
    await ctx.findByRole("link", { name: showAllText });
  };

  await (readyStateCheck || defaultReadyStateCheck)(renderResult);

  return renderResult;
};

// Mock getBaseUrl() function
updateMockGetBaseUrl();

describe("<PorletMyResources />", () => {
  it("renders without crashing and displays list of my resources types", async () => {
    const { getByText } = await setup(<Simple />);

    // Check that all my resources types are displayed from mock data
    const allTypesArePresent = pipe(
      getMyResourceCategoriesTransformedResp,
      A.every((type) => !!getByText(type.name!)),
    );

    expect(allTypesArePresent).toBe(true);
  });

  it("displays my resource types counts correctly", async () => {
    const { getByText } = await setup(<Simple />);

    const countsMatch = pipe(
      getMyResourceCategoriesTransformedResp,
      A.filter((item) => (item.count ?? 0) > 0),
      A.every((item) => getCountForItem(item.name, getByText) === item.count),
    );

    expect(countsMatch).toBe(true);
  });

  it("does not show badges for items with zero count", async () => {
    const { getByText } = await setup(<Simple />);

    const countsMatch = pipe(
      getMyResourceCategoriesTransformedResp,
      A.filter((item) => item.count === 0),
      // They should be undefined, as they should not exist
      A.every((item) => getCountForItem(item.name, getByText) === undefined),
    );

    expect(countsMatch).toBe(true);
  });

  it("does not show 'Scrapbook' my resource type in the list if the scrapbook is disabled for current user", async () => {
    const { queryByText } = await setup(<ScrapbookDisabled />);

    expect(queryByText("Scrapbook")).not.toBeInTheDocument();
  });

  it("shows error message when no results are returned", async () => {
    const { getByText } = await setup(<ErrorOnLoad />, waitForAlert);

    expect(
      getByText("Error: Failed to fetch my resources types"),
    ).toBeInTheDocument();
  });
});

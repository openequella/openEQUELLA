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
import { itemWithAttachment } from "../../../../__mocks__/SearchResult.mock";
import * as stories from "../../../../__stories__/dashboard/portlets/QuickSearch.stories";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const { Simple, NoResults, ErrorOnSearch, SettingsError, SlowSearch } =
  composeStories(stories);

const strings = {
  ...languageStrings.dashboard.portlets.quickSearch,
  actionShowAll: languageStrings.common.action.showAll,
};

const setup = async (element: React.ReactElement) => {
  const user = userEvent.setup();
  const renderResult = render(<MemoryRouter>{element}</MemoryRouter>);
  // Wait for the search field to be present
  const input: HTMLInputElement = (await renderResult.findByLabelText(
    strings.queryField,
  )) as HTMLInputElement;

  return { user, ...renderResult, input };
};

describe("<PortletQuickSearch />", () => {
  it("renders and shows results after typing", async () => {
    const { user, input, findByText } = await setup(<Simple />);

    await user.type(input, "test");
    // Wait for a result from the mock
    expect(
      await findByText(itemWithAttachment.description!),
    ).toBeInTheDocument();
  });

  it("shows no results message when search returns nothing", async () => {
    const { user, input, findByText } = await setup(<NoResults />);

    await user.type(input, "empty");
    expect(await findByText(strings.noResults)).toBeInTheDocument();
  });

  it("shows loading indicator while searching (slow search)", async () => {
    const { user, input, findByLabelText } = await setup(<SlowSearch />);

    await user.type(input, "slow");
    // Should show a progress indicator while waiting
    expect(await findByLabelText(strings.searching)).toBeInTheDocument();
  });

  it("shows error message if search settings fail to load", async () => {
    const { findByText } = render(<SettingsError />);
    expect(await findByText(strings.failedToInitialise)).toBeInTheDocument();
  });

  it("shows no results if search provider throws error", async () => {
    const { user, input, findByText } = await setup(<ErrorOnSearch />);

    await user.type(input, "fail");
    expect(await findByText(strings.noResults)).toBeInTheDocument();
  });

  it("clears the search when Escape is pressed", async () => {
    const { user, input } = await setup(<Simple />);

    await user.type(input, "something");
    expect(input.value).toBe("something");

    await user.keyboard("{Escape}");
    expect(input.value).toBe("");
  });

  it("shows the Show all button", async () => {
    const { findByRole } = await setup(<Simple />);
    expect(
      await findByRole("button", { name: strings.actionShowAll }),
    ).toBeInTheDocument();
  });
});

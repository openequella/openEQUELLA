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
import * as React from "react";
import { MemoryRouter } from "react-router-dom";
import { rootHierarchies } from "../../../../__mocks__/Hierarchy.mock";
import * as stories from "../../../../__stories__/dashboard/portlets/Browse.stories";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const { noneFound } = languageStrings.dashboard.portlets.browse;

const { Standard, NoResults, ErrorGet } = composeStories(stories);

const setup = async (element: React.ReactElement) =>
  render(<MemoryRouter>{element}</MemoryRouter>);

describe("<PortletBrowse />", () => {
  it("renders without crashing", async () => {
    const { findByText } = await setup(<Standard />);
    const rootHierarchy = await findByText(rootHierarchies[0].name!);

    expect(rootHierarchy).toBeInTheDocument();
  });

  it("shows no results message when get root hierarchies returns nothing", async () => {
    const { findByText } = await setup(<NoResults />);
    const notFoundMessage = await findByText(noneFound);

    expect(notFoundMessage).toBeInTheDocument();
  });

  it("handles error from get root hierarchies provider gracefully", async () => {
    const { findByText } = await setup(<ErrorGet />);
    const notFoundMessage = await findByText(noneFound);

    // Should show no results instead of crashing
    expect(notFoundMessage).toBeInTheDocument();
  });
});

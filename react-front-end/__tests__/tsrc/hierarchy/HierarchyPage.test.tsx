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
import {
  act,
  getAllByLabelText,
  render,
  RenderResult,
} from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Route, Router } from "react-router-dom";
import {
  getHierarchy,
  topicWithChildren,
} from "../../../__mocks__/Hierarchy.mock";
import { createMatchMedia } from "../../../__mocks__/MockUseMediaQuery";
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import HierarchyPage from "../../../tsrc/hierarchy/HierarchyPage";
import "@testing-library/jest-dom";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  initialiseEssentialMocks,
  mockCollaborators,
} from "../search/SearchPageTestHelper";

const {
  addKeyResource: addKeyResourceText,
  removeKeyResource: removeKeyResourceText,
} = languageStrings.hierarchy;

const {
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearchSettings,
  mockSearch,
} = mockCollaborators();
initialiseEssentialMocks({
  mockCollections,
  mockCurrentUser,
  mockListClassification,
  mockSearchSettings,
});
const searchPromise = mockSearch.mockResolvedValue(getSearchResult);

jest.mock("../../../tsrc/modules/HierarchyModule", () => ({
  getHierarchy: jest.fn(getHierarchy),
}));

const renderHierarchyPage = async (
  compoundUuid: string,
): Promise<RenderResult> => {
  const urlPrefix = "/page/hierarchy/";
  const history = createMemoryHistory();
  history.push(`${urlPrefix}${compoundUuid}`);
  window.matchMedia = createMatchMedia(1280);

  const result = render(
    <ThemeProvider theme={createTheme()}>
      <Router history={history}>
        <Route path={`${urlPrefix}:compoundUuid`}>
          <HierarchyPage updateTemplate={jest.fn()} />
        </Route>
      </Router>
    </ThemeProvider>,
  );

  const hierarchy = await getHierarchy(compoundUuid);
  // Wait for search result
  await act(async () => {
    await searchPromise;
  });
  // Wait for the hierarchy panel to finish rendering.
  await result.findByText(hierarchy.summary.name!, {
    selector: "h4",
  });
  return result;
};

describe("<HierarchyPage/>", () => {
  it("displays breadcrumb on hierarchy panel", async () => {
    const compoundUuid = topicWithChildren.compoundUuid;
    const hierarchy = await getHierarchy(compoundUuid);
    const { findByText } = await renderHierarchyPage(compoundUuid);

    // Display breadcrumb.
    for (const { name } of hierarchy.parents) {
      expect(
        await findByText(name!, {
          selector: "a, p",
        }),
      ).toBeInTheDocument();
    }
    expect.assertions(hierarchy.parents.length);
  });

  it("displays hierarchy details on hierarchy panel", async () => {
    const compoundUuid = topicWithChildren.compoundUuid;
    const hierarchy = await getHierarchy(compoundUuid);
    const { getByText, findByText } = await renderHierarchyPage(compoundUuid);

    // Display hierarchy name.
    expect(
      await findByText(hierarchy.summary.name!, {
        selector: "h4",
      }),
    ).toBeInTheDocument();
    // Display long description.
    expect(getByText(hierarchy.summary.longDescription!)).toBeInTheDocument();
    // Display hierarchy summary.
    hierarchy.summary.subHierarchyTopics.forEach(({ name }) =>
      expect(getByText(name!)).toBeInTheDocument(),
    );
    expect.assertions(hierarchy.summary.subHierarchyTopics.length + 2);
  });

  it("displays normal search result with outline pin icon", async () => {
    const { getByTestId } = await renderHierarchyPage(
      topicWithChildren.compoundUuid,
    );

    const resultList = getByTestId("search-result-list");
    const nonKeyResourcesCount = getSearchResult.results.length - 2;
    // Display unpin icons with `add key resource` tooltip.
    expect(getAllByLabelText(resultList, addKeyResourceText)).toHaveLength(
      nonKeyResourcesCount,
    );
  });

  it("displays search result with pin icon if it's a key resource", async () => {
    const { getByTestId } = await renderHierarchyPage(
      topicWithChildren.compoundUuid,
    );

    const resultList = getByTestId("search-result-list");
    // Display pin icon with `remove key resource` tooltip
    expect(getAllByLabelText(resultList, removeKeyResourceText)).toHaveLength(
      2,
    );
  });
});

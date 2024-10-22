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
  getByText,
  render,
  RenderResult,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Route, Router } from "react-router-dom";
import {
  getHierarchy,
  getMyAcls,
  topicWithChildren,
  topicWithHideNoResultChild,
  topicWithoutModifyKeyResources,
  topicWithoutSearchResults,
  topicWithShortAndLongDesc,
  virtualTopics,
} from "../../../__mocks__/Hierarchy.mock";
import {
  getSearchResult,
  itemNotInKeyResource,
} from "../../../__mocks__/SearchResult.mock";
import * as HierarchyModule from "../../../tsrc//modules/HierarchyModule";
import RootHierarchyPage from "../../../tsrc/hierarchy/RootHierarchyPage";
import "@testing-library/jest-dom";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  initialiseEssentialMocks,
  mockCollaborators,
} from "../search/SearchPageTestHelper";
import {
  closeSelectionSession,
  prepareSelectionSession,
} from "../SelectionSessionHelper";
import { mockWindowLocation } from "../WindowHelper";
import { clickAddKeyResource } from "./HierarchyTestHelper";

const {
  addKeyResource: addKeyResourceText,
  removeKeyResource: removeKeyResourceText,
} = languageStrings.hierarchy;
const { title: addToHierarchyTitle } =
  languageStrings.searchpage.addToHierarchy;
const { add: addToKeyResourceText } =
  languageStrings.searchpage.hierarchyKeyResourceDialog;

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

const mockGetHierarchy = jest
  .spyOn(HierarchyModule, "getHierarchy")
  .mockImplementation(getHierarchy);

jest.spyOn(HierarchyModule, "getMyAcls").mockImplementation(getMyAcls);

const renderHierarchyPage = async (
  compoundUuid: string,
  isNewPath: boolean = true,
): Promise<RenderResult> => {
  const NEW_HIERARCHY_PATH = "/page/hierarchy/";
  const OLD_HIERARCHY_PATH = "/hierarchy.do";
  const pathname = isNewPath
    ? `${NEW_HIERARCHY_PATH}${compoundUuid}`
    : OLD_HIERARCHY_PATH;

  // Create a mock window.location object with testing pathname and query parameters.
  mockWindowLocation(pathname, isNewPath ? "" : `topic=${compoundUuid}`);

  const history = createMemoryHistory();
  history.push(pathname);

  const result = render(
    <ThemeProvider theme={createTheme()}>
      <Router history={history}>
        <Route
          path={
            isNewPath
              ? `${NEW_HIERARCHY_PATH}:compoundUuid`
              : OLD_HIERARCHY_PATH
          }
        >
          <RootHierarchyPage updateTemplate={jest.fn()} />
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

describe("Display of Hierarchy panel", () => {
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
    // Display sub topic section name.
    expect(
      getByText(hierarchy.summary.subTopicSectionName!),
    ).toBeInTheDocument();
    // Display hierarchy summary.
    hierarchy.summary.subHierarchyTopics.forEach(({ name }) =>
      expect(getByText(name!)).toBeInTheDocument(),
    );
    expect.assertions(hierarchy.summary.subHierarchyTopics.length + 3);
  });

  it("hide hierarchy topic if it has no result and parent hierarchy `hideSubtopicsWithNoResults` is set to `true`", async () => {
    const { queryByText } = await renderHierarchyPage(
      topicWithHideNoResultChild.compoundUuid,
      true,
    );

    expect(
      queryByText(topicWithHideNoResultChild.subHierarchyTopics[0].name!),
    ).not.toBeInTheDocument();
  });
});

describe("Display of Key resource panel", () => {
  it("displays key resource panel if it has key resources", async () => {
    const compoundUuid = topicWithShortAndLongDesc.compoundUuid;
    const hierarchy = await getHierarchy(compoundUuid);
    const { getByTestId } = await renderHierarchyPage(compoundUuid);

    const keyResourcePanel = getByTestId("key-resource-panel");

    hierarchy.keyResources.forEach(({ item }) =>
      expect(
        getByText(keyResourcePanel, item.name ?? item.uuid),
      ).toBeInTheDocument(),
    );
    expect.assertions(hierarchy.keyResources.length);
  });
});

describe("Pin icon", () => {
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

  it("hide all pin icons if user doesn't have MODIFY_KEY_RESOURCE ACL", async () => {
    const compoundUuid = topicWithoutModifyKeyResources.compoundUuid;
    const { queryAllByLabelText } = await renderHierarchyPage(compoundUuid);

    expect(queryAllByLabelText(removeKeyResourceText)).toHaveLength(0);
    expect(queryAllByLabelText(addKeyResourceText)).toHaveLength(0);
  });
});

describe("Search result", () => {
  it("hide search result if 'Display resources' is set to false", async () => {
    const compoundUuid = topicWithoutSearchResults.compoundUuid;
    const { queryByTestId } = await renderHierarchyPage(compoundUuid);

    const resultList = queryByTestId("search-result-list");
    expect(resultList).not.toBeInTheDocument();
  });

  it("display search result section name if it has been set", async () => {
    const compoundUuid = topicWithChildren.compoundUuid;
    const hierarchy = await getHierarchy(compoundUuid);
    const { getByText } = await renderHierarchyPage(compoundUuid);

    expect(
      getByText(
        `${hierarchy.summary.searchResultSectionName!} (${
          getSearchResult.available
        })`,
      ),
    ).toBeInTheDocument();
  });

  it("hide `add to hierarchy` button", async () => {
    const { queryAllByLabelText } = await renderHierarchyPage(
      topicWithChildren.compoundUuid,
    );

    expect(queryAllByLabelText(addToHierarchyTitle)).toHaveLength(0);
  });
});

describe("Selection Session", () => {
  it("uses Selection Session specific URL when Selection Session is open", async () => {
    prepareSelectionSession();
    const parentTopicName = "Parent1";

    const compoundUuid = topicWithChildren.compoundUuid;
    const { getByText } = await renderHierarchyPage(compoundUuid);

    const breadcrumbUrl = getByText(parentTopicName, {
      selector: "a",
    }).getAttribute("href");
    expect(breadcrumbUrl).toBe(
      "http://localhost:8080/vanilla/hierarchy.do?topic=uuid1&_sl.stateId=1",
    );

    closeSelectionSession();
  });
});

describe("Share search", () => {
  it("supports a Hierarchy search shared from Legacy UI", async () => {
    const uuid = topicWithChildren.compoundUuid;
    await renderHierarchyPage(uuid, false);

    expect(mockGetHierarchy).toHaveBeenLastCalledWith(uuid);
  });

  it("generates a link for sharing the current Hierarchy search", async () => {
    const mockClipboard = jest
      .spyOn(navigator.clipboard, "writeText")
      .mockResolvedValueOnce();

    const compoundUuid = virtualTopics.compoundUuid;
    const { getByLabelText } = await renderHierarchyPage(compoundUuid);

    const copySearchButton = getByLabelText(
      languageStrings.searchpage.shareSearchHelperText,
    );

    await userEvent.click(copySearchButton);

    expect(mockClipboard).toHaveBeenCalledWith(
      "/page/hierarchy/886aa61d-f8df-4e82-8984-c487849f80ff:A James?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22rank%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22%22%2C%22collections%22%3A%5B%5D%2C%22lastModifiedDateRange%22%3A%7B%7D%2C%22mimeTypeFilters%22%3A%5B%5D%2C%22displayMode%22%3A%22list%22%2C%22dateRangeQuickModeEnabled%22%3Atrue%2C%22filterExpansion%22%3Atrue%7D",
    );
  });
});

describe("Select version dialog", () => {
  it("Display select version dialog before adding key resource", async () => {
    const uuid = topicWithChildren.compoundUuid;
    const { container, queryByText } = await renderHierarchyPage(uuid, false);
    await clickAddKeyResource(
      container,
      itemNotInKeyResource.uuid,
      itemNotInKeyResource.version,
    );

    // Check if dialog is displayed.
    expect(queryByText(addToKeyResourceText)).toBeInTheDocument();
  });
});

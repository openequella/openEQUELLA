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
import { pipe } from "fp-ts/function";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import * as React from "react";
import {
  act,
  getByLabelText,
  getByDisplayValue,
  queryByText,
  render,
  RenderResult,
  screen,
  getAllByRole,
  fireEvent,
  getByText,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import "@testing-library/jest-dom";
import FacetedSearchSettingsPage from "../../../tsrc/settings/Search/facetedsearch/FacetedSearchSettingsPage";
import * as FacetedSearchSettingsModule from "../../../tsrc/modules/FacetedSearchSettingsModule";
import { FacetedSearchClassificationWithFlags } from "../../../tsrc/modules/FacetedSearchSettingsModule";
import { NavigationGuardProps } from "../../../tsrc/components/NavigationGuard";
import { getSchemasResp } from "../../../__mocks__/getSchemasResp";
import { getSchemaUuidResp } from "../../../__mocks__/getSchemaUuidResp";
import * as OEQ from "@openequella/rest-api-client";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { getMuiButtonByText } from "../MuiQueries";

const mockFacets: OEQ.FacetedSearchSettings.FacetedSearchClassification[] = [
  {
    id: 1,
    name: "mocked facet1",
    schemaNode: "item/name",
    maxResults: 1,
    orderIndex: 0,
  },
  {
    id: 2,
    name: "mocked facet2",
    schemaNode: "item/age",
    maxResults: 2,
    orderIndex: 1,
  },
  {
    id: 3,
    name: "mocked facet3",
    schemaNode: "item/age",
    maxResults: 3,
    orderIndex: 2,
  },
];

jest.mock("@openequella/rest-api-client");
(
  OEQ.Schema.listSchemas as jest.Mock<
    Promise<OEQ.Common.PagedResult<OEQ.Common.BaseEntity>>
  >
).mockResolvedValue(getSchemasResp);
(
  OEQ.Schema.getSchema as jest.Mock<Promise<OEQ.Schema.EquellaSchema>>
).mockResolvedValue(getSchemaUuidResp);

const mockFacetsWithFlags: FacetedSearchClassificationWithFlags[] =
  mockFacets.map((f) => {
    return { ...f, updated: false, deleted: false };
  });

// This test does not need NavigationGuard which is part of the Setting page template.
jest.mock("../../../tsrc/components/NavigationGuard", () => ({
  NavigationGuard: (_: NavigationGuardProps) => {
    return <div />;
  },
}));

const mockGetFacetsFromServer = jest.spyOn(
  FacetedSearchSettingsModule,
  "getFacetsFromServer",
);
const mockBatchDelete = jest.spyOn(FacetedSearchSettingsModule, "batchDelete");
const mockBatchUpdateOrAdd = jest.spyOn(
  FacetedSearchSettingsModule,
  "batchUpdateOrAdd",
);

const getFacetsPromise = mockGetFacetsFromServer.mockImplementation(() =>
  Promise.resolve(mockFacets),
);

describe("<FacetedSearchSettingsPage />", () => {
  const facetedSearchSettingStrings =
    languageStrings.settings.searching.facetedsearchsetting;
  let page: RenderResult;
  beforeEach(async () => {
    page = render(<FacetedSearchSettingsPage updateTemplate={jest.fn()} />);
    await act(async () => {
      await getFacetsPromise;
    });
  });

  const getSaveButton = () => getMuiButtonByText(page.container, "Save");

  const getAllClassifications = (): HTMLElement[] =>
    Array.from(page.container.querySelectorAll<HTMLElement>("li[draggable]"));

  const getClassification = (facetName: string): HTMLElement =>
    pipe(
      getAllClassifications().filter((c) => queryByText(c, facetName) !== null),
      A.head,
      O.getOrElse<HTMLElement>(() => {
        throw new Error(`Failed to find classification ${facetName}`);
      }),
    );

  const getEditButton = (name: string): HTMLElement =>
    getByLabelText(getClassification(name), facetedSearchSettingStrings.edit);

  const getDeleteButton = (name: string): HTMLElement =>
    getByLabelText(getClassification(name), facetedSearchSettingStrings.delete);

  const openDialog = async (name?: string) => {
    await userEvent.click(
      name
        ? getEditButton(name)
        : page.getByLabelText(facetedSearchSettingStrings.add),
    );
    return screen.getByRole("dialog");
  };

  const deleteClassification = async (name: string) => {
    await userEvent.click(getDeleteButton(name));
  };

  const updateClassification = async (name: string, updatedName: string) => {
    const dialog = await openDialog(name);
    const nameInput = getByDisplayValue(dialog, name);
    await userEvent.clear(nameInput);
    await userEvent.type(nameInput, updatedName);
    await userEvent.click(
      getMuiButtonByText(dialog, languageStrings.common.action.ok),
    );
  };

  const addClassification = async (name: string) => {
    const dialog = await openDialog();
    const [nameInput, schemaInput] = getAllByRole(dialog, "textbox"); // Only two inputs in the dialog are "textbox".
    await userEvent.type(nameInput, name);

    // As the input for Schema node is disabled, use 'fireEvent.change`. Alternatively, we can
    // click the Schema node selector and select a node;
    fireEvent.change(schemaInput, { target: { value: "item/year" } });

    await userEvent.click(
      getMuiButtonByText(dialog, languageStrings.common.action.add),
    );
  };

  const makeChanges = async () => {
    await deleteClassification(mockFacets[0].name);
    await addClassification("new classification");
    await userEvent.click(getSaveButton());
  };

  it("should fetch facets", () => {
    expect(
      FacetedSearchSettingsModule.getFacetsFromServer,
    ).toHaveBeenCalledTimes(1);
  });

  it("should display a list of facets", () => {
    expect(getAllClassifications()).toHaveLength(3);
  });

  it("should show a dialog when an Edit button is clicked", async () => {
    await openDialog(mockFacets[0].name);
    expect(
      page.queryByText(facetedSearchSettingStrings.edit),
    ).toBeInTheDocument();
  });

  it("should show a dialog when the Add button is clicked", async () => {
    await openDialog();
    expect(
      page.queryByText(facetedSearchSettingStrings.add),
    ).toBeInTheDocument();
  });

  it("should remove a facet from the list and enable the Save button when a delete button is clicked", async () => {
    await deleteClassification(mockFacets[0].name);
    expect(getAllClassifications()).toHaveLength(2);
    expect(getSaveButton()).toBeEnabled();
  });

  it("should show the updated facet and enable the Save button when a facet is edited through the dialog", async () => {
    const name = mockFacets[0].name;
    await updateClassification(name, "updated name");
    expect(getAllClassifications()).toHaveLength(3);
    expect(getClassification("updated name")).toBeInTheDocument();
    expect(getSaveButton()).toBeEnabled();
  });

  it("should add a facet to the list and enable the Save button when a new facet is added through the dialog", async () => {
    const name = "new item";
    await addClassification(name);
    expect(getAllClassifications()).toHaveLength(4);
    expect(getClassification(name)).toBeInTheDocument();
    expect(getSaveButton()).toBeEnabled();
  });

  it("should display snackbar if no error messages are returned", async () => {
    mockBatchDelete.mockResolvedValueOnce([]);
    mockBatchUpdateOrAdd.mockResolvedValueOnce([]);
    await makeChanges();
    expect(
      screen.getByText(languageStrings.common.result.success),
    ).toBeInTheDocument();
  });

  it("should display message dialog if error messages are returned", async () => {
    const failedToDelete = "Failed to delete";
    const failedToUpdate = "Failed to update";
    mockBatchDelete.mockResolvedValueOnce([failedToDelete]);
    mockBatchUpdateOrAdd.mockResolvedValueOnce([failedToUpdate]);

    await makeChanges();
    const messageDialog = screen.getByRole("dialog");
    expect(getByText(messageDialog, failedToDelete)).toBeInTheDocument();
    expect(getByText(messageDialog, failedToUpdate)).toBeInTheDocument();
  });

  it("should update order indexes when facets are reordered", () => {
    // Given the mocked facets, reorder the second one and third one.
    const reorderFacets = FacetedSearchSettingsModule.reorder(
      mockFacetsWithFlags,
      1,
      2,
    );
    // The second facet's order index should be changed from 1 to 2.
    expect(reorderFacets.find((f) => f.id === 2)!.orderIndex).toBe(2);
    // The third facet's order index should be changed from 2 to 1.
    expect(reorderFacets.find((f) => f.id === 3)!.orderIndex).toBe(1);
    // The first facet's order index keeps unchanged.
    expect(reorderFacets.find((f) => f.id === 1)!.orderIndex).toBe(0);
  });

  describe("when remove a facet", () => {
    // Add a new mock facet which does not have an ID.
    const newFacet: FacetedSearchClassificationWithFlags = {
      name: "mocked facet4",
      schemaNode: "item/age",
      maxResults: 4,
      orderIndex: 3,
      updated: false,
      deleted: false,
    };
    mockFacetsWithFlags.push(newFacet);
    it("should keep this facet in state if it has an ID", () => {
      const facetToRemove = mockFacetsWithFlags[1];
      // Remove the second facet which has an ID.
      const updatedFacets = FacetedSearchSettingsModule.removeFacetFromList(
        mockFacetsWithFlags,
        facetToRemove.orderIndex,
      );
      // The total number of facets stored in state should keep 4.
      expect(updatedFacets).toHaveLength(4);
      // The first facet's order index keeps 0.
      expect(updatedFacets.find((f) => f.id === 1)!.orderIndex).toBe(0);
      // The third facet's order index is 1 now.
      expect(updatedFacets.find((f) => f.id === 3)!.orderIndex).toBe(1);
      // The fourth facet's order index is updated to 2 although it does not have an ID.
      expect(updatedFacets.find((f) => !f.id)!.orderIndex).toBe(2);
    });
    it("should remove this facet from state if it does not have an ID", () => {
      // Remove the last one.
      const updatedFacets = FacetedSearchSettingsModule.removeFacetFromList(
        mockFacetsWithFlags,
        newFacet.orderIndex,
      );
      // The total number of facets stored in state should change to 3.
      expect(updatedFacets).toHaveLength(3);
      // None of the first three facets should be updated.
      expect(updatedFacets.filter((f) => f.updated)).toHaveLength(0);
    });
  });
});

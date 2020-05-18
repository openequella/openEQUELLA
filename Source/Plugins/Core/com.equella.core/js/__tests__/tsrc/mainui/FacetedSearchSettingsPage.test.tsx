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
import * as React from "react";
import { mount, ReactWrapper } from "enzyme";
import FacetedSearchSettingsPage from "../../../tsrc/settings/Search/facetedsearch/FacetedSearchSettingsPage";
import * as FacetedSearchSettingsModule from "../../../tsrc/settings/Search/facetedsearch/FacetedSearchSettingsModule";
import { NavigationGuardProps } from "../../../tsrc/components/NavigationGuard";
import { act } from "react-dom/test-utils";
import FacetDialog from "../../../tsrc/settings/Search/facetedsearch/FacetDialog";
import MessageInfo from "../../../tsrc/components/MessageInfo";
import MessageDialog from "../../../tsrc/components/MessageDialog";
import { Draggable } from "react-beautiful-dnd";

const mockFacets: FacetedSearchSettingsModule.Facet[] = [
  {
    id: 1,
    name: "mocked facet1",
    schemaNode: "item/name",
    maxResults: 1,
    orderIndex: 1,
  },
  {
    id: 2,
    name: "mocked facet3",
    schemaNode: "item/age",
    maxResults: 2,
    orderIndex: 2,
  },
];

// This test does not need NavigationGuard which is part of the Setting page template.
jest.mock("../../../tsrc/components/NavigationGuard", () => ({
  NavigationGuard: (props: NavigationGuardProps) => {
    return <div />;
  },
}));

const mockGetFacetsFromServer = jest.spyOn(
  FacetedSearchSettingsModule,
  "getFacetsFromServer"
);
const mockBatchDelete = jest.spyOn(FacetedSearchSettingsModule, "batchDelete");
const mockBatchUpdateOrAdd = jest.spyOn(
  FacetedSearchSettingsModule,
  "batchUpdateOrAdd"
);

describe("<FacetedSearchSettingsPage />", () => {
  enum Actions {
    Edit,
    Add,
  }
  let component: ReactWrapper;
  beforeEach(async () => {
    const getFacetsPromise = mockGetFacetsFromServer.mockImplementation(() =>
      Promise.resolve(mockFacets)
    );
    component = mount(<FacetedSearchSettingsPage updateTemplate={jest.fn()} />);
    await act(async () => {
      await getFacetsPromise;
    });
    component.update();
  });
  afterEach(() => jest.clearAllMocks());

  const getListItems = () => component.find(Draggable);
  const getSaveButton = () => component.find("#_saveButton").hostNodes();
  const deleteFacet = () => {
    const deleteButton = getListItems().at(0).find("button").at(1);
    deleteButton.simulate("click");
  };
  const updateFacet = (action: Actions) => {
    const fields = component.find("input");
    fields.at(0).simulate("change", {
      target: { value: action === Actions.Add ? "new facet" : "updated facet" },
    });
    fields.at(1).simulate("change", { target: { value: "item/name/first" } });
    const button = component
      .findWhere(
        (node) =>
          node.text() === (action === Actions.Add ? "Add" : "OK") &&
          node.type() === "button"
      )
      .hostNodes();
    button.simulate("click");
  };

  const openDialog = (action: Actions) => {
    let button: ReactWrapper;
    if (action === Actions.Add) {
      button = component.find(".MuiCardActions-root button");
    } else {
      button = getListItems().at(0).find("button").at(0);
    }
    button.simulate("click");
  };

  it("should fetch facets", () => {
    expect(
      FacetedSearchSettingsModule.getFacetsFromServer
    ).toHaveBeenCalledTimes(1);
  });

  it("should display a list of facets", () => {
    expect(getListItems()).toHaveLength(2);
  });

  it("should show a dialog when an Edit button is clicked", async () => {
    openDialog(Actions.Edit);
    expect(component.find(FacetDialog).props().open).toBeTruthy();
  });

  it("should show a dialog when the Add button is clicked", async () => {
    openDialog(Actions.Add);
    expect(component.find(FacetDialog).props().open).toBeTruthy();
  });

  it("should remove a facet from the list and enable the Save button when a delete button is clicked", () => {
    deleteFacet();
    const items = getListItems();
    expect(items).toHaveLength(1);
    expect(getSaveButton().props().disabled).toBeFalsy();
  });

  it("should show the updated facet and enable the Save button when a facet is edited through the dialog", () => {
    openDialog(Actions.Edit);
    updateFacet(Actions.Edit);
    const items = getListItems();
    expect(items).toHaveLength(2);
    expect(items.at(0).text()).toContain("updated facet");
    expect(getSaveButton().props().disabled).toBeFalsy();
  });

  it("should add a facet to the list and enable the Save button when a new facet is added through the dialog", () => {
    openDialog(Actions.Add);
    updateFacet(Actions.Add);
    const items = getListItems();
    expect(items).toHaveLength(3);
    expect(items.at(2).text()).toContain("new facet");
    expect(getSaveButton().props().disabled).toBeFalsy();
  });

  describe("when the Save button is enabled and clicked", () => {
    const makeChanges = () => {
      deleteFacet();
      openDialog(Actions.Edit);
      updateFacet(Actions.Edit);
    };
    const save = async (errorsReturned: boolean) => {
      const deletePromise = mockBatchDelete.mockResolvedValueOnce(
        errorsReturned ? ["Failed to delete"] : []
      );
      const updatePromise = mockBatchUpdateOrAdd.mockResolvedValueOnce(
        errorsReturned ? ["Failed to update"] : []
      );
      makeChanges();
      getSaveButton().simulate("click");
      await act(async () => {
        await updatePromise;
        await deletePromise;
      });
    };

    it("should display snackbar if no error messages are returned", async () => {
      await save(false);
      component.update();
      const snackbar = component.find(MessageInfo);
      expect(snackbar.props().open).toBeTruthy();
    });

    it("should display message dialog if error messages are returned", async () => {
      await save(true);
      component.update();
      const messageDialog = component.find(MessageDialog);
      expect(messageDialog.props().open).toBeTruthy();
    });
  });
});

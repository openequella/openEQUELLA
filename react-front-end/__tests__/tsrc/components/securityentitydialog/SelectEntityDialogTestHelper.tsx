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
import userEvent from "@testing-library/user-event";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { getMuiButtonByText, queryMuiTextField } from "../../MuiQueries";
import {
  findByText,
  getByText,
  RenderResult,
  waitFor,
} from "@testing-library/react";
import { selectEntitiesInOneClickMode } from "../securityentitysearch/BaseSearchTestHelper";
import {
  findRolesByIds,
  searchRoles,
} from "../../../../__mocks__/RoleModule.mock";
import {
  findGroupsByIds,
  searchGroups,
} from "../../../../__mocks__/GroupModule.mock";
import * as GroupModule from "../../../../tsrc/modules/GroupModule";
import * as RoleModule from "../../../../tsrc/modules/RoleModule";

const {
  cancel: cancelLabel,
  ok: okLabel,
  removeAll: removeAllLabel,
} = languageStrings.common.action;

/**
 * Provides a centralised place to mock all roles and groups API functions.
 */
export const mockRoleAndGroupApis = () => {
  jest
    .spyOn(GroupModule, "findGroupsByIds")
    .mockImplementation(findGroupsByIds);
  jest.spyOn(GroupModule, "searchGroups").mockImplementation(searchGroups);
  jest.spyOn(RoleModule, "findRolesByIds").mockImplementation(findRolesByIds);
  jest.spyOn(RoleModule, "searchRoles").mockImplementation(searchRoles);
};

/**
 * Helper function to wait for the dialog to render.
 */
export const waitForEntityDialogToRender = async (result: RenderResult) => {
  const dialog = result.getByRole("dialog");
  // Wait for the confirm button to be enabled.
  await waitFor(() => {
    const okButton = getMuiButtonByText(dialog, okLabel);
    expect(okButton).toBeEnabled();
  });
};

/**
 * Generic helper function to do the steps of submitting a search in the `SelectSecurityEntityDialog`
 * component. It needs a `queryName` to locate the component.
 *
 * @param dialog the dialog which contains the search component
 * @param queryName the label value displayed in the query bar, and here use it to locate the search bar
 * @param queryValue the value to put in the query field before pressing enter
 */
export const doSearch = async (
  dialog: HTMLElement,
  queryName: string,
  queryValue: string,
) => {
  const queryField = queryMuiTextField(dialog, queryName);
  if (!queryField) {
    throw new Error("Unable to find query field!");
  }
  await userEvent.type(queryField, `${queryValue}{enter}`);
};

/**
 * Executes a search for entities with the `searchFor` query,
 * after which it will attempt to select the entity identified by selectEntityName.
 *
 * @param dialog The dialog HTMLElement which contains the search component.
 * @param searchFor A keyword will be used to do a search action.
 * @param selectEntityName The name of the entity which will be selected after search action.
 * @param doSearch A function that will trigger the search action.
 * */
export const searchAndSelect = async (
  dialog: HTMLElement,
  searchFor: string,
  selectEntityName: string,
  doSearch: (dialog: HTMLElement, queryValue: string) => Promise<void>,
): Promise<void> => {
  // Attempt search for a specific entity
  await doSearch(dialog, searchFor);
  // Wait for the results, and then click our entity of interest
  await selectEntitiesInOneClickMode(dialog, [selectEntityName]);
};

/**
 * Helper function to mock click the delete icon for a given entity name (located by name).
 *
 * @param dialog The HTML element which contains the delete icon.
 * @param name The role name of the entry where the delete icon is located.
 */
export const clickDeleteIconForEntity = async (
  dialog: HTMLElement,
  name: string,
): Promise<void> => {
  const deleteIcon = (await findByText(dialog, name)).parentElement
    ?.parentElement?.nextElementSibling?.firstElementChild;
  if (!deleteIcon) {
    throw Error(`Can't find delete icon for role with name: ${name}`);
  }
  await userEvent.click(deleteIcon);
};

/**
 * Click ok button in the dialog.
 */
export const clickOkButton = async (dialog: HTMLElement) => {
  const confirmBtn = getByText(dialog, okLabel);
  if (!confirmBtn) {
    throw new Error("Failed to find confirm button.");
  }
  await userEvent.click(confirmBtn);
};

/**
 * Click cancel button in the dialog.
 */
export const clickCancelButton = (dialog: HTMLElement) =>
  userEvent.click(getByText(dialog, cancelLabel));

/**
 * Click remove all button in the dialog.
 */
export const clickRemoveAllButton = (dialog: HTMLElement) =>
  userEvent.click(getByText(dialog, removeAllLabel));

/**
 * Test functionality of removing entity in the dialog.
 *
 * @param render A render function which takes a mock function to render the dialog.
 *               The mock function is used to get the final result when dialog is closed.
 * @param entityName The name of the entity users want to remove.
 */
export const testRemoveEntity = async (
  render: (onClose: jest.Mock) => Promise<RenderResult>,
  entityName: string,
) => {
  const onClose = jest.fn();
  const { getByRole } = await render(onClose);

  const dialog = getByRole("dialog");

  await clickDeleteIconForEntity(dialog, entityName);
  await clickOkButton(dialog);

  return onClose.mock.lastCall[0];
};

/**
 * Test functionality of the remove all button in the dialog.
 *
 * @param render A render function which takes a mock function to render the dialog.
 *               The mock function is used to get the final result when dialog is closed.
 */
export const testRemoveAllAsync = async (
  render: (onClose: jest.Mock) => Promise<RenderResult>,
) => {
  const onClose = jest.fn();
  const { getByRole } = await render(onClose);

  const dialog = getByRole("dialog");
  await clickRemoveAllButton(dialog);
  await clickOkButton(dialog);

  return onClose.mock.lastCall[0];
};

/**
 * Variety version of `testRemoveAllAsync`.
 * The only difference is it accepts a non-async render function as its param.
 *
 * @param render An async render function which takes a mock function to render the dialog.
 */
export const testRemoveAll = async (
  render: (onClose: jest.Mock) => Promise<RenderResult>,
) => await testRemoveAllAsync((onClose) => render(onClose));

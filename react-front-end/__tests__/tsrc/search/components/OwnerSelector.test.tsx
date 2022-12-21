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
import "@testing-library/jest-dom/extend-expect";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import * as UserModuleMock from "../../../../__mocks__/UserModule.mock";
import * as UserSearchMock from "../../../../__mocks__/UserSearch.mock";
import OwnerSelector from "../../../../tsrc/search/components/OwnerSelector";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { queryMuiButtonByText } from "../../MuiQueries";
import {
  clearSelection,
  clickSelect,
  getSelectButton,
  selectUser,
} from "./OwnerSelectTestHelpers";

describe("<OwnerSelector/>", () => {
  const testUser = UserModuleMock.users[0];

  it("should show the select button when no user selected", () => {
    const { container } = render(
      <OwnerSelector onClearSelect={jest.fn()} onSelect={jest.fn()} />
    );
    expect(getSelectButton(container)).toBeInTheDocument();
  });

  it("should not show the select button when a user has been selected, but instead show the user details", () => {
    const { container, queryByText } = render(
      <OwnerSelector
        onClearSelect={jest.fn()}
        onSelect={jest.fn()}
        value={testUser}
      />
    );
    expect(getSelectButton(container)).not.toBeInTheDocument();
    expect(queryByText(testUser.username)).toBeInTheDocument();
  });

  it("should trigger the clear callback when the clear user button is clicked", async () => {
    const clearCallback = jest.fn();
    render(
      <OwnerSelector
        onClearSelect={clearCallback}
        onSelect={jest.fn()}
        value={testUser}
      />
    );

    await clearSelection();

    expect(clearCallback).toHaveBeenCalled();
  });

  it("should display the select user dialog when select is clicked", async () => {
    const { container, queryByText } = render(
      <OwnerSelector onClearSelect={jest.fn()} onSelect={jest.fn()} />
    );

    await clickSelect(container);

    expect(
      queryByText(languageStrings.searchpage.filterOwner.selectTitle)
    ).toBeInTheDocument();
  });

  it("should trigger the callback with the correct details of the selected user", async () => {
    const onSelectCallback = jest.fn();
    const { container } = render(
      <OwnerSelector
        onClearSelect={jest.fn()}
        onSelect={onSelectCallback}
        userListProvider={UserSearchMock.userDetailsProvider}
      />
    );

    await selectUser(container, testUser.username);

    // Finally, was the callback called with the correct user details
    expect(onSelectCallback).toHaveBeenCalledWith(testUser);
  });

  it("should not call the onSelect callback if cancel is clicked", async () => {
    const onSelectCallback = jest.fn();
    const { container, getByRole } = render(
      <OwnerSelector onClearSelect={jest.fn()} onSelect={onSelectCallback} />
    );

    await clickSelect(container);
    const dialogCancelButton = queryMuiButtonByText(
      getByRole("dialog"),
      languageStrings.common.action.cancel
    );
    if (!dialogCancelButton) {
      throw new Error(
        "Unable to find 'cancel' button in the user select dialog"
      );
    }
    await userEvent.click(dialogCancelButton);

    expect(onSelectCallback).not.toHaveBeenCalled();
  });
});

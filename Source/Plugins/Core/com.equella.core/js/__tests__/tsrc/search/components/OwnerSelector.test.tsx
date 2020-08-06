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
import {
  fireEvent,
  queryByText,
  render,
  waitFor,
} from "@testing-library/react";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import OwnerSelector from "../../../../tsrc/search/components/OwnerSelector";
import * as UserSearchMock from "../../../../__mocks__/UserSearch.mock";
import { doSearch } from "../../components/UserSearchTestHelpers";

import "@testing-library/jest-dom/extend-expect";

const queryMuiButtonByText = (container: HTMLElement, text: string) =>
  queryByText(
    container,
    (content: string, element: HTMLElement) =>
      content === text && (element.parentElement?.matches("button") as boolean)
  );

describe("<OwnerSelector/>", () => {
  const testUser = UserSearchMock.users[0];
  const getSelectButton = (container: HTMLElement) =>
    queryMuiButtonByText(container, languageStrings.common.action.select);

  // Helper user action abstraction function
  const clickSelect = (container: HTMLElement) => {
    const selectButton = getSelectButton(container);
    if (!selectButton) {
      throw new Error(
        "Was expecting the 'select' button to be available, but no. :/"
      );
    }
    fireEvent.click(selectButton);
  };

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

  it("should trigger the clear callback when the clear user button is clicked", () => {
    const clearCallback = jest.fn();
    const { getByLabelText } = render(
      <OwnerSelector
        onClearSelect={clearCallback}
        onSelect={jest.fn()}
        value={testUser}
      />
    );

    fireEvent.click(
      getByLabelText(languageStrings.searchpage.filterOwner.clear)
    );

    expect(clearCallback).toHaveBeenCalled();
  });

  it("should display the select user dialog when select is clicked", () => {
    const { container, queryByText } = render(
      <OwnerSelector onClearSelect={jest.fn()} onSelect={jest.fn()} />
    );

    clickSelect(container);

    expect(
      queryByText(languageStrings.searchpage.filterOwner.selectTitle)
    ).toBeInTheDocument();
  });

  it("should trigger the callback with the correct details of the selected user", async () => {
    const onSelectCallback = jest.fn();
    const { container, getByText, getByRole } = render(
      <OwnerSelector
        onClearSelect={jest.fn()}
        onSelect={onSelectCallback}
        userListProvider={UserSearchMock.userDetailsProvider}
      />
    );

    clickSelect(container);
    doSearch(testUser.username);
    // Wait for mock latency
    const findUsername = () => getByText(testUser.username);
    await waitFor(findUsername);
    // Click on a user
    fireEvent.click(findUsername());
    // And click select
    const dialogSelectButton = getSelectButton(getByRole("dialog"));
    if (!dialogSelectButton) {
      throw new Error(
        "Unable to find the 'select' button in the user select dialog."
      );
    }
    fireEvent.click(dialogSelectButton);

    // Finally, was the callback called with the correct user details
    expect(onSelectCallback).toHaveBeenCalledWith(testUser);
  });

  it("should not call the onSelect callback if cancel is clicked", () => {
    const onSelectCallback = jest.fn();
    const { container, getByRole } = render(
      <OwnerSelector onClearSelect={jest.fn()} onSelect={onSelectCallback} />
    );

    clickSelect(container);
    const dialogCancelButton = queryMuiButtonByText(
      getByRole("dialog"),
      languageStrings.common.action.cancel
    );
    if (!dialogCancelButton) {
      throw new Error(
        "Unable to find 'cancel' button in the user select dialog"
      );
    }
    fireEvent.click(dialogCancelButton);

    expect(onSelectCallback).not.toHaveBeenCalled();
  });
});

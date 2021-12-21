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
import { queryMuiButtonByText } from "../../MuiQueries";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { doSearch } from "../../components/UserSearchTestHelpers";

export const getSelectButton = (container: HTMLElement) =>
  queryMuiButtonByText(container, languageStrings.common.action.select);

// Helper user action abstraction function
export const clickSelect = (container: HTMLElement) => {
  const selectButton = getSelectButton(container);
  if (!selectButton) {
    throw new Error(
      "Was expecting the 'select' button to be available, but no. :/"
    );
  }
  fireEvent.click(selectButton);
};

export const selectUser = async (container: HTMLElement, username: string) => {
  clickSelect(container);
  doSearch(screen.getByRole("dialog"), username);
  // Wait for mock latency
  const findUsername = () => screen.getByText(username);
  await waitFor(findUsername);
  // Click on a user
  fireEvent.click(findUsername());
  // And click select
  const dialogSelectButton = getSelectButton(screen.getByRole("dialog"));
  if (!dialogSelectButton) {
    throw new Error(
      "Unable to find the 'select' button in the user select dialog."
    );
  }
  fireEvent.click(dialogSelectButton);
};

export const clearSelection = () =>
  fireEvent.click(
    screen.getByLabelText(languageStrings.searchpage.filterOwner.clear)
  );

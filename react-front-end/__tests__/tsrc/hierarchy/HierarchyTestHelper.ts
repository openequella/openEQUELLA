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
import { getByLabelText, getByText, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { languageStrings } from "../../../tsrc/util/langstrings";
import "@testing-library/jest-dom";

const { addKeyResource: addKeyResourceText } = languageStrings.hierarchy;

/**
 * Helper function to mock select a Hierarchy summary node in the tree view.
 * @param container The element which contains the tree view.
 * @param name The name of the Hierarchy topic summary.
 */
export const selectHierarchy = async (container: HTMLElement, name: string) => {
  await userEvent.click(getByText(container, name));

  // Wait for the skeleton to disappear.
  await waitFor(() => {
    const skeleton = container.querySelector('span[class*="MuiSkeleton-root"]');
    if (skeleton) {
      throw new Error("Skeleton is still present");
    }
  });
};

/**
 * Helper function to mock click the add key resource button.
 */
export const clickAddKeyResource = async (
  container: HTMLElement,
  itemUuid: string,
  itemVersion: number,
) => {
  const item = container.querySelector(
    `li[data-item-id="${itemUuid}"][data-item-version="${itemVersion}"]`,
  );
  if (!item) {
    throw new Error(
      "Item not found: " + itemUuid + " with version: " + itemVersion,
    );
  }

  await userEvent.click(
    getByLabelText(item as HTMLElement, addKeyResourceText),
  );
};

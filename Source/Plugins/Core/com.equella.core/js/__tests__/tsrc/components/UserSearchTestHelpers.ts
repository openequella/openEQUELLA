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
// Helper function to execute a search (assuming rendered component)
import { fireEvent, screen } from "@testing-library/react";
import { languageStrings } from "../../../tsrc/util/langstrings";

/**
 * Helper function to do the steps of entering a submitting a search in the <UserSearch/>
 * component.
 *
 * @param queryValue the value to put in the query field before pressing enter
 */
export const doSearch = (queryValue: string) => {
  const queryField = screen
    .getByText(languageStrings.userSearchComponent.queryFieldLabel)
    .parentElement?.querySelector("input");
  if (!queryField) {
    throw new Error("Unable to find query field!");
  }

  fireEvent.change(queryField, { target: { value: queryValue } });
  fireEvent.keyDown(queryField, { key: "Enter", code: "Enter" });
};

/**
 * Helper function to assist in finding the list of users after a search.
 *
 * @param container a root container within which <UserSearch/> exists
 */
export const getUserList = (container: HTMLElement): Element | null =>
  container.querySelector("#UserSearch-UserList");

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
import * as OEQ from "@openequella/rest-api-client";
import { RenderResult } from "@testing-library/react";
import {
  defaultBaseSearchProps,
  generateDefaultCheckboxModeProps,
  searchAndSelect,
} from "./BaseSearchTestHelper";
import {
  searchUser,
  renderUserSearch,
  findUserFromMockData,
} from "./UserSearchTestHelpler";

describe("<UserSearch/>", () => {
  const defaultCheckboxModeProps =
    generateDefaultCheckboxModeProps<OEQ.UserQuery.UserDetails>();
  // do search and select user
  const searchAndSelectUser = (
    renderResult: RenderResult,
    searchFor: string,
    selectEntityName: string,
    onChange = jest.fn(),
  ) =>
    searchAndSelect(
      renderResult,
      searchFor,
      selectEntityName,
      onChange,
      searchUser,
    );

  it("can search and select", async () => {
    const queryName = "user100";
    // Prepare test values - aligning with mock data and function
    const selectUser = findUserFromMockData(queryName);
    // The expected result for the first arg of the first call of onChange function.
    const expectedSelections = new Set([selectUser]);
    const onChange = jest.fn();

    const renderResult = await renderUserSearch({
      ...defaultBaseSearchProps,
      mode: {
        ...defaultCheckboxModeProps,
        onChange,
      },
    });

    expect(
      await searchAndSelectUser(
        renderResult,
        queryName,
        selectUser.username,
        onChange,
      ),
    ).toEqual(expectedSelections);
  });
});

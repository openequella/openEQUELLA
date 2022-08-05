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
import * as React from "react";
import { render, screen } from "@testing-library/react";
import { AppContext } from "../../../../tsrc/mainui/App";
import { guestUser } from "../../../../tsrc/modules/UserModule";
import { MyResourcesSelector } from "../../../../tsrc/search/components/MyResourcesSelector";

describe("<MyResourcesSelector />", () => {
  it("drops the option of Scrapbook if access to Scrapbook is not enabled", () => {
    const { getByLabelText } = render(
      <AppContext.Provider
        value={{
          refreshUser: jest.fn(),
          appErrorHandler: jest.fn(),
          currentUser: guestUser,
        }}
      >
        <MyResourcesSelector value="Published" />
        );
      </AppContext.Provider>
    );

    // Click the caret to show the options.
    userEvent.click(getByLabelText("Open"));
    expect(screen.queryByText("Scrapbook", { selector: "li" })).toBeNull();
  });
});

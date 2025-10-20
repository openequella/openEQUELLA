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
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import {
  emptyDashboardDetails,
  mockPortlets,
  privateSearchPortlet,
} from "../../../__mocks__/Dashboard.mock";
import { systemUser } from "../../../__mocks__/UserModule.mock";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { renderDashboardPage } from "./DashboardPageTestHelper";
import * as DashboardModule from "../../../tsrc/modules/DashboardModule";

const {
  nonSystemUser: { hintForOeq: hintForOeqText },
} = languageStrings.dashboard.welcomeDesc;
const { edit: editText } = languageStrings.common.action;

const mockGetDashboardDetails = jest.spyOn(
  DashboardModule,
  "getDashboardDetails",
);

const mockEditPortlet = jest
  .spyOn(DashboardModule, "editPortlet")
  .mockResolvedValue("");

describe("<DashboardPage/>", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it.each([
    ["when portlets are empty", []],
    ["when portlets are not empty", mockPortlets],
  ])(
    "shows a special welcome message for system user %s",
    async (_, portlets) => {
      const partOfWelcomeMessage =
        "To modify or delete Dashboard portlets seen by non-admin openEQUELLA users";
      mockGetDashboardDetails.mockResolvedValueOnce({ portlets });

      const { getByText } = await renderDashboardPage(systemUser);
      expect(
        getByText(partOfWelcomeMessage, { exact: false }),
      ).toBeInTheDocument();
    },
  );

  it("shows welcome message if no portlets is configured", async () => {
    mockGetDashboardDetails.mockResolvedValueOnce(emptyDashboardDetails);

    const { getByText } = await renderDashboardPage();
    expect(getByText(hintForOeqText)).toBeInTheDocument();
  });

  it("displays portlet container when portlets are configured", async () => {
    mockGetDashboardDetails.mockResolvedValueOnce({ portlets: mockPortlets });
    const { container } = await renderDashboardPage();

    const portletContainer = container.querySelector(
      "#dashboard-portlet-container",
    );
    expect(portletContainer).toBeInTheDocument();
  });

  it("performs the initial API request", async () => {
    mockGetDashboardDetails.mockResolvedValueOnce({ portlets: mockPortlets });
    await renderDashboardPage();

    expect(mockGetDashboardDetails).toHaveBeenCalledTimes(1);
  });

  it("supports editing the private portlets", async () => {
    mockGetDashboardDetails.mockResolvedValueOnce({
      portlets: [privateSearchPortlet],
    });
    const { getByRole } = await renderDashboardPage();
    await userEvent.click(getByRole("button", { name: editText }));

    expect(mockEditPortlet).toHaveBeenCalledWith(
      privateSearchPortlet.commonDetails.uuid,
    );
  });
});

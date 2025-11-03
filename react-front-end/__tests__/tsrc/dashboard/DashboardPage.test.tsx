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
import { waitForElementToBeRemoved } from "@testing-library/react";
import { sprintf } from "sprintf-js";
import {
  basicPortlet,
  emptyDashboardDetails,
  minimisedPortlet,
  mockPortlets,
  privateSearchPortlet,
  publicHtmlPortlet,
} from "../../../__mocks__/Dashboard.mock";
import { systemUser } from "../../../__mocks__/UserModule.mock";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { clickButton } from "../MuiTestHelpers";
import {
  getPortletContent,
  mockDashboardPageApis,
  openConfirmDialog,
  queryPortletContent,
  renderDashboardPage,
} from "./DashboardPageTestHelper";
import * as E from "fp-ts/Either";
import * as OEQ from "@openequella/rest-api-client";

const {
  nonSystemUser: { hintForOeq: hintForOeqText, imageAlt: imageAltText },
} = languageStrings.dashboard.welcomeDesc;
const {
  maximise: maximiseText,
  minimise: minimiseText,
  close: closeText,
  delete: deleteText,
  cancel: cancelText,
  ok: okText,
} = languageStrings.common.action;
const {
  close: closeAlertTitle,
  delete: deleteAlertTitle,
  closeAlert,
  deleteAlert,
} = languageStrings.dashboard.portlets.dialog;

const {
  mockGetDashboardDetails,
  mockUpdatePortletPreference,
  mockDeletePortlet,
  mockGetCreatePortletAcl,
} = mockDashboardPageApis();

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

  it("shows welcome message if no portlets are configured for non-system users", async () => {
    mockGetDashboardDetails.mockResolvedValueOnce(emptyDashboardDetails);
    mockGetCreatePortletAcl.mockResolvedValueOnce(E.right(false));

    const { getByText } = await renderDashboardPage();
    expect(getByText(hintForOeqText)).toBeInTheDocument();
  });

  it("shows an enhanced welcome message if no portlets are configured for non-system users with CREATE_PORTLET permission", async () => {
    mockGetDashboardDetails.mockResolvedValueOnce(emptyDashboardDetails);

    const { getByText, getByAltText } = await renderDashboardPage();
    expect(
      getByText("as shown in the example below", { exact: false }),
    ).toBeInTheDocument();
    expect(getByAltText(imageAltText)).toBeInTheDocument();
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

  it("updates portlet preference when a user maximizes a portlet", async () => {
    mockGetDashboardDetails.mockResolvedValueOnce({
      portlets: [minimisedPortlet],
    });

    const { container } = await renderDashboardPage();
    const { uuid, isClosed, order, column } = minimisedPortlet.commonDetails;

    // A minimised portlet's content is not initially visible.
    expect(queryPortletContent(container, uuid)).not.toBeInTheDocument();

    await clickButton(container, maximiseText);

    expect(mockUpdatePortletPreference).toHaveBeenCalledWith(uuid, {
      isMinimised: false,
      isClosed,
      order,
      column,
    });
    expect(mockGetDashboardDetails).toHaveBeenCalledTimes(2);
    // After maximising, the content becomes visible.
    expect(getPortletContent(container, uuid)).toBeInTheDocument();
  });

  it("updates portlet preference when a user minimizes a portlet", async () => {
    mockGetDashboardDetails.mockResolvedValueOnce({ portlets: [basicPortlet] });

    const { container } = await renderDashboardPage();
    const { uuid, isClosed, order, column } = basicPortlet.commonDetails;

    // A maximised portlet's content is initially visible.
    expect(getPortletContent(container, uuid)).toBeInTheDocument();

    await clickButton(container, minimiseText);

    expect(mockUpdatePortletPreference).toHaveBeenCalledWith(
      basicPortlet.commonDetails.uuid,
      { isMinimised: true, isClosed, order, column },
    );
    expect(mockGetDashboardDetails).toHaveBeenCalledTimes(2);
    // After minimising, the content becomes hidden.
    expect(queryPortletContent(container, uuid)).not.toBeInTheDocument();
  });

  it.each([
    [
      closeText,
      publicHtmlPortlet,
      { title: closeAlertTitle, desc: closeAlert },
    ],
    [
      deleteText,
      privateSearchPortlet,
      { title: deleteAlertTitle, desc: deleteAlert },
    ],
  ])(
    "should open a confirmation dialog with expected title and description when user clicks on '%s' portlet button",
    async (buttonText, mockportlet, dialogDetails) => {
      mockGetDashboardDetails.mockResolvedValueOnce({
        portlets: [mockportlet],
      });

      const page = await renderDashboardPage();
      const { name } = mockportlet.commonDetails;
      const { title, desc } = dialogDetails;

      const dialog = await openConfirmDialog(page, buttonText);
      const dialogTitle = dialog.querySelector("#alert-dialog-title");
      const dialogDesc = dialog.querySelector("#alert-dialog-description");

      expect(dialog).toBeInTheDocument();
      expect(dialogTitle).toHaveTextContent(title);
      expect(dialogDesc).toHaveTextContent(sprintf(desc, name));
    },
  );

  it.each([
    [closeText, publicHtmlPortlet, mockUpdatePortletPreference],
    [deleteText, privateSearchPortlet, mockDeletePortlet],
  ])(
    "should dismiss the %s portlet confirmation dialog when a user clicks on 'Cancel' button",
    async (buttonText, mockportlet, mockApi) => {
      mockGetDashboardDetails.mockResolvedValueOnce({
        portlets: [mockportlet],
      });

      const page = await renderDashboardPage();

      const dialog = await openConfirmDialog(page, buttonText);
      await clickButton(dialog, cancelText);
      await waitForElementToBeRemoved(() => page.getByRole("dialog"));

      expect(page.queryByRole("dialog")).not.toBeInTheDocument();
      expect(mockApi).not.toHaveBeenCalled();
      expect(mockGetDashboardDetails).toHaveBeenCalledTimes(1);
    },
  );

  const { uuid, order, column, isMinimised } = publicHtmlPortlet.commonDetails;
  const closeArgs: [string, OEQ.Dashboard.PortletPreference] = [
    uuid,
    {
      order,
      column,
      isMinimised,
      isClosed: true,
    },
  ];
  const deleteArgs: [string] = [privateSearchPortlet.commonDetails.uuid];

  it.each([
    [closeText, mockUpdatePortletPreference, publicHtmlPortlet, closeArgs],
    [deleteText, mockDeletePortlet, privateSearchPortlet, deleteArgs],
  ])(
    "should execute the '%s' action with correct arguments and refresh the dashboard when 'Ok' is clicked",
    async (buttonText, mockApi, mockportlet, expectedArgs) => {
      mockGetDashboardDetails.mockResolvedValueOnce({
        portlets: [mockportlet],
      });

      const page = await renderDashboardPage();

      const dialog = await openConfirmDialog(page, buttonText);
      await clickButton(dialog, okText);

      expect(mockApi).toHaveBeenCalledWith(...expectedArgs);
      expect(mockGetDashboardDetails).toHaveBeenCalledTimes(2);
    },
  );
});

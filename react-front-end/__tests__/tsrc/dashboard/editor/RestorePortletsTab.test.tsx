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
import { composeStories } from "@storybook/react";
import { waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { getClosedPortletsRes } from "../../../../__mocks__/Dashboard.mock";
import * as stories from "../../../../__stories__/dashboard/editor/RestorePortletsTab.stories";
import { ClosedPortletsProvider } from "../../../../tsrc/dashboard/DashboardEditor";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  mockDashboardEditorApis,
  renderRestorePortletsTab,
} from "../DashboardEditorTestHelper";

const { WithClosedPortletsList, WithNoClosedPortlets, ErrorState } =
  composeStories(stories);
const { noClosedPortlets: noClosedPortletsLabel } =
  languageStrings.dashboard.editor.restorePortlet;
const { restore: restoreLabel } = languageStrings.common.action;

const { mockUpdatePortletPreference, mockGetClosedPortlets } =
  mockDashboardEditorApis();

const user = userEvent.setup();

const renderClosedPortletsList = async (element: React.ReactElement) => {
  const renderResult = renderRestorePortletsTab(element);

  const { getByTestId, getAllByRole } = renderResult;

  // Wait for closed portlets list to render
  await waitFor(() =>
    expect(getByTestId("closed-portlets-list")).toBeInTheDocument(),
  );

  const getRestoreButtons = () =>
    getAllByRole("button", { name: restoreLabel });

  const clickFirstPortletRestoreButton = async () => {
    const restoreButtons = getRestoreButtons();
    await user.click(restoreButtons[0]);
  };

  return { ...renderResult, clickFirstPortletRestoreButton, getRestoreButtons };
};

describe("<RestorePortletsTab />", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("shows an error text if `getClosedPortlets` API throws an error", async () => {
    const errorText = "Sample Error text";
    const { getByText } = renderRestorePortletsTab(<ErrorState />);

    await waitFor(() => expect(getByText(errorText)).toBeInTheDocument());
  });

  it("shows no closed portlets found text if none are available", async () => {
    const { getByText, queryByTestId } = renderRestorePortletsTab(
      <WithNoClosedPortlets />,
    );

    await waitFor(() =>
      expect(getByText(noClosedPortletsLabel)).toBeInTheDocument(),
    );
    expect(queryByTestId("closed-portlets-list")).not.toBeInTheDocument();
  });

  it("shows a list of closed portlets if they are available", async () => {
    const { getRestoreButtons, getByText } = await renderClosedPortletsList(
      <WithClosedPortletsList />,
    );
    const restoreButtons = getRestoreButtons();

    // The mock data contains a list of two closed portlets
    getClosedPortletsRes.forEach((portlet) => {
      expect(getByText(portlet.name)).toBeInTheDocument();
    });
    expect(restoreButtons).toHaveLength(getClosedPortletsRes.length);
  });

  it("restores portlet with correct preferences", async () => {
    const { mockRestorePortlet, clickFirstPortletRestoreButton } =
      await renderClosedPortletsList(<WithClosedPortletsList />);

    await clickFirstPortletRestoreButton();

    // The dashboardDetails being used in context mock data has 3 portlets in column 0 with orders [0, 1, 2]. So, next expected order for restored portlet is 3.
    expect(mockUpdatePortletPreference).toHaveBeenCalledWith(
      getClosedPortletsRes[0].uuid,
      {
        isClosed: false,
        isMinimised: false,
        column: 0,
        order: 3,
      },
    );
    expect(mockUpdatePortletPreference).toHaveBeenCalledTimes(1);
    expect(mockRestorePortlet).toHaveBeenCalledTimes(1);
    // performs optimistic state update instead of fetching updated closed portlets
    expect(mockGetClosedPortlets).not.toHaveBeenCalled();
  });

  it("fetches updated closed portlets if restoring portlet fails", async () => {
    mockUpdatePortletPreference.mockRejectedValueOnce({});

    const { mockRestorePortlet, clickFirstPortletRestoreButton } =
      await renderClosedPortletsList(
        <WithClosedPortletsList
          closedPortletsProvider={
            mockGetClosedPortlets as unknown as ClosedPortletsProvider
          }
        />,
      );

    await clickFirstPortletRestoreButton();

    expect(mockUpdatePortletPreference).toHaveBeenCalled();
    expect(mockRestorePortlet).not.toHaveBeenCalled();
    // Fetch updated list of closed portlets by calling `getClosedPortlets` API again if `updatePortletPreference` API throws error
    expect(mockGetClosedPortlets).toHaveBeenCalledTimes(2);
  });
});

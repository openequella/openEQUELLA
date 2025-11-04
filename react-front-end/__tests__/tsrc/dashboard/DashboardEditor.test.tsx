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
import { languageStrings } from "../../../tsrc/util/langstrings";
import { renderDashboardEditor } from "./DashboardEditorTestHelper";

const {
  title,
  alertInfo,
  dashboardLayout: dashLayoutLabel,
  createPortlet: createPortletLabel,
  restorePortlet: restorePortletLabel,
} = languageStrings.dashboard.editor;
const { close: closeLabel } = languageStrings.common.action;

describe("<DashboardEditor />", () => {
  it("should always render header, alert, and tabs", () => {
    const { getByText, getTabByRole, getButtonByRole } =
      renderDashboardEditor();

    // Header
    expect(getByText(title)).toBeInTheDocument();
    expect(getButtonByRole(closeLabel)).toBeInTheDocument();

    // Info alert
    expect(getByText(alertInfo)).toBeInTheDocument();

    // Tabs
    expect(getTabByRole(dashLayoutLabel)).toBeInTheDocument();
    expect(getTabByRole(createPortletLabel)).toBeInTheDocument();
    expect(getTabByRole(restorePortletLabel)).toBeInTheDocument();
  });

  it("marks the first tab as selected initially", () => {
    const { assertActiveTab } = renderDashboardEditor();

    assertActiveTab(dashLayoutLabel);
    expect.assertions(3);
  });

  it("switches tabs when clicked", async () => {
    const { user, getTabByRole, assertActiveTab } = renderDashboardEditor();

    await user.click(getTabByRole(createPortletLabel));
    assertActiveTab(createPortletLabel);

    await user.click(getTabByRole(restorePortletLabel));
    assertActiveTab(restorePortletLabel);

    expect.assertions(6);
  });

  it("shows the skeleton by default", () => {
    const { getTabContentSkeleton } = renderDashboardEditor();

    expect(getTabContentSkeleton()).toBeInTheDocument();
  });

  it("calls setOpenDashboardEditor(false) when Close button is clicked", async () => {
    const { user, mockSetOpenDashboardEditor, getButtonByRole } =
      renderDashboardEditor();

    const closeButton = getButtonByRole(closeLabel);
    await user.click(closeButton);

    expect(mockSetOpenDashboardEditor).toHaveBeenCalledWith(false);
  });
});

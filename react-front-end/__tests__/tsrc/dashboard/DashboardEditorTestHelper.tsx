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
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import {
  creatablePortletTypes,
  dashboardDetailsWithLayout,
  getClosedPortletsRes,
} from "../../../__mocks__/Dashboard.mock";
import {
  DashboardEditor,
  DashboardEditorProps,
} from "../../../tsrc/dashboard/DashboardEditor";
import { DashboardPageContext } from "../../../tsrc/dashboard/DashboardPageContext";
import { DashboardLayout } from "../../../tsrc/dashboard/editor/DashboardLayout";
import { DashboardLayoutSelector } from "../../../tsrc/dashboard/editor/DashboardLayoutSelector";
import * as DashboardModule from "../../../tsrc/modules/DashboardModule";
import { languageStrings } from "../../../tsrc/util/langstrings";

const {
  dashboardLayout: { title: dashLayoutLabel },
  createPortlet: { title: createPortletLabel },
  restorePortlet: { title: restorePortletLabel },
} = languageStrings.dashboard.editor;

/** The spies for the mocked API calls related to the Dashboard Editor. */
export interface MockDashboardEditorApiSpies {
  /** A spy for the `updateDashboardLayout` API call. */
  mockUpdateDashboardLayout: jest.SpyInstance;
  /** A spy for the `updatePortletPreference` API call. */
  mockUpdatePortletPreference: jest.SpyInstance;
  /** Spy for the `getClosedPortlets` API. */
  mockGetClosedPortlets: jest.SpyInstance;
}

/**
 * Mocks the API calls related to the Dashboard Editor and returns spies for them.
 *
 * @returns An object containing spies for the mocked API calls.
 */
export const mockDashboardEditorApis = (): MockDashboardEditorApiSpies => ({
  mockUpdateDashboardLayout: jest
    .spyOn(DashboardModule, "updateDashboardLayout")
    .mockResolvedValue(undefined),
  mockUpdatePortletPreference: jest
    .spyOn(DashboardModule, "updatePortletPreference")
    .mockResolvedValue(undefined),
  mockGetClosedPortlets: jest
    .spyOn(DashboardModule, "getClosedPortlets")
    .mockResolvedValue(getClosedPortletsRes),
});

/**
 * Wraps children with DashboardPageContext.Provider.
 *
 * @param children The React components to wrap.
 * @param dashboardDetails Optional dashboard details to override the default.
 * @returns The render result from Testing Library, and mock context functions.
 */
const renderWithDashboardPageContext = (
  children: React.ReactNode,
  dashboardDetails?: OEQ.Dashboard.DashboardDetails,
) => {
  const mockRefreshDashboard = jest
    .fn()
    .mockReturnValue(() => Promise.resolve());

  const mockRestorePortlet = jest.fn();

  const renderResult = render(
    <DashboardPageContext.Provider
      value={{
        dashboardDetails,
        refreshDashboard: mockRefreshDashboard,
        minimisePortlet: jest.fn(),
        closePortlet: jest.fn(),
        deletePortlet: jest.fn(),
        restorePortlet: mockRestorePortlet,
        scrollToRestoredPortletAndReset: jest.fn(),
      }}
    >
      {children}
    </DashboardPageContext.Provider>,
  );

  return { ...renderResult, mockRefreshDashboard, mockRestorePortlet };
};

/**
 * Renders the `DashboardEditor` component with default props.
 *
 * @returns An object containing render result, helper functions and the `userEvent` instance
 */
export const renderDashboardEditor = () => {
  const onClose = jest.fn();
  const defaultProps: DashboardEditorProps = {
    onClose,
    creatablePortletTypes,
  };

  const renderResult = render(<DashboardEditor {...defaultProps} />);

  const { getByRole } = renderResult;

  const getTabByRole = (name: string) => getByRole("tab", { name });

  const getButtonByRole = (name: string) => getByRole("button", { name });

  const allTabs = [dashLayoutLabel, createPortletLabel, restorePortletLabel];

  const assertActiveTab = (activeTabLabel: string) => {
    allTabs.forEach((tabLabel) => {
      const isSelected = tabLabel === activeTabLabel;
      expect(getTabByRole(tabLabel)).toHaveAttribute(
        "aria-selected",
        String(isSelected),
      );
    });
  };

  const user = userEvent.setup();

  return {
    user,
    onClose,
    getTabByRole,
    getButtonByRole,
    assertActiveTab,
    ...renderResult,
  };
};

/**
 * Renders the `DashboardLayout` component within a `DashboardPageContext.Provider`.
 *
 * @param dashboardDetails Optional `DashboardDetails` to provide to the context.
 * @returns The render result from Testing Library, and mock context functions.
 */
export const renderDashboardLayout = (
  dashboardDetails?: OEQ.Dashboard.DashboardDetails,
) => renderWithDashboardPageContext(<DashboardLayout />, dashboardDetails);

/**
 * Renders the `DashboardLayoutSelector` component with a mocked `onChange` handler.
 *
 * @param value Optional initial layout value to be selected.
 * @returns The mocked `onChange` handler and the render result.
 */
export const renderDashboardLayoutSelector = (
  value?: OEQ.Dashboard.DashboardLayout,
) => {
  const onChange = jest.fn();
  const renderResult = render(
    <DashboardLayoutSelector value={value} onChange={onChange} />,
  );
  return { onChange, ...renderResult };
};

/**
 * Renders a `RestorePortletsTab` component within a `DashboardPageContext.Provider`.
 *
 * @param element The `RestorePortletsTab` component element to render.
 * @returns The render result from Testing Library, and mock context functions.
 */
export const renderRestorePortletsTab = (element: React.ReactElement) => {
  const renderResult = renderWithDashboardPageContext(
    element,
    dashboardDetailsWithLayout(),
  );

  // skeleton should be there immediately
  expect(renderResult.getByTestId("tab-content-skeleton")).toBeInTheDocument();

  return renderResult;
};

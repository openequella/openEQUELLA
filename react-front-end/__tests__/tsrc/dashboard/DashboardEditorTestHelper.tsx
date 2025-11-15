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
import * as React from "react";
import { render, RenderResult } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DashboardLayoutSelector } from "../../../tsrc/dashboard/editor/DashboardLayoutSelector";
import {
  DashboardEditor,
  DashboardEditorProps,
} from "../../../tsrc/dashboard/DashboardEditor";
import { DashboardLayout } from "../../../tsrc/dashboard/editor/DashboardLayout";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { DashboardPageContext } from "../../../tsrc/dashboard/DashboardPageContext";
import * as DashboardModule from "../../../tsrc/modules/DashboardModule";
import * as OEQ from "@openequella/rest-api-client";

const {
  dashboardLayout: { title: dashLayoutLabel },
  createPortlet: createPortletLabel,
  restorePortlet: restorePortletLabel,
} = languageStrings.dashboard.editor;

const TabContentSkeletonTestId = "tab-content-skeleton";

/** The spies for the mocked API calls related to the Dashboard Editor. */
export interface MockDashboardEditorApiSpies {
  /** A spy for the `updateDashboardLayout` API call. */
  mockUpdateDashboardLayout: jest.SpyInstance;
  /** A spy for the `updatePortletPreference` API call. */
  mockUpdatePortletPreference: jest.SpyInstance;
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
});

/**
 * Renders the `DashboardEditor` component with default props and returns a set of
 * query helpers and spies for testing.
 *
 * @param props Optional props to override the defaults.
 * @returns An object containing query helpers, spies, and the `userEvent` instance.
 */
export const renderDashboardEditor = (
  props: Partial<DashboardEditorProps> = {},
) => {
  const mockSetOpenDashboardEditor = jest.fn();
  const defaultProps: DashboardEditorProps = {
    setOpenDashboardEditor: mockSetOpenDashboardEditor,
  };

  const renderResult = render(<DashboardEditor {...defaultProps} {...props} />);
  const { getByRole, getByTestId, queryByTestId } = renderResult;

  const getTabByRole = (name: string) => getByRole("tab", { name });

  const getButtonByRole = (name: string) => getByRole("button", { name });

  const getTabContentSkeleton = () => getByTestId(TabContentSkeletonTestId);

  const queryTabContentSkeleton = () => queryByTestId(TabContentSkeletonTestId);

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
    mockSetOpenDashboardEditor,
    getTabByRole,
    getButtonByRole,
    getTabContentSkeleton,
    queryTabContentSkeleton,
    assertActiveTab,
    ...renderResult,
  };
};

/**
 * A mock function for the `refreshDashboard` callback provided by `DashboardPageContext`.
 */
export const mockRefreshDashboard = jest.fn();

/**
 * Renders the `DashboardLayout` component within a `DashboardPageContext.Provider`.
 *
 * @param dashboardDetails Optional `DashboardDetails` to provide to the context.
 * @returns The `render` result from Testing Library.
 */
export const renderDashboardLayout = (
  dashboardDetails?: OEQ.Dashboard.DashboardDetails,
): RenderResult =>
  render(
    <DashboardPageContext.Provider
      value={{
        dashboardDetails,
        refreshDashboard: mockRefreshDashboard,
        minimisePortlet: jest.fn,
        closePortlet: jest.fn,
        deletePortlet: jest.fn,
      }}
    >
      <DashboardLayout />
    </DashboardPageContext.Provider>,
  );

/**
 * Renders the `DashboardLayoutSelector` component with a mocked `onChange` handler.
 *
 * @param value Optional initial layout value to be selected.
 * @returns The mocked `onChange` handler and the `render` result.
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

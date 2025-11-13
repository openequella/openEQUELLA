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
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { creatablePortletTypes } from "../../../__mocks__/Dashboard.mock";
import {
  DashboardEditor,
  DashboardEditorProps,
} from "../../../tsrc/dashboard/DashboardEditor";
import { languageStrings } from "../../../tsrc/util/langstrings";

const {
  dashboardLayout: dashLayoutLabel,
  createPortlet: createPortletLabel,
  restorePortlet: restorePortletLabel,
} = languageStrings.dashboard.editor;

export const TabContentSkeletonTestId = "tab-content-skeleton";

export const renderDashboardEditor = (
  props: Partial<DashboardEditorProps> = {},
) => {
  const mockSetOpenDashboardEditor = jest.fn();
  const defaultProps: DashboardEditorProps = {
    onClose: mockSetOpenDashboardEditor,
    creatablePortletTypes,
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

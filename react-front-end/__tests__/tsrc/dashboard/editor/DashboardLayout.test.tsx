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
import {
  dashboardDetailsWithLayout,
  emptyDashboardDetails,
  singleColLayoutDashboardDetails,
} from "../../../../__mocks__/Dashboard.mock";
import type { TwoColumnLayout } from "../../../../tsrc/dashboard/portlet/PortletHelper";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { clickButton, isToggleButtonChecked } from "../../MuiTestHelpers";
import {
  mockDashboardEditorApis,
  mockRefreshDashboard,
  renderDashboardLayout,
} from "../DashboardEditorTestHelper";
import * as A from "fp-ts/Array";
import * as O from "fp-ts/Option";
import { pipe } from "fp-ts/function";
import * as OEQ from "@openequella/rest-api-client";

const { dashboardLayout: strings } = languageStrings.dashboard.editor;

const { mockUpdateDashboardLayout, mockUpdatePortletPreference } =
  mockDashboardEditorApis();

describe("<DashboardLayout />", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("shows an error alert if dashboard details are not available", () => {
    const { getByText } = renderDashboardLayout(undefined);
    expect(getByText(strings.alertNoDashboardDetails)).toBeInTheDocument();
  });

  it("renders the layout selector and choose layout text if dashboard details are available", () => {
    const { getByText, getByRole } = renderDashboardLayout(
      emptyDashboardDetails,
    );
    expect(
      getByRole("button", { name: strings.singleColumn }),
    ).toBeInTheDocument();
    expect(getByText(strings.chooseLayout)).toBeInTheDocument();
  });

  it("does not make any API call if the selected layout is already active", async () => {
    const { container } = renderDashboardLayout(
      singleColLayoutDashboardDetails,
    );

    expect(isToggleButtonChecked(container, strings.singleColumn)).toBe(true);
    await clickButton(container, strings.singleColumn);

    expect(mockUpdateDashboardLayout).not.toHaveBeenCalled();
    expect(mockUpdatePortletPreference).not.toHaveBeenCalled();
    expect(mockRefreshDashboard).not.toHaveBeenCalled();
  });

  it.each<[string, TwoColumnLayout]>([
    [strings.twoColumnsEqual, "TwoEqualColumns"],
    [strings.twoColumnsRatio1to2, "TwoColumnsRatio1to2"],
    [strings.twoColumnsRatio2to1, "TwoColumnsRatio2to1"],
  ])(
    "only updates the layout and refreshes the dashboard when user chooses %s layout",
    async (label, layoutValue) => {
      const { container } = renderDashboardLayout(
        singleColLayoutDashboardDetails,
      );

      await clickButton(container, label);

      expect(mockUpdateDashboardLayout).toHaveBeenCalledWith(layoutValue);
      expect(mockRefreshDashboard).toHaveBeenCalled();
      expect(mockUpdatePortletPreference).not.toHaveBeenCalled();
    },
  );

  it("moves second-column portlets to first column when switching to SingleColumn layout", async () => {
    const mockDashboardDetails = dashboardDetailsWithLayout();
    const { container } = renderDashboardLayout(mockDashboardDetails);

    await clickButton(container, strings.singleColumn);

    expect(mockUpdateDashboardLayout).toHaveBeenCalledWith("SingleColumn");

    const { uuid, isMinimised, isClosed, order } = pipe(
      mockDashboardDetails.portlets,
      A.findFirst(
        (p: OEQ.Dashboard.BasicPortlet) => p.commonDetails.column === 1,
      ),
      O.map(({ commonDetails }) => ({
        uuid: commonDetails.uuid,
        isMinimised: commonDetails.isMinimised,
        isClosed: commonDetails.isClosed,
        order: commonDetails.order,
      })),
      O.getOrElseW(() => {
        throw new Error("Expected one portlet in column 1 in test data");
      }),
    );

    // There is one portlet in the second column in the mock data.
    expect(mockUpdatePortletPreference).toHaveBeenCalledTimes(1);
    expect(mockUpdatePortletPreference).toHaveBeenCalledWith(uuid, {
      column: 0,
      isMinimised,
      isClosed,
      order,
    });
    expect(mockRefreshDashboard).toHaveBeenCalled();
  });
});

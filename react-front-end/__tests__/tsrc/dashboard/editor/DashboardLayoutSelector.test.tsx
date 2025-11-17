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
import { clickButton, isToggleButtonChecked } from "../../MuiTestHelpers";
import { renderDashboardLayoutSelector } from "../DashboardEditorTestHelper";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import * as OEQ from "@openequella/rest-api-client";

const { dashboardLayout: strings } = languageStrings.dashboard.editor;

const NUM_LAYOUT_BUTTONS = 4;

describe("<DashboardLayoutSelector />", () => {
  it("renders a ButtonGroup with four layout buttons", () => {
    const { getByRole, getAllByRole } = renderDashboardLayoutSelector();
    const getButton = (label: string) => getByRole("button", { name: label });

    const buttons = getAllByRole("button");

    expect(getByRole("group")).toBeInTheDocument();
    expect(buttons).toHaveLength(NUM_LAYOUT_BUTTONS);
    expect(getButton(strings.singleColumn)).toBeInTheDocument();
    expect(getButton(strings.twoColumnsEqual)).toBeInTheDocument();
    expect(getButton(strings.twoColumnsRatio1to2)).toBeInTheDocument();
    expect(getButton(strings.twoColumnsRatio2to1)).toBeInTheDocument();
  });

  it("shows no layout button selected when value is undefined", () => {
    const { getAllByRole } = renderDashboardLayoutSelector(undefined);
    const buttons = getAllByRole("button");

    buttons.forEach((btn) =>
      expect(btn).toHaveAttribute("aria-checked", "false"),
    );
    expect.assertions(NUM_LAYOUT_BUTTONS);
  });

  const layoutOptions: {
    layout: OEQ.Dashboard.DashboardLayout;
    label: string;
  }[] = [
    { layout: "SingleColumn", label: strings.singleColumn },
    { layout: "TwoEqualColumns", label: strings.twoColumnsEqual },
    { layout: "TwoColumnsRatio1to2", label: strings.twoColumnsRatio1to2 },
    { layout: "TwoColumnsRatio2to1", label: strings.twoColumnsRatio2to1 },
  ];

  it.each(layoutOptions)(
    "emits $layout when $label button is clicked",
    async ({ layout, label }) => {
      const { onChange, container } = renderDashboardLayoutSelector(undefined);

      await clickButton(container, label);

      expect(onChange).toHaveBeenCalledTimes(1);
      expect(onChange).toHaveBeenCalledWith(layout);
    },
  );

  it.each(layoutOptions)(
    "marks $label button as selected when value is $layout",
    ({ layout }) => {
      const { container } = renderDashboardLayoutSelector(layout);

      layoutOptions.forEach((option) => {
        expect(isToggleButtonChecked(container, option.label)).toBe(
          option.layout === layout,
        );
      });
    },
  );
});

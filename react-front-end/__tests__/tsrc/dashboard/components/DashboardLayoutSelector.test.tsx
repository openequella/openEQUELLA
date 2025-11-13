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

const { dashboardLayout: strings } = languageStrings.dashboard.editor;

describe("<DashboardLayoutSelector />", () => {
  it("renders a ButtonGroup with four layout buttons", () => {
    const { getByRole, getAllByRole } = renderDashboardLayoutSelector();
    const getButton = (label: string) => getByRole("button", { name: label });

    const buttons = getAllByRole("button");

    expect(getByRole("group")).toBeInTheDocument();
    expect(buttons).toHaveLength(4);
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
    expect.assertions(buttons.length);
  });

  it("marks the matching layout button as selected", () => {
    const { container } = renderDashboardLayoutSelector("TwoEqualColumns");

    expect(isToggleButtonChecked(container, strings.singleColumn)).toBe(false);
    expect(isToggleButtonChecked(container, strings.twoColumnsEqual)).toBe(
      true,
    );
    expect(isToggleButtonChecked(container, strings.twoColumnsRatio1to2)).toBe(
      false,
    );
    expect(isToggleButtonChecked(container, strings.twoColumnsRatio2to1)).toBe(
      false,
    );
  });

  it("emits the correct layout value when each button is clicked", async () => {
    const { onChange, container } = renderDashboardLayoutSelector();

    await clickButton(container, strings.singleColumn);
    expect(onChange).toHaveBeenLastCalledWith("SingleColumn");

    await clickButton(container, strings.twoColumnsEqual);
    expect(onChange).toHaveBeenLastCalledWith("TwoEqualColumns");

    await clickButton(container, strings.twoColumnsRatio1to2);
    expect(onChange).toHaveBeenLastCalledWith("TwoColumnsRatio1to2");

    await clickButton(container, strings.twoColumnsRatio2to1);
    expect(onChange).toHaveBeenLastCalledWith("TwoColumnsRatio2to1");

    expect(onChange).toHaveBeenCalledTimes(4);
  });
});

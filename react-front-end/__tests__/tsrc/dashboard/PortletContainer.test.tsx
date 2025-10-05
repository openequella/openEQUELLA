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
import "@testing-library/jest-dom";
import { render } from "@testing-library/react";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";
import {
  mockPortlets,
  privateFavouritePortlet,
  privateSearchPortlet,
  privateTasksPortlet,
  publicHtmlPortlet,
  publicRecentContributionsPortlet,
} from "../../../__mocks__/Dashboard.mock";
import { PortletContainer } from "../../../tsrc/dashboard/portlet/PortletContainer";
import { TwoColumnLayout } from "../../../tsrc/dashboard/portlet/PortletHelper";

describe("<PortletContainer />", () => {
  /**
   * Helper function to get the portlets displayed in a specific column of the container.
   *
   * @param container - The container element housing the portlets.
   * @param columnId - ID of the target column.
   * @param portlets - A list of portlets that should be displayed in the specified column.
   * @returns An array of HTMLElements representing the displayed portlets.
   */
  const getDisplayedPortlets = (
    container: HTMLElement,
    columnId: string,
    portlets: OEQ.Dashboard.BasicPortlet[],
  ) => {
    const targetColumn = container.querySelector<HTMLDivElement>(columnId);
    expect(targetColumn).toBeInTheDocument();

    return pipe(
      portlets,
      A.map((p) =>
        targetColumn!.querySelector(`#portlet-${p.commonDetails.uuid}`),
      ),
      A.filter((p) => p !== null),
    );
  };

  const renderPortletContainer = (layout: OEQ.Dashboard.DashboardLayout) =>
    render(<PortletContainer portlets={mockPortlets} layout={layout} />);

  it("displays all the portlets in the single-column layout", () => {
    const { container } = renderPortletContainer("SingleColumn");
    const displayedPortlets = getDisplayedPortlets(
      container,
      "#portlet-container-single-column",
      mockPortlets,
    );
    expect(displayedPortlets).toHaveLength(mockPortlets.length);
  });

  it.each<[TwoColumnLayout]>([
    ["TwoEqualColumns"],
    ["TwoColumnsRatio1to2"],
    ["TwoColumnsRatio2to1"],
  ])(
    "displays all the portlets in two-column layout %s",
    (layout: TwoColumnLayout) => {
      const { container } = renderPortletContainer(layout);

      const leftColumnPortlets = getDisplayedPortlets(
        container,
        "#portlet-container-left-column",
        mockPortlets.filter((p) => p.commonDetails.column === 0),
      );
      expect(leftColumnPortlets).toHaveLength(3);

      const rightColumnPortlets = getDisplayedPortlets(
        container,
        "#portlet-container-right-column",
        mockPortlets.filter((p) => p.commonDetails.column === 1),
      );
      expect(rightColumnPortlets).toHaveLength(2);
    },
  );

  it("sorts portlets by order first and then by name", () => {
    const { container } = renderPortletContainer("SingleColumn");
    const singleColumn = container.querySelector<HTMLDivElement>(
      "#portlet-container-single-column",
    );
    const portlets = pipe(
      singleColumn?.querySelectorAll("div"),
      O.fromNullable,
      O.map(Array.from<HTMLDivElement>),
      O.match(
        () => [],
        A.map((portlet) => portlet.textContent),
      ),
    );

    // Portlets in the two columns are sorted independently, and those in the right column should appear
    // after those in the left column in the single-column layout.
    const expectedOrder = [
      privateTasksPortlet.commonDetails.name,
      privateFavouritePortlet.commonDetails.name,
      privateSearchPortlet.commonDetails.name,
      publicHtmlPortlet.commonDetails.name,
      publicRecentContributionsPortlet.commonDetails.name,
    ];

    expect(portlets).toStrictEqual(expectedOrder);
  });
});

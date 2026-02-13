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
import * as React from "react";
import { allManageableWorkflows } from "../../../../__mocks__/WorkflowModule.mock";
import * as stories from "../../../../__stories__/dashboard/portlets/TaskStatistics.stories";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  expectPortletToRender,
  itemCountRegex,
  itemCountTextForWorkflowSample,
  queryItemCountButton,
  selectWorkflow,
  setup,
  waitForNoPermission,
  waitForNoResults,
  waitForTable,
  waitForTrendToggle,
} from "./PortletTaskStatisticsTestHelper";

const strings = {
  ...languageStrings.dashboard.portlets.taskStatistics,
};

const WORKFLOW_WITH_NO_ITEMS = allManageableWorkflows[0].name;
const WORKFLOW_WITH_ITEMS = allManageableWorkflows[1].name;

const {
  Standard,
  WithMonthlyTrend,
  EmptyResult,
  NoPermission,
  ErrorTrends,
  ErrorWorkflowOptions,
  ErrorStatisticsForSpecificWorkflow,
  FetchingTrendData,
  HasViewManagementPagePermission,
} = composeStories(stories);

describe("<PortletTaskStatistics />", () => {
  it("renders without crashing and shows trend data for the standard story", async () => {
    const context = await setup(<Standard />);

    expectPortletToRender(context);
  });

  it("shows item count for a specific workflow when the item count is greater than zero", async () => {
    const context = await setup(<Standard />);
    const { getByText, container } = context;

    await selectWorkflow(container, WORKFLOW_WITH_ITEMS);
    // Wait for the table to appear again.
    await waitForTable(context);

    expect(getByText(itemCountTextForWorkflowSample)).toBeInTheDocument();

    const button = queryItemCountButton(container);
    expect(button).not.toBeInTheDocument();
  });

  it("shows a clickable item count link when user has VIEW_MANAGEMENT_PAGE permission", async () => {
    const context = await setup(<HasViewManagementPagePermission />);
    const { container, getByText } = context;

    await selectWorkflow(container, WORKFLOW_WITH_ITEMS);
    await waitForTable(context);

    expect(getByText(itemCountTextForWorkflowSample)).toBeInTheDocument();

    const button = queryItemCountButton(container);
    expect(button).toBeInTheDocument();
  });

  it("hides item count for a specific workflow when the item count is not greater than zero", async () => {
    const context = await setup(<Standard />);
    const { container, queryByText } = context;

    await selectWorkflow(container, WORKFLOW_WITH_NO_ITEMS);
    // Wait for the table to appear again.
    await waitForTable(context);

    expect(queryByText(itemCountRegex)).not.toBeInTheDocument();
  });

  it("can use MONTH as initial selected trend", async () => {
    const { getByRole } = await setup(<WithMonthlyTrend />);

    const monthBtn = getByRole("button", { name: strings.trend.monthly });
    expect(monthBtn).toHaveAttribute("aria-pressed", "true");
  });

  it("shows no-results message when EmptyResult story returns empty trends", async () => {
    const { getByText } = await setup(<EmptyResult />, waitForNoResults);
    expect(getByText(strings.noResult)).toBeInTheDocument();
  });

  it("shows no-permission message when users don't have MANAGE_WORKFLOW ACL", async () => {
    const { getByText } = await setup(<NoPermission />, waitForNoPermission);

    expect(getByText(strings.noPermission)).toBeInTheDocument();
  });

  it("shows no-results message when trends provider fails", async () => {
    const { getByText } = await setup(<ErrorTrends />, waitForNoResults);

    expect(getByText(strings.noResult)).toBeInTheDocument();
  });

  it("still show workflow selector and table even if workflow options provider fails", async () => {
    const context = await setup(<ErrorWorkflowOptions />);

    expectPortletToRender(context);
  });

  it("shows no-results message when selecting a specific workflow and statistics provider fails", async () => {
    const { findByText, container } = await setup(
      <ErrorStatisticsForSpecificWorkflow />,
    );

    await selectWorkflow(container, WORKFLOW_WITH_NO_ITEMS);

    expect(await findByText(strings.noResult)).toBeInTheDocument();
  });

  it("shows a skeleton while fetching trend data", async () => {
    const { getByLabelText } = await setup(
      <FetchingTrendData />,
      waitForTrendToggle,
    );

    expect(getByLabelText(strings.contentSkeletonLabel)).toBeInTheDocument();
  });
});

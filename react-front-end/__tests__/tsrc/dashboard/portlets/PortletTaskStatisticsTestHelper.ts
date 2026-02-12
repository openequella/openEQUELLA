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
import { queryByText } from "@testing-library/react";
import * as React from "react";
import { sprintf } from "sprintf-js";
import {
  allWorkflowTrends,
  WORKFLOW_SAMPLE,
  workflowStatisticsMap,
} from "../../../../__mocks__/WorkflowModule.mock";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { sprintfFormatToRegex } from "../../../../tsrc/util/TextUtils";
import { selectOption } from "../../MuiTestHelpers";
import { RenderContext, setupStoryComponent } from "../../TestSetupHelper";

const strings = {
  ...languageStrings.dashboard.portlets.taskStatistics,
};

export const itemCountRegex = sprintfFormatToRegex(strings.itemCount);

/**
 * Setup function that waits for the task statistics portlet to be ready.
 *
 * @param element The task statistics story to render.
 * @param readyStateCheck Optional function to check when the component is ready.
 */
export const setup = async (
  element: React.ReactElement,
  readyStateCheck?: (context: RenderContext) => Promise<void>,
) => setupStoryComponent(element, readyStateCheck ?? waitForTable);

/**
 * Ready state check function that waits for the "no permission" message.
 *
 * @param context The render context.
 */
export const waitForNoPermission = async (
  context: RenderContext,
): Promise<void> => {
  await context.findByText(strings.noPermission);
};

/**
 * Ready state check function that waits for the "no results" message.
 *
 * @param context The render context.
 */
export const waitForNoResults = async (
  context: RenderContext,
): Promise<void> => {
  await context.findByText(strings.noResult);
};

/** Ready state check function that waits for the trend toggle to appear.
 *
 * @param context The render context.
 */
export const waitForTrendToggle = async (
  context: RenderContext,
): Promise<void> => {
  await context.findByText(strings.trend.monthly);
};

/**
 * Ready state check function that waits for the table to appear.
 *
 * @param context The render context.
 */
export const waitForTable = async (context: RenderContext): Promise<void> => {
  await context.findByLabelText(strings.table.label);
};

/**
 * Select a workflow from the workflow selector.
 *
 * @param container The container HTMLElement.
 * @param workflowName The name of the workflow to select.
 */
export const selectWorkflow = (
  container: HTMLElement,
  workflowName: string,
): Promise<void> =>
  selectOption(container, "[aria-labelledby='workflow-label']", workflowName);

/**
 * Expect the workflow selector, trend toggle buttons, and table with trend data to be present.
 *
 * @param context The render context.
 */
export const expectPortletToRender = (context: RenderContext) => {
  const { getByText } = context;
  // Workflow Selector
  expect(getByText(strings.workflow.allWorkflows)).toBeInTheDocument();
  // Trend Toggle Buttons
  expect(getByText(strings.trend.weekly)).toBeInTheDocument();
  expect(getByText(strings.trend.monthly)).toBeInTheDocument();
  // Should display the task trend in the table.
  expect(getByText(allWorkflowTrends[0].name)).toBeInTheDocument();
};

/**
 * Queries for the item count button that shows the total number of items in the workflow.
 *
 * @param container The container HTMLElement.
 */
export const queryItemCountButton = (container: HTMLElement) =>
  queryByText(container, itemCountRegex, {
    selector: "button",
  });

export const itemCountTextForWorkflowSample = sprintf(
  strings.itemCount,
  workflowStatisticsMap[WORKFLOW_SAMPLE]["WEEK"].itemCount,
);

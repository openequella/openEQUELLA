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

/**
 * Mock data for all workflow trends.
 */
export const allWorkflowTrends: OEQ.Workflow.TaskTrendDetails[] = [
  {
    taskId: "1909836",
    name: "Approval",
    waiting: 9,
    trend: 2,
  },
  {
    taskId: "51893",
    name: "Create PDF",
    waiting: 2,
    trend: 23,
  },
  {
    taskId: "51896",
    name: "Validate content",
    waiting: 2,
    trend: -6,
  },
];

export const WORKFLOW_SAMPLE = "workflow-uuid";

export const WORKFLOW_ITEM_COUNT_0 = "workflow-ItemCount0";

/**
 * Mock data for workflow statistics keyed by uuid and trend.
 */
export const workflowStatisticsMap: Record<
  string,
  Record<OEQ.Task.Trend, OEQ.Workflow.WorkflowStatistics>
> = {
  [WORKFLOW_ITEM_COUNT_0]: {
    WEEK: {
      taskTrends: [
        { taskId: "t1", name: "Approval", waiting: 5, trend: 2 },
        { taskId: "t2", name: "Review", waiting: 2, trend: 1 },
        { taskId: "t3", name: "Finalise", waiting: 3, trend: 0 },
      ],
      itemCount: 0,
    },
    MONTH: {
      taskTrends: [
        { taskId: "t1", name: "Approval", waiting: 2, trend: -1 },
        { taskId: "t2", name: "Review", waiting: 4, trend: -2 },
        { taskId: "t3", name: "Finalise", waiting: 1, trend: 0 },
      ],
      itemCount: 0,
    },
  },
  [WORKFLOW_SAMPLE]: {
    WEEK: {
      taskTrends: [
        { taskId: "t4", name: "Draft", waiting: 8, trend: 3 },
        { taskId: "t5", name: "QA", waiting: 1, trend: 0 },
        { taskId: "t6", name: "Publish", waiting: 0, trend: 0 },
      ],
      itemCount: 9,
    },
    MONTH: {
      taskTrends: [
        { taskId: "t4", name: "Draft", waiting: 3, trend: -1 },
        { taskId: "t5", name: "QA", waiting: 2, trend: -1 },
        { taskId: "t6", name: "Publish", waiting: 2, trend: 0 },
      ],
      itemCount: 7,
    },
  },
};

/**
 * Mock implementation of getAllWorkflowTrends API.
 */
export const getAllWorkflowTrends = (): Promise<
  OEQ.Workflow.TaskTrendDetails[]
> => Promise.resolve(allWorkflowTrends);

/**
 * Mock implementation of getAllWorkflowTrends API returning empty list.
 */
export const getEmptyWorkflowTrends = (): Promise<
  OEQ.Workflow.TaskTrendDetails[]
> => Promise.resolve([]);

/**
 * Mock implementation of getAllWorkflowTrends API that fails.
 */
export const failingGetAllWorkflowTrends = () =>
  Promise.reject(new Error("Get all workflow trends failed"));

/**
 * Mock implementation of getWorkflowStatistics API.
 * Returns statistics for the given uuid and trend from the mock map, or a default empty statistics object if not found.
 */
export const getWorkflowStatistics = (
  uuid: OEQ.Common.UuidString,
  trend: OEQ.Task.Trend,
): Promise<OEQ.Workflow.WorkflowStatistics> => {
  const stats = workflowStatisticsMap[uuid]?.[trend] || {
    taskTrends: [],
    itemCount: 0,
  };
  return Promise.resolve(stats);
};

/**
 * Mock implementation of getWorkflowStatistics API that fails.
 */
export const failingGetWorkflowStatistics = () =>
  Promise.reject(new Error("Get workflow statistics failed"));

export const allManageableWorkflows: OEQ.Workflow.WorkflowSummary[] = [
  {
    uuid: WORKFLOW_ITEM_COUNT_0,
    name: "Sample workflow with item count 0",
  },
  {
    uuid: WORKFLOW_SAMPLE,
    name: "Sample workflow",
  },
];

/**
 * Mock implementation of getManageableWorkflows API.
 */
export const getManageableWorkflows = () =>
  Promise.resolve(allManageableWorkflows);

/**
 * Mock implementation of getManageableWorkflows API that fails.
 */
export const failingGetManageableWorkflows = () =>
  Promise.reject(new Error("Get manageable workflows failed"));

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

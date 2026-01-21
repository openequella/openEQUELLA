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
import { pipe } from 'fp-ts/function';
import * as t from 'io-ts';
import { GET } from './AxiosInstance';
import { UuidString } from './Common';
import { TaskTrendDetailsCodec } from './gen/Workflow';
import { Trend } from './Task';
import { validate } from './Utils';

/**
 * Base path segment for all workflow-related API endpoints.
 * Used to construct URLs like: {apiBasePath}/workflow/...
 */
const BASE_WORKFLOW_API_PATH = '/workflow' as const;

/**
 * Path segment for workflow trends endpoints.
 * Appended to workflow paths: /workflow/trends or /workflow/{uuid}/trends
 */
const TRENDS_API_PATH = '/trends' as const;

/**
 * Details of task trend including its id, name, current waiting count, and trend.
 * All fields are guaranteed to be present (non-null, non-undefined).
 */
export interface TaskTrendDetails {
  /**
   * The identifier of the task.
   */
  taskId: string;
  /**
   * The name of the task.
   */
  name: string;
  /**
   * The number of items currently waiting in this task.
   */
  waiting: number;
  /**
   * The trend value indicating the change in waiting items over the period.
   */
  trend: number;
}

const tasksTrendsValidator = pipe(TaskTrendDetailsCodec, t.array, validate);

/**
 * Retrieves a list of tasks trends across all workflows.
 *
 * @param apiBasePath The base path of the API.
 * @param trend The time period for trend calculation (e.g. 'WEEK' or 'MONTH').
 */
export const getAllWorkflowsTrends = (
  apiBasePath: string,
  trend: Trend
): Promise<TaskTrendDetails[]> =>
  GET<TaskTrendDetails[]>(
    `${apiBasePath}${BASE_WORKFLOW_API_PATH}${TRENDS_API_PATH}`,
    tasksTrendsValidator,
    { trend }
  );

/**
 * Retrieves a list of tasks trends for a specific workflow.
 *
 * @param apiBasePath The base path of the API.
 * @param uuid The UUID of the workflow to query.
 * @param trend The time period for trend calculation.
 */
export const getWorkflowTrends = (
  apiBasePath: string,
  uuid: UuidString,
  trend: Trend
): Promise<TaskTrendDetails[]> =>
  GET<TaskTrendDetails[]>(
    `${apiBasePath}${BASE_WORKFLOW_API_PATH}/${uuid}${TRENDS_API_PATH}`,
    tasksTrendsValidator,
    { trend }
  );

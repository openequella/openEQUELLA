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
import { GET } from './AxiosInstance';
import { ListResult } from './Common';
import { ListResultCodec } from './gen/Common';
import { TaskFilterCountCodec } from './gen/Task';
import { validate } from './Utils';

export const TASK_ROOT_PATH = '/task';

/**
 * Supported time period for an analysis of task statistics.
 *  - 'WEEK': a 7-day period.
 *  - 'MONTH': a 30-day period.
 */
export type Trend = 'WEEK' | 'MONTH';

/**
 * Supported task filters.
 *  - 'noteall': all notifications.
 *  - 'taskall': all tasks.
 */
export type TaskFilter = 'noteall' | 'taskall';

/**
 * Represents an item in a task filter result, including its ID, optional name,
 * optional parent filter, and count of tasks/notifications.
 */
export interface TaskFilterCount {
  /** Unique identifier for the task and notification types - examples include those in `TaskFilter`
   * but also ones like 'tasknoone', 'taskmust' for tasks and 'notewentlive', 'noterejected' for
   * notifications. */
  id: string;
  /** Optional human-readable name for the type of task or notification. */
  name?: string;
  /** Optional parent filter category for hierarchical organization. */
  parent?: TaskFilter;
  /** Count of tasks or notifications corresponding to this filter type. */
  count: number;
}

/**
 * Retrieve counts of tasks and notifications.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param ignoreZero Whether to ignore items with zero counts.
 * @param includeCounts Only takes effect if `ignoreZero` is `false` when this is also `false`,
 *                      in that case only `id` and `count` are returned for each item.
 */
export const getCounts = (
  apiBasePath: string,
  ignoreZero: boolean,
  includeCounts: boolean = true
): Promise<ListResult<TaskFilterCount>> =>
  GET<ListResult<TaskFilterCount>>(
    apiBasePath + TASK_ROOT_PATH + '/filter',
    validate(ListResultCodec(TaskFilterCountCodec)),
    { ignoreZero, includeCounts }
  );

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
import { API_BASE_URL } from "../AppConfig";

/**
 * Retrieves a list of tasks trends across all workflows for a given trend.
 *
 * @param trend The time period for trend calculation (e.g. 'WEEK' or 'MONTH').
 */
export const getAllWorkflowsTrends = (
  trend: OEQ.Task.Trend,
): Promise<OEQ.Workflow.TaskTrendDetails[]> =>
  OEQ.Workflow.getAllWorkflowsTrends(API_BASE_URL, trend);

/**
 * Retrieves statistics info for a specific workflow for a given trend.
 *
 * @param uuid The UUID of the workflow to query.
 * @param trend The time period for trend calculation.
 */
export const getWorkflowStatistics = (
  uuid: OEQ.Common.UuidString,
  trend: OEQ.Task.Trend,
): Promise<OEQ.Workflow.WorkflowStatistics> =>
  OEQ.Workflow.getWorkflowStatistics(API_BASE_URL, uuid, trend);

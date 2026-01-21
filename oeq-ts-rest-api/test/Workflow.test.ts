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
import * as OEQ from '../src';
import * as TC from './TestConfig';
import { logout } from './TestUtils';

type Trend = OEQ.Task.Trend;

const TARGET_WORKFLOW_UUID = '0f7bd496-8466-4fa5-b166-8132cc5294e4';
const NON_EXISTENT_WORKFLOW_UUID = '0f7bd496-8466-4fa5-b166-8832cc5294e4';
const NO_PERMISSION_WORKFLOW_UUID = '117dead8-767d-4eff-ba06-5e1964f2f2db';
const NO_TASKS_WORKFLOW_UUID = '23ced460-f8f8-4bd4-bdc2-971064636de8';

const TREND_WEEK: Trend = 'WEEK';
const TREND_MONTH: Trend = 'MONTH';

const getAllWorkflowsTrends = (trend: Trend) =>
  OEQ.Workflow.getAllWorkflowsTrends(TC.API_PATH, trend);

const getWorkflowTrends = (uuid: string, trend: Trend = TREND_WEEK) =>
  OEQ.Workflow.getWorkflowTrends(TC.API_PATH, uuid, trend);

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => logout(TC.API_PATH));

describe('Workflow Trends API', () => {
  describe('getAllWorkflowsTrends', () => {
    it.each([[TREND_WEEK], [TREND_MONTH]])(
      'should be able to retrieve trends with trend value: %s',
      async (trend: Trend) => {
        const result = await getAllWorkflowsTrends(trend);
        expect(Array.isArray(result)).toBe(true);
      }
    );
  });

  describe('getWorkflowTrends', () => {
    it.each([[TREND_WEEK], [TREND_MONTH]])(
      'should be able to retrieve trends for a specific workflow with trend value: %s',
      async (trend: Trend) => {
        const result = await getWorkflowTrends(TARGET_WORKFLOW_UUID, trend);
        expect(Array.isArray(result)).toBe(true);
        expect(result).toHaveLength(2);
      }
    );

    it('should retrieve an empty list if the workflow has no waiting tasks', async () => {
      const result = await getWorkflowTrends(NO_TASKS_WORKFLOW_UUID);
      expect(Array.isArray(result)).toBe(true);
      expect(result).toHaveLength(0);
    });

    it('should return 403 when user lacks permission for the workflow', async () => {
      await expect(
        getWorkflowTrends(NO_PERMISSION_WORKFLOW_UUID)
      ).rejects.toHaveProperty('status', 403);
    });

    it.each([
      [404, 'non-existing', NON_EXISTENT_WORKFLOW_UUID],
      [400, 'invalid', 'INVALID_UUID'],
    ])(
      'should return %d when requesting trends for a %s workflow',
      async (status, _description, uuid: string) => {
        await expect(getWorkflowTrends(uuid)).rejects.toHaveProperty(
          'status',
          status
        );
      }
    );
  });
});

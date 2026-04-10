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
import {
  deletePortlet,
  getClosedPortlets,
  getCreatablePortlets,
  getDashboardDetails,
  PortletPreference,
  updateDashboardLayout,
  updatePortletPreferences,
} from '../src/Dashboard';
import * as TC from './TestConfig';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => logout(TC.API_PATH));

describe('Dashboard', () => {
  it('retrieves Dashboard details including a list of Portlets and Dashboard layout', async () => {
    const { portlets, layout } = await getDashboardDetails(TC.API_PATH);
    expect(portlets).toHaveLength(3); // User autotest has 4 portlets but 1 is closed so only 3 are returned.
    expect(layout).toBe('SingleColumn'); // And the layout is SingleColumn.
  });

  it('updates the Dashboard layout', async () => {
    await expect(
      updateDashboardLayout(TC.API_PATH, 'TwoEqualColumns')
    ).resolves.not.toThrow();
  });
});

describe('Portlet', () => {
  const portletId = '6e34ab70-a8b2-4e7b-84b9-4dcff91470b7';

  it('retrieves a list of creatable Portlets', async () => {
    const portlets = await getCreatablePortlets(TC.API_PATH);
    expect(portlets).toHaveLength(9); // User autotest can create 9 different portlet types
  });

  it('retrieves a list of closed Portlets', async () => {
    const portlets = await getClosedPortlets(TC.API_PATH);
    expect(portlets).toHaveLength(1); // User autotest has one closed portlet.
  });

  it('updates Portlet preferences', async () => {
    const preference: PortletPreference = {
      isClosed: false,
      isMinimised: true,
      column: 1,
      order: 1,
    };

    await expect(
      updatePortletPreferences(TC.API_PATH, portletId, preference)
    ).resolves.not.toThrow();
  });

  it('deletes a Portlet', async () => {
    await expect(deletePortlet(TC.API_PATH, portletId)).resolves.not.toThrow();
  });
});

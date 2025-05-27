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
  checkHierarchyPrivilege,
  checkPrivilege,
  checkSettingPrivilege,
  EDIT_SYSTEM_SETTINGS,
  HIERARCHY_PAGE,
  SEARCH_PAGE,
  VIEW_HIERARCHY_TOPIC,
} from '../src/Acl';
import * as TC from './TestConfig';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => logout(TC.API_PATH));

describe('Check privileges', () => {
  it('returns privileges granted to the current user when given a list of privileges', async () => {
    const VIEW_APIDOCS = 'VIEW_APIDOCS'; // The test account does not have this privilege.
    const acls: string[] = [SEARCH_PAGE, HIERARCHY_PAGE, VIEW_APIDOCS];

    const result = await checkPrivilege(TC.API_PATH, acls);
    expect(result).toEqual([SEARCH_PAGE, HIERARCHY_PAGE]);
  });

  it('returns a flag for if the supplied privilege is granted to the current user for a setting', async () => {
    const result = await checkSettingPrivilege(
      TC.API_PATH,
      'searching',
      EDIT_SYSTEM_SETTINGS
    );
    expect(result).toBe(true);
  });

  it('returns a flag for if the supplied privilege is granted to the current user for a Hierarchy topic', async () => {
    const result = await checkHierarchyPrivilege(
      TC.API_PATH,
      '43e60e9a-a3ed-497d-b79d-386fed23675c',
      VIEW_HIERARCHY_TOPIC
    );
    expect(result).toBe(true);
  });
});

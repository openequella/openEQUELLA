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
  getAdvancedSearchByUuid,
  listAdvancedSearches,
} from '../src/AdvancedSearch';
import * as TC from './TestConfig';
import { logout } from './TestUtils';

const API_PATH = TC.API_PATH_VANILLA;

beforeAll(() => OEQ.Auth.login(API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => logout(API_PATH));

describe('listAdvancedSearches', () => {
  it('lists the available advanced searches in an institution', async () =>
    expect((await listAdvancedSearches(API_PATH)).length).toBeGreaterThan(0));
});

describe('getAdvancedSearchByUuid', () => {
  it('retrieves definition of an Advanced search in an institution', async () => {
    const { controls } = await getAdvancedSearchByUuid(
      TC.API_PATH_FIVEO,
      'c9fd1ae8-0dc1-ab6f-e923-1f195a22d537'
    );
    expect(controls).toHaveLength(13);
  });
});

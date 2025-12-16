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

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => logout(TC.API_PATH));

/**
 * Helper to assert an item exists in the map and matches expected values.
 *
 * @param map Lookup map of items keyed by ID
 * @param id The ID to look for
 * @param expectedName The expected display name
 * @param expectedCount The expected count number
 * @param expectLinks Whether to assert that the 'links' property exists (default: true)
 */
const assertItem = (
  map: Map<
    string,
    | OEQ.SearchMyResource.MyResourceSearchType
    | OEQ.SearchMyResource.MyResourceModeratingSubSearch
  >,
  id: string,
  expectedName: string,
  expectedCount: number,
  expectLinks = true
) => {
  const item = map.get(id);

  if (!item) {
    throw new Error(`Item with id '${id}' not found in response`);
  }

  expect(item.name).toBe(expectedName);
  expect(item.count).toBe(expectedCount);

  if (expectLinks) {
    expect(
      (item as OEQ.SearchMyResource.MyResourceSearchType).links
    ).toBeTruthy();
  }

  return item;
};

describe('SearchMyResource', () => {
  it('should retrieve search types with counts and correct Moderation Queue hierarchy', async () => {
    const result = await OEQ.SearchMyResource.getMyResourceSearchTypes(
      TC.API_PATH
    );

    expect(Array.isArray(result)).toBe(true);
    expect(result.length).toBeGreaterThanOrEqual(6);

    const resultMap = new Map<
      string,
      OEQ.SearchMyResource.MyResourceSearchType
    >(result.map((i) => [i.id, i]));

    assertItem(resultMap, 'published', 'Published', 47);
    assertItem(resultMap, 'draft', 'Drafts', 6);
    assertItem(resultMap, 'scrapbook', 'Scrapbook', 9);
    assertItem(resultMap, 'archived', 'Archive', 3);
    assertItem(resultMap, 'all', 'All resources', 74);

    const modQueue = assertItem(
      resultMap,
      'modqueue',
      'Moderation queue',
      9
    ) as OEQ.SearchMyResource.MyResourceSearchType;

    // Validate moderation queue subsearch
    expect(Array.isArray(modQueue.subSearches)).toBe(true);
    expect(modQueue.subSearches).toHaveLength(3);

    const subSearchMap = new Map<
      string,
      OEQ.SearchMyResource.MyResourceModeratingSubSearch
    >(modQueue.subSearches!.map((s) => [s.id, s]));

    // Validate subsearch entries
    assertItem(subSearchMap, 'modqueue_moderating', 'In moderation', 9, false);
    assertItem(subSearchMap, 'modqueue_review', 'Under review', 0, false);
    assertItem(subSearchMap, 'modqueue_rejected', 'Rejected', 0, false);
    expect.assertions(28);
  });
});

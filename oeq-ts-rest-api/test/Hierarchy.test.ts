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
import * as A from 'fp-ts/Array';
import { pipe } from 'fp-ts/function';
import * as OEQ from '../src';
import type { KeyResource } from '../src/BrowseHierarchy';
import * as TC from './TestConfig';

const NORMAL_TOPIC_UUID = '6135b550-ce1c-43c2-b34c-0a3cf793759d';
const ITEM_UUID = 'cadcd296-a4d7-4024-bb5d-6c7507e6872a';
const VERSION = 2;

const containsKeyResources = (
  result: KeyResource[],
  itemUuid: string,
  itemVersion: number
): boolean =>
  pipe(
    result,
    A.exists(
      ({ item }) => item.version === itemVersion && item.uuid === itemUuid
    )
  );

const checkKeyResource = async (keyResourceExists: boolean) => {
  const hierarchyTopic = await OEQ.BrowseHierarchy.browseHierarchyDetails(
    TC.API_PATH,
    NORMAL_TOPIC_UUID
  );

  expect(
    containsKeyResources(hierarchyTopic.keyResources, ITEM_UUID, VERSION)
  ).toBe(keyResourceExists);
};

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(TC.API_PATH));

describe('Hierarchy', () => {
  it('should be able to get ACLs for a hierarchy topic', async () => {
    const result = await OEQ.Hierarchy.getMyAcls(
      TC.API_PATH,
      NORMAL_TOPIC_UUID
    );
    expect(result.EDIT_HIERARCHY_TOPIC).toBeTruthy();
    expect(result.VIEW_HIERARCHY_TOPIC).toBeTruthy();
    expect(result.MODIFY_KEY_RESOURCE).toBeTruthy();
  });

  // eslint-disable-next-line jest/expect-expect
  it('should be able to add a key resource for a hierarchy topic', async () => {
    // Make sure the key resource is not existing.
    await checkKeyResource(false);

    // Add key resource.
    await OEQ.Hierarchy.addKeyResource(
      TC.API_PATH,
      NORMAL_TOPIC_UUID,
      ITEM_UUID,
      VERSION
    );

    // Make sure the key resource is added.
    await checkKeyResource(true);
  });

  // eslint-disable-next-line jest/expect-expect
  it('should be able to delete a key resource from a hierarchy topic', async () => {
    // Make sure the key resource is existing.
    await checkKeyResource(true);

    // Delete key resource.
    await OEQ.Hierarchy.deleteKeyResource(
      TC.API_PATH,
      NORMAL_TOPIC_UUID,
      ITEM_UUID,
      VERSION
    );

    // Make sure the key resource is deleted.
    await checkKeyResource(false);
  });
});

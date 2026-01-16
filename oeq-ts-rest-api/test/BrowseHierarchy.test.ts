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

describe('Browse hierarchy', () => {
  it('should be able to get all root hierarchies', async () => {
    const result = await OEQ.BrowseHierarchy.browseRootHierarchies(TC.API_PATH);
    expect(result).toHaveLength(9);
  });

  it('should be able to get all sub hierarchies of a provided hierarchy', async () => {
    const compoundUuid =
      '46249813-019d-4d14-b772-2a8ca0120c99:SG9iYXJ0,886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw==';
    const result = await OEQ.BrowseHierarchy.browseSubHierarchies(
      TC.API_PATH,
      compoundUuid
    );
    expect(result).toHaveLength(1);
  });

  it('should be able to get a hierarchy', async () => {
    const compoundUuid =
      '46249813-019d-4d14-b772-2a8ca0120c99:SG9iYXJ0,886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw==';
    const result = await OEQ.BrowseHierarchy.browseHierarchyDetails(
      TC.API_PATH,
      compoundUuid
    );
    expect(result.summary.compoundUuid).toEqual(compoundUuid);
    expect(result.keyResources).toHaveLength(2);
    expect(result.parents).toHaveLength(1);
    expect(result.children).toHaveLength(1);
  });

  it('should be able to get hierarchy IDs with given key resource', async () => {
    const BOOK_ITEM_UUID = 'cadcd296-a4d7-4024-bb5d-6c7507e6872a';
    const JAMES_HIERARCHY_UUID =
      '886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw==';

    const result = await OEQ.BrowseHierarchy.getHierarchyIdsWithKeyResource(
      TC.API_PATH,
      BOOK_ITEM_UUID,
      2
    );
    expect(result[0]).toBe(JAMES_HIERARCHY_UUID);
  });
});

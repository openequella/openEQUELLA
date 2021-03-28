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

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

const STATUS_LIVE = 'LIVE';
const STATUS_PERSONAL = 'PERSONAL';

const doSearch = async (params?: OEQ.Search.SearchParams) =>
  OEQ.Search.search(TC.API_PATH, params);

describe('Search for items', () => {
  it('should be able to search without params', async () => {
    const searchResult = await doSearch();
    // The default start is 0 and length of search results is 10.
    expect(searchResult.start).toBe(0);
    expect(searchResult).toHaveLength(10);
    expect(searchResult.available).toBeGreaterThan(0);
    expect(searchResult.results).toHaveLength(10);
  });

  it('should be able to search with params', async () => {
    const collection = 'a77112e6-3370-fd02-6ac6-6bc5aec22001';
    const searchParams: OEQ.Search.SearchParams = {
      query: 'API',
      status: [STATUS_LIVE],
      collections: [collection],
    };
    const searchResult = await doSearch(searchParams);
    const { uuid, status, collectionId } = searchResult.results[0];

    expect(searchResult.results).toHaveLength(searchResult.results.length);
    expect(searchResult.highlight).toEqual([searchParams.query]);
    expect(uuid).toBeTruthy();
    // Status returned is in lowercase so have to convert to uppercase.
    expect(status.toUpperCase()).toBe<OEQ.Common.ItemStatus>(STATUS_LIVE);
    expect(collectionId).toBe(collection);
  });

  it('should get 404 response if search for a non-existing collection', async () => {
    await expect(
      doSearch({ collections: ['testing collection'] })
    ).rejects.toHaveProperty('status', 404);
  });

  it('should return results which match one of multiple statuses', async () => {
    const results = await doSearch({ status: [STATUS_LIVE, STATUS_PERSONAL] });
    const findItemByStatus = (status: string) =>
      results.results.find((i) => i.status.toUpperCase() === status);

    expect(findItemByStatus(STATUS_LIVE)).toBeTruthy();
    expect(findItemByStatus(STATUS_PERSONAL)).toBeTruthy();
  });

  it('should return results which match one of multiple MIME types', async () => {
    const searchResult = await doSearch({
      mimeTypes: ['text/plain', 'application/pdf'],
    });
    expect(searchResult.results).toHaveLength(7);
  });

  it("supports a list of 'musts' specifications", async () => {
    const searchResult = await doSearch({
      musts: [
        ['moderating', ['true']],
        [
          'uuid',
          [
            'ab16b5f0-a12e-43f5-9d8b-25870528ad41',
            '24b977ec-4df4-4a43-8922-8ca6f82a296a',
          ],
        ],
      ],
    });
    expect(searchResult.results).toHaveLength(2);
  });

  it.each<[string, [string, string[]][]]>([
    ['Empty field name', [['', ['a value']]]],
    [
      'Empty field name - whitespace',
      [[' ', ['a value for whitespace field']]],
    ],
    ['Empty value array', [['field-no-values', []]]],
    ['Empty value', [['field-empty-value', ['']]]],
    ['Empty value - whitespace', [['field-empty-value-whitespace', [' ']]]],
    ['Colon in field name', [['field:colon', ['value-no-colon']]]],
    ['Colon in value', [['field-no-colon', ['value:colon']]]],
  ])(
    "attempts to validate the 'musts' client side before sending [%s]",
    async (_, musts) => {
      await expect(doSearch({ musts })).rejects.toThrow(TypeError);
    }
  );
});

describe('Search for attachments', () => {
  test.each([
    ['include attachments', true, 4],
    ['exclude attachments', false, 3],
  ])(
    'should be possible to %s in a search',
    async (
      _testName: string,
      searchAttachments: boolean,
      expectResultCount: number
    ) => {
      const searchParams: OEQ.Search.SearchParams = {
        query: 'size',
        searchAttachments: searchAttachments,
      };
      const searchResult = await doSearch(searchParams);
      expect(searchResult.results).toHaveLength(expectResultCount);
    }
  );
});

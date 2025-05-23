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
import { pipe } from 'fp-ts/function';
import * as NEA from 'fp-ts/NonEmptyArray';
import * as OEQ from '../src';
import { GET } from '../src/AxiosInstance';

import * as TC from './TestConfig';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => logout(TC.API_PATH));

const STATUS_LIVE = 'LIVE';
const STATUS_PERSONAL = 'PERSONAL';

const doSearch = async (params?: OEQ.Search.SearchParams) =>
  OEQ.Search.search(TC.API_PATH, params);

const doSearchWithPOST = async (params?: OEQ.Search.SearchParams) =>
  OEQ.Search.searchWithPOST(TC.API_PATH, params);

describe('Search with GET:', () => {
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
      const results = await doSearch({
        status: [STATUS_LIVE, STATUS_PERSONAL],
        length: 20,
      });
      const findItemByStatus = (status: string) =>
        results.results.find((i) => i.status.toUpperCase() === status);

      expect(findItemByStatus(STATUS_LIVE)).toBeTruthy();
      expect(findItemByStatus(STATUS_PERSONAL)).toBeTruthy();
    });

    it('should return results which match one of multiple MIME types', async () => {
      const searchResult = await doSearch({
        query: 'Search2 API test',
        mimeTypes: ['text/plain', 'image/png'],
      });
      expect(searchResult.results).toHaveLength(2);
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

  describe('Params related to attachments', () => {
    test.each([
      ['within attachments and their metadata', true, 4],
      ['within item metadata only (exclude searching attachments)', false, 3],
    ])(
      'should be possible to search %s',
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

    it('is possible to exclude attachment details from search results', async () => {
      const searchResults = await doSearch({
        includeAttachments: false,
        mimeTypes: ['application/pdf'],
      });
      const firstItem = searchResults.results[0];
      expect(firstItem.attachmentCount).toBeGreaterThan(0);
      expect(firstItem.attachments).toBeFalsy();
    });
  });

  describe('Dead attachment handling', () => {
    it.each<[string, string, number, number, boolean]>([
      [
        'an intact file attachment as not broken',
        'Keyword found in attachment test item',
        0,
        0,
        false,
      ],
      [
        'an intact resource selector attachment as not broken',
        'ItemApiViewTest - All attachments',
        0,
        1,
        false,
      ],
      [
        'a returned file attachment missing from the filestore as broken',
        'DeadAttachmentsTest',
        0,
        0,
        true,
      ],
      [
        'a resource attachment pointing at a non-existent attachment as broken',
        'NestedDeadResourceAttachmentTest - Child 1',
        0,
        0,
        true,
      ],
      [
        'a resource attachment pointing at a non-existent item summary as broken',
        'NestedDeadResourceAttachmentTest - Points at root item summary',
        0,
        0,
        true,
      ],
      [
        'a nested resource attachment with the end of the chain being a non-existent attachment as broken',
        'NestedDeadResourceAttachmentTest - Child 2',
        0,
        0,
        true,
      ],
    ])(
      'should mark %s',
      async (
        _: string,
        query: string,
        itemResultIndex: number,
        attachmentResultIndex: number,
        expectBrokenStatus: boolean
      ) => {
        const searchResult = await doSearch({ query: query });
        const { attachments } = searchResult.results[itemResultIndex];
        if (attachments === undefined) {
          throw new Error('Unexpected undefined attachments');
        }
        const brokenAttachment =
          attachments[attachmentResultIndex].brokenAttachment;
        expect(brokenAttachment).toEqual(expectBrokenStatus);
      }
    );
  });

  describe('Details for thumbnails', () => {
    it('provides no thumbnailDetails for items with no attachments', async () => {
      const searchResult = await doSearch({
        query: 'SearchApiTest - Basic',
      });

      expect(searchResult.results).toHaveLength(1);
      const thumbnailDetails = searchResult.results.pop()!.thumbnailDetails;
      expect(thumbnailDetails).toBeUndefined();
    });

    it('includes details for items which should have an explicit thumbnail', async () => {
      const searchResult = await doSearch({
        query: 'ItemApiViewTest - All attachments',
      });

      expect(searchResult.results).toHaveLength(1);
      const thumbnailDetails = searchResult.results.pop()!.thumbnailDetails;
      expect(thumbnailDetails).toBeDefined();
      // The expected item should have a fully qualified thumbnail, so the optional fields
      // are expected to be present.
      expect(thumbnailDetails!.mimeType).toBeDefined();
      expect(thumbnailDetails!.link).toBeDefined();
    });
  });
});

describe('Search with POST:', () => {
  it('should return results which large number of parameters', async () => {
    // create a large number of mime types
    const mimeTypes = pipe(
      NEA.range(1, 100),
      NEA.map((i) => 'text/plain' + i),
      NEA.concat(['text/plain', 'image/png'])
    );
    const searchResult = await doSearchWithPOST({
      mimeTypes: mimeTypes,
      query: 'Search2 API test',
    });

    expect(searchResult.results).toHaveLength(2);
  });
});

describe('Hierarchy search:', () => {
  it('should be able to search with criteria defined in a hierarchy topic', async () => {
    const results = await doSearch({
      hierarchy: '6135b550-ce1c-43c2-b34c-0a3cf793759d',
    });
    expect(results.available).toBe(58);
  });
});

describe('Advanced search:', () => {
  it('supports searching with advanced search params and normal params', async () => {
    const advancedParams: OEQ.Search.AdvancedSearchParams = {
      advancedSearchCriteria: [
        {
          schemaNodes: ['/item/name'],
          values: ['SearchApiTest - Crabs'],
          queryType: 'Tokenised',
        },
      ],
    };

    const normalParams: OEQ.Search.SearchParams = {
      status: ['LIVE'],
    };

    const searchResult = await OEQ.Search.searchWithAdvancedParams(
      TC.API_PATH,
      advancedParams,
      normalParams
    );
    expect(searchResult.available).toBe(6);
  });
});

describe('Exports search results for the specified search params', () => {
  const searchParams: OEQ.Search.SearchParams = {
    query: 'API',
    start: 0,
    length: 10,
    collections: ['a77112e6-3370-fd02-6ac6-6bc5aec22001'],
  };

  it('builds a full URL including query strings', () => {
    expect(OEQ.Search.buildExportUrl(TC.API_PATH, searchParams)).toBe(
      'http://localhost:8080/rest/api/search2/export?collections=a77112e6-3370-fd02-6ac6-6bc5aec22001&length=10&query=API&start=0'
    );
  });

  it('generates a URL which communicates to the export endpoints', async () => {
    await OEQ.Auth.login(TC.API_PATH, TC.USERNAME_SUPER, TC.PASSWORD_SUPER);
    const exportUrl = OEQ.Search.buildExportUrl(TC.API_PATH, searchParams);
    await expect(
      GET(exportUrl, (data): data is string => typeof data === 'string')
    ).resolves.toBeTruthy();
  });

  it('confirms if an export is valid by sending a HEAD request', async () => {
    await OEQ.Auth.login(TC.API_PATH, TC.USERNAME_SUPER, TC.PASSWORD_SUPER);
    await expect(
      OEQ.Search.confirmExportRequest(TC.API_PATH, searchParams)
    ).resolves.toBe(true);
  });
});

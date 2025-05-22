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
/* eslint jest/expect-expect: ["warn", { "assertFunctionNames": ["expect*"] }] */

import * as OEQ from '../src';
import { ItemStatus } from '../src/Common';
import * as TC from './TestConfig';
import { logout } from './TestUtils';
import JestMatchers = jest.JestMatchers;

const API_PATH = TC.API_PATH_FACET;
const defaultStatus: ItemStatus[] = ['LIVE'];

beforeAll(() => OEQ.Auth.login(API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => logout());

describe('Search for facets', () => {
  const nodeKeyword = '/item/keywords/keyword';
  const collectionProgramming = '20d40571-e577-4cd7-a12d-d46e5cefcd3f';
  const collectionHardware = 'b2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024e';

  const search = async (
    params: OEQ.SearchFacets.SearchFacetsParams
  ): Promise<OEQ.SearchFacets.SearchFacetsResult> =>
    OEQ.SearchFacets.searchFacets(API_PATH, params);

  const termPredicate = (term: string) => (facet: OEQ.SearchFacets.Facet) =>
    facet.term === term;

  const expectResults = (results: OEQ.SearchFacets.SearchFacetsResult) =>
    expect(results.results.length).toBeGreaterThan(0);

  const expectTerm = (
    results: OEQ.SearchFacets.SearchFacetsResult,
    term: string
  ): JestMatchers<OEQ.SearchFacets.Facet> =>
    // eslint-disable-next-line jest/valid-expect
    expect(results.results.find(termPredicate(term)));

  const expectTermPresent = (
    results: OEQ.SearchFacets.SearchFacetsResult,
    term: string
  ) => expectTerm(results, term).toBeTruthy();

  const expectTermAbsent = (
    results: OEQ.SearchFacets.SearchFacetsResult,
    term: string
  ) => expectTerm(results, term).toBeFalsy();

  it('should be possible to search with just a single node', async () => {
    const results = await search({
      nodes: [nodeKeyword],
      status: defaultStatus,
    });

    expectResults(results);
  });

  it('should be possible to search with multiple nodes', async () => {
    const results = await search({
      nodes: ['/item/category/@name', '/item/category/sub-category/@name'],
    });

    expectResults(results);
    // When you specify multiple nodes, the matching combinations form a term which is comma
    // delimited. So I know there is an item in the `epic` category an the `64` sub-category.
    expectTermPresent(results, 'epic,64');
  });

  it('should be possible to limit to a collection', async () => {
    const results = await search({
      nodes: [nodeKeyword],
      status: defaultStatus,
      collections: [collectionHardware],
    });

    expectResults(results);
    expectTermPresent(results, 'atmel');
    expectTermAbsent(results, 'jvm');
  });

  it('should be possible to limit to multiple collections', async () => {
    const results = await search({
      nodes: [nodeKeyword],
      status: defaultStatus,
      collections: [collectionHardware, collectionProgramming],
    });

    expectResults(results);
    expectTermPresent(results, 'atmel');
    expectTermPresent(results, 'jvm');
  });

  it('should be possible to filter by owner id', async () => {
    const params: OEQ.SearchFacets.SearchFacetsParams = {
      nodes: [nodeKeyword],
      status: defaultStatus,
      owner: 'TLE_ADMINISTRATOR', // has contributed items
    };

    // First, search with a user which has contributed
    expectResults(await search(params));

    // Now search with a user which has not contributed
    params.owner = 'f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a'; // demoteacher - someone who has not contributed
    const emptyResults = await search(params);
    expect(emptyResults.results).toHaveLength(0);
  });

  const maxResults = 24;
  const goodStartDate = '2020-08-01';
  const goodEndDate = '2020-08-18';
  it.each([
    [
      'Open ended period covering contribution',
      goodStartDate,
      undefined,
      maxResults,
    ],
    [
      'Open start period covering contribution',
      undefined,
      goodEndDate,
      maxResults,
    ],
    ['Known period of contribution', goodStartDate, goodEndDate, maxResults],
    ['Period after contribution', goodEndDate, undefined, 3],
  ])(
    'should be possible to filter by dates [%s]',
    async (_, modifiedAfter, modifiedBefore, expectedResults) => {
      const results = await search({
        nodes: [nodeKeyword],
        status: defaultStatus,
        modifiedAfter: modifiedAfter,
        modifiedBefore: modifiedBefore,
      });
      expect(results.results).toHaveLength(expectedResults);
    }
  );

  /**
   * In this test we are looking for our single draft item which has a keyword of 'draft'. It should
   * only be in the generated classifications/terms when status contains `DRAFT`.
   */
  it('should be possible to search with status filter', async () => {
    const params: OEQ.SearchFacets.SearchFacetsParams = {
      nodes: [nodeKeyword],
      status: defaultStatus,
    };
    const draftKeyword = 'draft';

    // Make sure it doesn't normally appear
    expectTermAbsent(await search(params), draftKeyword);

    // Then make sure it does when status include `DRAFT`
    params.status = ['DRAFT'];
    expectTermPresent(await search(params), draftKeyword);
  });

  it('should be possible to include a query string to filter items', async () => {
    const results = await search({
      nodes: [nodeKeyword],
      status: defaultStatus,
      query: 'scala',
    });
    expect(
      results.results.find((f) => f.term === 'jvm' && f.count === 1)
    ).toBeTruthy();
  });

  it('should be possible to limit to a MIME type', async () => {
    const results = await search({
      nodes: [nodeKeyword],
      status: defaultStatus,
      mimeTypes: ['text/plain'],
    });

    expectResults(results);
    expectTermPresent(results, 'Zilog');
    expectTermPresent(results, '8080');
  });
});

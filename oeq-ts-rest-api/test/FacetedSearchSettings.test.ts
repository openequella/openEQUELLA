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
import { BatchOperationResponse } from '../src/BatchOperationResponse';
import { FacetedSearchClassification } from '../src/FacetedSearchSettings';
import * as TC from './TestConfig';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => logout(TC.API_PATH));

describe('FacetedSearchSettings', () => {
  let facetedSearchClassificationAtStart: FacetedSearchClassification[];

  const isNumber = (value: unknown): value is number =>
    typeof value === 'number';

  const getIds = (facet: FacetedSearchClassification[]): number[] =>
    facet.map(({ id }) => id).filter((id) => isNumber(id)) as number[];

  const newFacetedSearchClassificationData = [
    {
      id: 0,
      name: 'first',
      schemaNode: 'string',
      maxResults: 0,
      orderIndex: 0,
    },
    {
      id: 0,
      name: 'second',
      schemaNode: 'string',
      maxResults: 0,
      orderIndex: 0,
    },
  ];

  const createNewFacetedSearchClassifications = async (): Promise<
    BatchOperationResponse[]
  > => {
    return OEQ.FacetedSearchSettings.batchUpdateFacetedSearchSetting(
      TC.API_PATH,
      newFacetedSearchClassificationData
    );
  };

  beforeAll(async () => {
    facetedSearchClassificationAtStart =
      await OEQ.FacetedSearchSettings.getFacetedSearchSettings(TC.API_PATH);
  });

  // Clear all faceted search classifications which were not present at the start of the test.
  afterEach(async () => {
    const facetedSearchClassificationAtEnd =
      await OEQ.FacetedSearchSettings.getFacetedSearchSettings(TC.API_PATH);

    const start_ids = getIds(facetedSearchClassificationAtStart);
    const end_ids = getIds(facetedSearchClassificationAtEnd);

    const ids = end_ids.filter((id) => !start_ids.includes(id));
    await OEQ.FacetedSearchSettings.batchDeleteFacetedSearchSetting(
      TC.API_PATH,
      ids
    );
  });

  it('Should be possible to retrieve the faceted search classifications', () =>
    expect(facetedSearchClassificationAtStart).toBeTruthy());

  it('Should be possible to create a batch of the faceted search classifications', async () => {
    // Create new faceted search classifications
    await createNewFacetedSearchClassifications();
    const allFacetedSearchClassifications =
      await OEQ.FacetedSearchSettings.getFacetedSearchSettings(TC.API_PATH);
    expect(allFacetedSearchClassifications).toHaveLength(
      facetedSearchClassificationAtStart.length +
        newFacetedSearchClassificationData.length
    );
  });

  it('Should be possible to get a faceted search classification by ID', async () => {
    // Create a new faceted search classification
    const responses = await createNewFacetedSearchClassifications();
    const firstId = parseInt(responses[0].id);
    const response =
      await OEQ.FacetedSearchSettings.getFacetedSearchSettingById(
        TC.API_PATH,
        firstId
      );
    expect(response).toMatchObject({
      ...newFacetedSearchClassificationData[0],
      id: firstId,
    });
  });

  it('Should be possible to update a batch of the faceted search classifications', async () => {
    // Create new faceted search classifications
    const responses = await createNewFacetedSearchClassifications();
    // Get new faceted search classifications ids and create update data
    const ids = responses.map(({ id }) => parseInt(id));
    const updateFacetedSearchClassificationsData = [
      {
        id: ids[0],
        name: 'faceted1-new-name',
        schemaNode: 'string',
        maxResults: 1,
        orderIndex: 1,
      },
      {
        id: ids[1],
        name: 'faceted2-new-name',
        schemaNode: 'string',
        maxResults: 2,
        orderIndex: 2,
      },
    ];

    // update filters
    await OEQ.FacetedSearchSettings.batchUpdateFacetedSearchSetting(
      TC.API_PATH,
      updateFacetedSearchClassificationsData
    );
    const firstFacetedSearchClassification =
      await OEQ.FacetedSearchSettings.getFacetedSearchSettingById(
        TC.API_PATH,
        ids[0]
      );
    const secondFacetedSearchClassification =
      await OEQ.FacetedSearchSettings.getFacetedSearchSettingById(
        TC.API_PATH,
        ids[1]
      );
    expect(firstFacetedSearchClassification).toMatchObject(
      updateFacetedSearchClassificationsData[0]
    );
    expect(secondFacetedSearchClassification).toMatchObject(
      updateFacetedSearchClassificationsData[1]
    );
  });

  it('Should be possible to delete a batch of the faceted search classifications', async () => {
    // Create new faceted search classifications
    const responses = await createNewFacetedSearchClassifications();
    const ids = responses.map(({ id }) => parseInt(id));

    // Delete
    await OEQ.FacetedSearchSettings.batchDeleteFacetedSearchSetting(
      TC.API_PATH,
      ids
    );
    const finalFilterSettings =
      await OEQ.FacetedSearchSettings.getFacetedSearchSettings(TC.API_PATH);
    expect(finalFilterSettings).toHaveLength(
      facetedSearchClassificationAtStart.length
    );
  });
});

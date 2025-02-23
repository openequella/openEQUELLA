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
import * as OEQ from '../src';
import { waitFor } from '../src/Utils';
import * as TC from './TestConfig';
import { BatchOperationResponse } from '../src/BatchOperationResponse';
import { MimeTypeFilter } from '../src/SearchFilterSettings';
import * as A from 'fp-ts/Array';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

describe('SearchFilterSettings', () => {
  let filterSettingsAtStart: MimeTypeFilter[];

  const isString = (value: unknown): value is string =>
    typeof value === 'string';

  const getIds = (mimeTypes: MimeTypeFilter[]): string[] =>
    mimeTypes.map(({ id }) => id).filter((id) => isString(id)) as string[];

  const newFilterSettingsData = [
    {
      name: 'filter1',
      mimeTypes: ['image/png'],
    },
    {
      name: 'filter2',
      mimeTypes: ['video/mp4'],
    },
  ];

  const createNewFilterSettings = async (): Promise<BatchOperationResponse[]> =>
    await OEQ.SearchFilterSettings.batchUpdateSearchFilterSetting(
      TC.API_PATH,
      newFilterSettingsData
    );

  beforeAll(async () => {
    filterSettingsAtStart =
      await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);
  });

  // Clear all filters which were not present at the start of the test.
  afterEach(async () => {
    const filterSettingsAtEnd =
      await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);

    const start_ids = getIds(filterSettingsAtStart);
    const end_ids = getIds(filterSettingsAtEnd);

    const ids = end_ids.filter((id) => !start_ids.includes(id));
    await OEQ.SearchFilterSettings.batchDeleteSearchFilterSetting(
      TC.API_PATH,
      ids
    );
  });

  it('Should be possible to retrieve the filter settings', () =>
    expect(filterSettingsAtStart).toBeTruthy());

  it('Should be possible to create a batch of the filter settings', async () => {
    // Create new filters
    const newFilterIds = await createNewFilterSettings();
    // Use waitFor to wait for the cache to be updated in the server.
    await waitFor(async () => {
      const allFilterSettings =
        await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);
      const allIds = getIds(allFilterSettings);
      expect(newFilterIds.every(({ id }) => allIds.includes(id))).toBe(true);
    });
  });

  it('Should be possible to update a batch of the filter settings', async () => {
    // Create new filter settings
    const responses = await createNewFilterSettings();
    // Get new filter ids and create update data
    const newFilterIds = responses.map(({ id }) => id);
    const filter1Id = newFilterIds[0];
    const filter2Id = newFilterIds[1];

    const updateFilterSettingsData = [
      {
        id: filter1Id,
        name: 'filter1-new-name',
        mimeTypes: ['image/png', 'video/mp4'],
      },
      {
        id: filter2Id,
        name: 'filter2-new-name',
        mimeTypes: ['image/png'],
      },
    ];

    // update filters
    await OEQ.SearchFilterSettings.batchUpdateSearchFilterSetting(
      TC.API_PATH,
      updateFilterSettingsData
    );

    const assertResultIsUpdated = async () => {
      const newFilterSettings =
        await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);
      const updatedFilterSettings = pipe(
        newFilterSettings,
        A.filter(({ id }) => id === filter1Id || id === filter2Id)
      );
      expect(updatedFilterSettings).toEqual(updateFilterSettingsData);
    };
    // Use waitFor to wait for the cache to be updated in the server.
    await waitFor(assertResultIsUpdated);
  });

  it('Should be possible to delete a batch of the filter settings', async () => {
    // Create new filter settings
    const responses = await createNewFilterSettings();
    const newFilterIds = responses.map(({ id }) => id);

    // Delete filters
    await OEQ.SearchFilterSettings.batchDeleteSearchFilterSetting(
      TC.API_PATH,
      newFilterIds
    );

    // Use waitFor to wait for the cache to be updated in the server.
    await waitFor(async () => {
      const finalFilterSettings =
        await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);
      const finalIds = getIds(finalFilterSettings);
      expect(newFilterIds.every((id) => !finalIds.includes(id))).toBe(true);
    });
  });
});

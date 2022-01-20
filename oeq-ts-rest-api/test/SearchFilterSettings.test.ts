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
import { MimeTypeFilter } from '../src/SearchFilterSettings';
import * as TC from './TestConfig';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

describe('SearchFilterSettings', () => {
  let filterSettingsAtStart: MimeTypeFilter[];

  beforeAll(async () => {
    filterSettingsAtStart =
      await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);
  });

  // clear all filter which is not appear at start
  afterAll(async () => {
    const filterSettingsAtEnd =
      await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);

    const start_ids = filterSettingsAtStart
      .filter((fs) => fs.id != undefined)
      .map((fs) => fs.id) as string[];
    const end_ids = filterSettingsAtEnd
      .filter((fs) => fs.id != undefined)
      .map((fs) => fs.id) as string[];

    const ids = end_ids.filter((id) => !start_ids.includes(id));
    await OEQ.SearchFilterSettings.batchDeleteSearchFilterSetting(
      TC.API_PATH,
      ids
    );
  });

  it('Should be possible to retrieve the filter settings', () =>
    expect(filterSettingsAtStart).toBeTruthy());

  it('Should be possible to create/change and delete a batch of the filter settings', async () => {
    // create new filter
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

    await OEQ.SearchFilterSettings.batchUpdateSearchFilterSetting(
      TC.API_PATH,
      newFilterSettingsData
    );
    const newFilterSettings =
      await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);
    expect(newFilterSettings).toHaveLength(2);

    // get new filter ids and create update data
    const ids = newFilterSettings
      .filter((fs) => fs.id != undefined)
      .map((fs) => fs.id) as string[];
    const updateFilterSettingsData = [
      {
        id: ids[0],
        name: 'filter1-new-name',
        mimeTypes: ['image/png', 'video/mp4'],
      },
      {
        id: ids[1],
        name: 'filter2-new-name',
        mimeTypes: ['image/png'],
      },
    ];

    // update filter
    await OEQ.SearchFilterSettings.batchUpdateSearchFilterSetting(
      TC.API_PATH,
      updateFilterSettingsData
    );
    const updatedFilterSettings =
      await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);
    expect(updatedFilterSettings).toEqual(updateFilterSettingsData);

    // delete filters
    await OEQ.SearchFilterSettings.batchDeleteSearchFilterSetting(
      TC.API_PATH,
      ids
    );

    const finalFilterSettings =
      await OEQ.SearchFilterSettings.getSearchFilterSettings(TC.API_PATH);
    expect(finalFilterSettings).toHaveLength(filterSettingsAtStart.length);
  });
});

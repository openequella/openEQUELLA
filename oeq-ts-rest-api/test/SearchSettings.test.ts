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
afterAll(() => OEQ.Auth.logout(TC.API_PATH));

describe('SearchSettings', () => {
  describe('General settings', () => {
    const defaultSearchSettings: OEQ.SearchSettings.Settings = {
      searchingShowNonLiveCheckbox: false,
      searchingDisableGallery: false,
      searchingDisableVideos: false,
      searchingDisableOwnerFilter: false,
      searchingDisableDateModifiedFilter: false,
      fileCountDisabled: false,
      authenticateFeedsByDefault: false,
      urlLevel: 0,
      titleBoost: 5,
      descriptionBoost: 3,
      attachmentBoost: 2,
    };

    it('retrieves general Search settings in an institution', async () => {
      const settings = await OEQ.SearchSettings.getSearchSettings(TC.API_PATH);
      expect(settings).toBeTruthy();
    });

    it('updates general search settings in an institution', async () => {
      await OEQ.SearchSettings.updateSearchSettings(TC.API_PATH, {
        ...defaultSearchSettings,
        defaultSearchSort: 'rating',
      });

      const settings = await OEQ.SearchSettings.getSearchSettings(TC.API_PATH);
      expect(settings.defaultSearchSort).toBe('rating');
    });
  });
});

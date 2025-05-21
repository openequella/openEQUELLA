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
import { getViewersForMimeType, listMimeTypes } from '../src/MimeType';
import * as TC from './TestConfig';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(TC.API_PATH));

describe('listMimeTypes', () => {
  it('lists MIME types for the collection', async () => {
    const mimeTypes = await listMimeTypes(TC.API_PATH);
    expect(mimeTypes.length).toBeGreaterThan(0);
  });
});

describe('getViewersForMimeType', () => {
  it('can retrieve the viewer configuration for each MIME type on the server', async () => {
    const allMimeTypes = await listMimeTypes(TC.API_PATH);
    for (const mt of allMimeTypes) {
      const viewerConfig = await getViewersForMimeType(
        TC.API_PATH,
        mt.mimeType
      );
      expect(viewerConfig.defaultViewer).toBeTruthy();
    }
  });
});

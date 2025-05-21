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
import {
  acceptDrmTerms,
  ItemDrmDetails,
  listDrmViolations,
  listDrmTerms,
} from '../src/Drm';
import * as TC from './TestConfig';

const ITEM_UUID = 'ea61a83c-b18b-49e2-8096-e6208776be92';
const UNAUTHORISED_ITEM_UUID = '73b67c33-aa72-419f-87aa-72d919fcf9f0';
const ITEM_VERSION = 1;

beforeAll(() => OEQ.Auth.login(TC.API_PATH_FIVEO, TC.USERNAME, TC.PASSWORD));
afterAll(() => OEQ.Auth.logout(TC.API_PATH_FIVEO));

describe('listDrmTerms', () => {
  it('lists DRM terms for an Item', async () => {
    const terms: ItemDrmDetails = await listDrmTerms(
      TC.API_PATH_FIVEO,
      ITEM_UUID,
      ITEM_VERSION
    );
    expect(terms).not.toBeNull();
  });
});

describe('acceptDrmTerms', () => {
  it('supports accepting DRM terms for an Item', async () => {
    await expect(
      acceptDrmTerms(TC.API_PATH_FIVEO, ITEM_UUID, ITEM_VERSION)
    ).resolves.not.toThrow();
  });
});

describe('listDrmViolations', () => {
  it('supports listing DRM violations', async () => {
    const { violation }: OEQ.Drm.DrmViolation = await listDrmViolations(
      TC.API_PATH_FIVEO,
      UNAUTHORISED_ITEM_UUID,
      ITEM_VERSION
    );
    expect(violation).toBeTruthy();
  });
});

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
import * as LtiPlatformModule from '../src/LtiPlatform';
import * as TC from './TestConfig';

const LTI_ROLE_TEACHER =
  'http://purl.imsglobal.org/vocab/lis/v2/membership#Teacher';
const OEQ_ROLE_TEACHER = '5553c1cb-109d-474a-a76b-d4af3859a467';
const OEQ_ROLE_TUTOR = '4443c1cb-109d-474a-a76b-d4af3859a467';
const OEQ_ROLE_BUILDER = '3333c1cb-109d-474a-a76b-d4af3859a467';

const BRIGHTSPACE_PLATFORM_ID = 'http://localhost:8300';
const MOODLE_PLATFORM_ID = 'http://localhost:8100';
const CANVAS_PLATFORM_ID = 'http://localhost:8200';

const platform: LtiPlatformModule.LtiPlatform = {
  authUrl: 'http://test',
  name: 'Test name',
  clientId: 'test',
  customRoles: new Map([[LTI_ROLE_TEACHER, new Set([OEQ_ROLE_TEACHER])]]),
  enabled: true,
  instructorRoles: new Set([OEQ_ROLE_TUTOR]),
  keysetUrl: 'http://test',
  platformId: BRIGHTSPACE_PLATFORM_ID,
  unknownRoles: new Set([OEQ_ROLE_BUILDER]),
  unknownUserHandling: 'ERROR',
};

const mockConvertToRawLtiPlatform = jest.spyOn(
  LtiPlatformModule,
  'convertToRawLtiPlatform'
);

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(TC.API_PATH));

describe('getAllPlatforms', () => {
  it('lists all the LTI platforms for the Institution', async () => {
    const platforms = await LtiPlatformModule.getAllPlatforms(TC.API_PATH);
    expect(platforms.length).toBeGreaterThan(0);
  });
});

describe('getPlatformById', () => {
  it('retrieves a LTI platform by a double URL encoded platform ID', async () => {
    const platform = await LtiPlatformModule.getPlatformById(
      TC.API_PATH,
      MOODLE_PLATFORM_ID
    );
    expect(platform.platformId).toBe('http://localhost:8100');
  });
});

describe('createPlatform', () => {
  it('converts the provided LTI platform to raw data structure and then creates a LTI platform', async () => {
    await expect(
      LtiPlatformModule.createPlatform(TC.API_PATH, platform)
    ).resolves.not.toThrow();
    expect(mockConvertToRawLtiPlatform).toHaveBeenCalledWith(platform);
  });
});

describe('updatePlatform', () => {
  it('converts the provided LTI platform to raw data structure and then updates a LTI platform', async () => {
    const update: LtiPlatformModule.LtiPlatform = {
      ...platform,
      unknownUserHandling: 'CREATE',
    };
    await expect(
      LtiPlatformModule.updatePlatform(TC.API_PATH, update)
    ).resolves.not.toThrow();
    expect(mockConvertToRawLtiPlatform).toHaveBeenCalledWith(update);
  });
});

describe('rotateKeyPair', () => {
  it('rotate Key Pair for an LTI platform by a double URL encoded platform ID', async () => {
    const originalPlatform = await LtiPlatformModule.getPlatformById(
      TC.API_PATH,
      platform.platformId
    );
    const newKeySetId = await LtiPlatformModule.rotateKeyPair(
      TC.API_PATH,
      platform.platformId
    );
    // make sure kid has been updated
    expect(newKeySetId).not.toEqual(originalPlatform.kid);
  });
});

describe('deletePlatformById', () => {
  it('deletes a LTI platform by a double URL encoded platform ID', async () => {
    await expect(
      LtiPlatformModule.deletePlatformById(TC.API_PATH, BRIGHTSPACE_PLATFORM_ID)
    ).resolves.not.toThrow();
  });
});

describe('updateEnabledPlatforms', () => {
  it('enable multiple LTI platforms', async () => {
    // get initial enabled value
    const initialEnabledStatusForMoodle = (
      await LtiPlatformModule.getPlatformById(TC.API_PATH, MOODLE_PLATFORM_ID)
    ).enabled;
    const initialEnabledStatusForCanvas = (
      await LtiPlatformModule.getPlatformById(TC.API_PATH, CANVAS_PLATFORM_ID)
    ).enabled;

    const targetEnabledStatusForMoodle = !initialEnabledStatusForMoodle;
    const targetEnabledStatusForCanvas = !initialEnabledStatusForCanvas;

    const responses = await LtiPlatformModule.updateEnabledPlatforms(
      TC.API_PATH,
      [
        {
          platformId: MOODLE_PLATFORM_ID,
          enabled: targetEnabledStatusForMoodle,
        },
        {
          platformId: CANVAS_PLATFORM_ID,
          enabled: targetEnabledStatusForCanvas,
        },
      ]
    );
    expect(responses.filter(({ status }) => status === 200)).toHaveLength(2);
  });
});

describe('deletePlatforms', () => {
  it('deletes multiple LTI platforms', async () => {
    const responses = await LtiPlatformModule.deletePlatforms(TC.API_PATH, [
      MOODLE_PLATFORM_ID,
      CANVAS_PLATFORM_ID,
    ]);
    expect(responses.filter(({ status }) => status === 200)).toHaveLength(2);
  });
});

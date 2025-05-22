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
import { UISettings } from '../src/Settings';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => logout());

test("That we're able to retrieve general settings", async () => {
  const settings = await OEQ.Settings.getGeneralSettings(TC.API_PATH);
  expect(settings.length).toBeGreaterThan(0);
});

describe('UI Settings', () => {
  let settingsAtStart: UISettings;
  beforeAll(async () => {
    settingsAtStart = await OEQ.Settings.getUiSettings(TC.API_PATH);
  });

  afterAll(() => OEQ.Settings.updateUiSettings(TC.API_PATH, settingsAtStart));

  it('Should be possible to retrieve the UI settings', () =>
    expect(settingsAtStart).toBeTruthy());

  it('Should be possible to change the settings', async () => {
    await OEQ.Settings.updateUiSettings(TC.API_PATH, {
      newUI: {
        ...settingsAtStart.newUI,
        enabled: !settingsAtStart.newUI.enabled,
      },
    });
    const settings = await OEQ.Settings.getUiSettings(TC.API_PATH);
    expect(settings.newUI.enabled).toEqual(!settingsAtStart.newUI.enabled);
  });
});

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
import { ThemeSettings } from '../src/Theme';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => logout());

describe('Theme settings', () => {
  let settingsAtStart: ThemeSettings;

  beforeAll(async () => {
    settingsAtStart = await OEQ.Theme.getThemeSettings(TC.API_PATH);
  });

  afterAll(async () => {
    await OEQ.Theme.updateThemeSettings(TC.API_PATH, settingsAtStart);
  });

  it('Should be able to retrieve theme settings', async () => {
    expect(settingsAtStart).not.toBeNull();
  });

  it('Should be possible to change the theme settings', async () => {
    await OEQ.Theme.updateThemeSettings(TC.API_PATH, {
      ...settingsAtStart,
      fontSize: 666,
    });
    const newSettings = await OEQ.Theme.getThemeSettings(TC.API_PATH);
    expect(newSettings).not.toEqual(settingsAtStart);
    expect(newSettings.fontSize).toBe(666);
  });
});

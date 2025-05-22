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
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => logout());

describe('Listing collections', () => {
  it('should be possible list collections with no params', async () => {
    const result = await OEQ.Collection.listCollections(TC.API_PATH);
    expect(result.length).toBeGreaterThan(0);
    expect(result.results).toHaveLength(result.length);
  });

  it('should be possible to retrieve a custom list of collections via params', async () => {
    await OEQ.Auth.logout(TC.API_PATH);
    await OEQ.Auth.login(TC.API_PATH, TC.USERNAME_SUPER, TC.PASSWORD_SUPER);
    const howMany = 8;
    const result = await OEQ.Collection.listCollections(TC.API_PATH, {
      length: howMany,
      full: true,
    });
    expect(result).toHaveLength(howMany);
    // confirm that `full` returned additional information
    expect(result.results[0].createdDate).toBeTruthy();
    expect((result.results[0] as OEQ.Collection.Collection).filestoreId).toBe(
      'default'
    );
  });
});

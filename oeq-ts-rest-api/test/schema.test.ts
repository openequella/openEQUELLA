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
import { EquellaSchema } from '../src/Schema';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => logout(TC.API_PATH));

describe('Retrieving schemas', () => {
  it('should be possible get schemas with no params', () => {
    expect.assertions(2);

    return OEQ.Schema.listSchemas(TC.API_PATH).then(
      (pagedResult: OEQ.Common.PagedResult<OEQ.Common.BaseEntity>) => {
        expect(pagedResult.length).toBeGreaterThan(0);
        expect(pagedResult).toHaveLength(pagedResult.results.length);
      }
    );
  });

  it('should be possible to get schemas customised with params', () => {
    expect.assertions(3);
    const howMany = 2;

    return OEQ.Schema.listSchemas(TC.API_PATH, {
      length: howMany,
      full: true,
    }).then((pagedResult: OEQ.Common.PagedResult<OEQ.Common.BaseEntity>) => {
      const result = pagedResult as OEQ.Common.PagedResult<EquellaSchema>;
      expect(result.results).toHaveLength(howMany);
      // confirm that `full` returned additional information
      expect(result.results[0].createdDate).toBeTruthy();
      expect(result.results[0].definition).toBeTruthy();
    });
  });
});

describe('Retrieval of a specific schema', () => {
  it('Should be possible to retrieve a known schema', () => {
    expect.assertions(2);
    const targetUuid = '71a27a31-d6b0-4681-b124-6db410ed420b';

    return OEQ.Schema.getSchema(TC.API_PATH, targetUuid).then(
      (result: EquellaSchema) => {
        expect(result.uuid).toBe(targetUuid);
        // Better make sure we got a schema
        expect(result.definition).toBeTruthy();
      }
    );
  });

  it('Should result in a 404 when attempting to retrieve an unknown UUID', async () => {
    await expect(() =>
      OEQ.Schema.getSchema(TC.API_PATH, 'fake-uuid')
    ).rejects.toThrow(
      new OEQ.Errors.ApiError('Entity with UUID fake-uuid not found', 404)
    );
  });
});

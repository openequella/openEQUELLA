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
import { SearchResult, tokens } from '../src/UserQuery';
import * as TC from './TestConfig';
import { logout } from './TestUtils';

// We use Vanilla for this one so that we also have 'roles' to test with.
const API_PATH = TC.API_PATH_VANILLA;

beforeAll(() => OEQ.Auth.login(API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => logout(API_PATH));

describe('/userquery/search', () => {
  test.each<keyof SearchResult>(['users', 'roles', 'groups'])(
    'should be possible to list %s',
    async (property: keyof SearchResult) => {
      const result = await OEQ.UserQuery.search(API_PATH, {
        // Disable all ...
        users: false,
        roles: false,
        groups: false,
        // ... and enable the one under test
        [property]: true,
      });
      const entities = result[property];
      expect(entities).toBeTruthy();
      expect(entities.length).toBeGreaterThan(0);
    }
  );

  it('should be possible to list a combination', async () => {
    const result = await OEQ.UserQuery.search(API_PATH, {
      users: true,
      roles: true,
      groups: false,
    });
    expect(result).toBeTruthy();
    expect(result.users.length).toBeGreaterThan(0);
    expect(result.roles.length).toBeGreaterThan(0);
    expect(result.groups).toHaveLength(0);
  });

  it('should be possible to list all types', async () => {
    const result = await OEQ.UserQuery.search(API_PATH, {
      users: true,
      roles: true,
      groups: true,
    });
    expect(result).toBeTruthy();
    expect(result.users.length).toBeGreaterThan(0);
    expect(result.roles.length).toBeGreaterThan(0);
    expect(result.groups.length).toBeGreaterThan(0);
  });

  it('should be possible to filter with a query string', async () => {
    const query = 'user';
    const result = await OEQ.UserQuery.search(API_PATH, {
      q: query,
      users: true,
      roles: true,
      groups: true,
    });
    const names = result.users
      .map((u) => u.username)
      .concat(result.groups.map((g) => g.name))
      .concat(result.roles.map((r) => r.name))
      .map((n) => n.toLowerCase());
    const doesNotContainQuery = (testValue: string) =>
      testValue.search(query) === -1;
    // There should be no names in the list which don't contain the query
    expect(names.find(doesNotContainQuery)).toBeFalsy();
  });
});

describe('/userquery/lookup', () => {
  const autoTestUser = {
    id: '20c8cfd5-6318-4251-a353-5b0b5a7994c3',
    username: 'AutoTest',
    firstName: 'auto',
    lastName: 'test',
    email: 'AutoTest@autotest.com',
  };
  const systemAdministratorGroup = {
    id: 'e91205b0-684e-51e2-a1be-3ab646aa98dd',
    name: 'Users with Administrator Role',
  };
  const ssoRole = {
    id: '2237ce17-de72-57a8-7321-aadca36b3ec4',
    name: 'Role from SSO',
  };

  it('should be possible to lookup a combination', async () => {
    const result = await OEQ.UserQuery.lookup(API_PATH, {
      users: [autoTestUser.id],
      groups: [systemAdministratorGroup.id],
      roles: [],
    });
    expect(result).toBeTruthy();
    expect(result.users).toHaveLength(1);
    expect(result.groups).toHaveLength(1);
    expect(result.roles).toHaveLength(0);
  });

  test.each<[keyof SearchResult, string]>([
    ['users', autoTestUser.id],
    ['groups', systemAdministratorGroup.id],
    ['roles', ssoRole.id],
  ])('Should be possible to lookup a %s with id of %s', async (type, id) => {
    const result = await OEQ.UserQuery.lookup(API_PATH, {
      users: [],
      groups: [],
      roles: [],
      //overwrite the one that is being tested
      [type]: [id],
    });
    const entities = result[type];
    expect(entities).toBeTruthy();
    expect(entities).toHaveLength(1);
    expect(entities).toMatchObject([{ id: id }]);
  });
});

describe('filter feature for user and group search', () => {
  it.each([
    ['users', OEQ.UserQuery.filtered, 'auto', 3],
    ['groups', OEQ.UserQuery.filteredGroups, 'Student', 1],
  ])(
    'returns %s matching the "q" param',
    async (_, searchWithFilter, queryText, expectedEntityNumber) => {
      const entities = await searchWithFilter(API_PATH, { q: queryText });
      expect(entities).toHaveLength(expectedEntityNumber);
    }
  );

  it.each([
    [
      'users',
      OEQ.UserQuery.filtered,
      'auto',
      'e91205b0-684e-51e2-a1be-3ab646aa98dd',
      1,
    ],
    [
      'groups',
      OEQ.UserQuery.filteredGroups,
      'Sub',
      '144ef487-97ba-4843-915e-24af78cde991',
      2,
    ],
  ])(
    'filters the %s matching the "q" param based on the byGroups param',
    async (
      _,
      searchWithFilter,
      queryText,
      filterGroupId,
      expectedEntityNumber
    ) => {
      const entities = await searchWithFilter(API_PATH, {
        q: queryText,
        byGroups: [filterGroupId],
      });
      expect(entities).toHaveLength(expectedEntityNumber);
    }
  );

  it.each([
    ['users', OEQ.UserQuery.filtered, 13],
    ['groups', OEQ.UserQuery.filteredGroups, 10],
  ])(
    'returns all %s if no params are specified',
    async (_, searchWithFilter, expectedEntityNumber) => {
      const entities = await searchWithFilter(API_PATH, {});
      expect(entities).toHaveLength(expectedEntityNumber);
    }
  );
});

describe('/userquery/tokens', () => {
  it('returns all shared secrets', async () => {
    const secrets = await tokens(API_PATH);
    expect(secrets).toEqual(['sso_ip', 'sso_group']);
  });
});

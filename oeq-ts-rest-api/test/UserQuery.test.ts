import * as OEQ from '../src';
import * as TC from './TestConfig';
import { SearchResult } from '../src/UserQuery';

// We use Vanilla for this one so that we also have 'roles' to test with.
const API_PATH = TC.API_PATH_VANILLA;

beforeAll(() => OEQ.Auth.login(API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(API_PATH, true));

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

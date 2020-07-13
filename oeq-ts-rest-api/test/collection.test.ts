import * as OEQ from '../src';
import * as TC from './TestConfig';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

describe('Listing collections', () => {
  it('should be possible list collections with no params', async () => {
    const result = await OEQ.Collection.listCollections(TC.API_PATH);
    expect(result.length).toBeGreaterThan(0);
    expect(result.results).toHaveLength(result.length);
  });

  it('should be possible to retrieve a custom list of collections via params', async () => {
    const howMany = 8;
    const result = await OEQ.Collection.listCollections(TC.API_PATH, {
      length: howMany,
      full: true,
    });
    expect(result).toHaveLength(howMany);
    // confirm that `full` returned additional information
    expect(result.results[0].createdDate).toBeTruthy();
    expect(
      (result.results[0] as OEQ.Collection.Collection).filestoreId
    ).toEqual('default');
  });
});

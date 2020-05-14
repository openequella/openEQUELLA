import * as OEQ from '../src';
import * as TC from './TestConfig';
import { EquellaSchema } from '../src/Schema';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

describe('Retrieving schemas', () => {
  it('should be possible get schemas with no params', () => {
    expect.assertions(2);

    return OEQ.Schema.listSchemas(TC.API_PATH, null).then(
      (pagedResult: OEQ.Common.PagedResult<OEQ.Common.BaseEntity>) => {
        expect(pagedResult.length).toBeGreaterThan(0);
        expect(pagedResult.length).toEqual(pagedResult.results.length);
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
      expect(result.results.length).toBe(howMany);
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

  it('Should result in a 404 when attempting to retrieve an unknown UUID', () => {
    expect.assertions(2);

    return OEQ.Schema.getSchema(TC.API_PATH, 'fake-uuid').catch(
      (error: OEQ.Errors.ApiError) => {
        expect(error.status).toBe(404);
        expect(error.errorResponse).toBeTruthy();
      }
    );
  });
});

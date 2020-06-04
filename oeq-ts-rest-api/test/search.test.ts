import * as Search from '../src/Search';
import * as SearchResult from '../src/SearchResult';
import * as Common from '../src/Common';
import * as Auth from '../src/Auth';
import * as TC from './TestConfig';
import * as Errors from "../src/Errors";

beforeAll(() => Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => Auth.logout(TC.API_PATH, true));

describe("Search for items", () => {
  const SEARCH_API_PATH = `${TC.API_PATH}/search2`;

  it("should be able to search without params", () => {
    expect.assertions(4);
    return Search.search(SEARCH_API_PATH).then(
      (pagedResult: Common.PagedResult<SearchResult.Item>) => {
        // The default start is 0 and length of search results is 10.
        expect(pagedResult.start).toBe(0);
        expect(pagedResult).toHaveLength(10);
        expect(pagedResult.available).toBeGreaterThan(0);
        expect(pagedResult.results).toHaveLength(10)
      }
    )
  });

  it("should be able to search with params", () => {
    expect.assertions(4);
    const collection = "a77112e6-3370-fd02-6ac6-6bc5aec22001";
    const searchParams: Search.SearchParams = {
      query: "API",
      status: [SearchResult.ItemStatus.LIVE],
      collections: [collection]
    };
    return Search.search(SEARCH_API_PATH, searchParams).then(
      (pagedResult: Common.PagedResult<SearchResult.Item>) => {
        const {uuid, status, collectionId} = pagedResult.results[0];
        expect(pagedResult).toHaveLength(pagedResult.results.length);
        expect(uuid).toBeTruthy();
        // Status returned is in lowercase so have to convert to uppercase.
        expect(status.toUpperCase()).toBe(SearchResult.ItemStatus.LIVE);
        expect(collectionId).toBe(collection);
      }
    )
  });

  it("should get 404 response if search for an non-existing collection", () => {
    expect.assertions(2);
    return Search.search(SEARCH_API_PATH, {collections: ["testing collection"]}).catch(
      (error: Errors.ApiError) => {
        expect(error.status).toBe(404);
        expect(error.errorResponse).toBeTruthy();
      }
    )
  })
});

import * as TC from './TestConfig';
import * as OEQ from '../src';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

describe("Search for items", () => {
  it("should be able to search without params", async () => {
    const searchResult = await OEQ.Search.search(TC.API_PATH);
    // The default start is 0 and length of search results is 10.
    expect(searchResult.start).toBe(0);
    expect(searchResult).toHaveLength(10);
    expect(searchResult.available).toBeGreaterThan(0);
    expect(searchResult.results).toHaveLength(10)
  });

  it("should be able to search with params", async () => {
    const collection = "a77112e6-3370-fd02-6ac6-6bc5aec22001";
    const searchParams: OEQ.Search.SearchParams = {
      query: "API",
      status: [OEQ.Common.ItemStatus.LIVE],
      collections: [collection]
    };
    const searchResult = await OEQ.Search.search(TC.API_PATH, searchParams);
    const {uuid, status, collectionId} = searchResult.results[0];

    expect(searchResult).toHaveLength(searchResult.results.length);
    expect(uuid).toBeTruthy();
    // Status returned is in lowercase so have to convert to uppercase.
    expect(status.toUpperCase()).toBe(OEQ.Common.ItemStatus.LIVE);
    expect(collectionId).toBe(collection);
  });

  it("should get 404 response if search for a non-existing collection", async () => {
    await expect(OEQ.Search.search(TC.API_PATH, {collections: ["testing collection"]}))
      .rejects.toHaveProperty('status', 404);
  })
});

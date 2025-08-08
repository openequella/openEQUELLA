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
import {
  addFavouriteItem,
  addFavouriteSearch,
  deleteFavouriteItem,
  getFavouriteSearches,
  FavouriteItem,
  FavouriteSearch,
  deleteFavouriteSearch,
} from '../src/Favourite';
import { SearchResult } from '../src/Search';
import * as TC from './TestConfig';
import { logout } from './TestUtils';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => logout(TC.API_PATH));

describe('FavouriteItem', () => {
  const addFavItem = (): Promise<FavouriteItem> => {
    const itemKey = '2f6e9be8-897d-45f1-98ea-7aa31b449c0e/1';
    const favouriteItem: FavouriteItem = {
      itemID: itemKey,
      keywords: ['a', 'b'],
      isAlwaysLatest: true,
    };
    return addFavouriteItem(TC.API_PATH, favouriteItem);
  };
  it('should be possible to add a favourite Item', async () => {
    const newFavouriteItem: FavouriteItem = await addFavItem();
    expect(newFavouriteItem).not.toBeNull();
    expect(newFavouriteItem.bookmarkID).toBeTruthy();
  });

  it('should be possible to delete a favourite Item', async () => {
    const newFavouriteItem: FavouriteItem = await addFavItem();
    const newBookmarkID = newFavouriteItem.bookmarkID;
    if (!newBookmarkID) {
      throw new Error("Bookmark ID can't be falsy");
    }
    await expect(
      deleteFavouriteItem(TC.API_PATH, newBookmarkID)
    ).resolves.not.toThrow();
  });
});

describe('FavouriteSearch', () => {
  // Keep track of the ids of the favourite search added in each test
  let favSearchIds: number[] = [];

  const addFavSearch = async (name: string): Promise<FavouriteSearch> => {
    const favAdded = await addFavouriteSearch(TC.API_PATH, {
      name,
      url: '/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22RATING%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22crab%22%2C%22collections%22%3A%5B%5D%2C%22lastModifiedDateRange%22%3A%7B%7D%2C%22mimeTypeFilters%22%3A%5B%5D%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D',
    });
    favSearchIds.push(favAdded.id);
    return favAdded;
  };

  const validateResultHasSearch = async (name: string) => {
    const res = await getFavouriteSearches(TC.API_PATH, { query: name });
    expect(res.results.map((r) => r.name)).toContain(name);
  };

  const getSearchName = (
    res: SearchResult<FavouriteSearch>,
    index: number
  ): string | undefined => res.results[index]?.name;

  afterEach(async () => {
    await Promise.all(
      favSearchIds.map((id) => deleteFavouriteSearch(TC.API_PATH, id))
    );
    favSearchIds = [];
  });

  // eslint-disable-next-line jest/expect-expect
  it('should be able to add a favourite search', async () => {
    const { name } = await addFavSearch('addFavSearch');
    await validateResultHasSearch(name);
  });

  it('should be able to delete a favourite search', async () => {
    const { id, name } = await addFavSearch('deleteFavSearch');
    await validateResultHasSearch(name);
    await deleteFavouriteSearch(TC.API_PATH, id);
    // Filter out this fav. search id from favSearchIds as it has already been deleted
    favSearchIds = favSearchIds.filter((i) => i !== id);

    const res = await getFavouriteSearches(TC.API_PATH, { query: name });
    expect(res.results.map((s) => s.id)).not.toContain(id);
  });

  describe('Favourite Search with GET:', () => {
    it('no filters', async () => {
      const res = await getFavouriteSearches(TC.API_PATH);
      expect(res.results).toHaveLength(3);
    });

    // eslint-disable-next-line jest/expect-expect
    it('filters by query', async () => {
      const { name } = await addFavSearch('getWithQueryParam');
      await validateResultHasSearch(name);
    });

    it('supports paging (start / length)', async () => {
      const { name } = await addFavSearch(`getWithPagingParams`);

      const page: SearchResult<FavouriteSearch> = await getFavouriteSearches(
        TC.API_PATH,
        { query: name, start: 0, length: 1 }
      );

      expect(page.start).toBe(0);
      expect(page).toHaveLength(1);
      expect(page.results).toHaveLength(1);
      expect(getSearchName(page, 0)).toBe(name);
    });

    it('sorts alphabetically when order="name"', async () => {
      const searchARes = await addFavSearch('searchA-getWithOrderParam');
      const searchZRes = await addFavSearch('searchZ-getWithOrderParam');

      const res = await getFavouriteSearches(TC.API_PATH, {
        query: 'getWithOrderParam',
        order: 'name',
      });

      expect(getSearchName(res, 0)).toBe(searchARes.name);
      expect(getSearchName(res, 1)).toBe(searchZRes.name);
    });

    it('Get favourite searches within a date range', async () => {
      const { id, name, addedAt } = await addFavSearch(
        'getWithDateRangeParams'
      );

      // Helper function to format a Date as 'yyyy-MM-dd' (ISO date without time)
      const toIsoDate = (d: Date) => d.toISOString().slice(0, 10);
      // Number of milliseconds in one day
      const MS_DAY = 24 * 60 * 60 * 1_000;

      const res = await getFavouriteSearches(TC.API_PATH, {
        query: name,
        addedAfter: toIsoDate(new Date(addedAt.getTime() - MS_DAY)),
        addedBefore: toIsoDate(new Date(addedAt.getTime() + MS_DAY)),
      });

      expect(res.results).toHaveLength(1);
      expect(res.results[0].id).toBe(id);
    });
  });
});

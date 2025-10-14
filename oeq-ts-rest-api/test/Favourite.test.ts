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
import { pipe } from 'fp-ts/function';
import * as O from 'fp-ts/Option';
import * as OEQ from '../src';
import {
  addFavouriteItem,
  addFavouriteSearch,
  deleteFavouriteItem,
  searchFavouriteSearches,
  FavouriteItem,
  FavouriteSearch,
  deleteFavouriteSearch,
} from '../src/Favourite';
import { SearchResult } from '../src/Search';
import { buildISODate, modifyDateByDays } from './DateHelper';
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
  const favSearches = {
    ids: [] as number[],
    async add(name: string): Promise<FavouriteSearch> {
      const favSearch = await addFavouriteSearch(TC.API_PATH, {
        name,
        url: '/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22RATING%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22crab%22%2C%22collections%22%3A%5B%5D%2C%22lastModifiedDateRange%22%3A%7B%7D%2C%22mimeTypeFilters%22%3A%5B%5D%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D',
      });
      this.ids.push(favSearch.id);
      return favSearch;
    },
    async del(id: number): Promise<void> {
      await deleteFavouriteSearch(TC.API_PATH, id);
      pipe(
        O.some(this.ids.indexOf(id)),
        O.filter((i) => i > -1),
        // Remove the search id from the ids array
        O.map((i) => this.ids.splice(i, 1))
      );
    },
    async cleanup(): Promise<void> {
      const deletionPromises = this.ids.map((id: number) =>
        deleteFavouriteSearch(TC.API_PATH, id)
      );
      await Promise.all(deletionPromises);
      this.ids = [];
    },
  };

  const expectResultHasSearch = async (
    name: string
  ): Promise<SearchResult<FavouriteSearch>> => {
    const res = await searchFavouriteSearches(TC.API_PATH, { query: name });
    expect(res.results.map((r) => r.name)).toContain(name);
    return res;
  };

  const getSearchName = (
    res: SearchResult<FavouriteSearch>,
    index: number
  ): string | undefined => res.results[index]?.name;

  const calculateRelativeISODate = (baseDate: Date, days: number): string =>
    pipe(baseDate, modifyDateByDays(days), buildISODate);

  afterEach(async () => {
    await favSearches.cleanup();
  });

  it('should be able to add a favourite search', async () => {
    const { name } = await favSearches.add('addFavSearch');
    await expectResultHasSearch(name);
  });

  it('should be able to delete a favourite search', async () => {
    const { id, name } = await favSearches.add('deleteFavSearch');
    await expectResultHasSearch(name);
    await favSearches.del(id);

    const res = await searchFavouriteSearches(TC.API_PATH, { query: name });
    expect(res.results.map((s) => s.id)).not.toContain(id);
  });

  describe('searchFavouriteSearches', () => {
    it('returns all favourite searches when no filters are applied', async () => {
      const res = await searchFavouriteSearches(TC.API_PATH);
      expect(res.results).toHaveLength(3);
    });

    it('filters by query', async () => {
      const { name } = await favSearches.add('searchByQuery');
      const res: SearchResult<FavouriteSearch> =
        await expectResultHasSearch(name);
      expect(res.results).toHaveLength(1);
    });

    it('supports paging (start / length)', async () => {
      const { name } = await favSearches.add(`searchWithPagingParams`);

      const page: SearchResult<FavouriteSearch> = await searchFavouriteSearches(
        TC.API_PATH,
        { query: name, start: 0, length: 1 }
      );

      expect(page.start).toBe(0);
      // eslint-disable-next-line jest/prefer-to-have-length
      expect(page.length).toBe(1);
      expect(page.results).toHaveLength(1);
      expect(getSearchName(page, 0)).toBe(name);
    });

    it('sorts alphabetically when order="name"', async () => {
      const searchZ = await favSearches.add('searchZ-byOrder');
      const searchB = await favSearches.add('searchB-byOrder');
      const searchY = await favSearches.add('searchY-byOrder');
      const searchA = await favSearches.add('searchA-byOrder');

      const res = await searchFavouriteSearches(TC.API_PATH, {
        query: 'byOrder',
        order: 'name',
      });

      expect(getSearchName(res, 0)).toBe(searchA.name);
      expect(getSearchName(res, 1)).toBe(searchB.name);
      expect(getSearchName(res, 2)).toBe(searchY.name);
      expect(getSearchName(res, 3)).toBe(searchZ.name);
    });

    it('sorts by date added when order="added_at"', async () => {
      const search1 = await favSearches.add('search1-byDate');
      const search2 = await favSearches.add('search2-byDate');
      const search3 = await favSearches.add('search3-byDate');

      const res = await searchFavouriteSearches(TC.API_PATH, {
        query: 'byDate',
        order: 'added_at',
      });

      expect(getSearchName(res, 0)).toBe(search3.name);
      expect(getSearchName(res, 1)).toBe(search2.name);
      expect(getSearchName(res, 2)).toBe(search1.name);
    });

    it('returns favourite searches within a date range', async () => {
      const { id, name, addedAt } = await favSearches.add(
        'searchWithinDateRange'
      );

      const res = await searchFavouriteSearches(TC.API_PATH, {
        query: name,
        addedAfter: calculateRelativeISODate(addedAt, -1),
        addedBefore: calculateRelativeISODate(addedAt, 1),
      });

      expect(res.results).toHaveLength(1);
      expect(res.results[0].id).toBe(id);
    });

    it('does not return favourite searches outside date range', async () => {
      const { name, addedAt } = await favSearches.add('searchWithinDateRange');

      const res = await searchFavouriteSearches(TC.API_PATH, {
        query: name,
        addedAfter: calculateRelativeISODate(addedAt, -2),
        addedBefore: calculateRelativeISODate(addedAt, -1),
      });

      expect(res.results).toHaveLength(0);
    });
  });
});

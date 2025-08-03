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
import { getISODateString } from '../../react-front-end/tsrc/util/Date';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => logout(TC.API_PATH));

describe('FavouriteItem', () => {
  const add = (): Promise<FavouriteItem> => {
    const itemKey = '2f6e9be8-897d-45f1-98ea-7aa31b449c0e/1';
    const favouriteItem: FavouriteItem = {
      itemID: itemKey,
      keywords: ['a', 'b'],
      isAlwaysLatest: true,
    };
    return addFavouriteItem(TC.API_PATH, favouriteItem);
  };
  it('should be possible to add a favourite Item', async () => {
    const newFavouriteItem: FavouriteItem = await add();
    expect(newFavouriteItem).not.toBeNull();
    expect(newFavouriteItem.bookmarkID).toBeTruthy();
  });

  it('should be possible to delete a favourite Item', async () => {
    const newFavouriteItem: FavouriteItem = await add();
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
  const add = async (name: string): Promise<FavouriteSearch> =>
    addFavouriteSearch(TC.API_PATH, {
      name,
      url: '/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22RATING%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22crab%22%2C%22collections%22%3A%5B%5D%2C%22lastModifiedDateRange%22%3A%7B%7D%2C%22mimeTypeFilters%22%3A%5B%5D%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D',
    });
  it('should be able to add a favourite search', async () => {
    const newFavouriteSearch = await add('addFavSearch');
    expect(newFavouriteSearch).toBeTruthy();
  });

  it('should be able to delete a favourite search', async () => {
    const { id } = await add('deleteFavSearch');
    await expect(deleteFavouriteSearch(TC.API_PATH, id)).resolves.not.toThrow();
  });

  describe('Favourite Search with GET:', () => {
    it('no filters', async () => {
      await add('getWithNoFilters');
      const res = await getFavouriteSearches(TC.API_PATH);
      console.log(res);
      expect(res.results.length).toBeGreaterThan(0);
    });

    it('filters by query', async () => {
      const fs: FavouriteSearch = await add('getWithQueryParam');
      console.log(fs);
      const res = await getFavouriteSearches(TC.API_PATH, { query: fs.name });
      console.log(res);
      expect(res.results.map((r) => r.name)).toContain(fs.name);
    });

    it('supports paging (start / length)', async () => {
      const fs: FavouriteSearch = await add(`getWithPagingParams`);

      const page: SearchResult<FavouriteSearch> = await getFavouriteSearches(
        TC.API_PATH,
        { query: fs.name, start: 0, length: 1 }
      );

      expect(page.start).toBe(0);
      expect(page).toHaveLength(1);
      expect(page.results.length).toBeLessThanOrEqual(1);
    });

    it('sorts alphabetically when order="name"', async () => {
      const a = await add('aa-getWithOrderParam');
      const z = await add('zz-getWithOrderParam');

      const res = await getFavouriteSearches(TC.API_PATH, {
        query: 'getWithOrderParam',
        order: 'name',
      });

      expect(res.results[0].name).toBe(a.name);
      expect(res.results[1].name).toBe(z.name);
    });

    it('Get favourite searches within a date range', async () => {
      const fs = await add('getWithDateRangeParams');
      const { oneDayBefore: addedAfter, oneDayAfter: addedBefore } =
        getDayBeforeAndAfter(fs.addedAt);

      const res = await getFavouriteSearches(TC.API_PATH, {
        query: fs.name,
        addedAfter,
        addedBefore,
      });

      expect(res.results).toHaveLength(1);
      expect(res.results[0].id).toBe(fs.id);
    });
  });
});

const getDayBeforeAndAfter = (date: Date) => {
  const MS_DAY = 24 * 60 * 60 * 1000;
  return {
    oneDayBefore: getISODateString(new Date(date.getTime() - MS_DAY)),
    oneDayAfter: getISODateString(new Date(date.getTime() + MS_DAY)),
  };
};

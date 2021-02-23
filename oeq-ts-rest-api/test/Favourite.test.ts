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
  deleteFavouriteItem,
  FavouriteItem,
} from '../src/Favourite';
import * as TC from './TestConfig';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));
afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

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

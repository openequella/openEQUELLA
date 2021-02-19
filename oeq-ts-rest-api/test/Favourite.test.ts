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
  const itemUUID = '2f6e9be8-897d-45f1-98ea-7aa31b449c0e';
  const itemVersion = 1;

  it('should be possible to add a favourite Item', async () => {
    const favouriteItem: FavouriteItem = {
      itemID: `${itemUUID}/${itemVersion}`,
      keywords: ['a', 'b'],
      isAlwaysLatest: true,
    };
    const newFavouriteItem = await addFavouriteItem(TC.API_PATH, favouriteItem);
    expect(newFavouriteItem).not.toBeNull();
  });

  it('should be possible to delete a favourite Item', async () => {
    await expect(
      deleteFavouriteItem(TC.API_PATH, itemUUID, itemVersion)
    ).resolves.not.toThrow();
  });
});

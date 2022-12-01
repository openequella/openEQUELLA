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
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as ORD from "fp-ts/Ord";
import * as RA from "fp-ts/ReadonlyArray";
import * as RR from "fp-ts/ReadonlyRecord";
import * as RSET from "fp-ts/ReadonlySet";
import { last } from "fp-ts/Semigroup";
import * as S from "fp-ts/string";
import { resolveUsers, users } from "../../../__mocks__/UserModule.mock";
import {
  resolveUsersCached,
  UserCache,
} from "../../../tsrc/modules/UserModule";
import { expectRight } from "../FpTsMatchers";

describe("resolveUsersCached", () => {
  // Note that this function retrieves unknown users via `resolveUsersProvider` mock which just
  // looks up IDs within the mock `users` data.
  const retrieveTestUser = async (
    ids: ReadonlySet<string>,
    cache: UserCache,
    setCache: (_: UserCache) => void
  ) => {
    const result = await pipe(
      ids,
      resolveUsersCached(cache, setCache, resolveUsers)
    )();

    return expectRight(result)!;
  };

  const lastUserDetailsMagma = last<OEQ.UserQuery.UserDetails>();

  const buildCacheFromUsers = (
    cacheUsers: ReadonlyArray<OEQ.UserQuery.UserDetails>
  ): UserCache =>
    RR.fromFoldableMap(lastUserDetailsMagma, RA.Foldable)(
      cacheUsers,
      (u: OEQ.UserQuery.UserDetails) => [u.id, u]
    );

  it("retrieves unknown user, and places the new details in (previously empty) cache", async () => {
    const testUser = users[0];

    const emptyCache = {};
    const setCache = jest.fn();

    const retrievedUser = await retrieveTestUser(
      RSET.singleton(testUser.id),
      emptyCache,
      setCache
    );

    expect(retrievedUser).toStrictEqual(new Set([testUser]));
    expect(setCache).toHaveBeenLastCalledWith(
      RR.singleton(testUser.id, testUser)
    );
  });

  it("retrieves unknown user, and merges the new details with existing cache", async () => {
    const testUser = users[0];

    const existingCache = buildCacheFromUsers([users[1], users[2]]);
    const setCache = jest.fn();

    const retrievedUser = await retrieveTestUser(
      RSET.singleton(testUser.id),
      existingCache,
      setCache
    );

    expect(retrievedUser).toStrictEqual(new Set([testUser]));
    expect(setCache).toHaveBeenLastCalledWith(
      pipe(
        existingCache,
        RR.union(lastUserDetailsMagma)(RR.singleton(testUser.id, testUser))
      )
    );
  });

  it("returns a known users, with no update to cache", async () => {
    const testUsers: ReadonlySet<OEQ.UserQuery.UserDetails> = new Set([
      users[1],
      users[2],
    ]);

    const existingCache = buildCacheFromUsers(users);
    const setCache = jest.fn();

    const retrievedUsers = await retrieveTestUser(
      pipe(
        testUsers,
        RSET.map(S.Eq)(({ id }) => id)
      ),
      existingCache,
      setCache
    );

    expect(retrievedUsers).toStrictEqual(testUsers);
    expect(setCache).not.toHaveBeenCalled();
  });

  it("returns users details from both the cache and by retrieval as required in the one call", async () => {
    const cachedUser = users[0];
    const testUsers: ReadonlySet<OEQ.UserQuery.UserDetails> = new Set([
      cachedUser,
      users[1],
    ]);

    const existingCache = buildCacheFromUsers([cachedUser]);
    const setCache = jest.fn();

    const retrievedUsers = await retrieveTestUser(
      pipe(
        testUsers,
        RSET.map(S.Eq)(({ id }) => id)
      ),
      existingCache,
      setCache
    );

    expect(retrievedUsers).toStrictEqual(testUsers);
    expect(setCache).toHaveBeenLastCalledWith(
      buildCacheFromUsers(
        RSET.toReadonlyArray<OEQ.UserQuery.UserDetails>(ORD.trivial)(testUsers)
      )
    );
  });
});

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
import { findEntityById } from "../tsrc/modules/ACLEntityModule";
import { entityDetailsProvider } from "./SecurityEntitySearch.mock";

/**
 * A list of users to test with, deliberately out of order. Names are randomly generated.
 */
export const users: OEQ.UserQuery.UserDetails[] = [
  {
    id: "f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a",
    username: "user200",
    firstName: "Racheal",
    lastName: "Carlyle",
  },
  {
    id: "20483af2-fe56-4499-a54b-8d7452156895",
    username: "user100",
    firstName: "Fabienne",
    lastName: "Hobson",
  },
  {
    id: "1c2ff1d0-9040-4985-a450-0ff6422ba5ef",
    username: "user400",
    firstName: "Ronny",
    lastName: "Southgate",
  },
  {
    id: "eb75a832-6533-4d72-93f4-2b7a1b108951",
    username: "user300",
    firstName: "Yasmin",
    lastName: "Day",
  },
  {
    id: "75abbd62-d91c-4ce5-b4b5-339e0d44ac0e",
    username: "admin999",
    firstName: "Wat",
    lastName: "Swindlehurst",
  },
  {
    id: "2",
    username: "ContentAdmin",
    firstName: "Content",
    lastName: "Content",
  },
];

export const getCurrentUserMock: OEQ.LegacyContent.CurrentUserDetails = {
  id: "test",
  username: "test",
  firstName: "test",
  lastName: "test",
  accessibilityMode: true,
  autoLoggedIn: false,
  guest: false,
  prefsEditable: true,
  menuGroups: [],
  canDownloadSearchResult: true,
  roles: [],
  scrapbookEnabled: true,
};

export const tokens: string[] = ["moodle", "token1", "token2"];

/**
 * Helper function to inject into component for user retrieval by an array of ids.
 *
 * @param ids A list of user IDs to lookup, should be one of those in `users`
 */
export const resolveUsers = async (
  ids: ReadonlySet<string>,
): Promise<OEQ.UserQuery.UserDetails[]> =>
  Promise.resolve(users.filter(({ id }) => ids.has(id)));

/**
 * Helper function to inject into component for user retrieval by user id.
 *
 * @param id oEQ id
 */
export const findUserById = (id: string) => findEntityById(id, resolveUsers);

/**
 * Helper function to inject into component for user retrieval.
 *
 * @param query A simple string to filter by (no wildcard support)
 */
export const listUsers = async (
  query?: string,
): Promise<OEQ.UserQuery.UserDetails[]> =>
  entityDetailsProvider(
    users,
    (u: OEQ.UserQuery.UserDetails, q) => u.username.search(q) === 0,
    query,
  );

/**
 * Helper function to inject into component for token retrieval.
 */
export const getTokens = async (): Promise<string[]> => Promise.resolve(tokens);

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
import { API_BASE_URL } from "../AppConfig";

/**
 * List users known in oEQ. Useful for filtering by users, or assigning permissions etc.
 *
 * @param query A wildcard supporting string to filter the result based on name
 */
export const listUsers = (
  query?: string
): Promise<OEQ.UserQuery.UserDetails[]> =>
  OEQ.UserQuery.search(API_BASE_URL, {
    q: query,
    users: true,
    groups: false,
    roles: false,
  }).then((result: OEQ.UserQuery.SearchResult) => result.users);

/**
 * Gets the current user's info from the server as OEQ.LegacyContent.CurrentUserDetails.
 */
export const getCurrentUserDetails = () =>
  OEQ.LegacyContent.getCurrentUserDetails(API_BASE_URL).then(
    (result: OEQ.LegacyContent.CurrentUserDetails) => result
  );

/**
 * Lookup users known in oEQ.
 * @param ids An array of oEQ ids
 */
export const resolveUsers = (
  ids: string[]
): Promise<OEQ.UserQuery.UserDetails[]> =>
  OEQ.UserQuery.lookup(API_BASE_URL, {
    users: ids,
    groups: [],
    roles: [],
  }).then((result: OEQ.UserQuery.SearchResult) => result.users);

/**
 * Find a user's details by ID.
 *
 * @param userId The unique ID of a user
 */
export const findUserById = async (
  userId: string
): Promise<OEQ.UserQuery.UserDetails | undefined> => {
  const userDetails = await resolveUsers([userId]);
  if (userDetails.length > 1)
    throw new Error(`More than one user was resolved for id: ${userId}`);
  return userDetails[0];
};

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
 * A list of groups to test with group search, deliberately out of order.
 */
export const groups: OEQ.UserQuery.GroupDetails[] = [
  {
    id: "99806ac8-410e-4c60-b3ab-22575276f0f0",
    name: "Engineering & Computer Science Students",
  },
  {
    id: "d0265a33-8f89-4cea-8a36-45fd3c4cf5a1",
    name: "Engineering & Computer Science Staff",
  },
  {
    id: "d8a3f968-c0a8-44ce-83c9-a5bfc99b03b3",
    name: "Information Technology Services",
  },
  {
    id: "7ec9fb32-35f4-44bc-9c1d-7c8b02b9d0cb",
    name: "Library",
  },
  {
    id: "d7dd1907-5731-4244-9a65-e0e847f68604",
    name: "group200",
  },
  {
    id: "303e758c-0051-4aea-9a8e-421f93ed9d1a",
    name: "group100",
  },
  {
    id: "a2576dea-bd5c-490b-a065-637068e1a4fb",
    name: "group400",
  },
  {
    id: "f921a6e3-69a6-4ec4-8cf8-bc193beda5f6",
    name: "group300",
  },
];

/**
 * Helper function to inject into component for group retrieval by an array of ids.
 *
 * @param ids A list of group IDs to lookup, should be one of those in `groups`
 */
export const findGroupsByIds = async (
  ids: ReadonlySet<string>,
): Promise<OEQ.UserQuery.GroupDetails[]> =>
  Promise.resolve(groups.filter(({ id }) => ids.has(id)));

/**
 * Helper function for group retrieval by group id.
 *
 * @param id oEQ id
 */
export const findGroupById = (id: string) =>
  findEntityById(id, findGroupsByIds);

/**
 * Helper function to inject into component for group retrieval.
 *
 * @param query A simple string to filter by (no wildcard support)
 */
export const searchGroups = async (
  query?: string,
): Promise<OEQ.UserQuery.GroupDetails[]> =>
  entityDetailsProvider(
    groups,
    (g: OEQ.UserQuery.GroupDetails, q) => g.name.search(q) === 0,
    query,
  );

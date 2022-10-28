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

/**
 * A list of groups to test with group search, deliberately out of order.
 */
export const groups: OEQ.UserQuery.GroupDetails[] = [
  {
    id: "42237595-7c90-4292-bf16-059b9a59fcfb",
    name: "Engineering & Computer Science Students",
  },
  {
    id: "485b5831-e66c-4419-8305-e99e4d2361a0",
    name: "Engineering & Computer Science Staff",
  },
  {
    id: "ddb28dd0-e186-47c8-aed7-23e4797d8bf3",
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
 * A mock of `GroupModule.resolveGroups` which simply looks up the provided ids in `groups` within
 * `GroupModule.mock.ts`
 *
 * @param ids group UUIDs which are in the mocked list of groups
 */
export const resolveGroups = async (
  ids: ReadonlyArray<string>
): Promise<OEQ.UserQuery.GroupDetails[]> => {
  const result = groups.filter(({ id }) => ids.includes(id));
  return Promise.resolve(result);
};

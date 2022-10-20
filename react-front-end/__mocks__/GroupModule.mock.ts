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
    id: "3d96df92-4d0b-496f-b865-9f1ad4a67d8d",
    name: "Teachers",
  },
  {
    id: "d0265a33-8f89-4cea-8a36-45fd3c4cf5a1",
    name: "Systems Administrators",
  },
  {
    id: "e810bee1-f2da-4145-8bc3-dc6fec827429",
    name: "Content Administrators",
  },
  {
    id: "680f5eb7-22e2-4ab6-bcea-25205167e36e",
    name: "group200",
  },
  {
    id: "cda09b86-3662-46bd-b60e-4bce89efbada",
    name: "group100",
  },
  {
    id: "97254515-6e32-48e9-ba65-5b5c6a1182a6",
    name: "group400",
  },
  {
    id: "8db50158-757d-44f3-8ccf-7b230a3a6405",
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

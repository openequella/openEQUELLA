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
 * A list of users to test with, deliberately out of order. Names are randomly generated.
 */
export const users: OEQ.UserQuery.UserDetails[] = [
  {
    id: "680f5eb7-22e2-4ab6-bcea-25205165e36e",
    username: "user200",
    firstName: "Fabienne",
    lastName: "Hobson",
  },
  {
    id: "cda09b86-3662-46bd-b60e-4bce89efba7a",
    username: "user100",
    firstName: "Racheal",
    lastName: "Carlyle",
  },
  {
    id: "97254515-6e32-48e9-ba65-5b5c6aa182a6",
    username: "user400",
    firstName: "Ronny",
    lastName: "Southgate",
  },
  {
    id: "8db50158-757d-44f3-8ccf-7b2e0a3a6405",
    username: "user300",
    firstName: "Yasmin",
    lastName: "Day",
  },
  {
    id: "3f25f543-7231-46f4-8f3b-aaccc8fcf52a",
    username: "admin999",
    firstName: "Wat",
    lastName: "Swindlehurst",
  },
];

/**
 * Helper function to inject into component for user retrieval.
 *
 * @param query A simple string to filter by (no wildcard support)
 */
export const userDetailsProvider = async (
  query?: string
): Promise<OEQ.UserQuery.UserDetails[]> => {
  // A sleep to emulate latency
  await new Promise((resolve) => setTimeout(resolve, 500));
  return Promise.resolve(
    query ? users.filter((u) => u.username.search(query) === 0) : users
  );
};

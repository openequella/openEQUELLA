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
import { users } from "./UserModule.mock";

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

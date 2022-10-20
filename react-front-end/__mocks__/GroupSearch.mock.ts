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
import { groups } from "./GroupModule.mock";

/**
 * Helper function to inject into component for group retrieval.
 *
 * @param query A simple string to filter by (no wildcard support)
 */
export const groupDetailsProvider = async (
  query?: string
): Promise<OEQ.UserQuery.GroupDetails[]> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
  return Promise.resolve(
    query
      ? groups.filter(
          (g: OEQ.UserQuery.GroupDetails) => g.name.search(query) === 0
        )
      : groups
  );
};

/**
 * Helper function to inject into component for group retrieval by an array of ids.
 *
 * @param ids A list of group IDs to lookup, should be one of those in `groups`
 */
export const resolveGroupsProvider = async (
  ids: ReadonlyArray<string>
): Promise<OEQ.UserQuery.GroupDetails[]> =>
  Promise.resolve(groups.filter(({ id }) => ids.includes(id)));

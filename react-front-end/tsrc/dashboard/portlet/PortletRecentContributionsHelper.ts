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
import { DehydratedSearchPageOptions } from "../../search/SearchPageHelper";

/**
 * Converts a subset of SearchParams to SearchOptions for URL redirection.
 * This handles the mapping between the API search parameters and the UI search options.
 */
export const convertSearchParamsToOptions = (
  searchParams: Pick<
    OEQ.Search.SearchParams,
    "query" | "collections" | "status" | "modifiedAfter" | "order"
  >,
): DehydratedSearchPageOptions => {
  // Convert modifiedAfter date string to DateRange
  const lastModifiedDateRange = searchParams.modifiedAfter
    ? { start: new Date(searchParams.modifiedAfter), end: undefined }
    : undefined;

  // Convert collection UUIDs to Collection objects (simplified - just using UUID as name)
  const collections = searchParams.collections?.map((uuid) => ({
    uuid,
    name: uuid, // Not relevant for intended use case
  }));

  return {
    query: searchParams.query,
    collections,
    status: searchParams.status,
    lastModifiedDateRange,
    sortOrder: searchParams.order,
  };
};

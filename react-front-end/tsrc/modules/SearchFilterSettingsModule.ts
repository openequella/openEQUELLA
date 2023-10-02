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

export const getMimeTypeFiltersFromServer = (): Promise<
  OEQ.SearchFilterSettings.MimeTypeFilter[]
> => OEQ.SearchFilterSettings.getSearchFilterSettings(API_BASE_URL);

/**
 * Find MIME type filters by a list of ID.
 *
 * @param filterIds MIME type filter IDs used to filter the list of all MIME type filters.
 * @returns { MimeTypeFilter[] | undefined } An array of `MimeTypeFilter` instances matching the provided IDs or `undefined` if none could be found.
 */
export const getMimeTypeFiltersById = async (
  filterIds: string[],
): Promise<OEQ.SearchFilterSettings.MimeTypeFilter[] | undefined> => {
  const allFilters = await getMimeTypeFiltersFromServer();
  return allFilters.filter(({ id }) => id && filterIds.includes(id));
};

export const batchUpdateOrAdd = (
  filters: OEQ.SearchFilterSettings.MimeTypeFilter[],
) =>
  OEQ.SearchFilterSettings.batchUpdateSearchFilterSetting(
    API_BASE_URL,
    filters,
  ).then((res) => OEQ.BatchOperationResponse.groupErrorMessages(res));

export const batchDelete = (ids: string[]) =>
  OEQ.SearchFilterSettings.batchDeleteSearchFilterSetting(
    API_BASE_URL,
    ids,
  ).then((res) => OEQ.BatchOperationResponse.groupErrorMessages(res));

export const getMimeTypeDetail = (entry: OEQ.MimeType.MimeTypeEntry) => {
  const { mimeType, desc } = entry;
  if (desc) {
    return `${desc} (${mimeType})`;
  }
  return mimeType;
};

export const validateMimeTypeName = (name: string | undefined): boolean =>
  !!name?.trim();

/**
 * Return a function which does reference comparison for two filters.
 */
export const filterComparator = (
  targetFilter: OEQ.SearchFilterSettings.MimeTypeFilter,
) => {
  return (filter: OEQ.SearchFilterSettings.MimeTypeFilter) =>
    filter === targetFilter;
};

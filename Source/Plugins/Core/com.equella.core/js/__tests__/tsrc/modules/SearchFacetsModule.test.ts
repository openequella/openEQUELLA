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
import { Facet } from "../../../tsrc/modules/FacetedSearchSettingsModule";
import * as FacetedSearchSettingsModule from "../../../tsrc/modules/FacetedSearchSettingsModule";
import {
  Classification,
  listClassifications,
} from "../../../tsrc/modules/SearchFacetsModule";

const CLASSIFICATION_SUBJECT: Facet = {
  id: 0,
  name: "Classification 1",
  schemaNode: "/item/subject",
  maxResults: 1,
  orderIndex: 0,
};
const CLASSIFICATION_KEYWORD: Facet = {
  id: 1,
  name: "Classification 2",
  schemaNode: "/item/keyword",
  orderIndex: 1,
  maxResults: 10,
};
const CATEGORIES_SUBJECT: OEQ.SearchFacets.Facet[] = [
  { term: "subject1", count: 10 },
  { term: "subject2", count: 20 },
  { term: "subject3", count: 30 },
];
const CATEGORIES_KEYWORD: OEQ.SearchFacets.Facet[] = [
  { term: "keyword1", count: 101 },
  { term: "keyword2", count: 202 },
  { term: "keyword3", count: 303 },
];

jest
  .spyOn(FacetedSearchSettingsModule, "getFacetsFromServer")
  .mockResolvedValue([CLASSIFICATION_SUBJECT, CLASSIFICATION_KEYWORD]);

jest.mock("@openequella/rest-api-client");
const mockedSearchFacets = (OEQ.SearchFacets.searchFacets as jest.Mock<
  Promise<OEQ.SearchFacets.SearchFacetsResult>
>).mockImplementation(
  (
    _: string,
    params?: OEQ.SearchFacets.SearchFacetsParams
  ): Promise<OEQ.SearchFacets.SearchFacetsResult> => {
    const mockData = new Map<string, OEQ.SearchFacets.Facet[]>([
      [CLASSIFICATION_SUBJECT.schemaNode, CATEGORIES_SUBJECT],
      [CLASSIFICATION_KEYWORD.schemaNode, CATEGORIES_KEYWORD],
    ]);
    if (!params?.nodes[0]) {
      throw new Error("Issue with test data, no schema node provided!");
    }
    return Promise.resolve({
      results: mockData.get(params.nodes[0]),
    } as OEQ.SearchFacets.SearchFacetsResult);
  }
);

describe("SearchFacetsModule", () => {
  it("Listing classifications", async () => {
    // Do a fully defined call to listClassifications
    const queryString = "some query";
    const collections = ["uuid1", "uuid2"];
    const dateStart = "2020-01-01";
    const dateEnd = "2020-02-02";
    const ownerUuid = "uuidOwner";
    const mimeTypes = ["Image/png"];
    const classifications = await listClassifications({
      query: queryString,
      rowsPerPage: 10, // n/a
      currentPage: 1, // n/a
      collections: collections.map((id) => ({
        uuid: id,
        name: `name of ${id}`,
      })),
      rawMode: false, // n/a
      lastModifiedDateRange: {
        start: new Date(dateStart),
        end: new Date(dateEnd),
      },
      owner: {
        id: ownerUuid,
        username: "jest",
        firstName: "Test",
        lastName: "Owner",
      },
      status: OEQ.Common.ItemStatuses.alternatives.map((i) => i.value), // i.e. All statuses
      sortOrder: undefined,
      mimeTypes: mimeTypes,
    });

    // Expect the correct data is generated
    expect(classifications).toEqual([
      {
        id: CLASSIFICATION_SUBJECT.id,
        name: CLASSIFICATION_SUBJECT.name,
        maxDisplay: CLASSIFICATION_SUBJECT.maxResults,
        categories: CATEGORIES_SUBJECT,
        orderIndex: CLASSIFICATION_SUBJECT.orderIndex,
        schemaNode: CLASSIFICATION_SUBJECT.schemaNode,
      },
      {
        id: CLASSIFICATION_KEYWORD.id,
        name: CLASSIFICATION_KEYWORD.name,
        maxDisplay: CLASSIFICATION_KEYWORD.maxResults,
        categories: CATEGORIES_KEYWORD,
        orderIndex: CLASSIFICATION_KEYWORD.orderIndex,
        schemaNode: CLASSIFICATION_KEYWORD.schemaNode,
      },
    ] as Classification[]);
    // ... and that the SearchOptions are correctly converted
    expect(mockedSearchFacets).toHaveBeenLastCalledWith("api", {
      nodes: [CLASSIFICATION_KEYWORD.schemaNode],
      q: `${queryString}*`,
      collections: collections,
      modifiedAfter: dateStart,
      modifiedBefore: dateEnd,
      owner: ownerUuid,
      showall: true,
      mimeTypes: mimeTypes,
    } as OEQ.SearchFacets.SearchFacetsParams);
  });
});

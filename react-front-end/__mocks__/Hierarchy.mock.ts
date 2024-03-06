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
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { itemWithAttachment, normalItemWithoutName } from "./SearchResult.mock";

export const normalItem: OEQ.Search.SearchResultItem = {
  uuid: "cadcd296-a4d7-4024-bb5d-6c7507e6872a",
  version: 2,
  name: "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
  description:
    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
  status: "live",
  createdDate: new Date("2023-11-21T10:30:41.728+11:00"),
  modifiedDate: new Date("2023-11-21T10:30:41.726+11:00"),
  collectionId: "4c147089-cddb-e67c-b5ab-189614eb1463",
  commentCount: 0,
  starRatings: -1.0,
  attachmentCount: 0,
  attachments: [],
  thumbnail: "default",
  displayFields: [],
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/cadcd296-a4d7-4024-bb5d-6c7507e6872a/2/",
    self: "http://localhost:8080/rest/api/item/cadcd296-a4d7-4024-bb5d-6c7507e6872a/2/",
  },
  isLatestVersion: true,
};

export const itemWithThumbnail: OEQ.Search.SearchResultItem = {
  uuid: "e35390cf-7c45-4f71-bb94-e6ccc1f09394",
  version: 1,
  name: "Book B",
  status: "live",
  createdDate: new Date("2023-11-08T15:05:38.347+11:00"),
  modifiedDate: new Date("2023-11-08T15:19:02.590+11:00"),
  collectionId: "4c147089-cddb-e67c-b5ab-189614eb1463",
  commentCount: 0,
  starRatings: -1.0,
  attachmentCount: 0,
  attachments: [],
  thumbnail: "default",
  thumbnailDetails: {
    attachmentType: "file",
    mimeType: "image/jpeg",
    link: "./thumb.jpg",
  },
  displayFields: [],
  keywordFoundInAttachment: false,
  links: {
    view: "http://localhost:8080/rest/items/e35390cf-7c45-4f71-bb94-e6ccc1f09394/1/",
    self: "http://localhost:8080/rest/api/item/e35390cf-7c45-4f71-bb94-e6ccc1f09394/1/",
  },
  isLatestVersion: true,
};

export const keyResources: OEQ.Search.SearchResultItem[] = [
  normalItem,
  {
    uuid: "724fda1c-11d0-4304-9653-8e3bc17a2fa0",
    version: 1,
    name: "Keyword found in attachment test item",
    status: "live",
    createdDate: new Date("2020-08-13T14:42:57.662+10:00"),
    modifiedDate: new Date("2020-08-13T14:42:57.661+10:00"),
    collectionId: "b28f1ffe-2008-4f5e-d559-83c8acd79316",
    commentCount: 0,
    starRatings: -1.0,
    attachmentCount: 1,
    attachments: [
      {
        attachmentType: "file",
        id: "c409f228-36aa-4979-ab0c-f05a9f43ba7a",
        description: "amphibians.pdf",
        brokenAttachment: false,
        preview: false,
        mimeType: "application/pdf",
        hasGeneratedThumb: false,
        links: {
          view: "http://localhost:8080/rest/items/724fda1c-11d0-4304-9653-8e3bc17a2fa0/1/?attachment.uuid=c409f228-36aa-4979-ab0c-f05a9f43ba7a",
          thumbnail:
            "http://localhost:8080/rest/thumbs/724fda1c-11d0-4304-9653-8e3bc17a2fa0/1/c409f228-36aa-4979-ab0c-f05a9f43ba7a",
        },
        filePath: "amphibians.pdf",
      },
    ],
    thumbnail: "default",
    thumbnailDetails: {
      attachmentType: "file",
      mimeType: "application/pdf",
    },
    displayFields: [],
    keywordFoundInAttachment: false,
    links: {
      view: "http://localhost:8080/rest/items/724fda1c-11d0-4304-9653-8e3bc17a2fa0/1/",
      self: "http://localhost:8080/rest/api/item/724fda1c-11d0-4304-9653-8e3bc17a2fa0/1/",
    },
    isLatestVersion: true,
  },
  itemWithThumbnail,
  itemWithAttachment,
  normalItemWithoutName,
];

export const topicWithShortAndLongDesc: OEQ.BrowseHierarchy.HierarchyTopicSummary =
  {
    compoundUuid: "43e60e9a-a3ed-497d-b79d-386fed23675c",
    matchingItemCount: 14,
    name: "HierarchyApiTestClient",
    shortDescription:
      "a simple one level persistent hierarchy for make benefit glorious HierarchyApiTest",
    longDescription:
      ";; from https://en.wikipedia.org/wiki/Scheme_%28programming_language%29<br>\n;; Calculation of Hofstadter's male and female sequences as a list of pairs<br>\n(define (hofstadter-male-female n)<br>\n  (letrec ((female (lambda (n) (if (= n 0) 1 (- n (male (female (- n 1))))))) (male (lambda (n) (if (= n 0) 0 (- n (female (male (- n 1)))))))) (let loop ((i 0)) (if (> i n) '() (cons (cons (female i) (male i)) (loop (+ i 1))))))) (hofstadter-male-female 8)<br>\n<br>\n===> ((1 . 0) (1 . 0) (2 . 1) (2 . 2) (3 . 2) (3 . 3) (4 . 4) (5 . 4) (5 . 5))",
    showResults: true,
    hideSubtopicsWithNoResults: true,
    subHierarchyTopics: [],
  };

export const topicWithHtmlDesc: OEQ.BrowseHierarchy.HierarchyTopicSummary = {
  compoundUuid: "91a08805-d5f9-478d-aaaf-eff61a266667",
  matchingItemCount: 4,
  name: "Browse 4 book",
  shortDescription: "a simple text with <h1>HTML</h1> tag.",
  longDescription:
    "a long long long long long long long long text with img <img src='./thumb.jpg' alt='thumb'></img> and <h1>HTML</h1> tag.",
  showResults: true,
  hideSubtopicsWithNoResults: true,
  subHierarchyTopics: [],
};

export const simpleTopic: OEQ.BrowseHierarchy.HierarchyTopicSummary = {
  compoundUuid: "59214785-a3ed-478d-aaaf-eff61a266667",
  matchingItemCount: 1,
  name: "A simple topic",
  showResults: true,
  hideSubtopicsWithNoResults: true,
  subHierarchyTopics: [],
};

export const topicWithChildren: OEQ.BrowseHierarchy.HierarchyTopicSummary = {
  compoundUuid: "6135b550-ce1c-43c2-b34c-0a3cf793759d",
  matchingItemCount: 53,
  name: "Parent Topic",
  showResults: true,
  longDescription:
    "This is long long long long long long long long long long long long long long long description",
  hideSubtopicsWithNoResults: true,
  subTopicSectionName: "Subtopic Section",
  searchResultSectionName: "Search Result Section",
  subHierarchyTopics: [
    {
      compoundUuid: "8dcb1d04-33cb-4935-9fa3-d753daca0b17",
      matchingItemCount: 53,
      name: "Child Topic",
      showResults: true,
      hideSubtopicsWithNoResults: true,
      subHierarchyTopics: [
        {
          compoundUuid:
            "46249813-019d-4d14-b772-2a8ca0120c99:Hobart,886aa61d-f8df-4e82-8984-c487849f80ff:A James",
          matchingItemCount: 3,
          name: "Sub Virtual Topics: Place Hobart",
          showResults: true,
          hideSubtopicsWithNoResults: true,
          subHierarchyTopics: [],
        },
        {
          compoundUuid: "bb9d5fbd-07e9-4e61-8eb6-e0c06ae39dfc",
          matchingItemCount: 55,
          name: "Grandchild Topic",
          showResults: true,
          hideSubtopicsWithNoResults: true,
          subHierarchyTopics: [],
        },
      ],
    },
  ],
};

export const virtualTopics = {
  compoundUuid: "886aa61d-f8df-4e82-8984-c487849f80ff:A James",
  matchingItemCount: 3,
  name: "Virtual Topics: Author A James",
  shortDescription: "Author: A James",
  showResults: true,
  hideSubtopicsWithNoResults: true,
  subHierarchyTopics: [
    {
      compoundUuid:
        "46249813-019d-4d14-b772-2a8ca0120c99:Hobart,886aa61d-f8df-4e82-8984-c487849f80ff:A James",
      matchingItemCount: 3,
      name: "Sub Virtual Topics: Place Hobart",
      showResults: true,
      hideSubtopicsWithNoResults: true,
      subHierarchyTopics: [],
    },
    {
      compoundUuid:
        "46249813-019d-4d14-b772-2a8ca0120c99:Paris,886aa61d-f8df-4e82-8984-c487849f80ff:A James",
      matchingItemCount: 1,
      name: "Sub Virtual Topics: Place Paris",
      showResults: true,
      hideSubtopicsWithNoResults: true,
      subHierarchyTopics: [],
    },
  ],
};

export const topicWithoutSearchResults: OEQ.BrowseHierarchy.HierarchyTopicSummary =
  {
    compoundUuid: "e7d262b5-1a95-4a22-bfa1-127ac5584298",
    matchingItemCount: 0,
    name: "No Search Results Topic",
    showResults: false,
    hideSubtopicsWithNoResults: true,
    subHierarchyTopics: [],
  };

export const hierarchies: OEQ.BrowseHierarchy.HierarchyTopicSummary[] = [
  topicWithShortAndLongDesc,
  topicWithHtmlDesc,
  virtualTopics,
  topicWithChildren,
  topicWithoutSearchResults,
];

/**
 * Mock async function to get hierarchy.
 */
export const getHierarchy = (
  compoundUuid: string,
): Promise<OEQ.BrowseHierarchy.HierarchyTopic<OEQ.Search.SearchResultItem>> =>
  pipe(
    hierarchies.find((h) => h.compoundUuid === compoundUuid),
    O.fromNullable,
    O.fold(
      () => Promise.reject(`Can't find hierarchy: ${compoundUuid}`),
      (hierarchy) =>
        Promise.resolve({
          summary: hierarchy,
          keyResources,
          parents: [
            { name: "Parent1", compoundUuid: "uuid1" },
            { name: "Parent2", compoundUuid: "uuid2" },
          ],
        }),
    ),
  );

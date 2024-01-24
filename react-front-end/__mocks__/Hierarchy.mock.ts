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

export const topicWithHtmlShortDesc: OEQ.BrowseHierarchy.HierarchyTopicSummary =
  {
    compoundUuid: "91a08805-d5f9-478d-aaaf-eff61a266667",
    matchingItemCount: 4,
    name: "Browse 4 book",
    shortDescription: "a simple text with <h1>HTML</h1> tag.",
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
  name: "Parent Topics",
  showResults: true,
  hideSubtopicsWithNoResults: true,
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

export const hierarchies: OEQ.BrowseHierarchy.HierarchyTopicSummary[] = [
  topicWithShortAndLongDesc,
  topicWithHtmlShortDesc,
  virtualTopics,
  topicWithChildren,
];

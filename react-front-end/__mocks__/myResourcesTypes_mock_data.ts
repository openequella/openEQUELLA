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
import { MyResourcesTypeData } from "../tsrc/modules/SearchMyResourceModule";

export const mockMyResourcesResponseTypes: OEQ.SearchMyResource.MyResourceSearchType[] =
  [
    {
      name: "Published",
      id: "published",
      count: 47,
      links: "http://localhost:8080/rest/api/search/myresources/published",
    },
    {
      name: "Drafts",
      id: "draft",
      count: 6,
      links: "http://localhost:8080/rest/api/search/myresources/draft",
    },
    {
      name: "Scrapbook",
      id: "scrapbook",
      count: 9,
      links: "http://localhost:8080/rest/api/search/myresources/scrapbook",
    },
    {
      name: "Moderation queue",
      id: "modqueue",
      count: 9,
      links: "http://localhost:8080/rest/api/search/myresources/modqueue",
      subSearches: [
        {
          name: "In moderation",
          id: "moderating",
          count: 9,
        },
        {
          name: "Under review",
          id: "review",
          count: 0,
        },
        {
          name: "Rejected",
          id: "rejected",
          count: 0,
        },
      ],
    },
    {
      name: "Archive",
      id: "archived",
      count: 3,
      links: "http://localhost:8080/rest/api/search/myresources/archived",
    },
    {
      name: "All resources",
      id: "all",
      count: 74,
      links: "http://localhost:8080/rest/api/search/myresources/all",
    },
  ];

export const getMyResourceTypesTransformedResp: MyResourcesTypeData[] = [
  {
    name: "Published",
    id: "published",
    count: 47,
    to: "/page/myresources?type=published",
  },
  {
    name: "Drafts",
    id: "draft",
    count: 6,
    to: "/page/myresources?type=draft",
  },
  {
    name: "Scrapbook",
    id: "scrapbook",
    count: 9,
    to: "/page/myresources?type=scrapbook",
  },
  {
    name: "Moderation queue",
    id: "modqueue",
    count: 9,
    to: "/page/myresources?type=modqueue",
    subSearches: [
      {
        name: "In moderation",
        id: "moderating",
        count: 9,
        to: "/page/myresources?type=modqueue&mstatus=moderating",
      },
      {
        name: "Under review",
        id: "review",
        count: 0,
        to: "/page/myresources?type=modqueue&mstatus=review",
      },
      {
        name: "Rejected",
        id: "rejected",
        count: 0,
        to: "/page/myresources?type=modqueue&mstatus=rejected",
      },
    ],
  },
  {
    name: "Archive",
    id: "archived",
    count: 3,
    to: "/page/myresources?type=archived",
  },
];

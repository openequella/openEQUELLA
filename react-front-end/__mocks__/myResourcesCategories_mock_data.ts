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
import { MyResourcesCategory } from "../tsrc/modules/MyResourceModule";

export const getMyResourceCategoriesTransformedResp: MyResourcesCategory[] = [
  {
    name: "Published",
    id: "published",
    count: 47,
    to: new URL(
      "http://localhost:8080/rest/page/myresources?myResourcesType=Published",
    ),
  },
  {
    name: "Drafts",
    id: "draft",
    count: 6,
    to: new URL(
      "http://localhost:8080/rest/page/myresources?myResourcesType=Drafts",
    ),
  },
  {
    name: "Scrapbook",
    id: "scrapbook",
    count: 9,
    to: new URL(
      "http://localhost:8080/rest/page/myresources?myResourcesType=Scrapbook",
    ),
  },
  {
    name: "Moderation queue",
    id: "modqueue",
    count: 9,
    to: new URL(
      "http://localhost:8080/rest/page/myresources?myResourcesType=Moderation queue",
    ),
    subCategories: [
      {
        name: "In moderation",
        id: "moderating",
        count: 9,
        to: new URL(
          "http://localhost:8080/rest//page/myresources?myResourcesType=Moderation queue&searchOptions=%7B%22status%22%3A%5B%22MODERATING%22%5D%7D",
        ),
      },
      {
        name: "Under review",
        id: "review",
        count: 0,
        to: new URL(
          "http://localhost:8080/rest/page/myresources?myResourcesType=Moderation queue&searchOptions=%7B%22status%22%3A%5B%22REVIEW%22%5D%7D",
        ),
      },
      {
        name: "Rejected",
        id: "rejected",
        count: 0,
        to: new URL(
          "http://localhost:8080/rest/page/myresources?myResourcesType=Moderation queue&searchOptions=%7B%22status%22%3A%5B%22REJECTED%22%5D%7D",
        ),
      },
    ],
  },
  {
    name: "Archive",
    id: "archived",
    count: 3,
    to: new URL(
      "http://localhost:8080/rest/page/myresources?myResourcesType=Archive",
    ),
  },
];

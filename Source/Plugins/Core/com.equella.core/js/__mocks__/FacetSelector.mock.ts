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

import type { Classification } from "../tsrc/modules/SearchFacetsModule";

export const classifications: Classification[] = [
  {
    id: 766942,
    name: "Language",
    categories: [
      {
        term: "scala",
        count: 312,
      },
      {
        term: "java",
        count: 1212,
      },
      {
        term: "python",
        count: 222,
      },
      {
        term: "php",
        count: 612,
      },
      {
        term: "c#",
        count: 888,
      },
      {
        term:
          "purescript - the last version is xxxxxxxxx because maintenance is so hard!!!",
        count: 1,
      },
    ],
    schemaNode: "/item/language",
    maxDisplay: 7,
    orderIndex: 1,
  },
  {
    id: 766943,
    name: "City",
    categories: [
      {
        term: "Hobart",
        count: 111,
      },
      {
        term: "Sydney",
        count: 222,
      },
      {
        term: "Adelaide",
        count: 333,
      },
    ],
    schemaNode: "/item/city",
    maxDisplay: 2,
    orderIndex: 0,
  },
  {
    id: 766944,
    name: "Color",
    categories: [],
    schemaNode: "item/color",
    maxDisplay: 2,
    orderIndex: 2,
  },
];

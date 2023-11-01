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

export const mockedTaxonomyTerms: OEQ.Taxonomy.Term[] = [
  {
    term: "ringneck",
    fullTerm: "bird/ringneck",
    readonly: false,
    index: 1,
    uuid: "26361407-fb54-45f0-8e4e-2ea0a4a41862",
  },
  {
    term: "indian ringneck",
    fullTerm: "bird/ringneck/indian ringneck",
    readonly: false,
    index: 2,
    uuid: "26361407-fb54-45f0-8e4e-2ea0a4a41864",
  },
  {
    term: "african ringneck",
    fullTerm: "bird/ringneck/african ringneck",
    readonly: false,
    index: 3,
    uuid: "26361407-fb54-45f0-8e4e-2ea0a4a41865",
  },
  {
    term: "macaw",
    fullTerm: "bird/macaw",
    readonly: false,
    index: 4,
    uuid: "26361407-fb54-45f0-8e4e-2ea0a4a41863",
  },
  {
    term: "blue gold macaw",
    fullTerm: "bird/macaw/blue gold macaw",
    readonly: false,
    index: 5,
    uuid: "26361407-fb54-45f0-8e4e-2ea0a4a41866",
  },
  {
    term: "scarlet macaw",
    fullTerm: "bird/macaw/scarlet macaw",
    readonly: false,
    index: 6,
    uuid: "26361407-fb54-45f0-8e4e-2ea0a4a41867",
  },
];

export const mockedSearchTaxonomyTerms = (
  query: string,
): Promise<OEQ.Common.PagedResult<OEQ.Taxonomy.Term>> =>
  Promise.resolve({
    start: 0,
    length: 6,
    available: 6,
    results: mockedTaxonomyTerms.filter(({ term }) =>
      term.includes(query.replace(/\*/g, "")),
    ),
  });

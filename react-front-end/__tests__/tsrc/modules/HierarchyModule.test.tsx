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

import { getTopicIdFromUrl } from "../../../tsrc/modules/HierarchyModule";

describe("getTopicIdFromUrl", () => {
  const hierarchyId = "ecf0b378-ffb5-46bd-9d7f-a72ea1637714";
  const virtualHierarchyId =
    "9bc71fcc-ff0f-4a70-b11a-05eb903cf468:Q291cnNlIG1hdGVyaWFs";
  const origin = "http://localhost:8080";

  it.each([
    {
      name: "extract topic ID from new UI hierarchy page path",
      path: `/page/hierarchy/${hierarchyId}`,
      expected: hierarchyId,
    },
    {
      name: "extract virtual topic ID from new UI virtual hierarchy page path",
      path: `/page/hierarchy/${virtualHierarchyId}`,
      expected: virtualHierarchyId,
    },
    {
      name: "extract topic ID from old UI hierarchy page query parameter",
      path: `/hierarchy.do?topic=${hierarchyId}`,
      expected: hierarchyId,
    },
    {
      name: "extract topic ID from old UI virtual hierarchy page query parameter",
      path: `/hierarchy.do?topic=${virtualHierarchyId}`,
      expected: virtualHierarchyId,
    },
    {
      name: "return undefined if no topic ID is found",
      path: "/page/not-hierarchy/",
      expected: undefined,
    },
  ])("%s", ({ path, expected }) => {
    const urlObj = new URL(path, origin);
    expect(getTopicIdFromUrl(urlObj)).toBe(expected);
  });
});

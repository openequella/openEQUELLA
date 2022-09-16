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
import { createMemoryHistory } from "history";
import {
  getMyResourcesTypeFromQueryParam,
  getSortOrderFromQueryParam,
  getSubStatusFromQueryParam,
  MyResourcesType,
} from "../../../tsrc/myresources/MyResourcesPageHelper";

describe("MyResourcesPageHelper", () => {
  const history = createMemoryHistory();

  describe("support for Legacy query params", () => {
    it("gets My resources type from Legacy query param", () => {
      history.push(
        "http://localhost:8080/fiveo/access/myresources.do?type=modqueue"
      );

      expect(
        getMyResourcesTypeFromQueryParam(history.location)
      ).toBe<MyResourcesType>("Moderation queue");
    });

    it("gets Item status from Legacy query param", () => {
      history.push(
        "http://localhost:8080/fiveo/access/myresources.do?type=modqueue&mstatus=moderating&status=live"
      );

      expect(getSubStatusFromQueryParam(history.location)).toStrictEqual<
        OEQ.Common.ItemStatus[]
      >(["MODERATING"]);
    });

    it("gets sort order from Legacy query param", () => {
      history.push(
        "http://localhost:8080/fiveo/access/myresources.do?type=modqueue&modsort=lastmod&sort=title&sbsort=datecreated"
      );

      expect(
        getSortOrderFromQueryParam(history.location)
      ).toBe<OEQ.Search.SortOrder>("task_lastaction");
    });
  });
});

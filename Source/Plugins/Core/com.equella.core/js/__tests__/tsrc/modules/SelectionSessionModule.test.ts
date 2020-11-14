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
import { getSearchResult } from "../../../__mocks__/SearchResult.mock";
import type { SelectionSessionInfo } from "../../../tsrc/AppConfig";
import { buildSelectionSessionItemSummaryLink } from "../../../tsrc/modules/SelectionSessionModule";

describe("buildSelectionSessionItemSummaryLink", () => {
  const selectionSessionInfo: SelectionSessionInfo = {
    stateId: "1",
    layout: "coursesearch",
  };
  const { uuid, version } = getSearchResult.results[0];

  it("builds basic URLs for accessing ItemSummary pages in Selection Session mode", () => {
    const link = buildSelectionSessionItemSummaryLink(
      selectionSessionInfo,
      uuid,
      version
    );
    expect(link).toBe(
      "items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?_sl.stateId=1&a=coursesearch"
    );
  });

  it("will include the Integration ID in the URL if provided in SelectionSessionInfo", () => {
    const link = buildSelectionSessionItemSummaryLink(
      { ...selectionSessionInfo, integId: "2" },
      uuid,
      version
    );
    expect(link).toBe(
      "items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?_sl.stateId=1&a=coursesearch&_int.id=2"
    );
  });
});

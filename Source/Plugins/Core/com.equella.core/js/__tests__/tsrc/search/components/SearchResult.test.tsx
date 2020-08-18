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
import { render, queryByTestId } from "@testing-library/react";
import * as React from "react";
import { getSearchResult } from "../../../../__mocks__/getSearchResult";
import SearchResult from "../../../../tsrc/search/components/SearchResult";

import "@testing-library/jest-dom/extend-expect";

describe("<SearchResult/>", () => {
  it("Should show indicator in Attachment panel if keyword was found inside attachment", () => {
    const itemWithSearchTermFound = getSearchResult.results[0];
    const { container } = render(
      <SearchResult
        {...itemWithSearchTermFound}
        key={itemWithSearchTermFound.uuid}
      />
    );
    expect(
      queryByTestId(container, "keywordFoundInAttachment")
    ).toBeInTheDocument();
  });

  it("Should not show indicator in Attachment panel if keyword was found inside attachment", () => {
    const itemWithNoSearchTermFound = getSearchResult.results[1];
    const { container } = render(
      <SearchResult
        {...itemWithNoSearchTermFound}
        key={itemWithNoSearchTermFound.uuid}
      />
    );
    expect(
      queryByTestId(container, "keywordFoundInAttachment")
    ).not.toBeInTheDocument();
  });
});

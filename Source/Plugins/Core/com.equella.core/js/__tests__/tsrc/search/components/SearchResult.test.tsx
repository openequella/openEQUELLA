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
import { render, queryByLabelText } from "@testing-library/react";
import * as React from "react";
import * as mockData from "../../../../__mocks__/searchresult_mock_data";
import SearchResult from "../../../../tsrc/search/components/SearchResult";
import * as OEQ from "@openequella/rest-api-client";

import "@testing-library/jest-dom/extend-expect";
import { BrowserRouter } from "react-router-dom";

describe("<SearchResult/>", () => {
  const renderSearchResult = (itemResult: OEQ.Search.SearchResultItem) => {
    return render(
      //This needs to be wrapped inside a BrowserRouter, to prevent an `Invariant failed: You should not use <Link> outside a <Router>` error  because of the <Link/> tag within SearchResult
      <BrowserRouter>
        <SearchResult key={itemResult.uuid} item={itemResult} highlights={[]} />
      </BrowserRouter>
    );
  };

  const keywordFoundInAttachmentLabel =
    "Search term found in attachment content";

  it("Should show indicator in Attachment panel if keyword was found inside attachment", () => {
    const itemWithSearchTermFound = mockData.keywordFoundInAttachmentObj;
    const { container } = renderSearchResult(itemWithSearchTermFound);
    expect(
      queryByLabelText(container, keywordFoundInAttachmentLabel)
    ).toBeInTheDocument();
  });

  it("Should not show indicator in Attachment panel if keyword was not found inside attachment", () => {
    const itemWithNoSearchTermFound = mockData.attachSearchObj;
    const { container } = renderSearchResult(itemWithNoSearchTermFound);
    expect(
      queryByLabelText(container, keywordFoundInAttachmentLabel)
    ).not.toBeInTheDocument();
  });
});

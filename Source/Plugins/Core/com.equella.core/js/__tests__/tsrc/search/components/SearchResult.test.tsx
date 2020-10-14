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

import "@testing-library/jest-dom/extend-expect";
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { act } from "react-dom/test-utils";
import { BrowserRouter } from "react-router-dom";
import * as mockData from "../../../../__mocks__/searchresult_mock_data";
import * as MimeTypesModule from "../../../../tsrc/modules/MimeTypesModule";
import SearchResult from "../../../../tsrc/search/components/SearchResult";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const defaultViewerPromise = jest
  .spyOn(MimeTypesModule, "getMimeTypeDefaultViewerDetails")
  .mockResolvedValue({
    viewerId: "fancy",
  } as OEQ.MimeType.MimeTypeViewerDetail);

describe("<SearchResult/>", () => {
  const renderSearchResult = async (
    itemResult: OEQ.Search.SearchResultItem
  ) => {
    const renderResult = render(
      //This needs to be wrapped inside a BrowserRouter, to prevent an `Invariant failed: You should not use <Link> outside a <Router>` error  because of the <Link/> tag within SearchResult
      <BrowserRouter>
        <SearchResult key={itemResult.uuid} item={itemResult} highlights={[]} />
      </BrowserRouter>
    );

    // Make sure we wait for the resolution of viewers - which will update the attachment lists
    await act(async () => {
      await defaultViewerPromise;
    });

    return renderResult;
  };

  const keywordFoundInAttachmentLabel =
    "Search term found in attachment content";

  it("Should show indicator in Attachment panel if keyword was found inside attachment", async () => {
    const itemWithSearchTermFound = mockData.keywordFoundInAttachmentObj;
    const { queryByLabelText } = await renderSearchResult(
      itemWithSearchTermFound
    );
    expect(queryByLabelText(keywordFoundInAttachmentLabel)).toBeInTheDocument();
  });

  it("Should not show indicator in Attachment panel if keyword was not found inside attachment", async () => {
    const itemWithNoSearchTermFound = mockData.attachSearchObj;
    const { queryByLabelText } = await renderSearchResult(
      itemWithNoSearchTermFound
    );
    expect(
      queryByLabelText(keywordFoundInAttachmentLabel)
    ).not.toBeInTheDocument();
  });

  it("displays the lightbox when an image attachment is clicked", async () => {
    const { attachSearchObj } = mockData;
    const { getByText, queryByLabelText } = await renderSearchResult(
      attachSearchObj
    );

    // Given a user clicks on an attachment
    userEvent.click(getByText(attachSearchObj.attachments![0].description!));

    // Then they see the lightbox
    expect(
      queryByLabelText(languageStrings.common.action.openInNewWindow)
    ).toBeInTheDocument();
  });
});

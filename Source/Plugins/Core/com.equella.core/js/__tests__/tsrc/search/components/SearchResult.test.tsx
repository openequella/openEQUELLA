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
import { render, RenderResult } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { act } from "react-dom/test-utils";
import { BrowserRouter } from "react-router-dom";
import { sprintf } from "sprintf-js";
import * as mockData from "../../../../__mocks__/searchresult_mock_data";
import * as MimeTypesModule from "../../../../tsrc/modules/MimeTypesModule";
import SearchResult from "../../../../tsrc/search/components/SearchResult";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import * as AppConfig from "../../../../tsrc/AppConfig";

const mockGetRenderData = jest.spyOn(AppConfig, "getRenderData");
// Mock the value of 'getRenderData'.
const updateMockRenderData = () =>
  mockGetRenderData.mockReturnValue({
    baseResources: "p/r/2020.2.0/com.equella.core/",
    newUI: true,
    autotestMode: false,
    newSearch: true,
    selectionSessionInfo: {
      stateId: "1",
      integId: "2",
      layout: "coursesearch",
    },
  });

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
        <SearchResult
          key={itemResult.uuid}
          item={itemResult}
          handleError={(error) =>
            console.warn(`Testing error handler: ${error}`)
          }
          highlights={[]}
        />
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

  it.each<[string, OEQ.Search.SearchResultItem, string]>([
    ["singular comment", mockData.basicSearchObj, "1 comment"],
    ["plural comments", mockData.attachSearchObj, "2 comments"],
  ])(
    "should show comment count as a link for %s",
    async (
      testName: string,
      item: OEQ.Search.SearchResultItem,
      expectedLinkText: string
    ) => {
      const { queryByText } = await renderSearchResult(item);
      expect(
        queryByText(expectedLinkText, { selector: "a" })
      ).toBeInTheDocument();
    }
  );

  it("should hide comment count link if count is 0", async () => {
    const { queryByText } = await renderSearchResult(
      mockData.keywordFoundInAttachmentObj
    );
    expect(
      queryByText(languageStrings.searchpage.comments.zero, { selector: "a" })
    ).toBeNull();
  });

  it("should show Star icons to represent Item ratings", async () => {
    const { starRatings } = mockData.attachSearchObj;
    const { queryByLabelText } = await renderSearchResult(
      mockData.attachSearchObj
    );
    expect(
      queryByLabelText(
        sprintf(languageStrings.searchpage.starRatings.label, starRatings)
      )
    ).toBeInTheDocument();
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

  it("should use different link to open ItemSummary page, depending on renderData", async () => {
    const item = mockData.basicSearchObj;
    const checkItemTitleLink = (page: RenderResult, url: string) => {
      expect(page.getByText(item.name!, { selector: "a" })).toHaveAttribute(
        "href",
        url
      );
    };
    const basicURL = `items/${item.uuid}/${item.version}/`;

    let page = await renderSearchResult(item);
    checkItemTitleLink(page, `/${basicURL}`);

    updateMockRenderData();
    page.unmount();
    page = await renderSearchResult(item);
    checkItemTitleLink(
      page,
      `${basicURL}?_sl.stateId=1&a=coursesearch&_int.id=2`
    );
  });

  it.each<[string]>([
    [languageStrings.searchpage.selectResource.summaryPage],
    [languageStrings.searchpage.selectResource.allAttachments],
    [languageStrings.searchpage.selectResource.attachment],
  ])(
    // todo: pass event handler in and check if it has been called.
    "should display buttons for %s in Selection Session",
    async (buttonLabel: string) => {
      updateMockRenderData();
      const { queryByLabelText } = await renderSearchResult(
        mockData.attachSearchObj
      );
      expect(queryByLabelText(buttonLabel)).toBeInTheDocument();
    }
  );
});

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
import { render, RenderResult, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { act } from "react-dom/test-utils";
import { BrowserRouter } from "react-router-dom";
import { sprintf } from "sprintf-js";
import * as mockData from "../../../../__mocks__/searchresult_mock_data";
import type {
  RenderData,
  SelectionSessionInfo,
} from "../../../../tsrc/AppConfig";
import {
  selectResourceForCourseList,
  selectResourceForNonCourseList,
} from "../../../../tsrc/modules/LegacySelectionSessionModule";
import * as MimeTypesModule from "../../../../tsrc/modules/MimeTypesModule";
import * as LegacySelectionSessionModule from "../../../../tsrc/modules/LegacySelectionSessionModule";
import SearchResult from "../../../../tsrc/search/components/SearchResult";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import * as AppConfig from "../../../../tsrc/AppConfig";

const basicSelectionSessionInfo: SelectionSessionInfo = {
  stateId: "1",
  layout: "coursesearch",
  integId: "2",
};

const basicRenderData: RenderData = {
  baseResources: "p/r/2020.2.0/com.equella.core/",
  newUI: true,
  autotestMode: false,
  newSearch: true,
  selectionSessionInfo: basicSelectionSessionInfo,
};
const mockGetRenderData = jest.spyOn(AppConfig, "getRenderData");
// Mock the value of 'getRenderData'.
const updateMockRenderData = (renderData: RenderData = basicRenderData) =>
  mockGetRenderData.mockReturnValue(renderData);

const defaultViewerPromise = jest
  .spyOn(MimeTypesModule, "getMimeTypeDefaultViewerDetails")
  .mockResolvedValue({
    viewerId: "fancy",
  } as OEQ.MimeType.MimeTypeViewerDetail);

const {
  summaryPage: selectSummaryPageString,
  allAttachments: selectAllAttachmentsString,
  attachment: selectAttachmentString,
} = languageStrings.searchpage.selectResource;

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

  it.each<[string]>([
    [selectSummaryPageString],
    [selectAllAttachmentsString],
    [selectAttachmentString],
  ])(
    "should hide %s button in non-Selection session",
    async (selectorLabel: string) => {
      const { queryByLabelText } = await renderSearchResult(
        mockData.attachSearchObj
      );
      expect(queryByLabelText(selectorLabel)).not.toBeInTheDocument();
    }
  );

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

  describe("In Selection Session", () => {
    const mockGlobalCourseList = jest.spyOn(
      LegacySelectionSessionModule,
      "getGlobalCourseList"
    );
    mockGlobalCourseList.mockReturnValue({ updateCourseList: jest.fn() });

    const mockSelectResourceForCourseList = jest.spyOn(
      LegacySelectionSessionModule,
      "selectResourceForCourseList"
    );
    mockSelectResourceForCourseList.mockResolvedValue();

    const mockSelectResourceForNonCourseList = jest.spyOn(
      LegacySelectionSessionModule,
      "selectResourceForNonCourseList"
    );
    mockSelectResourceForNonCourseList.mockResolvedValue();

    const selectOrAddInfo: SelectionSessionInfo = {
      ...basicSelectionSessionInfo,
      layout: "search",
    };

    const STRUCTURED = "structured";
    const SELECT_OR_ADD = "selectOrAdd";
    const selectForCourseFunc = selectResourceForCourseList;
    const selectForNonCourseFunc = selectResourceForNonCourseList;

    const makeSelection = (
      selectorLabel: string,
      queryByLabelText: (text: string) => HTMLElement | null,
      getByLabelText: (text: string) => HTMLElement
    ) => {
      // First, make sure the selector control is active
      expect(queryByLabelText(selectorLabel)).toBeInTheDocument();
      // And then make a selection by clicking it
      fireEvent.click(getByLabelText(selectorLabel));
    };

    it.each([
      [
        selectSummaryPageString,
        STRUCTURED,
        selectForCourseFunc,
        basicSelectionSessionInfo,
      ],
      [
        selectAllAttachmentsString,
        STRUCTURED,
        selectForCourseFunc,
        basicSelectionSessionInfo,
      ],
      [
        selectAttachmentString,
        STRUCTURED,
        selectForCourseFunc,
        basicSelectionSessionInfo,
      ],
      [
        selectSummaryPageString,
        SELECT_OR_ADD,
        selectForNonCourseFunc,
        selectOrAddInfo,
      ],
      [
        selectAllAttachmentsString,
        SELECT_OR_ADD,
        selectForNonCourseFunc,
        selectOrAddInfo,
      ],
      [
        selectAttachmentString,
        SELECT_OR_ADD,
        selectForNonCourseFunc,
        selectOrAddInfo,
      ],
    ])(
      "supports %s in %s mode",
      async (
        resourceType: string,
        selectionSessionMode: string,
        selectResourceFunc: (
          itemKey: string,
          attachmentUUIDs: string[]
        ) => Promise<void>,
        selectionSessionInfo: SelectionSessionInfo
      ) => {
        updateMockRenderData({
          ...basicRenderData,
          selectionSessionInfo: selectionSessionInfo,
        });

        const { queryByLabelText, getByLabelText } = await renderSearchResult(
          mockData.attachSearchObj
        );
        makeSelection(resourceType, queryByLabelText, getByLabelText);
        expect(selectResourceFunc).toHaveBeenCalled();
      }
    );
  });
});

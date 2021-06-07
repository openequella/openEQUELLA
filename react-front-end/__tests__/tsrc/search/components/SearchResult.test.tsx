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
import { createMuiTheme, MuiThemeProvider, Theme } from "@material-ui/core";
import * as OEQ from "@openequella/rest-api-client";
import "@testing-library/jest-dom/extend-expect";
import { render, RenderResult, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { act } from "react-dom/test-utils";
import { BrowserRouter } from "react-router-dom";
import { sprintf } from "sprintf-js";
import * as mockData from "../../../../__mocks__/searchresult_mock_data";
import type { RenderData } from "../../../../tsrc/AppConfig";
import {
  getGlobalCourseList,
  selectResourceForCourseList,
  selectResourceForSelectOrAdd,
  selectResourceForSkinny,
} from "../../../../tsrc/modules/LegacySelectionSessionModule";
import * as MimeTypesModule from "../../../../tsrc/modules/MimeTypesModule";
import * as LegacySelectionSessionModule from "../../../../tsrc/modules/LegacySelectionSessionModule";
import SearchResult from "../../../../tsrc/search/components/SearchResult";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { defaultBaseUrl, updateMockGetBaseUrl } from "../../BaseUrlHelper";
import { updateMockGlobalCourseList } from "../../CourseListHelper";
import {
  basicRenderData,
  renderDataForSelectOrAdd,
  renderDataForSkinny,
  selectSummaryButtonDisabled,
  updateMockGetRenderData,
  withIntegId,
} from "../../RenderDataHelper";

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

const defaultTheme = createMuiTheme({
  props: { MuiWithWidth: { initialWidth: "md" } },
});

describe("<SearchResult/>", () => {
  const renderSearchResult = async (
    itemResult: OEQ.Search.SearchResultItem,
    theme: Theme = defaultTheme
  ) => {
    const renderResult = render(
      //This needs to be wrapped inside a BrowserRouter, to prevent an `Invariant failed: You should not use <Link> outside a <Router>` error  because of the <Link/> tag within SearchResult
      <MuiThemeProvider theme={theme}>
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
      </MuiThemeProvider>
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

  it("hide star rating and comment count in small screen", async () => {
    const theme = createMuiTheme({
      props: { MuiWithWidth: { initialWidth: "sm" } },
    });
    const { starRatings, commentCount } = mockData.attachSearchObj;
    const { queryByLabelText, queryByText } = await renderSearchResult(
      mockData.attachSearchObj,
      theme
    );
    expect(
      queryByLabelText(
        sprintf(languageStrings.searchpage.starRatings.label, starRatings)
      )
    ).not.toBeInTheDocument();

    expect(
      queryByText(
        sprintf(languageStrings.searchpage.comments.more, commentCount),
        { selector: "a" }
      )
    ).not.toBeInTheDocument();
  });

  it.each([
    [
      "solid Heart",
      "remove",
      mockData.basicSearchObj,
      languageStrings.searchpage.favouriteItem.title.remove,
    ],
    [
      "empty Heart",
      "add",
      mockData.attachSearchObj,
      languageStrings.searchpage.favouriteItem.title.add,
    ],
  ])(
    "should show %s icon to %s favourite Item",
    async (
      iconType: string,
      action: string,
      item: OEQ.Search.SearchResultItem,
      iconLabel: string
    ) => {
      const { queryByLabelText } = await renderSearchResult(item);
      const iconButton = queryByLabelText(iconLabel);
      expect(iconButton).toBeInTheDocument();
    }
  );

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
    updateMockGetBaseUrl();
    updateMockGlobalCourseList();
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

    updateMockGetRenderData({
      ...basicRenderData,
      selectionSessionInfo: withIntegId,
    });
    page.unmount();
    page = await renderSearchResult(item);
    checkItemTitleLink(
      page,
      `${defaultBaseUrl}${basicURL}?_sl.stateId=1&_int.id=2&a=coursesearch`
    );
  });

  describe("Dead attachments handling", () => {
    it("should display dead attachments with a warning label", async () => {
      const { oneDeadAttachObj } = mockData;
      const { queryByTitle } = await renderSearchResult(oneDeadAttachObj);
      expect(
        queryByTitle(languageStrings.searchpage.deadAttachmentWarning)
      ).toBeInTheDocument();
    });

    it("should not render dead attachments as clickable links", async () => {
      //item with one dead attachment and one intact attachment
      const { oneDeadOneAliveAttachObj } = mockData;
      const { getByText, queryByLabelText } = await renderSearchResult(
        oneDeadOneAliveAttachObj
      );

      // Given a user clicks on a broken attachment
      userEvent.click(
        getByText(oneDeadOneAliveAttachObj.attachments![0].description!)
      );

      // There is no lightbox, as it is not rendered as a link
      expect(
        queryByLabelText(languageStrings.common.action.openInNewWindow)
      ).not.toBeInTheDocument();

      // Now if they click on the intact attachment instead...
      userEvent.click(
        getByText(oneDeadOneAliveAttachObj.attachments![1].description!)
      );

      // ...There is a lightbox
      expect(
        queryByLabelText(languageStrings.common.action.openInNewWindow)
      ).toBeInTheDocument();
    });
  });

  describe("In Selection Session", () => {
    beforeAll(() => {
      updateMockGlobalCourseList();
      updateMockGetBaseUrl();
    });

    const mockSelectResourceForCourseList = jest.spyOn(
      LegacySelectionSessionModule,
      "selectResourceForCourseList"
    );
    mockSelectResourceForCourseList.mockResolvedValue();

    const mockSelectResourceForSelectOrAdd = jest.spyOn(
      LegacySelectionSessionModule,
      "selectResourceForSelectOrAdd"
    );
    mockSelectResourceForSelectOrAdd.mockResolvedValue();

    const mockSelectResourceForSkinny = jest.spyOn(
      LegacySelectionSessionModule,
      "selectResourceForSkinny"
    );
    mockSelectResourceForSkinny.mockResolvedValue();

    const STRUCTURED = "structured";
    const SELECT_OR_ADD = "selectOrAdd";
    const SKINNY = "skinny";
    const selectForCourseFunc = selectResourceForCourseList;
    const selectForSelectOrAdd = selectResourceForSelectOrAdd;
    const selectForSkinny = selectResourceForSkinny;

    const makeSelection = (findSelector: () => HTMLElement | null) => {
      const selectorControl = findSelector();
      // First, make sure the selector control is active
      expect(selectorControl).toBeInTheDocument();
      // And then make a selection by clicking it.
      // Above expect can make sure selectorControl is not null.
      fireEvent.click(selectorControl!);
    };

    type selectResourceFuncType =
      | ((itemKey: string, attachmentUUIDs: string[]) => Promise<void>)
      | ((itemKey: string, attachmentUUID?: string) => Promise<void>);

    it.each([
      [
        selectSummaryPageString,
        STRUCTURED,
        selectForCourseFunc,
        basicRenderData,
      ],
      [
        selectAllAttachmentsString,
        STRUCTURED,
        selectForCourseFunc,
        basicRenderData,
      ],
      [
        selectAttachmentString,
        STRUCTURED,
        selectForCourseFunc,
        basicRenderData,
      ],
      [
        selectSummaryPageString,
        SELECT_OR_ADD,
        selectForSelectOrAdd,
        renderDataForSelectOrAdd,
      ],
      [
        selectAllAttachmentsString,
        SELECT_OR_ADD,
        selectForSelectOrAdd,
        renderDataForSelectOrAdd,
      ],
      [
        selectAttachmentString,
        SELECT_OR_ADD,
        selectForSelectOrAdd,
        renderDataForSelectOrAdd,
      ],
      [selectSummaryPageString, SKINNY, selectForSkinny, renderDataForSkinny],
      [selectAttachmentString, SKINNY, selectForSkinny, renderDataForSkinny],
    ])(
      "supports %s in %s mode",
      async (
        resourceType: string,
        selectionSessionMode: string,
        selectResourceFunc: selectResourceFuncType,
        renderData: RenderData
      ) => {
        updateMockGetRenderData(renderData);

        const { queryByLabelText } = await renderSearchResult(
          mockData.attachSearchObj
        );
        makeSelection(() => queryByLabelText(resourceType));
        expect(selectResourceFunc).toHaveBeenCalled();
      }
    );

    it("should hide the Select Summary button if it's disabled", async () => {
      updateMockGetRenderData({
        ...basicRenderData,
        selectionSessionInfo: selectSummaryButtonDisabled,
      });
      const { queryByLabelText } = await renderSearchResult(
        mockData.attachSearchObj
      );
      expect(queryByLabelText(selectSummaryPageString)).toBeNull();
    });

    it("should respect the integrationOpen displayOption", async () => {
      updateMockGetRenderData(basicRenderData);

      const expandedAttachment = await renderSearchResult(
        mockData.attachSearchObj
      );
      const collapsedAttachment = await renderSearchResult(
        mockData.keywordFoundInAttachmentObj
      );
      expect(expandedAttachment.queryByText("image.png")).toBeVisible();
      expect(collapsedAttachment.queryByText("config.json")).not.toBeVisible();
    });

    it("should make each attachment draggable", async () => {
      updateMockGetRenderData(basicRenderData);
      await renderSearchResult(mockData.attachSearchObj);

      const attachments = mockData.attachSearchObj.attachments!;
      // Make sure there are attachments in the SearchResult.
      expect(attachments.length).toBeGreaterThan(0);

      attachments.forEach((attachment) => {
        expect(
          getGlobalCourseList().prepareDraggableAndBind
        ).toHaveBeenCalledWith(`#${attachment.id}`, false);
      });
    });

    it("should hide All attachment button in Skinny", async () => {
      updateMockGetRenderData(renderDataForSkinny);
      const { queryByLabelText } = await renderSearchResult(
        mockData.attachSearchObj
      );
      expect(
        queryByLabelText(selectAllAttachmentsString)
      ).not.toBeInTheDocument();
    });

    describe("Dead attachments handling", () => {
      it("Should not be possible to select a dead attachment", async () => {
        updateMockGetRenderData({
          ...basicRenderData,
          selectionSessionInfo: selectSummaryButtonDisabled,
        });
        const { queryByLabelText } = await renderSearchResult(
          mockData.oneDeadAttachObj
        );
        expect(
          queryByLabelText(selectAttachmentString)
        ).not.toBeInTheDocument();
      });

      it("Should not show the Select All Attachments button if all attachments are dead", async () => {
        const { queryByLabelText } = await renderSearchResult(
          mockData.oneDeadAttachObj
        );
        expect(
          queryByLabelText(selectAllAttachmentsString)
        ).not.toBeInTheDocument();
      });

      it("Should show the Select All Attachments button if at least one attachment is not dead", async () => {
        const { queryByLabelText, getByTitle } = await renderSearchResult(
          mockData.oneDeadOneAliveAttachObj
        );
        expect(
          queryByLabelText(selectAllAttachmentsString)
        ).toBeInTheDocument();
        // Given the user clicks Select All Attachments for an item with a dead attachment
        // and an alive attachment...
        userEvent.click(getByTitle(selectAllAttachmentsString));

        // The function should only have been called with the attachment
        // 78883eff-7cf6-4b14-ab76-2b7f84dbe833 which is the intact one
        expect(
          mockSelectResourceForCourseList
        ).toHaveBeenCalledWith("72558c1d-8788-4515-86c8-b24a28cc451e/1", [
          "78883eff-7cf6-4b14-ab76-2b7f84dbe833",
        ]);
      });
    });
  });
});

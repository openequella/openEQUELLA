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
import CloseIcon from "@mui/icons-material/Close";
import { ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import "@testing-library/jest-dom";
import {
  queryByText,
  render,
  RenderResult,
  screen,
  waitFor,
  act,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { BrowserRouter } from "react-router-dom";
import { sprintf } from "sprintf-js";
import { DRM_VIOLATION, drmTerms } from "../../../../__mocks__/Drm.mock";
import { createMatchMedia } from "../../../../__mocks__/MockUseMediaQuery";
import {
  imageScrapbook,
  itemWithBookmark,
} from "../../../../__mocks__/SearchResult.mock";
import * as mockData from "../../../../__mocks__/searchresult_mock_data";
import {
  DRM_ATTACHMENT_NAME,
  DRM_ITEM_NAME,
  basicSearchObj,
} from "../../../../__mocks__/searchresult_mock_data";
import type { RenderData } from "../../../../tsrc/AppConfig";
import { TooltipIconButton } from "../../../../tsrc/components/TooltipIconButton";
import * as DrmModule from "../../../../tsrc/modules/DrmModule";
import * as LegacySelectionSessionModule from "../../../../tsrc/modules/LegacySelectionSessionModule";
import {
  getGlobalCourseList,
  selectResourceForCourseList,
  selectResourceForSelectOrAdd,
  selectResourceForSkinny,
} from "../../../../tsrc/modules/LegacySelectionSessionModule";
import * as MimeTypesModule from "../../../../tsrc/modules/MimeTypesModule";
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
const { tags: tagsLabel } = languageStrings.favourites.favouritesItem;

describe("<SearchResult/>", () => {
  const renderSearchResultWithConfig = async ({
    itemResult,
    customActionButtons,
    customOnClickTitleHandler,
    showBookmarkTags,
    screenWidth = 1280,
  }: {
    itemResult: OEQ.Search.SearchResultItem;
    customActionButtons?: React.JSX.Element[];
    customOnClickTitleHandler?: () => void;
    showBookmarkTags?: boolean;
    screenWidth?: number;
  }) => {
    window.matchMedia = createMatchMedia(screenWidth);
    const renderResult = render(
      // This needs to be wrapped inside a BrowserRouter, to prevent an
      // `Invariant failed: You should not use <Link> outside a <Router>`
      // error  because of the <Link/> tag within SearchResult
      <BrowserRouter>
        <ThemeProvider theme={createTheme()}>
          <SearchResult
            key={itemResult.uuid}
            item={itemResult}
            highlights={[]}
            getItemAttachments={async () => itemResult.attachments!}
            customActionButtons={customActionButtons}
            customOnClickTitleHandler={customOnClickTitleHandler}
            showBookmarkTags={showBookmarkTags}
          />
        </ThemeProvider>
      </BrowserRouter>,
    );

    // Make sure we wait for the resolution of viewers - which will update the attachment lists
    await act(async () => {
      await defaultViewerPromise;
    });

    return renderResult;
  };

  const renderSearchResult = async (
    itemResult: OEQ.Search.SearchResultItem,
    screenWidth?: number,
  ) => renderSearchResultWithConfig({ itemResult, screenWidth });

  const keywordFoundInAttachmentLabel =
    "Search term found in attachment content";

  it("Should show indicator in Attachment panel if keyword was found inside attachment", async () => {
    const { queryByLabelText } = await renderSearchResult(
      mockData.keywordFoundInAttachmentObj,
    );
    expect(queryByLabelText(keywordFoundInAttachmentLabel)).toBeInTheDocument();
  });

  it("Should not show indicator in Attachment panel if keyword was not found inside attachment", async () => {
    const { queryByLabelText } = await renderSearchResult(
      mockData.attachSearchObj,
    );
    expect(
      queryByLabelText(keywordFoundInAttachmentLabel),
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
      expectedLinkText: string,
    ) => {
      const { queryByText } = await renderSearchResult(item);
      expect(
        queryByText(expectedLinkText, { selector: "a" }),
      ).toBeInTheDocument();
    },
  );

  it("should hide comment count link if count is 0", async () => {
    const { queryByText } = await renderSearchResult(
      mockData.keywordFoundInAttachmentObj,
    );
    expect(
      queryByText(languageStrings.searchpage.comments.zero, { selector: "a" }),
    ).toBeNull();
  });

  it("should show Star icons to represent Item ratings", async () => {
    const { starRatings } = mockData.attachSearchObj;
    const { queryByLabelText } = await renderSearchResult(
      mockData.attachSearchObj,
    );
    expect(
      queryByLabelText(
        sprintf(languageStrings.searchpage.starRatings.label, starRatings),
      ),
    ).toBeInTheDocument();
  });

  it("displays the lightbox when an image attachment is clicked", async () => {
    const { attachSearchObj } = mockData;
    const { getByText, queryByLabelText } =
      await renderSearchResult(attachSearchObj);

    // Given a user clicks on an attachment
    await userEvent.click(
      getByText(attachSearchObj.attachments![0].description!),
    );

    // Then they see the lightbox
    expect(
      queryByLabelText(languageStrings.common.action.openInNewTab),
    ).toBeInTheDocument();
  });

  it("displays a dialog for sharing an Attachment", async () => {
    const { attachSearchObj } = mockData;
    const page = await renderSearchResult(attachSearchObj);
    const shareButton = page.getByLabelText(
      languageStrings.common.action.share,
    );
    await userEvent.click(shareButton);

    const dialog = page.getByRole("dialog");
    const { embedCode, link } = languageStrings.shareAttachment;
    expect(queryByText(dialog, embedCode)).toBeInTheDocument();
    expect(queryByText(dialog, link)).toBeInTheDocument();
  });

  it("disable the access to Item summary page in Lightbox for Scrapbook", async () => {
    const { getByText, queryByLabelText, container } =
      await renderSearchResult(imageScrapbook);

    await userEvent.click(
      getByText(imageScrapbook.attachments![0].description!),
    );

    // The lightbox has now been displayed with the unique element being the lightbox's 'embed code' button.
    const lightbox = container.querySelector(".Lightbox-lightboxBackdrop");
    expect(lightbox).toBeInTheDocument();
    expect(
      queryByLabelText(languageStrings.lightboxComponent.openSummaryPage),
    ).not.toBeInTheDocument();
  });

  it("hide star rating and comment count in small screen", async () => {
    const { starRatings, commentCount } = mockData.attachSearchObj;
    const { queryByLabelText, queryByText } = await renderSearchResult(
      mockData.attachSearchObj,
      600,
    );
    expect(
      queryByLabelText(
        sprintf(languageStrings.searchpage.starRatings.label, starRatings),
      ),
    ).not.toBeInTheDocument();

    expect(
      queryByText(
        sprintf(languageStrings.searchpage.comments.more, commentCount),
        { selector: "a" },
      ),
    ).not.toBeInTheDocument();
  });

  it.each([
    [
      "solid Heart",
      "remove",
      mockData.basicSearchObj,
      languageStrings.searchpage.favouriteItem.remove,
    ],
    [
      "empty Heart",
      "add",
      mockData.attachSearchObj,
      languageStrings.searchpage.favouriteItem.add,
    ],
  ])(
    "should show %s icon to %s favourite Item",
    async (
      iconType: string,
      action: string,
      item: OEQ.Search.SearchResultItem,
      iconLabel: string,
    ) => {
      const { queryByLabelText } = await renderSearchResult(item);
      const iconButton = queryByLabelText(iconLabel);
      expect(iconButton).toBeInTheDocument();
    },
  );

  it("displays custom action buttons", async () => {
    const text = "This is a custom action button";
    const actionButtons = [
      <TooltipIconButton title={text}>
        <CloseIcon />
      </TooltipIconButton>,
    ];
    const { queryByLabelText } = await renderSearchResultWithConfig({
      itemResult: mockData.basicSearchObj,
      customActionButtons: actionButtons,
    });

    expect(queryByLabelText(text, { selector: "button" })).toBeInTheDocument();
  });

  it.each<[string]>([
    [selectSummaryPageString],
    [selectAllAttachmentsString],
    [selectAttachmentString],
  ])(
    "should hide %s button in non-Selection session",
    async (selectorLabel: string) => {
      const { queryByLabelText } = await renderSearchResult(
        mockData.attachSearchObj,
      );
      expect(queryByLabelText(selectorLabel)).not.toBeInTheDocument();
    },
  );

  it("should use different link to open ItemSummary page, depending on renderData", async () => {
    updateMockGetBaseUrl();
    updateMockGlobalCourseList();
    const item = mockData.basicSearchObj;
    const checkItemTitleLink = (page: RenderResult, url: string) => {
      expect(page.getByRole("link", { name: item.name! })).toHaveAttribute(
        "href",
        url,
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
      `${defaultBaseUrl}${basicURL}?_sl.stateId=1&_int.id=2&a=coursesearch`,
    );
  });

  it("supports custom handler for clicking the title", async () => {
    const customHandler = jest.fn();
    const item = mockData.basicSearchObj;

    const { getByText } = await renderSearchResultWithConfig({
      itemResult: item,
      customOnClickTitleHandler: customHandler,
    });

    const title = getByText(`${item.name}`, { selector: "span" });
    await userEvent.click(title);

    expect(customHandler).toHaveBeenCalledTimes(1);
  });

  describe("Dead attachments handling", () => {
    it("should display dead attachments with a warning label", async () => {
      const { oneDeadAttachObj } = mockData;
      const { queryByLabelText } = await renderSearchResult(oneDeadAttachObj);
      expect(
        queryByLabelText(languageStrings.searchpage.deadAttachmentWarning),
      ).toBeInTheDocument();
    });

    it("should not render dead attachments as clickable links", async () => {
      //item with one dead attachment and one intact attachment
      const { oneDeadOneAliveAttachObj } = mockData;
      const { getByText, queryByLabelText } = await renderSearchResult(
        oneDeadOneAliveAttachObj,
      );

      // Given a user clicks on a broken attachment
      await userEvent.click(
        getByText(oneDeadOneAliveAttachObj.attachments![0].description!),
      );

      // There is no lightbox, as it is not rendered as a link
      expect(
        queryByLabelText(languageStrings.common.action.openInNewWindow),
      ).not.toBeInTheDocument();

      // Now if they click on the intact attachment instead...
      await userEvent.click(
        getByText(oneDeadOneAliveAttachObj.attachments![1].description!),
      );

      // ...There is a lightbox
      expect(
        queryByLabelText(languageStrings.common.action.openInNewTab),
      ).toBeInTheDocument();
    });
  });

  describe("In Selection Session", () => {
    beforeAll(() => {
      updateMockGlobalCourseList();
      updateMockGetBaseUrl();
    });

    afterEach(() => {
      jest.clearAllMocks();
    });

    const mockSelectResourceForCourseList = jest.spyOn(
      LegacySelectionSessionModule,
      "selectResourceForCourseList",
    );
    mockSelectResourceForCourseList.mockResolvedValue();

    const mockSelectResourceForSelectOrAdd = jest.spyOn(
      LegacySelectionSessionModule,
      "selectResourceForSelectOrAdd",
    );
    mockSelectResourceForSelectOrAdd.mockResolvedValue();

    const mockSelectResourceForSkinny = jest.spyOn(
      LegacySelectionSessionModule,
      "selectResourceForSkinny",
    );
    mockSelectResourceForSkinny.mockResolvedValue();

    const STRUCTURED = "structured";
    const SELECT_OR_ADD = "selectOrAdd";
    const SKINNY = "skinny";
    const selectForCourseFunc = selectResourceForCourseList;
    const selectForSelectOrAdd = selectResourceForSelectOrAdd;
    const selectForSkinny = selectResourceForSkinny;

    const makeSelection = async (findSelector: () => HTMLElement | null) => {
      const selectorControl = findSelector();
      // First, make sure the selector control is active
      expect(selectorControl).toBeInTheDocument();
      // And then make a selection by clicking it.
      // Above expect can make sure selectorControl is not null.
      await userEvent.click(selectorControl!);
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
        renderData: RenderData,
      ) => {
        updateMockGetRenderData(renderData);

        const { queryByLabelText } = await renderSearchResult(
          mockData.attachSearchObj,
        );
        await makeSelection(() => queryByLabelText(resourceType));
        expect(selectResourceFunc).toHaveBeenCalled();
      },
    );

    it("should hide the Select Summary button if it's disabled", async () => {
      updateMockGetRenderData({
        ...basicRenderData,
        selectionSessionInfo: selectSummaryButtonDisabled,
      });
      const { queryByLabelText } = await renderSearchResult(
        mockData.attachSearchObj,
      );
      expect(queryByLabelText(selectSummaryPageString)).toBeNull();
    });

    it("should respect the integrationOpen displayOption", async () => {
      updateMockGetRenderData(basicRenderData);

      const expandedAttachment = await renderSearchResult(
        mockData.attachSearchObj,
      );
      const collapsedAttachment = await renderSearchResult(
        mockData.keywordFoundInAttachmentObj,
      );
      expect(expandedAttachment.queryByText("image.png")).toBeVisible();
      expect(collapsedAttachment.queryByText("config.json")).toBeFalsy(); // i.e. not rendered so not visible
    });

    it("should make each attachment of live Items draggable", async () => {
      updateMockGetRenderData(basicRenderData);
      await renderSearchResult(mockData.attachSearchObj);

      const attachments = mockData.attachSearchObj.attachments!;
      // Make sure there are attachments in the SearchResult.
      expect(attachments.length).toBeGreaterThan(0);

      attachments.forEach((attachment) => {
        expect(
          getGlobalCourseList().prepareDraggableAndBind,
        ).toHaveBeenCalledWith(`#${attachment.id}`, false);
      });
    });

    it("should not make attachments of non-live Items draggable", async () => {
      updateMockGetRenderData(basicRenderData);
      await renderSearchResult(mockData.nonLiveObj);

      expect(
        getGlobalCourseList().prepareDraggableAndBind,
      ).not.toHaveBeenCalled();
    });

    it("should not make broken attachments draggable", async () => {
      updateMockGetRenderData(basicRenderData);
      await renderSearchResult(mockData.oneDeadOneAliveAttachObj);

      const deadAttachment = mockData.oneDeadOneAliveAttachObj.attachments![0];
      const intactAttachment =
        mockData.oneDeadOneAliveAttachObj.attachments![1];

      //expect intact attachment to be draggable
      expect(
        getGlobalCourseList().prepareDraggableAndBind,
      ).toHaveBeenCalledWith(`#${intactAttachment.id}`, false);

      //expect dead attachment not to be draggable
      expect(
        getGlobalCourseList().prepareDraggableAndBind,
      ).not.toHaveBeenCalledWith(`#${deadAttachment.id}`, false);
    });

    it("should hide All attachment button in Skinny", async () => {
      updateMockGetRenderData(renderDataForSkinny);
      const { queryByLabelText } = await renderSearchResult(
        mockData.attachSearchObj,
      );
      expect(
        queryByLabelText(selectAllAttachmentsString),
      ).not.toBeInTheDocument();
    });

    describe("Dead attachments handling", () => {
      it("Should not be possible to select a dead attachment", async () => {
        updateMockGetRenderData({
          ...basicRenderData,
          selectionSessionInfo: selectSummaryButtonDisabled,
        });
        const { queryByLabelText } = await renderSearchResult(
          mockData.oneDeadAttachObj,
        );
        expect(
          queryByLabelText(selectAttachmentString),
        ).not.toBeInTheDocument();
      });

      it("Should not show the Select All Attachments button if all attachments are dead", async () => {
        updateMockGetRenderData(basicRenderData);
        const { queryByLabelText } = await renderSearchResult(
          mockData.oneDeadAttachObj,
        );
        expect(
          queryByLabelText(selectAllAttachmentsString),
        ).not.toBeInTheDocument();
      });

      it("Should show the Select All Attachments button if at least one attachment is not dead", async () => {
        updateMockGetRenderData(basicRenderData);
        const { queryByLabelText, getByLabelText } = await renderSearchResult(
          mockData.oneDeadOneAliveAttachObj,
        );

        expect(
          queryByLabelText(selectAllAttachmentsString),
        ).toBeInTheDocument();
        // Given the user clicks Select All Attachments for an item with a dead attachment
        // and an alive attachment...
        await userEvent.click(getByLabelText(selectAllAttachmentsString));

        // The function should only have been called with the attachment
        // 78883eff-7cf6-4b14-ab76-2b7f84dbe833 which is the intact one
        expect(mockSelectResourceForCourseList).toHaveBeenCalledWith(
          "72558c1d-8788-4515-86c8-b24a28cc451e/1",
          ["78883eff-7cf6-4b14-ab76-2b7f84dbe833"],
        );
      });
    });
  });

  describe("DRM support", () => {
    jest
      .spyOn(DrmModule, "listDrmViolations")
      .mockResolvedValue({ violation: DRM_VIOLATION });
    jest.spyOn(DrmModule, "listDrmTerms").mockResolvedValue(drmTerms);

    it.each([
      [
        "DRM terms must be accepted before viewing a DRM Item's summary page",
        DRM_ITEM_NAME,
      ],
      ["preview an attachment from a DRM item", DRM_ATTACHMENT_NAME],
    ])(
      "shows the DRM Accept dialog when %s",
      async (_: string, linkText: string) => {
        const { drmAttachObj } = mockData;
        const { getByText } = await renderSearchResult(drmAttachObj);
        await userEvent.click(getByText(linkText));

        await waitFor(() => {
          expect(
            queryByText(screen.getByRole("dialog"), drmTerms.title),
          ).toBeInTheDocument();
        });
      },
    );

    it.each([
      ["item", DRM_ITEM_NAME],
      ["attachment", DRM_ATTACHMENT_NAME],
    ])(
      "shows a dialog to list DRM violations for %s",
      async (_: string, linkText: string) => {
        const { drmUnauthorisedObj } = mockData;
        const { getByText } = await renderSearchResult(drmUnauthorisedObj);
        await userEvent.click(getByText(linkText));

        await waitFor(() => {
          expect(
            queryByText(screen.getByRole("dialog"), DRM_VIOLATION),
          ).toBeInTheDocument();
        });
      },
    );

    it("supports viewing a DRM Item's summary page without accepting the terms", async () => {
      const { getByText } = await renderSearchResult(
        mockData.drmAllowSummaryObj,
      );
      await userEvent.click(getByText(DRM_ITEM_NAME));

      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    });
  });

  it("should hide bookmark tags by default", async () => {
    const item = itemWithBookmark;
    const { queryByText } = await renderSearchResult(item);

    expect(queryByText(tagsLabel)).not.toBeInTheDocument();
    item.bookmark?.tags.forEach((tag) => {
      expect(queryByText(tag)).not.toBeInTheDocument();
    });
    expect.assertions(3);
  });

  it("should display bookmark tags when the 'showBookmarkTags' prop is true", async () => {
    const item = itemWithBookmark;
    const { getByText } = await renderSearchResultWithConfig({
      itemResult: item,
      showBookmarkTags: true,
    });

    expect(getByText(tagsLabel)).toBeInTheDocument();
    item.bookmark?.tags.forEach((tag) => {
      expect(getByText(tag)).toBeInTheDocument();
    });
    expect.assertions(3);
  });

  it("should hide bookmark tags when 'showBookmarkTags' is true but the item has no tags", async () => {
    const { queryByText } = await renderSearchResultWithConfig({
      itemResult: basicSearchObj,
      showBookmarkTags: true,
    });

    expect(queryByText(tagsLabel)).not.toBeInTheDocument();
  });
});

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
import type { RenderData, SelectionSessionInfo } from "../../../tsrc/AppConfig";
import {
  buildFavouritesSearchSelectionSessionLink,
  buildPostDataForSelectOrAdd,
  buildPostDataForStructured,
  buildSelectionSessionAdvancedSearchLink,
  buildSelectionSessionItemSummaryLink,
  buildSelectionSessionSearchPageLink,
  isSelectionSessionOpen,
  isSelectSummaryButtonDisabled,
  SelectionSessionPostData,
} from "../../../tsrc/modules/LegacySelectionSessionModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { defaultBaseUrl, updateMockGetBaseUrl } from "../BaseUrlHelper";
import {
  basicRenderData,
  basicSelectionSessionInfo,
  renderDataForSelectOrAdd,
  selectSummaryButtonDisabled,
  updateMockGetRenderData,
  withIntegId,
} from "../RenderDataHelper";

const {
  summaryPage: selectSummaryPageString,
  allAttachments: selectAllAttachmentsString,
  attachment: selectAttachmentString,
} = languageStrings.searchpage.selectResource;
const itemKey = "72558c1d-8788-4515-86c8-b24a28cc451e/1";
const attachmentUUID = "4c636d7e-21fe-4202-b7de-2e8a728b8ffc";
const attachmentUUIDs = [
  "4c636d7e-21fe-4202-b7de-2e8a728b8ffc",
  "29e0fe1b-dbd6-4c98-9e7a-d957d9c731f5",
];

describe("buildSelectionSessionItemSummaryLink", () => {
  const { uuid, version } = getSearchResult.results[0];
  beforeAll(() => {
    updateMockGetBaseUrl();
  });

  it("builds basic URLs for accessing ItemSummary pages in Selection Session mode", () => {
    updateMockGetRenderData(basicRenderData);
    const link = buildSelectionSessionItemSummaryLink(uuid, version);
    expect(link).toBe(
      `${defaultBaseUrl}items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?_sl.stateId=1&a=coursesearch`,
    );
  });

  it("will include the Integration ID in the URL if provided in SelectionSessionInfo", () => {
    updateMockGetRenderData({
      ...basicRenderData,
      selectionSessionInfo: withIntegId,
    });

    const link = buildSelectionSessionItemSummaryLink(uuid, version);
    expect(link).toBe(
      `${defaultBaseUrl}items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?_sl.stateId=1&_int.id=2&a=coursesearch`,
    );
  });
});

describe("isSelectionSessionOpen", () => {
  it.each<[string, boolean, RenderData | undefined]>([
    ["RenderData includes SelectionSessionInfo", true, basicRenderData],
    [
      "selectionSessionInfo is null",
      false,
      { ...basicRenderData, selectionSessionInfo: null },
    ],
    ["renderData is undefined", false, undefined],
  ])(
    "when %s return %s",
    (
      when: string,
      inSelectionSession: boolean,
      renderData: RenderData | undefined,
    ) => {
      updateMockGetRenderData(renderData);
      expect(isSelectionSessionOpen()).toBe(inSelectionSession);
    },
  );
});

describe("buildPostDataForStructured", () => {
  const basicPostData = {
    "_sl.stateId": ["1"],
    a: ["coursesearch"],
    event__: ["_slcl.reloadFolder"],
  };
  const ajaxIds = `"ajaxIds":["courselistajax"]`;

  const itemSummaryData: SelectionSessionPostData = {
    ...basicPostData,
    eventp__0: [`{${ajaxIds},"event":["_slcl.selectItem","${itemKey}",null]}`],
  };

  const oneAttachmentData: SelectionSessionPostData = {
    ...basicPostData,
    eventp__0: [
      `{${ajaxIds},"event":["_slcl.selectAllAttachments","${attachmentUUID}","${itemKey}",null]}`,
    ],
  };

  const allAttachmentsData: SelectionSessionPostData = {
    ...basicPostData,
    eventp__0: [
      `{${ajaxIds},"event":["_slcl.selectAllAttachments","${attachmentUUIDs.join(
        ",",
      )}","${itemKey}",null]}`,
    ],
  };

  it.each<[string, string[], SelectionSessionPostData]>([
    [selectSummaryPageString, [], itemSummaryData],
    [selectAttachmentString, [attachmentUUID], oneAttachmentData],
    [selectAllAttachmentsString, attachmentUUIDs, allAttachmentsData],
  ])(
    "builds POST data for %s in 'structured'",
    (
      resourceType: string,
      attachmentUUIDs: string[],
      expectedPostData: SelectionSessionPostData,
    ) => {
      updateMockGetRenderData(basicRenderData);
      const data: SelectionSessionPostData = buildPostDataForStructured(
        itemKey,
        attachmentUUIDs,
      );
      expect(data).toMatchObject(expectedPostData);
    },
  );
});

describe("buildPostDataForSelectOrAdd", () => {
  const basicPostData = {
    "_sl.stateId": ["1"],
    a: ["search"],
  };

  const itemSummaryData: SelectionSessionPostData = {
    ...basicPostData,
    event__: ["sile.select"],
    eventp__0: [`${itemKey}`],
    eventp__1: [null],
  };

  const basicAttachmentData = {
    ...basicPostData,
    event__: [`ilad.selectAttachmentsFromNewSearch`],
    eventp__1: [`${itemKey}`],
    eventp__2: [null],
  };

  const oneAttachmentData: SelectionSessionPostData = {
    ...basicAttachmentData,
    eventp__0: [attachmentUUID],
  };

  const allAttachmentsData: SelectionSessionPostData = {
    ...basicAttachmentData,
    eventp__0: [attachmentUUIDs.join(",")],
  };

  it.each<[string, string[], SelectionSessionPostData]>([
    [selectSummaryPageString, [], itemSummaryData],
    [selectAttachmentString, [attachmentUUID], oneAttachmentData],
    [selectAllAttachmentsString, attachmentUUIDs, allAttachmentsData],
  ])(
    "builds POST data for %s in 'selectOrAdd'",
    (
      resourceType: string,
      attachmentUUIDs: string[],
      expectedPostData: SelectionSessionPostData,
    ) => {
      updateMockGetRenderData(renderDataForSelectOrAdd);
      const data: SelectionSessionPostData = buildPostDataForSelectOrAdd(
        itemKey,
        attachmentUUIDs,
      );
      expect(data).toMatchObject(expectedPostData);
    },
  );
});

describe("isSelectSummaryButtonDisabled", () => {
  it.each<[string, boolean, RenderData | undefined]>([
    ["'isSelectSummaryButtonDisabled' is false", false, basicRenderData],
    [
      "'isSelectSummaryButtonDisabled' is true",
      true,
      {
        ...basicRenderData,
        selectionSessionInfo: selectSummaryButtonDisabled,
      },
    ],
    [
      "selectionSessionInfo is null",
      true,
      { ...basicRenderData, selectionSessionInfo: null },
    ],
    ["renderData is undefined", true, undefined],
  ])(
    "when %s return %s",
    (
      when: string,
      isButtonDisabled: boolean,
      renderData: RenderData | undefined,
    ) => {
      updateMockGetRenderData(renderData);
      expect(isSelectSummaryButtonDisabled()).toBe(isButtonDisabled);
    },
  );
});

describe("buildSelectionSessionAdvancedSearchLink", () => {
  beforeEach(() => {
    updateMockGetRenderData(basicRenderData);
    updateMockGetBaseUrl();
  });

  const advSearchId = "72558c1d-8788-4515-86c8-b24a28cc451e";

  it("builds a link for accessing an Advanced search", () => {
    const link = buildSelectionSessionAdvancedSearchLink(advSearchId);
    expect(link).toBe(
      "http://localhost:8080/vanilla/advanced/searching.do?in=P72558c1d-8788-4515-86c8-b24a28cc451e&editquery=true&_sl.stateId=1",
    );
  });

  it("supports including external MIME types in the link", () => {
    const link = buildSelectionSessionAdvancedSearchLink(advSearchId, [
      "image/gif",
    ]);
    expect(link).toBe(
      "http://localhost:8080/vanilla/advanced/searching.do?in=P72558c1d-8788-4515-86c8-b24a28cc451e&editquery=true&_sl.stateId=1&_int.mimeTypes=image%2Fgif",
    );
  });
});

describe("buildSelectionSessionSearchPageLink", () => {
  it.each<[string, SelectionSessionInfo, string]>([
    ["structured", basicSelectionSessionInfo, "access/course"],
    [
      "selectoradd",
      { ...basicSelectionSessionInfo, layout: "search" },
      "selectoradd",
    ],
    [
      "skinny",
      { ...basicSelectionSessionInfo, layout: "skinnysearch" },
      "access/skinny",
    ],
  ])(
    "builds a link for accessing search page in %s layout",
    (layout: string, selectionSessionInfo: SelectionSessionInfo, path) => {
      updateMockGetRenderData({ ...basicRenderData, selectionSessionInfo });
      updateMockGetBaseUrl();
      const link = buildSelectionSessionSearchPageLink(
        new URLSearchParams("?query=apple"),
        ["image/gif"],
      );

      expect(link).toBe(
        `http://localhost:8080/vanilla/${path}/searching.do?query=apple&_sl.stateId=1&_int.mimeTypes=image%2Fgif`,
      );
    },
  );
});

describe("buildFavouritesSearchSelectionSessionLink", () => {
  it.each<[string, string, string]>([
    [
      "hierarchy",
      "/page/hierarchy/hierarchy-uuid",
      "http://localhost:8080/vanilla/hierarchy.do?topic=hierarchy-uuid&_sl.stateId=1",
    ],
    [
      "advanced search",
      "/page/advancedsearch/advanced-uuid?searchOptions=test",
      "http://localhost:8080/vanilla/advanced/searching.do?in=Padvanced-uuid&editquery=true&_sl.stateId=1",
    ],
    [
      "normal search",
      "/page/search?searchOptions=%7B%22query%22%3A%22apple%22%7D",
      "http://localhost:8080/vanilla/access/course/searching.do?searchOptions=%7B%22query%22%3A%22apple%22%7D&_sl.stateId=1",
    ],
  ])("builds a link for accessing %s page", (_: string, path, exceptResult) => {
    updateMockGetRenderData({
      ...basicRenderData,
      selectionSessionInfo: basicSelectionSessionInfo,
    });
    updateMockGetBaseUrl();

    const link = buildFavouritesSearchSelectionSessionLink(path);

    expect(link).toBe(exceptResult);
  });
});

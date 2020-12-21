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
import type { RenderData } from "../../../tsrc/AppConfig";
import {
  buildPostDataForSelectOrAdd,
  buildPostDataForStructured,
  buildSelectionSessionItemSummaryLink,
  isSelectionSessionOpen,
  isSelectSummaryButtonDisabled,
  SelectionSessionPostData,
} from "../../../tsrc/modules/LegacySelectionSessionModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { defaultBaseUrl, updateMockGetBaseUrl } from "../BaseUrlHelper";
import {
  basicRenderData,
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
      `${defaultBaseUrl}items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?_sl.stateId=1&a=coursesearch`
    );
  });

  it("will include the Integration ID in the URL if provided in SelectionSessionInfo", () => {
    updateMockGetRenderData({
      ...basicRenderData,
      selectionSessionInfo: withIntegId,
    });

    const link = buildSelectionSessionItemSummaryLink(uuid, version);
    expect(link).toBe(
      `${defaultBaseUrl}items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?_sl.stateId=1&_int.id=2&a=coursesearch`
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
      renderData: RenderData | undefined
    ) => {
      updateMockGetRenderData(renderData);
      expect(isSelectionSessionOpen()).toBe(inSelectionSession);
    }
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
        ","
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
      expectedPostData: SelectionSessionPostData
    ) => {
      updateMockGetRenderData(basicRenderData);
      const data: SelectionSessionPostData = buildPostDataForStructured(
        itemKey,
        attachmentUUIDs
      );
      expect(data).toMatchObject(expectedPostData);
    }
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
      expectedPostData: SelectionSessionPostData
    ) => {
      updateMockGetRenderData(renderDataForSelectOrAdd);
      const data: SelectionSessionPostData = buildPostDataForSelectOrAdd(
        itemKey,
        attachmentUUIDs
      );
      expect(data).toMatchObject(expectedPostData);
    }
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
      renderData: RenderData | undefined
    ) => {
      updateMockGetRenderData(renderData);
      expect(isSelectSummaryButtonDisabled()).toBe(isButtonDisabled);
    }
  );
});

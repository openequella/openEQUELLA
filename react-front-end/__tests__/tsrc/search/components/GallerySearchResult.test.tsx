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
import "@testing-library/jest-dom";
import { queryByText, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import { Router } from "react-router-dom";
import * as React from "react";
import { DRM_VIOLATION, drmTerms } from "../../../../__mocks__/Drm.mock";
import * as DrmModule from "../../../../tsrc/modules/DrmModule";
import { GallerySearchResultItem } from "../../../../tsrc/modules/GallerySearchModule";
import GallerySearchResult from "../../../../tsrc/search/components/GallerySearchResult";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import {
  buildItems,
  galleryDrmItem,
  galleryDrmItemSummaryAllow,
  galleryDrmUnauthorisedItem,
  galleryScrapbook,
} from "./GallerySearchResultHelpers";
import * as ReactRouterDom from "react-router-dom";

const {
  searchpage: {
    gallerySearchResult: { ariaLabel, viewItem },
  },
  common: {
    action: { openInNewTab, share },
  },
  lightboxComponent: { viewNext, viewPrevious, openSummaryPage },
} = languageStrings;

/*
 * Need to mock the react-router context and function 'push' so that we can test for navigating
 * to item summary pages.
 */
const mockUseHistoryPush = jest.fn();
jest.mock("react-router", () => ({
  ...(jest.requireActual("react-router") as typeof ReactRouterDom), // Only mock 'useHistory'.
  useHistory: () => ({
    push: mockUseHistoryPush,
  }),
}));

describe("<GallerySearchResult />", () => {
  const renderGallery = (items: GallerySearchResultItem[] = buildItems(5)) =>
    render(
      <Router history={createMemoryHistory()}>
        <GallerySearchResult items={items} />
      </Router>,
    ); // 16 entries in total.

  it("displays the lightbox when the image is clicked on", async () => {
    const { getAllByLabelText, queryAllByLabelText } = renderGallery();
    await userEvent.click(getAllByLabelText(ariaLabel)[0]);

    // Then they see the lightbox
    expect(queryAllByLabelText(openInNewTab)[0]).toBeInTheDocument();
  });

  it("navigates to the images item when the information icon is clicked on", async () => {
    const { getAllByLabelText } = renderGallery();
    await userEvent.click(getAllByLabelText(viewItem)[0]);

    expect(mockUseHistoryPush).toHaveBeenCalled();
    expect(mockUseHistoryPush.mock.calls[0][0].match("/items/")).toBeTruthy();
  });

  it("displays the Share Attachment dialog when the Share icon is clicked on", async () => {
    const { getAllByLabelText, getByRole } = renderGallery();
    await userEvent.click(getAllByLabelText(share)[0]);

    const dialog = getByRole("dialog");
    const { embedCode, link } = languageStrings.shareAttachment;
    expect(queryByText(dialog, embedCode)).toBeInTheDocument();
    expect(queryByText(dialog, link)).toBeInTheDocument();
  });

  it("doesn't show the Info icon for Scrapbook", async () => {
    const { queryAllByLabelText } = renderGallery([galleryScrapbook]);
    expect(queryAllByLabelText(viewItem)).toHaveLength(0);
  });

  it("doesn't support opening Item summary page from Lightbox for Scrapbook", async () => {
    const { getByLabelText, queryByLabelText } = renderGallery([
      galleryScrapbook,
    ]);

    await userEvent.click(getByLabelText(ariaLabel));

    // The icon button for accessing summary page should not exist.
    expect(queryByLabelText(openSummaryPage)).not.toBeInTheDocument();
  });

  describe("viewing gallery entries in a loop", () => {
    it.each([
      ["first", "next", "last", 15, viewNext],
      ["last", "previous", "first", 0, viewPrevious],
    ])(
      "shows the %s entry when navigate to the %s entry of the %s entry",
      async (
        currentPosition: string,
        direction: string,
        newPosition: string,
        index: number,
        arrowButtonLabel: string,
      ) => {
        const { getAllByLabelText, queryByLabelText } = renderGallery();
        await userEvent.click(getAllByLabelText(ariaLabel)[index]);
        expect(queryByLabelText(arrowButtonLabel)).toBeInTheDocument();
      },
    );
  });

  describe("DRM support", () => {
    jest
      .spyOn(DrmModule, "listDrmViolations")
      .mockResolvedValue({ violation: DRM_VIOLATION });
    jest.spyOn(DrmModule, "listDrmTerms").mockResolvedValue(drmTerms);

    it.each([
      [
        "DRM terms must be accepted before viewing a DRM Item's summary page",
        viewItem,
      ],
      ["preview an gallery entry protected by DRM", ariaLabel],
    ])(
      "shows DRM acceptance dialog when %s",
      async (_: string, galleryEntryLabel: string) => {
        const { getByLabelText } = await renderGallery([galleryDrmItem]);
        await userEvent.click(getByLabelText(galleryEntryLabel));

        await waitFor(() => {
          expect(
            queryByText(screen.getByRole("dialog"), drmTerms.title),
          ).toBeInTheDocument();
        });
      },
    );

    it.each([
      ["item", viewItem],
      ["attachment", ariaLabel],
    ])(
      "shows a dialog to list DRM violations for %s",
      async (_: string, galleryEntryLabel: string) => {
        const { getByLabelText } = await renderGallery([
          galleryDrmUnauthorisedItem,
        ]);
        await userEvent.click(getByLabelText(galleryEntryLabel));

        await waitFor(() => {
          expect(
            queryByText(screen.getByRole("dialog"), DRM_VIOLATION),
          ).toBeInTheDocument();
        });
      },
    );

    it("supports viewing a DRM Item's summary page without accepting the terms", async () => {
      const { getByLabelText } = await renderGallery([
        galleryDrmItemSummaryAllow,
      ]);
      await userEvent.click(getByLabelText(viewItem));

      expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    });
  });
});

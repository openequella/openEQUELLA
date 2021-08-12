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
import "@testing-library/jest-dom/extend-expect";
import { queryByText, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import { act } from "react-dom/test-utils";
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
  galleryDrmUnauthorisedItem,
} from "./GallerySearchResultHelpers";
import * as ReactRouterDom from "react-router-dom";

const {
  searchpage: {
    gallerySearchResult: { ariaLabel, viewItem },
  },
  common: {
    action: { openInNewWindow },
  },
  lightboxComponent: { viewNext, viewPrevious },
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
      </Router>
    ); // 16 entries in total.

  it("displays the lightbox when the image is clicked on", async () => {
    const { getAllByLabelText, queryAllByLabelText } = renderGallery();
    await act(async () => {
      await userEvent.click(getAllByLabelText(ariaLabel)[0]);
    });

    // Then they see the lightbox
    expect(queryAllByLabelText(openInNewWindow)[0]).toBeInTheDocument();
  });

  it("navigates to the images item when the information icon is clicked on", async () => {
    const { getAllByLabelText } = renderGallery();
    await act(async () => {
      await userEvent.click(getAllByLabelText(viewItem)[0]);
    });

    expect(mockUseHistoryPush).toHaveBeenCalled();
    expect(mockUseHistoryPush.mock.calls[0][0].match("/items/")).toBeTruthy();
  });

  describe("viewing gallery entries in a loop", () => {
    it.each([
      ["first", "next", "last", 15, viewNext],
      ["last", "previous", "first", 0, viewPrevious],
    ])(
      "shows the %s entry when navigate to the %s entry of the %s entry",
      (
        currentPosition: string,
        direction: string,
        newPosition: string,
        index: number,
        arrowButtonLabel: string
      ) => {
        const { getAllByLabelText, queryByLabelText } = renderGallery();
        userEvent.click(getAllByLabelText(ariaLabel)[index]);
        expect(queryByLabelText(arrowButtonLabel)).toBeInTheDocument();
      }
    );
  });

  describe("DRM support", () => {
    jest
      .spyOn(DrmModule, "listDrmViolations")
      .mockResolvedValue({ violation: DRM_VIOLATION });
    jest.spyOn(DrmModule, "listDrmTerms").mockResolvedValue(drmTerms);

    it.each([
      ["view a DRM Item's summary page", viewItem],
      ["preview an gallery entry protected by DRM", ariaLabel],
    ])(
      "shows DRM acceptance dialog when %s",
      async (_: string, galleryEntryLabel: string) => {
        const { getByLabelText } = await renderGallery([galleryDrmItem]);
        await act(async () => {
          await userEvent.click(getByLabelText(galleryEntryLabel));
        });

        await waitFor(() => {
          expect(
            queryByText(screen.getByRole("dialog"), drmTerms.title)
          ).toBeInTheDocument();
        });
      }
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
        await act(async () => {
          await userEvent.click(getByLabelText(galleryEntryLabel));
        });

        await waitFor(() => {
          expect(
            queryByText(screen.getByRole("dialog"), DRM_VIOLATION)
          ).toBeInTheDocument();
        });
      }
    );
  });
});

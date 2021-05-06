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
import { render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import * as React from "react";
import GallerySearchResult from "../../../../tsrc/search/components/GallerySearchResult";
import { languageStrings } from "../../../../tsrc/util/langstrings";
import { buildItems } from "./GallerySearchResultHelpers";

const {
  searchpage: {
    gallerySearchResult: { ariaLabel, viewItem },
  },
  common: {
    action: { openInNewWindow },
  },
} = languageStrings;

/*
 * Need to mock the react-router context and function 'push' so that we can test for navigating
 * to item summary pages.
 */
const mockUseHistoryPush = jest.fn();
jest.mock("react-router", () => ({
  useHistory: () => ({
    push: mockUseHistoryPush,
  }),
}));

describe("<GallerySearchResult />", () => {
  const renderGallery = () =>
    render(<GallerySearchResult items={buildItems(5)} />);

  it("displays the lightbox when the image is clicked on", () => {
    const { getAllByLabelText, queryAllByLabelText } = renderGallery();
    userEvent.click(getAllByLabelText(ariaLabel)[0]);

    // Then they see the lightbox
    expect(queryAllByLabelText(openInNewWindow)[0]).toBeInTheDocument();
  });

  it("navigates to the images item when the information icon is clicked on", () => {
    const { getAllByLabelText } = renderGallery();
    userEvent.click(getAllByLabelText(viewItem)[0]);

    expect(mockUseHistoryPush).toHaveBeenCalled();
    expect(mockUseHistoryPush.mock.calls[0][0].match("/items/")).toBeTruthy();
  });
});

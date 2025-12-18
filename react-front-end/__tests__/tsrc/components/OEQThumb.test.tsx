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
import "@testing-library/jest-dom";
import { queryByLabelText, render } from "@testing-library/react";
import * as React from "react";
import {
  brokenFileDetails,
  equellaItemDetails,
  fileDetails,
  htmlDetails,
  linkDetails,
  resourceHtmlDetails,
  resourceLinkDetails,
} from "../../../__mocks__/OEQThumb.mock";
import OEQThumb from "../../../tsrc/components/OEQThumb";
import { languageStrings } from "../../../tsrc/util/langstrings";

describe("<OEQThumb/>", () => {
  const thumbLabels = languageStrings.searchpage.thumbnails;

  it.each<[string, OEQ.Search.ThumbnailDetails | undefined, string]>([
    [
      "shows the placeholder icon when no details are provided",
      undefined,
      thumbLabels.placeholder,
    ],
    ["shows thumbnail image when available", fileDetails, thumbLabels.provided],
    [
      "shows thumbnail based on MIME type when no thumbnail link is provided for file attachments - e.g. broken attachments",
      brokenFileDetails,
      thumbLabels.image,
    ],
    [
      "shows link icon thumbnail for a link attachment",
      linkDetails,
      thumbLabels.link,
    ],
    [
      "shows link icon thumbnail for a resource link attachment",
      resourceLinkDetails,
      thumbLabels.link,
    ],
    [
      "shows equella item thumbnail for a resource attachment pointing at an item summary",
      equellaItemDetails,
      thumbLabels.item,
    ],
    [
      "shows html thumbnail for a web page attachment",
      htmlDetails,
      thumbLabels.html,
    ],
    [
      "shows html thumbnail for a resource web page attachment",
      resourceHtmlDetails,
      thumbLabels.html,
    ],
  ])(
    "%s",
    (
      _: string,
      details: OEQ.Search.ThumbnailDetails | undefined,
      query: string,
    ) => {
      const { container } = render(<OEQThumb details={details} />);
      expect(queryByLabelText(container, query)).toBeInTheDocument();
    },
  );
});

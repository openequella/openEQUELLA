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
import { render } from "@testing-library/react";
import * as React from "react";
import {
  brokenFileAttachment,
  equellaItemAttachment,
  fileAttachment,
  htmlAttachment,
  linkAttachment,
  resourceHtmlAttachment,
  resourceLinkAttachment,
} from "../../../__mocks__/OEQThumb.mock";
import OEQThumb from "../../../tsrc/components/OEQThumb";
import * as OEQ from "@openequella/rest-api-client";

describe("<OEQThumb/>", () => {
  const OEQThumbComponent = (
    attachment: OEQ.Search.Attachment,
    showPlaceHolder: boolean
  ) =>
    render(
      <OEQThumb attachment={attachment} showPlaceholder={showPlaceHolder} />
    );
  const queryForIconId = (container: Element, query: string) => {
    return container.querySelector(`[id="${query}"]`);
  };

  it.each<[string, OEQ.Search.Attachment, boolean, string]>([
    [
      "shows the placeholder icon when showPlaceholder is true",
      fileAttachment,
      true,
      "placeholderIcon",
    ],
    [
      "shows thumbnail image when showPlaceholder is false",
      fileAttachment,
      false,
      "providedIcon",
    ],
    [
      "shows default file thumbnail when brokenAttachment is true",
      brokenFileAttachment,
      false,
      "defaultFileIcon",
    ],
    [
      "shows link icon thumbnail for a link attachment",
      linkAttachment,
      false,
      "linkIcon",
    ],
    [
      "shows link icon thumbnail for a resource link attachment",
      resourceLinkAttachment,
      false,
      "linkIcon",
    ],
    [
      "shows equella item thumbnail for a resource attachment pointing at an item summary",
      equellaItemAttachment,
      false,
      "equellaItemIcon",
    ],
    [
      "shows html thumbnail for a web page attachment",
      htmlAttachment,
      false,
      "htmlIcon",
    ],
    [
      "shows html thumbnail for a resource web page attachment",
      resourceHtmlAttachment,
      false,
      "htmlIcon",
    ],
  ])(
    "%s",
    (
      _: string,
      attachment: OEQ.Search.Attachment,
      showPlaceHolder: boolean,
      query: string
    ) => {
      const { container } = OEQThumbComponent(attachment, showPlaceHolder);
      expect(queryForIconId(container, query)).not.toBeNull();
    }
  );
});

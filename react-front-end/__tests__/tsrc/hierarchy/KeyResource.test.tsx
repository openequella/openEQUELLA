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
import {
  findByText,
  render,
  RenderResult,
  screen,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Route, Router } from "react-router-dom";
import { DRM_VIOLATION, drmTerms } from "../../../__mocks__/Drm.mock";
import {
  keyResourceWithAttachment,
  keyResourceWithoutName,
  normalKeyResource,
} from "../../../__mocks__/Hierarchy.mock";
import {
  itemWithAttachment,
  normalItemWithoutName,
} from "../../../__mocks__/SearchResult.mock";
import {
  DRM_ITEM_NAME,
  drmAllowSummaryObj,
  drmAttachObj,
} from "../../../__mocks__/searchresult_mock_data";
import KeyResource from "../../../tsrc/hierarchy/components/KeyResource";
import * as DrmModule from "../../../tsrc/modules/DrmModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import "@testing-library/jest-dom";

const { removeKeyResource: removeKeyResourceText } = languageStrings.hierarchy;

const renderKeyResource = (
  keyResource: OEQ.BrowseHierarchy.KeyResource,
): RenderResult => {
  const path = "/page/hierarchy/uuid";
  const history = createMemoryHistory();
  history.push(path);

  return render(
    <Router history={history}>
      <Route path={path}>
        <KeyResource keyResource={keyResource} onPinIconClick={() => {}} />
      </Route>
    </Router>,
  );
};

describe("<KeyResource/>", () => {
  jest
    .spyOn(DrmModule, "listDrmViolations")
    .mockResolvedValue({ violation: DRM_VIOLATION });
  jest.spyOn(DrmModule, "listDrmTerms").mockResolvedValue(drmTerms);

  it("displays title if item has name", async () => {
    const { findByText } = renderKeyResource(normalKeyResource);

    expect(await findByText(normalKeyResource.item.name!)).toBeInTheDocument();
  });

  it("displays uuid if item doesn't have name", async () => {
    const { findByText } = renderKeyResource(keyResourceWithoutName);

    expect(await findByText(normalItemWithoutName.uuid)).toBeInTheDocument();
  });

  it("displays pin icon", async () => {
    const { findByLabelText } = renderKeyResource(normalKeyResource);

    expect(await findByLabelText(removeKeyResourceText)).toBeInTheDocument();
  });

  it("displays attachment count", async () => {
    const { findByText } = renderKeyResource(keyResourceWithAttachment);

    expect(
      await findByText(itemWithAttachment.attachments!.length),
    ).toBeInTheDocument();
  });

  it("shows the DRM dialog", async () => {
    const { getByText } = renderKeyResource({
      item: {
        ...drmAttachObj,
        createdDate: "2020-05-26T13:24:00.889+10:00",
        modifiedDate: "2020-05-26T12:45:06.857+10:00",
      } as OEQ.Search.SearchResultItemRaw,
      isLatest: false,
    });
    await userEvent.click(getByText(DRM_ITEM_NAME));

    expect(
      await findByText(screen.getByRole("dialog"), drmTerms.title),
    ).toBeInTheDocument();
  });

  it("supports viewing a DRM Item's summary page without accepting the terms", async () => {
    const { getByText } = renderKeyResource({
      item: {
        ...drmAllowSummaryObj,
        createdDate: "2020-05-26T13:24:00.889+10:00",
        modifiedDate: "2020-05-26T12:45:06.857+10:00",
      } as OEQ.Search.SearchResultItemRaw,
      isLatest: false,
    });
    await userEvent.click(getByText(DRM_ITEM_NAME));

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });
});

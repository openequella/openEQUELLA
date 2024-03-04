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
import { render, RenderResult } from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Route, Router } from "react-router-dom";
import { normalItem } from "../../../__mocks__/Hierarchy.mock";
import {
  itemWithAttachment,
  normalItemWithoutName,
} from "../../../__mocks__/SearchResult.mock";
import KeyResource from "../../../tsrc/hierarchy/components/KeyResource";
import { languageStrings } from "../../../tsrc/util/langstrings";
import "@testing-library/jest-dom";

const { removeKeyResource: removeKeyResourceText } = languageStrings.hierarchy;

const renderKeyResource = (item: OEQ.Search.SearchResultItem): RenderResult => {
  const path = "/page/hierarchy/uuid";
  const history = createMemoryHistory();
  history.push(path);

  return render(
    <Router history={history}>
      <Route path={path}>
        <KeyResource item={item} onPinIconClick={() => {}} />
      </Route>
    </Router>,
  );
};

describe("<KeyResource/>", () => {
  it("displays title if item has name", async () => {
    const { findByText } = renderKeyResource(normalItem);

    expect(await findByText(normalItem.name!)).toBeInTheDocument();
  });

  it("displays uuid if item doesn't have name", async () => {
    const { findByText } = renderKeyResource(normalItemWithoutName);

    expect(await findByText(normalItemWithoutName.uuid)).toBeInTheDocument();
  });

  it("displays pin icon", async () => {
    const { findByLabelText } = renderKeyResource(normalItem);

    expect(await findByLabelText(removeKeyResourceText)).toBeInTheDocument();
  });

  it("displays attachment count", async () => {
    const { findByText } = renderKeyResource(itemWithAttachment);

    expect(
      await findByText(itemWithAttachment.attachments!.length),
    ).toBeInTheDocument();
  });
});

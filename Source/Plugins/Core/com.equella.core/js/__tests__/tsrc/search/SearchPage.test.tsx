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
import { getSearchResult } from "../../../__mocks__/getSearchResult";
import { createMount } from "@material-ui/core/test-utils";
import * as React from "react";
import SearchPage from "../../../tsrc/search/SearchPage";
import * as OEQ from "@openequella/rest-api-client";
import { ReactWrapper } from "enzyme";
import { act } from "react-dom/test-utils";

jest.useFakeTimers();
jest.mock("@openequella/rest-api-client");
(OEQ.Search.search as jest.Mock<
  Promise<OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>>
>).mockResolvedValue(getSearchResult);

describe("<SearchPage/>", () => {
  let mount: ReturnType<typeof createMount>;
  let component: ReactWrapper<any, Readonly<{}>, React.Component<{}, {}, any>>;

  beforeEach(() => {
    mount = createMount();
    component = mount(<SearchPage updateTemplate={jest.fn()} />);
  });

  afterEach(() => {
    mount.cleanUp();
  });

  it("should display 'No results found.' when there are no search results", () => {
    expect(component.html()).not.toContain(
      "266bb0ff-a730-4658-aec0-c68bbefc227c"
    );
    expect(component.html()).toContain("No results found.");
  });

  it("should contain the test data after a search bar text change and render", () => {
    act(() => {
      const input = component.find("input.MuiInputBase-input");
      input.simulate("change", { target: { value: "new title" } });
    });
    //use a timed callback to wait for the debounce before asserting results have populated the page
    setTimeout(() => {
      expect(component.html()).not.toContain("No results found.");
      expect(component.html()).toContain(
        "266bb0ff-a730-4658-aec0-c68bbefc227c"
      );
    }, 1000);
  });
});

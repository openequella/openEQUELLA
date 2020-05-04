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
import * as React from "react";
import { createMount } from "@material-ui/core/test-utils";
import ErrorPage from "../../../tsrc/mainui/ErrorPage";

jest.mock("@material-ui/core/styles", () => ({
  makeStyles: () => () => ({ errorPage: "mock-classname" }),
}));

describe("<ErrorPage />", () => {
  let mount: ReturnType<typeof createMount>;

  beforeEach(() => {
    mount = createMount();
  });

  afterEach(() => {
    mount.cleanUp();
  });

  it("should render with no code or description", () => {
    const wrapper = mount(
      <ErrorPage error={{ id: "mock-error", error: "example" }} />
    );

    expect(wrapper.find(".mock-classname")).toHaveLength(1);
    expect(wrapper.find("h3")).toHaveLength(1);
    expect(wrapper.find("h3").text()).toContain("example");
  });

  it("should render with code and description", () => {
    const wrapper = mount(
      <ErrorPage
        error={{
          id: "mock-error",
          error: "example",
          code: 404,
          error_description: "mock description",
        }}
      />
    );

    expect(wrapper.find(".mock-classname")).toHaveLength(1);
    expect(wrapper.find("h3")).toHaveLength(1);
    expect(wrapper.find("h3").text()).toContain("example");
    expect(wrapper.find("h3").text()).toContain("404");
    expect(wrapper.find("h5")).toHaveLength(1);
    expect(wrapper.find("h5").text()).toContain("mock description");
  });
});

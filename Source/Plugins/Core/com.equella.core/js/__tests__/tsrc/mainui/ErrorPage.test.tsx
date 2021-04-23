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
import ErrorPage from "../../../tsrc/mainui/ErrorPage";
import { render } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";

jest.mock("@material-ui/core/styles", () => ({
  makeStyles: () => () => ({ errorPage: "mock-classname" }),
}));

describe("<ErrorPage />", () => {
  it("should render with no code or description", () => {
    const { container, queryByText } = render(
      <ErrorPage error={{ id: "mock-error", error: "example" }} />
    );

    expect(container.querySelectorAll(".mock-classname")).toHaveLength(1);
    expect(container.querySelectorAll("h3")).toHaveLength(1);
    expect(queryByText("example", { selector: "h3" })).toBeInTheDocument();
  });

  it("should render with code and description", () => {
    const { container, queryByText } = render(
      <ErrorPage
        error={{
          id: "mock-error",
          error: "example",
          code: 404,
          error_description: "mock description",
        }}
      />
    );

    expect(container.querySelectorAll(".mock-classname")).toHaveLength(1);
    expect(container.querySelectorAll("h3")).toHaveLength(1);
    expect(
      queryByText("404 : example", { selector: "h3" })
    ).toBeInTheDocument();
    expect(container.querySelectorAll("h5")).toHaveLength(1);
    expect(
      queryByText("mock description", { selector: "h5" })
    ).toBeInTheDocument();
  });
});

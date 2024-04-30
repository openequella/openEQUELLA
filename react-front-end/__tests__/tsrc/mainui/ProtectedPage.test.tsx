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
import { render, waitFor } from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Router } from "react-router-dom";
import ProtectedPage, {
  ProtectedPageProps,
} from "../../../tsrc/mainui/ProtectedPage";

describe("<ProtectedPage/>", () => {
  const PAGE = "Page";
  const hasPermission = jest.fn().mockResolvedValue(true);
  const defaultPageProps: ProtectedPageProps = {
    Page: jest.fn().mockReturnValue(<div>{PAGE}</div>),
    hasPermission,
    newUIProps: {
      updateTemplate: jest.fn(),
      redirect: jest.fn(),
      setPreventNavigation: jest.fn(),
      isReloadNeeded: false,
    },
    path: "/some/path",
  };

  const renderProtectedPage = (props: ProtectedPageProps = defaultPageProps) =>
    render(
      <Router history={createMemoryHistory()}>
        <ProtectedPage {...props} />
      </Router>,
    );

  it("users the provided function to check permissions", async () => {
    renderProtectedPage();
    await waitFor(() => expect(hasPermission).toHaveBeenCalledTimes(1));
  });

  it("renders the page if permitted", async () => {
    const { findByText } = renderProtectedPage();

    expect(await findByText(PAGE)).toBeInTheDocument();
  });

  it("redirects if not permitted", async () => {
    const notPermitted = jest.fn().mockResolvedValue(false);
    const { findByText } = renderProtectedPage({
      ...defaultPageProps,
      hasPermission: notPermitted,
    });

    await expect(findByText(PAGE)).rejects.toBeTruthy();
  });
});

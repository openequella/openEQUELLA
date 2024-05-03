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
import { Route, Router, Switch } from "react-router-dom";
import ProtectedPage, {
  ProtectedPageProps,
} from "../../../tsrc/mainui/ProtectedPage";

describe("<ProtectedPage/>", () => {
  const page = "Page";
  const path = "/some/path";
  const login = "login";
  const aclCheck = jest.fn().mockResolvedValue(true);
  const defaultPageProps: ProtectedPageProps = {
    Page: jest.fn().mockReturnValue(<div>{page}</div>),
    newUIProps: {
      updateTemplate: jest.fn(),
      redirect: jest.fn(),
      setPreventNavigation: jest.fn(),
      isReloadNeeded: false,
    },
    path: path,
    isAuthenticated: true,
  };

  const renderProtectedPage = (
    props: ProtectedPageProps = defaultPageProps,
  ) => {
    const history = createMemoryHistory();
    history.push(path);

    return render(
      <Router history={history}>
        <Switch>
          <Route path="/logon.do" render={() => <div>{login}</div>} />
          <Route path={path} render={() => <ProtectedPage {...props} />} />
        </Switch>
      </Router>,
    );
  };

  describe("Authentication only", () => {
    it("renders the page if authenticated", async () => {
      const { findByText } = renderProtectedPage();

      expect(await findByText(page)).toBeInTheDocument();
    });

    it("redirects to the Login page if not authenticated", async () => {
      const { getByText } = renderProtectedPage({
        ...defaultPageProps,
        isAuthenticated: false,
      });

      await waitFor(() => {
        expect(getByText(login)).toBeInTheDocument();
      });
    });
  });

  describe("ACL check", () => {
    const aclGranted = {
      ...defaultPageProps,
      authenticationOnly: false,
      aclCheck,
    };

    it("users the provided function to check permissions", async () => {
      renderProtectedPage(aclGranted);
      await waitFor(() => expect(aclCheck).toHaveBeenCalledTimes(1));
    });

    it("renders the page if the ACL is granted", async () => {
      const { getByText } = renderProtectedPage(aclGranted);
      await waitFor(() => expect(getByText(page)).toBeInTheDocument());
    });

    it("renders Error page if the ACL isn't granted but the user is authenticated", async () => {
      const { getByText, container } = renderProtectedPage({
        ...aclGranted,
        aclCheck: aclCheck.mockResolvedValue(false),
      });

      await waitFor(() => {
        expect(container.querySelector("#errorPage")).toBeInTheDocument();
        expect(
          getByText(`No permission to access ${path}`),
        ).toBeInTheDocument();
      });
    });

    it("redirects to login page if the ACL isn't granted and the user isn't authenticated", async () => {
      const { getByText } = renderProtectedPage({
        ...aclGranted,
        aclCheck: aclCheck.mockResolvedValue(false),
        isAuthenticated: false,
      });

      await waitFor(() => {
        expect(getByText(login)).toBeInTheDocument();
      });
    });
  });
});

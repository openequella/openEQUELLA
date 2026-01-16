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
import * as TE from "fp-ts/TaskEither";
import type { RequiredPermissionCheck } from "../../../tsrc/modules/SecurityModule";

describe("<ProtectedPage/>", () => {
  const page = "Page";
  const path = "/some/path";
  const login = "login";

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

  describe("Permission check", () => {
    const permissionCheck = jest.fn().mockResolvedValue(true);
    const task: RequiredPermissionCheck = TE.tryCatch(
      permissionCheck,
      () => "Failed",
    );

    const permitted = {
      ...defaultPageProps,
      authenticationOnly: false,
      permissionChecks: [task],
    };

    it("users the provided function to check permissions", async () => {
      renderProtectedPage(permitted);
      await waitFor(() => expect(permissionCheck).toHaveBeenCalledTimes(1));
    });

    it("renders the page if permitted", async () => {
      const { getByText } = renderProtectedPage(permitted);
      await waitFor(() => expect(getByText(page)).toBeInTheDocument());
    });

    it("renders Error page if the user is authenticated but not permitted", async () => {
      const { getByText, container } = renderProtectedPage({
        ...permitted,
        permissionChecks: [TE.left("TEST_ACL")],
      });

      await waitFor(() => {
        expect(container.querySelector("#errorPage")).toBeInTheDocument();
        expect(
          getByText(
            `No permission to access ${path} - missing ACL(s): TEST_ACL`,
          ),
        ).toBeInTheDocument();
      });
    });

    it("redirects to login page if the user isn't authenticated and permitted", async () => {
      const { getByText } = renderProtectedPage({
        ...permitted,
        permissionChecks: [TE.left("Failed")],
        isAuthenticated: false,
      });

      await waitFor(() => {
        expect(getByText(login)).toBeInTheDocument();
      });
    });
  });
});

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
import { render, RenderResult, waitFor } from "@testing-library/react";
import * as E from "fp-ts/Either";
import { createMemoryHistory } from "history";
import * as React from "react";
import { getCurrentUserMock } from "../../../__mocks__/UserModule.mock";
import DashboardPage from "../../../tsrc/dashboard/DashboardPage";
import "@testing-library/jest-dom";
import * as OEQ from "@openequella/rest-api-client";
import { AppContext } from "../../../tsrc/mainui/App";
import * as DashboardModule from "../../../tsrc/modules/DashboardModule";
import * as SecurityModule from "../../../tsrc/modules/SecurityModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { Router } from "react-router-dom";

const { welcomeTitle } = languageStrings.dashboard;
const history = createMemoryHistory();
/**
 * Helper to render DashboardPage and wait for it to load (i.e. the skeleton to disappear).
 *
 * @param currentUser The current user details to provide via context.
 */
export const renderDashboardPage = async (
  currentUser: OEQ.LegacyContent.CurrentUserDetails = getCurrentUserMock,
): Promise<RenderResult> => {
  const page = render(
    <Router history={history}>
      <AppContext.Provider
        value={{
          appErrorHandler: jest.fn(),
          refreshUser: jest.fn(),
          currentUser,
        }}
      >
        <DashboardPage updateTemplate={jest.fn()} />
      </AppContext.Provider>
    </Router>,
  );

  const { container, queryByText } = page;

  await waitFor(() => {
    const welcome = queryByText(welcomeTitle);
    const portlets = container.querySelector("#dashboard-portlet-container");
    expect(welcome || portlets).toBeInTheDocument();
  });

  return page;
};

/**
 * The spies for the mocked Dashboard APIs.
 */
export interface MockDashboardApis {
  /** Spy for the `getDashboardDetails` API. */
  mockGetDashboardDetails: jest.SpyInstance;
  /** Spy for the `updatePortletPreference` API. */
  mockUpdatePortletPreference: jest.SpyInstance;
  /** Spy for the `hasCreatePortletACL` API. */
  mockGetCreatePortletAcl: jest.SpyInstance;
}

/**
 * Mocks the APIs used by the Dashboard page and returns spies for them.
 *
 * @returns An object containing spies for the mocked APIs.
 */
export const mockDashboardPageApis = (): MockDashboardApis => {
  return {
    mockGetDashboardDetails: jest.spyOn(DashboardModule, "getDashboardDetails"),
    mockUpdatePortletPreference: jest
      .spyOn(DashboardModule, "updatePortletPreference")
      .mockResolvedValue(undefined),
    mockGetCreatePortletAcl: jest
      .spyOn(SecurityModule, "hasCreatePortletACL")
      .mockResolvedValue(E.right(true)),
  };
};

/**
 * Queries for a portlet's content element by its UUID.
 *
 * @param container The root container to search within.
 * @param uuid The UUID of the portlet.
 * @returns The portlet's content element, or `null` if not found.
 */
export const queryPortletContent = (
  container: Element,
  uuid: string,
): HTMLDivElement | null =>
  container.querySelector<HTMLDivElement>(`#portlet-content-${uuid}`);

/**
 * Gets a portlet's content element by its UUID, throwing an error if not found.
 *
 * @param container The root container to search within.
 * @param uuid The UUID of the portlet.
 * @returns The portlet's content element.
 * @throws An error if the portlet content cannot be found.
 */
export const getPortletContent = (
  container: Element,
  uuid: string,
): HTMLDivElement => {
  const portletContent = queryPortletContent(container, uuid);
  if (!portletContent) {
    throw new Error(`Unable to Portlet content with uuid: ${uuid}`);
  }

  return portletContent;
};

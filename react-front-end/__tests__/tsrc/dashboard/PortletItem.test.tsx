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
import {
  basicPortlet,
  minimisedPortlet,
  noClosePortlet,
  noDeletePortlet,
  noEditPortlet,
  noMinimisePortlet,
  privatePortlet,
  privateSearchPortlet,
} from "../../../__mocks__/Dashboard.mock";
import { PortletItemSkeletonTestId } from "../../../tsrc/dashboard/components/PortletItemSkeleton";
import * as DashboardModule from "../../../tsrc/modules/DashboardModule";
import { languageStrings } from "../../../tsrc/util/langstrings";
import { clickButton } from "../MuiTestHelpers";
import {
  defaultProps,
  portletContent,
  renderPortletItem,
} from "./PortletItemTestHelper";
import * as OEQ from "@openequella/rest-api-client";

const {
  edit: editText,
  delete: deleteText,
  close: closeText,
  minimise: minimiseText,
  maximise: maximiseText,
} = languageStrings.common.action;

const mockEditPortlet = jest
  .spyOn(DashboardModule, "editPortlet")
  .mockResolvedValue("");

describe("<PortletItem/>", () => {
  it("displays a skeleton in portlet content area when content is loading", () => {
    const { getByTestId, getByText, getByLabelText } = renderPortletItem({
      ...defaultProps,
      isLoading: true,
    });

    // Always show portlet title and actions when content is loading.
    expect(getByText(basicPortlet.commonDetails.name)).toBeInTheDocument();
    expect(getByLabelText(minimiseText)).toBeInTheDocument();
    // Show skeleton when content is loading.
    expect(getByTestId(PortletItemSkeletonTestId)).toBeInTheDocument();
  });

  it("does not show skeleton for a loading minimised portlet", () => {
    const { getByLabelText, queryByText, queryByTestId } = renderPortletItem({
      ...defaultProps,
      portlet: minimisedPortlet,
      isLoading: true,
    });

    expect(getByLabelText(maximiseText)).toBeInTheDocument();
    expect(queryByText(portletContent)).not.toBeInTheDocument();
    expect(queryByTestId(PortletItemSkeletonTestId)).not.toBeInTheDocument();
  });

  it("displays portlet name", async () => {
    const { getByText } = renderPortletItem();

    expect(getByText(basicPortlet.commonDetails.name)).toBeInTheDocument();
  });

  it("displays icons for actions", async () => {
    const actions = [editText, closeText, minimiseText];

    const { getByLabelText, queryByText } = renderPortletItem();
    actions.forEach((actionLabel) =>
      expect(getByLabelText(actionLabel)).toBeInTheDocument(),
    );
    // Delete icon should not be displayed for institution-wide portlet.
    expect(queryByText(deleteText)).not.toBeInTheDocument();
    expect.assertions(actions.length + 1);
  });

  it("display delete icon for private portlet", () => {
    const { getByLabelText, queryByText } = renderPortletItem({
      ...defaultProps,
      portlet: privatePortlet,
    });
    expect(getByLabelText(deleteText)).toBeInTheDocument();
    // Close icon should not be displayed for private portlet.
    expect(queryByText(closeText)).not.toBeInTheDocument();
  });

  it("displays portlet content", async () => {
    const { getByText } = renderPortletItem();

    expect(getByText(portletContent)).toBeInTheDocument();
  });

  it("hides portlet content and displays maximize icon if it's minimised", async () => {
    const { queryByText, getByLabelText } = renderPortletItem({
      ...defaultProps,
      portlet: minimisedPortlet,
    });

    expect(getByLabelText(maximiseText)).toBeInTheDocument();
    expect(queryByText(portletContent)).not.toBeInTheDocument();
  });

  it.each([
    ["edit", editText, noEditPortlet],
    ["delete", deleteText, noDeletePortlet],
    ["close", closeText, noClosePortlet],
    ["minimise", minimiseText, noMinimisePortlet],
  ])(
    "not displays icon for %s action if it's disabled",
    (_: string, actionLabel: string, portlet: OEQ.Dashboard.BasicPortlet) => {
      const { queryByLabelText } = renderPortletItem({
        ...defaultProps,
        portlet: portlet,
      });

      expect(queryByLabelText(actionLabel)).not.toBeInTheDocument();
    },
  );

  it("supports editing the private portlets", async () => {
    const { container } = renderPortletItem({
      ...defaultProps,
      portlet: privateSearchPortlet,
    });

    await clickButton(container, editText);

    expect(mockEditPortlet).toHaveBeenCalledWith(
      privateSearchPortlet.commonDetails.uuid,
    );
  });
});

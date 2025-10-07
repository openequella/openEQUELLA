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
} from "../../../__mocks__/Dashboard.mock";
import { languageStrings } from "../../../tsrc/util/langstrings";
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

describe("<PortletItem/>", () => {
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
});

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
import { generateWarnMsgForMissingIds } from "../../../../../../tsrc/components/securityentitydialog/SecurityEntityHelper";

import { languageStrings } from "../../../../../../tsrc/util/langstrings";
import {
  commonUnknownUserHandlingControlProps,
  renderUnknownUserHandlingControl,
} from "./UnknownUserHandlingControlTestHelper";

const { groups: groupsLabel } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .accessControl;
describe("UnknownUserHandlingControlProps", () => {
  it("Should not be able to see the group selector if user didn't choose option `CREATE`", () => {
    const { queryByText } = renderUnknownUserHandlingControl();

    expect(queryByText(groupsLabel)).not.toBeInTheDocument();
  });

  it("Should be able to see a group selector if user choose option `CREATE`", () => {
    const { getByText } = renderUnknownUserHandlingControl({
      ...commonUnknownUserHandlingControlProps,
      selection: "CREATE",
    });

    expect(getByText(groupsLabel)).toBeInTheDocument();
  });

  it("Shows warning messages for groups has been deleted but still in the initial value", async () => {
    const groupIds = ["deletedGroup1", "deletedGroup2"];

    const { findByText } = renderUnknownUserHandlingControl({
      ...commonUnknownUserHandlingControlProps,
      groups: new Set(groupIds),
      selection: "CREATE",
    });

    const expectedWarnMsg = generateWarnMsgForMissingIds(
      new Set(groupIds),
      "group",
    );
    const message = await findByText(expectedWarnMsg);

    expect(message).toBeInTheDocument();
  });
});

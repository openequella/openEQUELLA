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
import { generateWarnMsgForMissingIds } from "../../../tsrc/components/securityentitydialog/SecurityEntityHelper";

import {
  commonSelectRoleControlProps,
  renderSelectRoleControl,
} from "./SelectRoleControlTestHelper";

describe("SelectRoleControl", () => {
  it("Shows warning messages for roles has been deleted but still in the initial value", async () => {
    const roleIds = ["deletedRole1", "deletedRole2"];

    const { findByText } = renderSelectRoleControl({
      ...commonSelectRoleControlProps,
      value: new Set(roleIds),
    });

    const expectedWarnMsg = generateWarnMsgForMissingIds(
      new Set(roleIds),
      "role",
    );
    const message = await findByText(expectedWarnMsg);

    expect(message).toBeInTheDocument();
  });
});

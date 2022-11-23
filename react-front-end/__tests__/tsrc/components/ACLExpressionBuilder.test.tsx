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
import "@testing-library/jest-dom/extend-expect";
import { languageStrings } from "../../../tsrc/util/langstrings";
import {
  renderACLExpressionBuilder,
  selectGroupsRadio,
  selectRolesRadio,
} from "./ACLExpressionBuilderTestHelper";

const { queryFieldLabel: userSearchQueryFieldLabel } =
  languageStrings.userSearchComponent;
const { queryFieldLabel: groupSearchQueryFieldLabel } =
  languageStrings.groupSearchComponent;
const { queryFieldLabel: roleSearchQueryFieldLabel } =
  languageStrings.roleSearchComponent;

describe("<ACLExpressionBuilder/>", () => {
  it("displays home panel and user search on initial render", async () => {
    const renderResult = await renderACLExpressionBuilder();

    expect(
      renderResult.queryByText(userSearchQueryFieldLabel)
    ).toBeInTheDocument();
  });

  it("displays home panel's group search when users select groups radio", async () => {
    const renderResult = await renderACLExpressionBuilder();

    selectGroupsRadio(renderResult);

    expect(
      renderResult.queryByText(groupSearchQueryFieldLabel)
    ).toBeInTheDocument();
  });

  it("displays home panel's role search when users select roles radio", async () => {
    const renderResult = await renderACLExpressionBuilder();

    selectRolesRadio(renderResult);

    expect(
      renderResult.queryByText(roleSearchQueryFieldLabel)
    ).toBeInTheDocument();
  });
});

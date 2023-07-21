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
import { render, RenderResult } from "@testing-library/react";
import * as React from "react";
import { getTokens, tokens } from "../../../../__mocks__/UserModule.mock";
import ACLSSOMenu, {
  ACLSSOMenuProps,
} from "../../../../tsrc/components/aclexpressionbuilder/ACLSSOMenu";
import { languageStrings } from "../../../../tsrc/util/langstrings";

const { ssoTokensNotFound, ssoTokensFailed } =
  languageStrings.aclExpressionBuilder.errors;

// Helper to render ACLSSOMenu
const renderACLSSOMenu = (props: ACLSSOMenuProps): RenderResult =>
  render(<ACLSSOMenu {...props} />);

describe("<ACLSSOMenu />", () => {
  const defaultSelectedToken = tokens[0];

  it("displays the SSO select", async () => {
    const { findByText } = renderACLSSOMenu({
      onChange: jest.fn(),
      getSSOTokens: getTokens,
    });

    const ssoToken = await findByText(defaultSelectedToken);

    expect(ssoToken).toBeInTheDocument();
  });

  it("displays the not found error message if no SSO tokens can be found", async () => {
    const { queryByText, findByText } = renderACLSSOMenu({
      onChange: jest.fn(),
      getSSOTokens: () => Promise.resolve([]),
    });

    const errorMessage = await findByText(ssoTokensNotFound);
    const select = queryByText(defaultSelectedToken);

    expect(select).not.toBeInTheDocument();
    expect(errorMessage).toBeInTheDocument();
  });

  it("displays the failed error message if failed to get SSO tokens", async () => {
    const failedMessage = "test failed message";
    const { queryByText, findByText } = renderACLSSOMenu({
      onChange: jest.fn(),
      getSSOTokens: () => Promise.reject(failedMessage),
    });

    const errorMessage = await findByText(
      `${ssoTokensFailed}: ${failedMessage}`
    );
    const select = queryByText(defaultSelectedToken);

    expect(select).not.toBeInTheDocument();
    expect(errorMessage).toBeInTheDocument();
  });
});

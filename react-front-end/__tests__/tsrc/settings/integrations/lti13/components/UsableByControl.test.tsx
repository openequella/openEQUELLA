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
import { render, RenderResult } from "@testing-library/react";
import * as React from "react";
import { defaultACLEntityResolvers } from "../../../../../../__mocks__/ACLExpressionBuilder.mock";
import { aclEveryone } from "../../../../../../__mocks__/ACLExpressionModule.mock";
import UsableByControl, {
  UsableByControlProps,
} from "../../../../../../tsrc/settings/Integrations/lti13/components/UsableByControl";
import { aclEveryoneInfix } from "../../../../../../__mocks__/ACLExpressionModule.mock";

const renderUsableByControl = (props: UsableByControlProps): RenderResult =>
  render(<UsableByControl {...props} />);

describe("<UsableControl />", () => {
  it("displays ACLExpression human-readable string on initial render", async () => {
    const { findByText } = renderUsableByControl({
      aclEntityResolversProvider: defaultACLEntityResolvers,
      value: aclEveryone,
      onChange: jest.fn(),
    });

    const aclExpressionString = await findByText(aclEveryoneInfix);
    expect(aclExpressionString).toBeInTheDocument();
  });
});

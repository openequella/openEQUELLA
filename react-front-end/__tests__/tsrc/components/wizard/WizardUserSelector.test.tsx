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
import * as React from "react";
import * as UserModuleMock from "../../../../__mocks__/UserModule.mock";
import { WizardUserSelector } from "../../../../tsrc/components/wizard/WizardUserSelector";

describe("<WizardUserSelector/>", () => {
  it("displays a specified set of users", async () => {
    const testUser = UserModuleMock.users[1];
    const { queryByText } = render(
      <WizardUserSelector
        groupFilter={new Set()}
        multiple
        onChange={jest.fn()}
        users={new Set([testUser.id])}
        mandatory={false}
        resolveUsersProvider={UserModuleMock.resolveUsers}
      />,
    );

    await waitFor(() =>
      expect(queryByText(testUser.username)).toBeInTheDocument(),
    );
  });
});

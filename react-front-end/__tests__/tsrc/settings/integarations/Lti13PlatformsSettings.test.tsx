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
import { createMemoryHistory } from "history";
import { Router } from "react-router-dom";
import {
  getPlatforms,
  platforms,
} from "../../../../__mocks__/Lti13PlatformsModule.mock";

import Lti13PlatformsSettings, {
  Lti13PlatformsSettingsProps,
} from "../../../../tsrc/settings/Integrations/Lti13PlatformsSettings";
import * as React from "react";

// Helper to render ACLExpressionBuilder and wait for component under test
const renderLti13PlatformsSettings = async (
  props: Lti13PlatformsSettingsProps = {
    updateTemplate: () => {},
    getPlatformsProvider: getPlatforms,
  }
): Promise<RenderResult> => {
  const history = createMemoryHistory();
  const result = render(
    <Router history={history}>
      <Lti13PlatformsSettings {...props} />
    </Router>
  );
  // wait for platform list rendered
  await waitFor(() => result.getByText(platforms[0].name));
  return result;
};

describe("Lti13PlatformsSettings", () => {
  it("Should be able to show a list of platforms", async () => {
    const { getAllByRole } = await renderLti13PlatformsSettings();

    expect(getAllByRole("listitem")).toHaveLength(platforms.length);
  });
});

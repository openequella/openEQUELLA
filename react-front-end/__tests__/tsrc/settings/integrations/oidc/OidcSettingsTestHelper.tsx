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
import { Router } from "react-router-dom";
import { listRoles } from "../../../../../__mocks__/RoleModule.mock";
import OidcSettings from "../../../../../tsrc/settings/Integrations/oidc/OidcSettings";
import { render, RenderResult, waitFor } from "@testing-library/react";
import { createMemoryHistory } from "history";

import * as React from "react";
import { languageStrings } from "../../../../../tsrc/util/langstrings";

const {
  generalDetails: { title: generalDetailsTitle },
} = languageStrings.settings.integration.oidc;

/**
 * Helper to render OidcSettings page.
 */
export const renderOidcSettings = async (): Promise<RenderResult> => {
  const props = {
    updateTemplate: () => {},
    searchRoleProvider: listRoles,
  };

  const history = createMemoryHistory();
  const result = render(
    <Router history={history}>
      <OidcSettings {...props} />
    </Router>,
  );
  // wait for OIDC configuration page rendered
  await waitFor(() => result.getByText(generalDetailsTitle));
  return result;
};

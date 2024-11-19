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
import { render, RenderResult } from "@testing-library/react";
import { createMemoryHistory } from "history";
import * as React from "react";
import { Route, Router } from "react-router-dom";
import {
  defaultACLEntityResolversMulti,
  defaultACLEntityResolvers,
} from "../../../../../../__mocks__/ACLExpressionBuilder.mock";
import { listGroups } from "../../../../../../__mocks__/GroupModule.mock";
import { getPlatform } from "../../../../../../__mocks__/Lti13PlatformsModule.mock";
import { listRoles } from "../../../../../../__mocks__/RoleModule.mock";
import { listUsers } from "../../../../../../__mocks__/UserModule.mock";
import EditLti13Platform, {
  EditLti13PlatformProps,
} from "../../../../../../tsrc/settings/Integrations/lti13/components/EditLti13Platform";
import { languageStrings } from "../../../../../../tsrc/util/langstrings";

const { cancel: cancelLabel } = languageStrings.common.action;

export const commonEditLti13PlatformProps: EditLti13PlatformProps = {
  updateTemplate: () => {},
  getPlatformProvider: getPlatform,
  searchUserProvider: listUsers,
  searchGroupProvider: listGroups,
  searchRoleProvider: listRoles,
  aclEntityResolversProvider: defaultACLEntityResolvers,
  aclEntityResolversMultiProvider: defaultACLEntityResolversMulti,
};

/**
 * Helper to render EditLti13Platform page.
 */
export const renderEditLti13Platform = async (
  props: EditLti13PlatformProps = commonEditLti13PlatformProps,
  encodedPlatformId: string,
): Promise<RenderResult> => {
  const urlPrefix = "/page/editLti13Platform/";
  const history = createMemoryHistory();
  history.push(`${urlPrefix}${encodedPlatformId}`);

  const renderResult = render(
    <Router history={history}>
      <Route path={`${urlPrefix}:platformIdBase64`}>
        <EditLti13Platform {...props} />
      </Route>
    </Router>,
  );

  await renderResult.findByText(cancelLabel);

  return renderResult;
};

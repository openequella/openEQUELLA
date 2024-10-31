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
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import SettingsList from "../../../../components/SettingsList";
import { languageStrings } from "../../../../util/langstrings";
import UnknownUserHandlingControl, {
  GroupWarning,
} from "./UnknownUserHandlingControl";
import UsableByControl, { UsableByControlProps } from "./UsableByControl";

const { accessControl } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage;

/**
 * Contains the selection for Unknown User Handling and
 * a list of groups if the unknown user handling is CREATE
 */
export interface UnknownUserHandlingData {
  selection: OEQ.LtiPlatform.UnknownUserHandling;
  groups: ReadonlySet<OEQ.UserQuery.RoleDetails>;
}

export interface AccessControlSectionProps
  extends Pick<
      UsableByControlProps,
      | "searchUserProvider"
      | "searchGroupProvider"
      | "searchRoleProvider"
      | "aclEntityResolversProvider"
    >,
    GroupWarning {
  /**
   * AclExpression string used to control who can use the platform.
   */
  aclExpression: string;
  /**
   * Function used to update the value of `aclExpression`.
   */
  setAclExpression: (value: string) => void;
  /**
   * Value for UnknownUserHandling control.
   */
  unknownUserHandling: UnknownUserHandlingData;
  /**
   * Function used to update the value of `selectedUnknownUserHandling`.
   */
  setUnknownUserHandling: (data: UnknownUserHandlingData) => void;
}

/**
 * This component is used to display and edit who can access the platform,
 * and the way to handle unknown user of an LTI platform
 * within the LTI 1.3 platform creation page.
 */
const AccessControlSection = ({
  aclExpression,
  setAclExpression,
  setUnknownUserHandling,
  unknownUserHandling,
  searchUserProvider,
  searchGroupProvider,
  searchRoleProvider,
  aclEntityResolversProvider,
  warningMessageForGroups,
}: AccessControlSectionProps) => (
  <SettingsList subHeading={accessControl.title}>
    <UsableByControl
      value={aclExpression}
      onChange={setAclExpression}
      searchUserProvider={searchUserProvider}
      searchGroupProvider={searchGroupProvider}
      searchRoleProvider={searchRoleProvider}
      aclEntityResolversProvider={aclEntityResolversProvider}
    />

    <UnknownUserHandlingControl
      selection={unknownUserHandling.selection}
      groups={unknownUserHandling.groups}
      onChange={(selection, groups) =>
        setUnknownUserHandling({
          selection: selection,
          groups: groups,
        })
      }
      groupListProvider={searchGroupProvider}
      warningMessageForGroups={warningMessageForGroups}
    />
  </SettingsList>
);

export default AccessControlSection;
